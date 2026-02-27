package com.example.query;

import org.apache.kafka.streams.KafkaStreams;
import org.apache.kafka.streams.Topology;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.config.StreamsBuilderFactoryBeanConfigurer;
import org.springframework.kafka.config.StreamsBuilderFactoryBean;
import org.springframework.kafka.config.KafkaStreamsConfiguration;

import java.util.HashMap;
import java.util.Map;

import static org.apache.kafka.streams.StreamsConfig.*;

@Configuration
public class KafkaStreamsRunner {

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${spring.kafka.streams.application-id}")
    private String appId;

    @Bean(name = "defaultKafkaStreamsConfig")
    public KafkaStreamsConfiguration kStreamsConfigs() {
        Map<String, Object> props = new HashMap<>();
        props.put(APPLICATION_ID_CONFIG, appId);
        props.put(BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        props.put(PROCESSING_GUARANTEE_CONFIG, "exactly_once_v2");
        return new KafkaStreamsConfiguration(props);
    }

    @Bean
    public StreamsBuilderFactoryBeanConfigurer configurer() {
        return fb -> fb.setAutoStartup(true);
    }

    @Bean
    public KafkaStreams kafkaStreams(Topology topology, KafkaStreamsConfiguration config) {
        KafkaStreams streams = new KafkaStreams(topology, config.asProperties());
        streams.start();
        Runtime.getRuntime().addShutdownHook(new Thread(streams::close));
        return streams;
    }
}
