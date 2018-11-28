package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.Event;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.WithdrawalMessageDao;
import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.mockito.Matchers.any;

public class WithdrawalSucceededHandlerTest extends AbstractIntegrationTest {

    @MockBean
    private WithdrawalMessageDao withdrawalMessageDao;

    @Autowired
    private WithdrawalSucceededHandler withdrawalSucceededHandler;

    @Before
    public void before(){
        MockitoAnnotations.initMocks(this);
        Mockito.when(withdrawalMessageDao.getAny(any())).thenAnswer((Answer<WithdrawalMessage>) invocationOnMock-> {
            WithdrawalMessage withdrawalMessage = random(WithdrawalMessage.class);
            withdrawalMessage.setWithdrawalId((String) invocationOnMock.getArguments()[0]);
            return withdrawalMessage;
        });
    }

    @Test
    public void handle() {
        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setId(random(Long.class));
        Event payload = new Event();
        payload.setOccuredAt("2016-03-22T06:12:27Z");
        sinkEvent.setPayload(payload);
        withdrawalSucceededHandler.handle(null, sinkEvent);
    }
}