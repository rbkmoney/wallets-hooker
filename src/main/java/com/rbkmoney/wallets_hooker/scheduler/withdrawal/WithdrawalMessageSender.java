package com.rbkmoney.wallets_hooker.scheduler.withdrawal;

import com.rbkmoney.wallets_hooker.dao.TaskDao;
import com.rbkmoney.wallets_hooker.model.WithdrawalMessage;
import com.rbkmoney.wallets_hooker.model.WithdrawalQueue;
import com.rbkmoney.wallets_hooker.scheduler.MessageSender;
import com.rbkmoney.wallets_hooker.service.PostSender;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;

import java.util.List;

public class WithdrawalMessageSender extends MessageSender<WithdrawalMessage, WithdrawalQueue> {
    public WithdrawalMessageSender(MessageSender.QueueStatus<WithdrawalQueue> queueStatus,
                                   List<WithdrawalMessage> messages,
                                   TaskDao taskDao,
                                   Signer signer,
                                   PostSender postSender,
                                   WithdrawalMessageConverter converter) {
        super(queueStatus, messages, taskDao, signer, postSender, converter);
    }
}
