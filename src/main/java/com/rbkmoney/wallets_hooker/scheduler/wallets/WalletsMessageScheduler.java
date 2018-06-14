package com.rbkmoney.wallets_hooker.scheduler.wallets;

import com.rbkmoney.wallets_hooker.dao.TaskDao;
import com.rbkmoney.wallets_hooker.dao.WalletsMessageDao;
import com.rbkmoney.wallets_hooker.dao.impl.WalletsQueueDao;
import com.rbkmoney.wallets_hooker.dao.impl.WalletsTaskDao;
import com.rbkmoney.wallets_hooker.model.WalletsMessage;
import com.rbkmoney.wallets_hooker.model.WalletsQueue;
import com.rbkmoney.wallets_hooker.scheduler.MessageScheduler;
import com.rbkmoney.wallets_hooker.scheduler.MessageSender;
import com.rbkmoney.wallets_hooker.service.PostSender;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class WalletsMessageScheduler extends MessageScheduler<WalletsMessage, WalletsQueue> {

    public WalletsMessageScheduler(
            @Autowired WalletsTaskDao taskDao,
            @Autowired WalletsQueueDao queueDao,
            @Autowired WalletsMessageDao messageDao,
            @Value("${message.sender.number}") int numberOfWorkers) {
        super(taskDao, queueDao, messageDao, numberOfWorkers);
    }

    @Override
    protected MessageSender<WalletsMessage, WalletsQueue> getMessageSender(MessageSender.QueueStatus<WalletsQueue> queueStatus, List<WalletsMessage> messagesForQueue, TaskDao taskDao, Signer signer, PostSender postSender) {
        return new WalletsMessageSender(queueStatus, messagesForQueue, taskDao, signer, postSender);
    }
}
