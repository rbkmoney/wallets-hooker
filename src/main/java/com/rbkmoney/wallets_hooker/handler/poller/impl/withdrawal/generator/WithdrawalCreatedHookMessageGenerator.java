package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.base.Cash;
import com.rbkmoney.fistful.withdrawal.Withdrawal;
import com.rbkmoney.swag.wallets.webhook.events.model.Event;
import com.rbkmoney.swag.wallets.webhook.events.model.WithdrawalBody;
import com.rbkmoney.swag.wallets.webhook.events.model.WithdrawalStarted;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.exception.GenerateMessageException;
import com.rbkmoney.wallets_hooker.handler.poller.impl.AdditionalHeadersGenerator;
import com.rbkmoney.wallets_hooker.handler.poller.impl.model.GeneratorParam;
import com.rbkmoney.wallets_hooker.service.BaseHookMessageGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import java.time.OffsetDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Component
public class WithdrawalCreatedHookMessageGenerator extends BaseHookMessageGenerator<Withdrawal> {

    private final WebHookMessageGeneratorServiceImpl<Withdrawal> generatorService;
    private final ObjectMapper objectMapper;
    private final AdditionalHeadersGenerator additionalHeadersGenerator;

    public WithdrawalCreatedHookMessageGenerator(WebHookMessageGeneratorServiceImpl<Withdrawal> generatorService,
                                                 ObjectMapper objectMapper,
                                                 AdditionalHeadersGenerator additionalHeadersGenerator,
                                                 @Value("${parent.not.exist.id}") Long parentId) {
        super(parentId);
        this.generatorService = generatorService;
        this.objectMapper = objectMapper;
        this.additionalHeadersGenerator = additionalHeadersGenerator;
    }

    @Override
    protected WebhookMessage generateMessage(Withdrawal event, WebHookModel model, GeneratorParam generatorParam) {
        try {
            var withdrawal = new com.rbkmoney.swag.wallets.webhook.events.model.Withdrawal();
            withdrawal.setDestination(event.getDestination());
            withdrawal.setId(generatorParam.getSourceId());
            withdrawal.setWallet(event.getSource());
            withdrawal.setBody(initBody(event));
            withdrawal.setExternalID(event.getExternalId());
            WithdrawalStarted withdrawalStarted = new WithdrawalStarted();
            withdrawalStarted.setWithdrawal(withdrawal);
            withdrawalStarted.setEventType(Event.EventTypeEnum.WITHDRAWALSTARTED);
            withdrawalStarted.setEventID(generatorParam.getEventId().toString());
            withdrawalStarted.setOccuredAt(OffsetDateTime.parse(generatorParam.getCreatedAt(), DateTimeFormatter.ISO_DATE_TIME));
            withdrawalStarted.setTopic(Event.TopicEnum.WITHDRAWALTOPIC);

            String requestBody = objectMapper.writeValueAsString(withdrawalStarted);

            WebhookMessage webhookMessage = generatorService.generate(event, model, generatorParam);
            webhookMessage.setRequestBody(requestBody.getBytes());
            webhookMessage.setAdditionalHeaders(additionalHeadersGenerator.generate(model, requestBody));
            webhookMessage.setParentEventId(generatorParam.getParentId());

            log.info("Webhook message from withdrawal_event_created was generated, withdrawalStarted={}, model={}, requestBody={}",
                    generatorParam.getSourceId(), model.toString(), requestBody);

            return webhookMessage;
        } catch (JsonProcessingException e) {
            log.error("Error when generate webhookMessage event: {} model: {} eventId: {} e: ", event, model, generatorParam.getEventId(), e);
            throw new GenerateMessageException("WithdrawalCreated error when generate webhookMessage!", e);
        }

    }

    private WithdrawalBody initBody(Withdrawal event) {
        Cash body = event.getBody();
        WithdrawalBody withdrawalBody = new WithdrawalBody();
        withdrawalBody.setAmount(body.getAmount());
        withdrawalBody.setCurrency(body.getCurrency().getSymbolicCode());
        return withdrawalBody;
    }

}
