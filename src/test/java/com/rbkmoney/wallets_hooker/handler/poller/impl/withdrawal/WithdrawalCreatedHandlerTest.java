package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.base.Cash;
import com.rbkmoney.fistful.base.CurrencyRef;
import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.Event;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.fistful.withdrawal.Withdrawal;
import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.WalletMessageDao;
import com.rbkmoney.wallets_hooker.model.WalletMessage;
import org.junit.Before;
import org.junit.Test;
import org.mockito.Mockito;
import org.mockito.MockitoAnnotations;
import org.mockito.stubbing.Answer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;

import static io.github.benas.randombeans.api.EnhancedRandom.random;
import static org.mockito.Matchers.any;

public class WithdrawalCreatedHandlerTest extends AbstractIntegrationTest {

    @MockBean
    private WalletMessageDao walletMessageDao;

    @Autowired
    private WithdrawalCreatedHandler withdrawalCreatedHandler;

    @Before
    public void before() {
        MockitoAnnotations.initMocks(this);
        Mockito.when(walletMessageDao.getAny(any())).thenAnswer((Answer<WalletMessage>) invocationOnMock-> {
            WalletMessage walletMessage = random(WalletMessage.class);
            walletMessage.setWalletId((String) invocationOnMock.getArguments()[0]);
            return walletMessage;
        });
    }

    @Test
    public void handle() {
        Change change = new Change();
        Withdrawal withdrawal = new Withdrawal();
        withdrawal.setSource(random(String.class));
        withdrawal.setDestination(random(String.class));
        Cash cash = new Cash();
        cash.setAmount(random(Long.class));
        CurrencyRef currency = new CurrencyRef();
        currency.setSymbolicCode(random(String.class));
        cash.setCurrency(currency);
        withdrawal.setBody(cash);
        change.setCreated(withdrawal);
        SinkEvent sinkEvent = new SinkEvent();
        sinkEvent.setCreatedAt("2016-03-22T06:12:27Z");
        sinkEvent.setSource(random(String.class));
        sinkEvent.setId(random(Long.class));
        Event payload = new Event();
        payload.setOccuredAt("2016-03-22T06:12:27Z");
        sinkEvent.setPayload(payload);
        withdrawalCreatedHandler.handle(change, sinkEvent);
    }
}