package com.rbkmoney.wallets_hooker.scheduler;

import com.rbkmoney.wallets_hooker.dao.MessageDao;
import com.rbkmoney.wallets_hooker.dao.QueueDao;
import com.rbkmoney.wallets_hooker.dao.TaskDao;
import com.rbkmoney.wallets_hooker.model.Message;
import com.rbkmoney.wallets_hooker.model.Queue;
import com.rbkmoney.wallets_hooker.model.Task;
import com.rbkmoney.wallets_hooker.model.TaskQueuePair;
import com.rbkmoney.wallets_hooker.retry.RetryPoliciesService;
import com.rbkmoney.wallets_hooker.retry.RetryPolicyType;
import com.rbkmoney.wallets_hooker.service.PostSender;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public abstract class MessageScheduler<M extends Message, Q extends Queue> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private final TaskDao taskDao;
    private final QueueDao<Q> queueDao;
    private final MessageDao<M> messageDao;
    private final RetryPoliciesService retryPoliciesService;
    private final Set<Long> processedQueues = Collections.synchronizedSet(new HashSet<>());
    private ExecutorService executorService;


    public MessageScheduler(TaskDao taskDao, QueueDao<Q> queueDao, MessageDao<M> messageDao, RetryPoliciesService retryPoliciesService, int numberOfWorkers) {
        this.taskDao = taskDao;
        this.queueDao = queueDao;
        this.messageDao = messageDao;
        this.retryPoliciesService = retryPoliciesService;
        this.executorService = Executors.newFixedThreadPool(numberOfWorkers);
    }

    @Scheduled(fixedRateString = "${message.scheduler.delay}")
    public void loop() throws InterruptedException {
        final List<Long> currentlyProcessedQueues = new ArrayList<>(processedQueues);
        Map<Long, List<TaskQueuePair<Q>>> taskQueuePairsMap = queueDao.getTaskQueuePairsMap(currentlyProcessedQueues);
        if (taskQueuePairsMap.entrySet().isEmpty()) {
            return;
        }
        final Map<Long, Q> queuesMap = taskQueuePairsMap.entrySet()
                .stream().collect(Collectors.toMap(Map.Entry::getKey, v -> v.getValue().get(0).getQueue()));

        processedQueues.addAll(queuesMap.keySet());

        //Set of message ids.
        final Set<Long> messageIdsToSend = taskQueuePairsMap.values().stream().flatMap(Collection::stream).map(x -> x.getTask().getMessageId()).collect(Collectors.toSet());
        final Map<Long, M> messagesMap = messageDao.getBy(messageIdsToSend).stream().collect(Collectors.toMap(Message::getId, x -> x));

        log.info("Schedulled tasks count = {}", messageIdsToSend.size());

        List<MessageSender<M, Q>> messageSenderList = new ArrayList<>(queuesMap.keySet().size());
        for (long queueId : queuesMap.keySet()) {
            List<Task> tasks = taskQueuePairsMap.get(queueId).stream().map(TaskQueuePair::getTask).collect(Collectors.toList());
            List<M> messagesForQueue = new ArrayList<>();
            for (Task task : tasks) {
                M e = messagesMap.get(task.getMessageId());
                if (e != null) {
                    messagesForQueue.add(e);
                } else {
                    log.error("WithdrawalMessage with id {} couldn't be null", task.getMessageId());
                }
            }
            MessageSender<M, Q> messageSender = getMessageSender(new MessageSender.QueueStatus<>(queuesMap.get(queueId)), messagesForQueue);
            messageSenderList.add(messageSender);
        }

        List<Future<MessageSender.QueueStatus<Q>>> futureList = executorService.invokeAll(messageSenderList);
        for (Future<MessageSender.QueueStatus<Q>> status : futureList) {
            if (!status.isCancelled()) {
                try {
                    MessageSender.QueueStatus<Q> queueStatus = status.get();
                    processedQueues.remove(queueStatus.getQueue().getId());
                    if (queueStatus.isSuccess()) {
                        done(queueStatus.getQueue());
                    } else {
                        fail(queueStatus.getQueue());
                    }
                } catch (ExecutionException e) {
                    log.error("Unexpected error when get queue");
                }
            }
        }
    }

    protected abstract MessageSender<M, Q> getMessageSender(MessageSender.QueueStatus<Q> queueStatus, List<M> messagesForQueue);

    //worker should invoke this method when it is done with scheduled messages for hookId
    private void done(Q queue) {
        //reset fail count for hook
        if (queue.isFailed()) {
            queue.resetRetries();
            queueDao.updateRetries(queue);
        }
    }

    //worker should invoke this method when it is fail to send message to hookId
    private void fail(Q queue) {
        log.warn("Queue {} failed.", queue.getId());
        RetryPolicyType retryPolicyType = queue.getHook().getRetryPolicyType();
        if (retryPoliciesService.getRetryPolicyByType(retryPolicyType).shouldDisable(queue)) {
            queueDao.disable(queue.getId());
            taskDao.removeAll(queue.getId());
            log.warn("Queue {} was disabled according to retry policy.", queue.getId());
        }
        queueDao.updateRetries(queue);
    }

    @PreDestroy
    public void preDestroy(){
        executorService.shutdownNow();
        try {
            if (!executorService.awaitTermination(5, TimeUnit.SECONDS)) {
                log.warn("Failed to stop scheduller in time.");
            } else {
                log.info("Poller stopped.");
            }
        } catch (InterruptedException e) {
            log.warn("Waiting for scheduller shutdown is interrupted.");
        }
    }
}
