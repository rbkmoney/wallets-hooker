package com.rbkmoney.wallets_hooker.handler.poller.impl.wallet;

import com.rbkmoney.fistful.account.Account;
import com.rbkmoney.fistful.wallet.*;
import com.rbkmoney.wallets_hooker.AbstractIntegrationTest;
import com.rbkmoney.wallets_hooker.dao.IdentityMessageDao;
import com.rbkmoney.wallets_hooker.dao.WalletMessageDao;
import com.rbkmoney.wallets_hooker.model.IdentityMessage;
import com.rbkmoney.wallets_hooker.model.WalletMessage;
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
import static org.mockito.Matchers.any;

public class WalletAccountCreatedHandlerTest extends AbstractIntegrationTest {

    @MockBean
    private WalletMessageDao walletMessageDao;

    @MockBean
    private IdentityMessageDao identityMessageDao;

    @Autowired
    private WalletAccountCreatedHandler walletAccountCreatedHandler;

    @Before
    public void before(){
        MockitoAnnotations.initMocks(this);
        Mockito.when(identityMessageDao.getAny(any())).thenAnswer((Answer<IdentityMessage>) invocationOnMock-> {
            IdentityMessage identityMessage = random(IdentityMessage.class);
            identityMessage.setIdentityId((String) invocationOnMock.getArguments()[0]);
            return identityMessage;
        });

        Mockito.when(walletMessageDao.getAny(any())).thenAnswer((Answer<WalletMessage>) invocationOnMock-> {
            WalletMessage walletMessage = random(WalletMessage.class);
            walletMessage.setWalletId((String) invocationOnMock.getArguments()[0]);
            return walletMessage;
        });
    }

    @Test
    public void handle() {
        Change change = new Change();
        AccountChange value = new AccountChange();
        Account account = new Account();
        account.setIdentity(random(String.class));
        value.setCreated(account);
        change.setAccount(value);
        SinkEvent sinkEvent = new SinkEvent();
        Event payload = new Event();
        sinkEvent.setId(random(Long.class));
        payload.setOccuredAt("2016-03-22T06:12:27Z");
        sinkEvent.setPayload(payload);
        sinkEvent.setSource(random(String.class));
        walletAccountCreatedHandler.handle(change, sinkEvent);
    }
}