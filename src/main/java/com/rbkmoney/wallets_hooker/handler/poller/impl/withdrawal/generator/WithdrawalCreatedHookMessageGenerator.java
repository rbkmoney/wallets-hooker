package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal.generator;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.rbkmoney.fistful.base.Cash;
import com.rbkmoney.fistful.withdrawal.Withdrawal;
import com.rbkmoney.swag.wallets.webhook.events.model.WithdrawalBody;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.service.HookMessageGenerator;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorServiceImpl;
import com.rbkmoney.webhook.dispatcher.WebhookMessage;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WithdrawalCreatedHookMessageGenerator implements HookMessageGenerator<Withdrawal> {

    private final WebHookMessageGeneratorServiceImpl<Withdrawal> generatorService;
    private final ObjectMapper objectMapper;

    @Override
    public WebhookMessage generate(Withdrawal event, WebHookModel model, Long eventId, Long parentId) {
        WebhookMessage webhookMessage = generatorService.generate(event, model, eventId, parentId);
        com.rbkmoney.swag.wallets.webhook.events.model.Withdrawal withdrawal = new com.rbkmoney.swag.wallets.webhook.events.model.Withdrawal();
        withdrawal.setDestination(event.getDestination());
        withdrawal.setId(event.getId());
        withdrawal.setWallet(event.getSource());
        withdrawal.setBody(initBody(event));
        try {
            webhookMessage.setRequestBody(objectMapper.writeValueAsBytes(withdrawal));
        } catch (JsonProcessingException e) {
            log.error("Error when generate webhookMessage event: {} model: {} eventId: {} e: ", event, model, eventId, e);
        }
        webhookMessage.setParentEventId(parentId);
        return webhookMessage;
    }

    private WithdrawalBody initBody(Withdrawal event) {
        Cash body = event.getBody();
        WithdrawalBody withdrawalBody = new WithdrawalBody();
        withdrawalBody.setAmount(body.getAmount());
        withdrawalBody.setCurrency(body.getCurrency().getSymbolicCode());
        return withdrawalBody;
    }

}
