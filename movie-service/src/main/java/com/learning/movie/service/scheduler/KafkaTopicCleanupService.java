package com.learning.movie.service.scheduler;

import org.apache.kafka.clients.admin.AdminClient;
import org.apache.kafka.clients.admin.AdminClientConfig;
import org.apache.kafka.clients.admin.DeleteTopicsResult;
import org.apache.kafka.clients.admin.ListTopicsResult;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.time.format.DateTimeFormatter;
import java.util.List;
import java.util.Properties;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
public class KafkaTopicCleanupService {
    private static final Logger LOGGER = LoggerFactory.getLogger(KafkaTopicCleanupService.class);

    private static final DateTimeFormatter TOPIC_DATE_FORMAT = DateTimeFormatter.ISO_DATE;

    private final AdminClient adminClient;
    private final String topicPrefix;
    private final int topicTtl;

    @Autowired
    public KafkaTopicCleanupService(@Value("${spring.kafka.bootstrap-servers}") final String bootstrapServers,
                                    @Value("${kafka.topic.prefix:new-movies-}") final String topicPrefix,
                                    @Value("${kafka.topic.retention.days:31}") final int topicTtl) {
        this.topicPrefix = topicPrefix;
        this.topicTtl = topicTtl;

        final Properties props = new Properties();
        props.put(AdminClientConfig.BOOTSTRAP_SERVERS_CONFIG, bootstrapServers);
        this.adminClient = AdminClient.create(props);
    }

    @Scheduled(cron = "0 0 1 * * *", zone = "UTC")
    public void cleanupOldTopics() {
        LOGGER.info("Starting Kafka topic cleanup job");

        try {
            final ListTopicsResult topicsResult = this.adminClient.listTopics();
            final Set<String> topics = topicsResult.names().get();

            final LocalDate thresholdDate = LocalDate.now().minusDays(this.topicTtl);

            final List<String> topicsToDelete = topics.stream()
                    .filter(topic -> {
                        String datePart = topic.substring(this.topicPrefix.length());
                        try {
                            LocalDate topicDate = LocalDate.parse(datePart, TOPIC_DATE_FORMAT);
                            return topicDate.isBefore(thresholdDate);
                        } catch (Exception e) {
                            LOGGER.warn("Skipping topic {}: failed to parse date part", datePart);
                            return false;
                        }
                    })
                    .toList();

            if (topicsToDelete.isEmpty()) {
                LOGGER.info("No topics to delete. Job completed");
                return;
            }

            LOGGER.info("Deleting topics: {}", topicsToDelete);
            final DeleteTopicsResult deleteResult = this.adminClient.deleteTopics(topicsToDelete);
            deleteResult.all().get();

            LOGGER.info("Deleted {} topics older than {} days", topicsToDelete.size(), this.topicTtl);
        } catch (InterruptedException | ExecutionException e) {
            LOGGER.info("Error during Kafka topic cleanup: {}", e.getMessage(), e);
            throw new IllegalStateException("Could not clean up Kafka topic");
        }
    }
}
