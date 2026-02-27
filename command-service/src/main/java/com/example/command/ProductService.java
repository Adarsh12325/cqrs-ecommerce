package com.example.command;

import com.example.command.dto.ProductRequest;
import com.example.command.events.ProductCreatedEvent;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class ProductService {

    private final ProductRepository productRepository;
    private final KafkaEventProducer eventProducer;

    public ProductService(ProductRepository productRepository, KafkaEventProducer eventProducer) {
        this.productRepository = productRepository;
        this.eventProducer = eventProducer;
    }

    @Transactional
    public Product createProduct(ProductRequest request) {
        Product product = new Product(request.getName(), request.getCategory(), request.getPrice());
        Product saved = productRepository.save(product);

        ProductCreatedEvent.Payload payload =
                new ProductCreatedEvent.Payload(saved.getId(), saved.getName(), saved.getCategory(), saved.getPrice());
        eventProducer.publishProductCreated(new ProductCreatedEvent(payload));

        return saved;
    }
}
