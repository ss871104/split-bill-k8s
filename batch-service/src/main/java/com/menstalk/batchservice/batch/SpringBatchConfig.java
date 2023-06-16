package com.menstalk.batchservice.batch;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.menstalk.batchservice.dto.MemberResponse;
import com.menstalk.batchservice.models.Notification;
import com.menstalk.batchservice.proxy.MasterQueryProxy;
import lombok.RequiredArgsConstructor;
import org.springframework.batch.core.Job;
import org.springframework.batch.core.Step;
import org.springframework.batch.core.configuration.annotation.EnableBatchProcessing;
import org.springframework.batch.core.configuration.annotation.JobBuilderFactory;
import org.springframework.batch.core.configuration.annotation.StepBuilderFactory;
import org.springframework.batch.item.ItemProcessor;
import org.springframework.batch.item.ItemReader;
import org.springframework.batch.item.ItemWriter;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.kafka.core.KafkaTemplate;

import java.util.List;
import java.util.Map;

@Configuration
@EnableBatchProcessing
@RequiredArgsConstructor
public class SpringBatchConfig {
    private final JobBuilderFactory jobBuilderFactory;
    private final StepBuilderFactory stepBuilderFactory;
    private final MasterQueryProxy masterQueryProxy;
    private final KafkaTemplate<String, Object> kafkaTemplate;
    private final ObjectMapper objectMapper;

    @Bean
    public ItemReader<Map<Long, List<MemberResponse>>> owedWeeklyNotificationReader() {
        return new OwedWeeklyNotificationReader(masterQueryProxy);
    }

    @Bean
    public ItemProcessor<Map<Long, List<MemberResponse>>, List<Notification>> owedWeeklyNotificationProcessor() {
        return new OwedWeeklyNotificationProcessor(masterQueryProxy);
    }

    @Bean
    public ItemWriter<List<Notification>> owedWeeklyNotificationWriter() {
        return new OwedWeeklyNotificationWriter(kafkaTemplate, objectMapper);
    }

    @Bean
    public Step notificationStep(ItemReader<Map<Long, List<MemberResponse>>> reader, ItemProcessor<Map<Long, List<MemberResponse>>, List<Notification>> processor, ItemWriter<List<Notification>> writer) {
        return stepBuilderFactory.get("weeklyOwedNotificationStep")
                .<Map<Long, List<MemberResponse>>, List<Notification>>chunk(10)
                .reader(reader)
                .processor(processor)
                .writer(writer)
                .build();
    }

    @Bean
    public Job notificationJob(Step notificationStep) {
        return jobBuilderFactory.get("weeklyOwedNotificationJob")
                .start(notificationStep)
                .build();
    }
}
