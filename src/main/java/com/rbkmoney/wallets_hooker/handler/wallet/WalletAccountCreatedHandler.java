package com.rbkmoney.wallets_hooker.handler.wallet;

import com.rbkmoney.fistful.wallet.TimestampedChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.machinegun.eventsink.MachineEvent;
import com.rbkmoney.wallets_hooker.dao.wallet.WalletReferenceDao;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WalletIdentityReference;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
@RequiredArgsConstructor
public class WalletAccountCreatedHandler implements WalletEventHandler {

    private final WalletReferenceDao walletReferenceDao;

    @SuppressWarnings("rawtypes")
    private final Filter filter = new PathConditionFilter(new PathConditionRule(
            "account.created",
            new IsNullCondition().not()));

    @Override
    public void handle(TimestampedChange change, MachineEvent event) {
        String walletId = event.getSourceId();
        String identityId = change.getChange().getAccount().getCreated().getIdentity();

        log.info("Start handling WalletAccountCreatedChange: walletId={}, identityId={}", walletId, identityId);

        WalletIdentityReference reference = new WalletIdentityReference();
        reference.setWalletId(walletId);
        reference.setIdentityId(identityId);

        walletReferenceDao.create(reference);

        log.info("Finish handling WalletAccountCreatedChange: walletId={}, identityId={}", walletId, identityId);
    }

    @Override
    @SuppressWarnings("rawtypes")
    public Filter getFilter() {
        return filter;
    }

}
