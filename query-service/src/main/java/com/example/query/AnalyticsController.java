package com.example.query;
import org.apache.kafka.streams.StoreQueryParameters;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.state.QueryableStoreTypes;
import org.apache.kafka.streams.state.ReadOnlyKeyValueStore;
import org.apache.kafka.streams.state.ReadOnlyWindowStore;
import org.apache.kafka.streams.state.KeyValueIterator;
import org.apache.kafka.streams.kstream.Windowed;
import org.apache.kafka.streams.KeyValue;
import org.apache.kafka.streams.state.*;
import org.apache.kafka.streams.Topology;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.web.bind.annotation.*;

import java.time.Instant;
import java.time.ZoneOffset;
import java.time.ZonedDateTime;
import java.util.*;

@RestController
@RequestMapping("/api/analytics")
public class AnalyticsController {

    private final KafkaStreams streams;
    private final Topology topology;

    public AnalyticsController(KafkaStreams streams, Topology topology) {
        this.streams = streams;
        this.topology = topology;
    }

    @GetMapping("/products/{productId}/sales")
    public Map<String, Object> getProductSales(@PathVariable Long productId) {
        ReadOnlyKeyValueStore<Long, Double> store =
        streams.store(
                StoreQueryParameters.fromNameAndType(
                        "product-sales-store",
                        QueryableStoreTypes.<Long, Double>keyValueStore()
                )
        );


        Double total = store.get(productId);
        Map<String, Object> response = new HashMap<>();
        response.put("productId", productId);
        response.put("totalSales", total != null ? total : 0.0);
        return response;
    }

    @GetMapping("/categories/{category}/revenue")
    public Map<String, Object> getCategoryRevenue(@PathVariable String category) {
        ReadOnlyKeyValueStore<String, Double> store =
        streams.store(
                StoreQueryParameters.fromNameAndType(
                        "category-revenue-store",
                        QueryableStoreTypes.<String, Double>keyValueStore()
                )
        );


        Double total = store.get(category);
        Map<String, Object> response = new HashMap<>();
        response.put("category", category);
        response.put("totalRevenue", total != null ? total : 0.0);
        return response;
    }

    @GetMapping("/hourly-sales")
    public List<Map<String, Object>> getHourlySales(
            @RequestParam("start") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime start,
            @RequestParam("end") @DateTimeFormat(iso = DateTimeFormat.ISO.DATE_TIME) ZonedDateTime end) {

        ReadOnlyWindowStore<Long, Double> store =
        streams.store(
                StoreQueryParameters.fromNameAndType(
                        "hourly-sales-store",
                        QueryableStoreTypes.<Long, Double>windowStore()
                )
        );


        Instant from = start.toInstant();
        Instant to = end.toInstant();

        List<Map<String, Object>> result = new ArrayList<>();

        KeyValueIterator<Windowed<Long>, Double> iterator = store.fetchAll(from, to);
        while (iterator.hasNext()) {
            KeyValue<Windowed<Long>, Double> kv = iterator.next();
            Windowed<Long> window = kv.key;
            Double total = kv.value;

            Map<String, Object> entry = new HashMap<>();
            entry.put("windowStart", ZonedDateTime.ofInstant(window.window().startTime(), ZoneOffset.UTC).toString());
            entry.put("windowEnd", ZonedDateTime.ofInstant(window.window().endTime(), ZoneOffset.UTC).toString());
            entry.put("totalSales", total);

            result.add(entry);
        }
        iterator.close();

        return result;
    }

    @GetMapping("/topology")
    public String getTopology() {
        return topology.describe().toString();
    }
}
