package com.rbkmoney.wallets_hooker.scheduler.withdrawal;

import com.rbkmoney.swag_wallets_webhook_events.WithdrawalStatus;
import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.junit4.SpringRunner;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.springframework.boot.test.context.SpringBootTest.WebEnvironment.RANDOM_PORT;

public class WithdrawalMessageConverterTest extends AbstractIntegrationTest {

    @Autowired
    private WithdrawalMessageConverter converter;

    @Test
    public void convertToJson() {
        WithdrawalMessage message = random(WithdrawalMessage.class);
        message.setEventType(EventType.WITHDRAWAL_SUCCEEDED);
        message.setOccuredAt("2016-03-22T06:12:27Z");
        message.setWithdrawalCreatedAt("2016-03-22T06:12:27Z");
        message.setWithdrawalStatus(WithdrawalStatus.StatusEnum.SUCCEEDED.name());
        System.out.println(converter.convertToJson(message));
    }
}