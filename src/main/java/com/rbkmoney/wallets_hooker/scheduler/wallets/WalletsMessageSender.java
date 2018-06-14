package com.rbkmoney.wallets_hooker.scheduler.wallets;

import com.rbkmoney.wallets_hooker.dao.TaskDao;
import com.rbkmoney.wallets_hooker.model.WalletsMessage;
import com.rbkmoney.wallets_hooker.model.WalletsQueue;
import com.rbkmoney.wallets_hooker.scheduler.MessageSender;
import com.rbkmoney.wallets_hooker.service.PostSender;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;

import java.util.List;

public class WalletsMessageSender extends MessageSender<WalletsMessage, WalletsQueue> {
    public WalletsMessageSender(MessageSender.QueueStatus<WalletsQueue> queueStatus, List<WalletsMessage> messages, TaskDao taskDao, Signer signer, PostSender postSender) {
        super(queueStatus, messages, taskDao, signer, postSender);
    }
}
