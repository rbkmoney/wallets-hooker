package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.withdrawal.WithdrawalStatus;
import com.rbkmoney.swag.wallets.webhook.events.model.WithdrawalFailed;
import com.rbkmoney.swag.wallets.webhook.events.model.WithdrawalSucceeded;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.service.HookMessageGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalStatusChangedHookMessageGenerator implements HookMessageGenerator<WithdrawalStatus> {

    private final WebHookMessageGeneratorServiceImpl<WithdrawalStatus> generatorService;
    private final ObjectMapper objectMapper;

    @Override
    public WebhookMessage generate(WithdrawalStatus event, WebHookModel model, Long eventId, Long parentId) {
        WebhookMessage webhookMessage = generatorService.generate(event, model, eventId, parentId);
        if (model.getEventTypes() != null && model.getEventTypes().contains(EventType.WITHDRAWAL_CREATED)) {
            webhookMessage.setParentEventId(parentId);
        } else {
            webhookMessage.setParentEventId(0);
        }
        byte[] message = initRequestBody(event);
        webhookMessage.setRequestBody(message);
        return webhookMessage;
    }

    private byte[] initRequestBody(WithdrawalStatus event) {
        byte[] message = null;
        try {
            if (event.isSetFailed()) {
                WithdrawalFailed withdrawalFailed = new WithdrawalFailed();
                message = objectMapper.writeValueAsBytes(withdrawalFailed);
            } else if (event.isSetSucceeded()) {
                WithdrawalSucceeded withdrawalSucceeded = new WithdrawalSucceeded();
                message = objectMapper.writeValueAsBytes(withdrawalSucceeded);
            }
        } catch (JsonProcessingException e) {
            log.error("Error when generate webhookMessage e: ", e);
        }
        return message;
    }


}
