package com.learning.movie.service.publisher;

import jakarta.annotation.PostConstruct;
import jakarta.annotation.PreDestroy;
import org.apache.kafka.clients.producer.KafkaProducer;
import org.apache.kafka.clients.producer.ProducerConfig;
import org.apache.kafka.clients.producer.ProducerRecord;
import org.apache.kafka.common.serialization.StringSerializer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.Properties;

@Service
public class MoviePublisherService {
    private static final Logger LOGGER = LoggerFactory.getLogger(MoviePublisherService.class);

    private static final DateTimeFormatter TOPIC_DATE_FORMAT = DateTimeFormatter.ISO_DATE;

    private KafkaProducer<String, String> producer;

    @Value("${spring.kafka.bootstrap-servers}")
    private String bootstrapServers;

    @Value("${kafka.topic.prefix:new-movies-}")
    private String topicPrefix;

    @PostConstruct
    public void init() {
        final Properties props = new Properties();
        props.put(ProducerConfig.BOOTSTRAP_SERVERS_CONFIG, this.bootstrapServers);
        props.put(ProducerConfig.KEY_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.VALUE_SERIALIZER_CLASS_CONFIG, StringSerializer.class);
        props.put(ProducerConfig.ACKS_CONFIG, "all");
        props.put(ProducerConfig.RETRIES_CONFIG, 3);
        this.producer = new KafkaProducer<>(props);
    }

    public void publishDailyNewMovie(final String movieJson) {
        final ProducerRecord<String, String> producerRecord = new ProducerRecord<>(
                this.topicPrefix.concat(LocalDate.now().format(TOPIC_DATE_FORMAT)), movieJson);

        this.producer.send(producerRecord, ((recordMetadata, e) -> {
            if (e != null) {
                LOGGER.error("Failed to send message: {}", e.getMessage());
            } else {
                LOGGER.info("Sent message to topic [{}], partition: [{}], offset: [{}]",
                             recordMetadata.topic(), recordMetadata.partition(), recordMetadata.offset());
            }
        }));
    }

    @PreDestroy
    public void cleanup() {
        this.producer.close();
    }
}
