package com.rbkmoney.wallets_hooker.service;

import com.rbkmoney.kafka.common.exception.KafkaProduceException;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.kafka.core.KafkaTemplate;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class WebHookMessageSenderServiceImpl implements WebHookMessageSenderService {

    private final KafkaTemplate<String, WebhookMessage> kafkaTemplate;

    @Value("${kafka.topic.hook}")
    private String topicName;

    public void send(WebhookMessage webhookMessage) {
        try {
            kafkaTemplate.send(topicName, webhookMessage.getSourceId(), webhookMessage).get();

            log.info("Webhook message to kafka was sent, topicName={}, sourceId={}", topicName, webhookMessage.getSourceId());
        } catch (InterruptedException e) {
            log.error("InterruptedException command: {} e: ", webhookMessage, e);
            Thread.currentThread().interrupt();
            throw new KafkaProduceException(e);
        } catch (Exception e) {
            log.error("Error when send command: {} e: ", webhookMessage, e);
            throw new KafkaProduceException(e);
        }
    }

}
