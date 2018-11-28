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
    private MessageConverter<M> messageConverter;

    public MessageSender(MessageSender.QueueStatus<Q> queueStatus, List<M> messages, TaskDao taskDao, Signer signer, PostSender postSender, MessageConverter<M> messageConverter) {
        this.queueStatus = queueStatus;
        this.messages = messages;
        this.taskDao = taskDao;
        this.signer = signer;
        this.postSender = postSender;
        this.messageConverter = messageConverter;
    }

    @Override
    public MessageSender.QueueStatus<Q> call() {
        M currentMessage = null;
        try {
            for (M message : messages) {
                currentMessage = message;
                final String messageJson = messageConverter.convertToJson(message);
                final String signature = signer.sign(messageJson, queueStatus.getQueue().getHook().getPrivKey());
                int statusCode = postSender.doPost(queueStatus.getQueue().getHook().getUrl(), message.getId(), messageJson, signature);
                if (statusCode != HttpStatus.SC_OK) {
                    String wrongCodeMessage = String.format("Wrong status code: %d from merchant, we'll try to resend it. Message with id: %d %s", statusCode, message.getId(), message);
                    log.info(wrongCodeMessage);
                    throw new PostRequestException(wrongCodeMessage);
                }
                taskDao.remove(queueStatus.getQueue().getId(), message.getId()); //required after message is sent
            }
            queueStatus.setSuccess(true);
        } catch (Exception e) {
            log.warn("Couldn't send message with id {} {} to hook {}. We'll try to resend it", currentMessage.getId(), currentMessage, queueStatus.getQueue().getHook(), e);
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
