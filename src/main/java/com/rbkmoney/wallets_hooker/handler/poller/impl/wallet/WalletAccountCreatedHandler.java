package com.rbkmoney.wallets_hooker.handler.poller.impl.wallet;

import com.rbkmoney.fistful.wallet.Change;
import com.rbkmoney.fistful.wallet.SinkEvent;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.wallet.WalletReferenceDao;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WalletIdentityReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class WalletAccountCreatedHandler extends AbstractWalletEventHandler {

    private final WalletReferenceDao walletReferenceDao;

    private Filter filter;

    public WalletAccountCreatedHandler(WalletReferenceDao walletReferenceDao) {
        this.walletReferenceDao = walletReferenceDao;
        filter = new PathConditionFilter(new PathConditionRule("account.created", new IsNullCondition().not()));
    }

    @Override
    public void handle(Change change, SinkEvent event) {
        String walletId = event.getSource();
        String identityId = change.getAccount().getCreated().getIdentity();
        WalletIdentityReference reference = new WalletIdentityReference();
        reference.setWalletId(walletId);
        reference.setIdentityId(identityId);
        walletReferenceDao.create(reference);
        log.info("Finish handling wallet account created, walletId={}, identityId={} saved to db.", walletId, identityId);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
