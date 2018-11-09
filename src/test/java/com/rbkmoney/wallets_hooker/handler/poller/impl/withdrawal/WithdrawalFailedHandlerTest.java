package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.*;
import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.WithdrawalMessageDao;
import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.junit4.SpringRunner;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static io.github.benas.randombeans.api.EnhancedRandom.randomListOf;
import static org.junit.Assert.*;
import static org.mockito.Matchers.any;

public class WithdrawalFailedHandlerTest extends AbstractIntegrationTest {

    @MockBean
    private WithdrawalMessageDao withdrawalMessageDao;

    @Autowired
    private WithdrawalFailedHandler withdrawalFailedHandler;

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
        Change change = new Change();
        WithdrawalStatus statusChanged = new WithdrawalStatus();
        WithdrawalFailed withdrawalFailed = new WithdrawalFailed();
        withdrawalFailed.setFailure(new Failure());
        statusChanged.setFailed(withdrawalFailed);
        change.setStatusChanged(statusChanged);
        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setId(random(Long.class));
        Event payload = new Event();
        payload.setOccuredAt("2016-03-22T06:12:27Z");
        sinkEvent.setPayload(payload);
        withdrawalFailedHandler.handle(change, sinkEvent);
    }
}