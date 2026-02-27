package com.example.query;
import org.apache.kafka.common.serialization.Serializer;
import org.apache.kafka.common.serialization.Deserializer;
import org.apache.kafka.streams.state.KeyValueStore;
import org.apache.kafka.common.utils.Bytes;
import com.example.query.model.*;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.apache.kafka.common.serialization.Serde;
import org.apache.kafka.common.serialization.Serdes;
import org.apache.kafka.streams.state.WindowStore;
// for KeyValue.pair
import org.apache.kafka.streams.*;
// for windowed keys
import org.apache.kafka.streams.kstream.*;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.time.Duration;

@Configuration
public class StreamsConfig {

    @Value("${app.topics.product}")
    private String productTopic;

    @Value("${app.topics.order}")
    private String orderTopic;

    private final ObjectMapper objectMapper = new ObjectMapper();

    @Bean
    public Topology topology() {
        StreamsBuilder builder = new StreamsBuilder();

        Serde<String> stringSerde = Serdes.String();

        // Raw JSON streams
        KStream<String, String> productEvents = builder.stream(productTopic, Consumed.with(stringSerde, stringSerde));
        KStream<String, String> orderEvents = builder.stream(orderTopic, Consumed.with(stringSerde, stringSerde));

        // Build product KTable (ProductCreated only)
        KTable<Long, ProductEventPayload> productTable = productEvents
                .mapValues(this::parseProductEvent)
                .filter((k, v) -> v != null)
                .selectKey((key, value) -> value.getId())
                .toTable(Materialized.with(Serdes.Long(), JsonSerdes.product()));

        // Enriched order items stream (JOIN)
        KStream<Long, EnrichedOrderItem> enrichedItemsStream = orderEvents
                .mapValues(this::parseOrderCreated)
                .filter((k, v) -> v != null && "CREATED".equalsIgnoreCase(v.getStatus()))
                .flatMap((k, order) -> {
                    // expand each order into multiple items
                    return order.getItems().stream().map(item -> {
                        EnrichedOrderItem enriched = new EnrichedOrderItem();
                        enriched.setOrderId(order.getId());
                        enriched.setProductId(item.getProductId());
                        enriched.setQuantity(item.getQuantity());
                        enriched.setPrice(item.getPrice());
                        enriched.setCustomerId(order.getCustomerId());
                        return KeyValue.pair(item.getProductId(), enriched);
                    }).toList();
                })
                .leftJoin(productTable,
                        (enriched, product) -> {
                            if (product != null) {
                                enriched.setCategory(product.getCategory());
                            } else {
                                enriched.setCategory("UNKNOWN");
                            }
                            return enriched;
                        },
                        Joined.with(Serdes.Long(), JsonSerdes.enrichedOrderItem(), JsonSerdes.product()));

        // PRODUCT SALES STORE
        enrichedItemsStream
    .groupByKey(Grouped.<Long, EnrichedOrderItem>with(Serdes.Long(), JsonSerdes.enrichedOrderItem()))
    .aggregate(
        () -> 0.0,
        (productId, value, aggregate) -> aggregate + value.getQuantity() * value.getPrice(),
        Materialized.<Long, Double, KeyValueStore<org.apache.kafka.common.utils.Bytes, byte[]>>as("product-sales-store")
                .withKeySerde(Serdes.Long())
                .withValueSerde(Serdes.Double())
    );


        // CATEGORY REVENUE STORE
        enrichedItemsStream
    .groupBy((key, value) -> value.getCategory(),
             Grouped.<String, EnrichedOrderItem>with(Serdes.String(), JsonSerdes.enrichedOrderItem()))
    .aggregate(
        () -> 0.0,
        (category, value, aggregate) -> aggregate + value.getQuantity() * value.getPrice(),
        Materialized.<String, Double, KeyValueStore<Bytes, byte[]>>as("category-revenue-store")
                .withKeySerde(Serdes.String())
                .withValueSerde(Serdes.Double())
    );


        // HOURLY SALES STORE
        TimeWindows windows = TimeWindows.ofSizeWithNoGrace(Duration.ofHours(1));

enrichedItemsStream
    .groupByKey(Grouped.<Long, EnrichedOrderItem>with(Serdes.Long(), JsonSerdes.enrichedOrderItem()))
    .windowedBy(windows)
    .aggregate(
        () -> 0.0,
        (productId, value, aggregate) -> aggregate + value.getQuantity() * value.getPrice(),
        Materialized.<Long, Double, WindowStore<Bytes, byte[]>>as("hourly-sales-store")
                .withKeySerde(Serdes.Long())
                .withValueSerde(Serdes.Double())
    );


        return builder.build();
    }

    // Helper parse methods

    private ProductEventPayload parseProductEvent(String json) {
        try {
            GenericEvent<ProductEventPayload> event =
                    objectMapper.readValue(json,
                            objectMapper.getTypeFactory().constructParametricType(GenericEvent.class, ProductEventPayload.class));
            if (!"ProductCreated".equals(event.getEventType())) return null;
            return event.getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    private OrderCreatedPayload parseOrderCreated(String json) {
        try {
            GenericEvent<?> base = objectMapper.readValue(json, GenericEvent.class);
            if (!"OrderCreated".equals(base.getEventType())) return null;

            GenericEvent<OrderCreatedPayload> event =
                    objectMapper.readValue(json,
                            objectMapper.getTypeFactory().constructParametricType(GenericEvent.class, OrderCreatedPayload.class));
            return event.getPayload();
        } catch (Exception e) {
            return null;
        }
    }

    // Enriched item model

    public static class EnrichedOrderItem {
        private Long orderId;
        private Long productId;
        private Long customerId;
        private Integer quantity;
        private Double price;
        private String category;

        public EnrichedOrderItem() {}

        public Long getOrderId() { return orderId; }
        public void setOrderId(Long orderId) { this.orderId = orderId; }
        public Long getProductId() { return productId; }
        public void setProductId(Long productId) { this.productId = productId; }
        public Long getCustomerId() { return customerId; }
        public void setCustomerId(Long customerId) { this.customerId = customerId; }
        public Integer getQuantity() { return quantity; }
        public void setQuantity(Integer quantity) { this.quantity = quantity; }
        public Double getPrice() { return price; }
        public void setPrice(Double price) { this.price = price; }
        public String getCategory() { return category; }
        public void setCategory(String category) { this.category = category; }
    }

    // Simple JSON Serdes using Jackson
    public static class JsonSerdes {
    public static Serde<ProductEventPayload> product() {
        return Serdes.serdeFrom(
                new JsonSerde<>(ProductEventPayload.class).serializer(),
                new JsonSerde<>(ProductEventPayload.class).deserializer()
        );
    }

    public static Serde<EnrichedOrderItem> enrichedOrderItem() {
        return Serdes.serdeFrom(
                new JsonSerde<>(EnrichedOrderItem.class).serializer(),
                new JsonSerde<>(EnrichedOrderItem.class).deserializer()
        );
    }
}


public static class JsonSerde<T> implements Serde<T> {
    private final ObjectMapper mapper = new ObjectMapper();
    private final Class<T> clazz;

    public JsonSerde(Class<T> clazz) {
        this.clazz = clazz;
    }

    @Override
    public Serializer<T> serializer() {
        return (topic, data) -> {
            try {
                if (data == null) return null;
                return mapper.writeValueAsBytes(data);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }

    @Override
    public Deserializer<T> deserializer() {
        return (topic, data) -> {
            try {
                if (data == null) return null;
                return mapper.readValue(data, clazz);
            } catch (Exception e) {
                throw new RuntimeException(e);
            }
        };
    }
}

}
