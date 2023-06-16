package com.menstalk.batchservice.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstalk.batchservice.models.Notification;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.batch.item.ItemWriter;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;

@RequiredArgsConstructor
@Slf4j
public class OwedWeeklyNotificationWriter implements ItemWriter<List<Notification>> {
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Override
    public void write(List<? extends List<Notification>> list) throws Exception {
        List<Notification> notifications = list.stream()
                .flatMap(List::stream)
                .toList();

        String notificationJson = objectMapper.writeValueAsString(notifications);
        try {
            log.info("Send weeklyOwedTopic");
            kafkaTemplate.send("weeklyOwedTopic", notificationJson);
        } catch (Exception e) {
            e.printStackTrace();
            log.error("Failed sending weeklyOwedTopic, error: {}", e.toString());
        }

        log.info("Weekly Owed Notification batch finish!");
    }
}
