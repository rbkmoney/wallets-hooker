package com.rbkmoney.wallets_hooker.scheduler.withdrawal;

import com.rbkmoney.wallets_hooker.dao.TaskDao;
import com.rbkmoney.wallets_hooker.dao.WithdrawalMessageDao;
import com.rbkmoney.wallets_hooker.dao.impl.WithdrawalQueueDao;
import com.rbkmoney.wallets_hooker.dao.impl.WithdrawalTaskDao;
import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;
import com.rbkmoney.wallets_hooker.model.WithdrawalQueue;
import com.rbkmoney.wallets_hooker.retry.RetryPoliciesService;
import com.rbkmoney.wallets_hooker.scheduler.MessageScheduler;
import com.rbkmoney.wallets_hooker.scheduler.MessageSender;
import com.rbkmoney.wallets_hooker.service.PostSender;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WithdrawalMessageScheduler extends MessageScheduler<WithdrawalMessage, WithdrawalQueue> {

    private final WithdrawalTaskDao taskDao;
    private final WithdrawalMessageConverter converter;
    private final Signer signer;
    private final PostSender postSender;

    public WithdrawalMessageScheduler(WithdrawalTaskDao taskDao,
                                      WithdrawalQueueDao queueDao,
                                      WithdrawalMessageDao messageDao,
                                      WithdrawalMessageConverter converter,
                                      RetryPoliciesService retryPoliciesService,
                                      Signer signer,
                                      PostSender postSender,
                                      @Value("${message.sender.number}") int numberOfWorkers) {
        super(taskDao, queueDao, messageDao, retryPoliciesService, numberOfWorkers);
        this.taskDao = taskDao;
        this.converter = converter;
        this.signer = signer;
        this.postSender = postSender;
    }

    @Override
    protected MessageSender<WithdrawalMessage, WithdrawalQueue> getMessageSender(MessageSender.QueueStatus<WithdrawalQueue> queueStatus, List<WithdrawalMessage> messagesForQueue) {
        return new WithdrawalMessageSender(queueStatus, messagesForQueue, taskDao, signer, postSender, converter);
    }
}
