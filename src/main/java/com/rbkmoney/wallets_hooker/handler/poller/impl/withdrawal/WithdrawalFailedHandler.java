package com.rbkmoney.wallets_hooker.handler.poller.impl.withdrawal;

import com.rbkmoney.fistful.withdrawal.Change;
import com.rbkmoney.fistful.withdrawal.SinkEvent;
import com.rbkmoney.geck.filter.Filter;
import com.rbkmoney.geck.filter.PathConditionFilter;
import com.rbkmoney.geck.filter.condition.IsNullCondition;
import com.rbkmoney.geck.filter.rule.PathConditionRule;
import com.rbkmoney.wallets_hooker.dao.webhook.WebHookDao;
import com.rbkmoney.wallets_hooker.dao.withdrawal.WithdrawalReferenceDao;
import com.rbkmoney.wallets_hooker.domain.WebHookModel;
import com.rbkmoney.wallets_hooker.domain.enums.EventType;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WithdrawalIdentityWalletReference;
import com.rbkmoney.wallets_hooker.service.WebHookMessageGeneratorService;
import com.rbkmoney.wallets_hooker.service.WebHookMessageSenderService;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;

@Slf4j
@Component
public class WithdrawalFailedHandler extends AbstractWithdrawalEventHandler {

    private final WithdrawalReferenceDao withdrawalReferenceDao;
    private final WebHookDao webHookDao;
    private final WebHookMessageGeneratorService webHookMessageGeneratorService;
    private final WebHookMessageSenderService webHookMessageSenderService;

    private Filter filter;

    public WithdrawalFailedHandler(WithdrawalReferenceDao withdrawalReferenceDao, WebHookDao webHookDao,
                                   WebHookMessageGeneratorService webHookMessageGeneratorService,
                                   WebHookMessageSenderService webHookMessageSenderService) {
        this.withdrawalReferenceDao = withdrawalReferenceDao;
        this.webHookDao = webHookDao;
        this.webHookMessageGeneratorService = webHookMessageGeneratorService;
        this.webHookMessageSenderService = webHookMessageSenderService;
        filter = new PathConditionFilter(new PathConditionRule("status_changed.failed", new IsNullCondition().not()));
    }

    @Override
    public void handle(Change change, SinkEvent event) {
        WithdrawalIdentityWalletReference reference = withdrawalReferenceDao.get(event.getSource());

        List<WebHookModel> webHookModels = webHookDao.getModelByIdentityAndWalletId(reference.getIdentityId(), reference.getIdentityId(), EventType.WITHDRAWAL_FAILED);

        webHookModels.stream()
                .map(webhook -> webHookMessageGeneratorService.generate(event.getSource(), webhook))
                .forEach(webHookMessageSenderService::send);
    }

    @Override
    public Filter getFilter() {
        return filter;
    }
}
