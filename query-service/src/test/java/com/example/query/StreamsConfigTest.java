package com.example.query;

import org.apache.kafka.streams.Topology;
import org.junit.jupiter.api.Test;

import java.lang.reflect.Field;

import static org.junit.jupiter.api.Assertions.assertNotNull;

class StreamsConfigTest {

    @Test
    void topologyBuildsSuccessfully() throws Exception {
        StreamsConfig config = new StreamsConfig();

        // Inject non-null topic names for the test
        setField(config, "productTopic", "product-events");
        setField(config, "orderTopic", "order-events");

        Topology topology = config.topology();
        assertNotNull(topology);
    }

    private static void setField(Object target, String name, Object value) throws Exception {
        Field f = target.getClass().getDeclaredField(name);
        f.setAccessible(true);
        f.set(target, value);
    }
}
