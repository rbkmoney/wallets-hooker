package com.rbkmoney.wallets_hooker.scheduler;

import com.rbkmoney.wallets_hooker.dao.TaskDao;
import com.rbkmoney.wallets_hooker.model.Message;
import com.rbkmoney.wallets_hooker.model.Queue;
import com.rbkmoney.wallets_hooker.service.PostSender;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;
import com.rbkmoney.wallets_hooker.service.err.PostRequestException;
import com.rbkmoney.wallets_hooker.utils.ConverterUtils;
import org.apache.http.HttpStatus;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.AbstractMap;
import java.util.List;
import java.util.concurrent.Callable;

public abstract class MessageSender<M extends Message, Q extends Queue> implements Callable<MessageSender.QueueStatus<Q>> {
    public static Logger log = LoggerFactory.getLogger(MessageSender.class);

    private MessageSender.QueueStatus<Q> queueStatus;
    private List<M> messages;
    private TaskDao taskDao;
    private Signer signer;
    private PostSender postSender;

    public MessageSender(MessageSender.QueueStatus<Q> queueStatus, List<M> messages, TaskDao taskDao, Signer signer, PostSender postSender) {
        this.queueStatus = queueStatus;
        this.messages = messages;
        this.taskDao = taskDao;
        this.signer = signer;
        this.postSender = postSender;
    }

    @Override
    public MessageSender.QueueStatus<Q> call() {
        try {
            for (M message : messages) {
                final String messageJson = ConverterUtils.getObjectMapper().writeValueAsString(message.getEvent());
                final String signature = signer.sign(messageJson, queueStatus.getQueue().getHook().getPrivKey());
                log.info("Sending message to hook: messageId: {}, {}, {}", message.getId(), queueStatus.getQueue().getHook().getUrl(), messageJson);
                AbstractMap.SimpleEntry<Integer, String> entry = postSender.doPost(queueStatus.getQueue().getHook().getUrl(), messageJson, signature);
                log.info("Response from hook: messageId: {}, code: {}; body: {}", message.getId(), entry.getKey(), entry.getValue());
                if (entry.getKey() != HttpStatus.SC_OK) {
                    log.info("Wrong status code {} from merchant, we'll try to resend it. MessageId: {}", entry.getKey(), message.getId());
                    throw new PostRequestException("Internal server error for message id = " + message.getId());
                }
                taskDao.remove(queueStatus.getQueue().getId(), message.getId()); //required after message is sent
            }
            queueStatus.setSuccess(true);
        } catch (Exception e) {
            log.warn("Couldn't send message to hook {}. We'll try to resend it", queueStatus.getQueue().getHook(), e);
            queueStatus.setSuccess(false);
        }
        return queueStatus;
    }

    public static class QueueStatus<Q extends Queue> {
        private Q queue;
        private boolean isSuccess;

        public QueueStatus(Q queue) {
            this.queue = queue;
        }

        public Q getQueue() {
            return queue;
        }

        public void setQueue(Q queue) {
            this.queue = queue;
        }

        public boolean isSuccess() {
            return isSuccess;
        }

        public void setSuccess(boolean success) {
            isSuccess = success;
        }
    }
}
