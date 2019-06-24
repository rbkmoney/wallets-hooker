package com.rbkmoney.wallets_hooker.handler.poller.impl.destination;

import com.rbkmoney.fistful.destination.AccountChange;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.destination.DestinationReferenceDao;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationIdentityReference;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

@Slf4j
@Component
public class DestinationAccountChangeHandler extends AbstractDestinationEventHandler {

    private final DestinationReferenceDao destinationReferenceDao;

    private Filter filter;

    public DestinationAccountChangeHandler(DestinationReferenceDao destinationReferenceDao) {
        this.destinationReferenceDao = destinationReferenceDao;
        filter = new PathConditionFilter(new PathConditionRule("account", new IsNullCondition().not()));
    }

    @Override
    public void handle(com.rbkmoney.fistful.destination.Change change, com.rbkmoney.fistful.destination.SinkEvent parent) {
        AccountChange account = change.getAccount();
        DestinationIdentityReference reference = new DestinationIdentityReference();
        reference.setDestinationId(parent.getSource());
        reference.setIdentityId(account.getCreated().getIdentity());
        destinationReferenceDao.create(reference);
        log.info("Handle destination event account change with account: {} ", account);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
