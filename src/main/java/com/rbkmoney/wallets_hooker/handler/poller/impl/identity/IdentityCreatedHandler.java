package com.rbkmoney.wallets_hooker.handler.poller.impl.identity;

import com.rbkmoney.fistful.identity.Change;
import com.rbkmoney.fistful.identity.SinkEvent;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.IdentityMessageDao;
import com.rbkmoney.wallets_hooker.model.EventType;
import com.rbkmoney.wallets_hooker.model.IdentityMessage;
import org.springframework.stereotype.Component;

@Component
public class IdentityCreatedHandler extends AbstractIdentityEventHandler {

    private final IdentityMessageDao messageDao;

    private Filter filter;

    public IdentityCreatedHandler(IdentityMessageDao messageDao) {
        this.messageDao = messageDao;
        filter = new PathConditionFilter(new PathConditionRule("created", new IsNullCondition().not()));
    }

    @Override
    public void handle(Change change, SinkEvent event) {
        IdentityMessage message = new IdentityMessage();
        message.setEventType(EventType.IDENTITY_CREATED);
        message.setEventId(event.getPayload().getId());
        message.setOccuredAt(event.getPayload().getOccuredAt());
        message.setIdentityId(event.getSource());
        messageDao.create(message);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }

}
