package com.rbkmoney.wallets_hooker.scheduler;

import com.rbkmoney.wallets_hooker.dao.MessageDao;
import com.rbkmoney.wallets_hooker.dao.QueueDao;
import com.rbkmoney.wallets_hooker.dao.TaskDao;
import com.rbkmoney.wallets_hooker.model.Message;
import com.rbkmoney.wallets_hooker.model.Queue;
import com.rbkmoney.wallets_hooker.model.Task;
import com.rbkmoney.wallets_hooker.retry.RetryPoliciesService;
import com.rbkmoney.wallets_hooker.retry.RetryPolicyType;
import com.rbkmoney.wallets_hooker.service.PostSender;
import com.rbkmoney.wallets_hooker.service.crypt.Signer;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;

import javax.annotation.PreDestroy;
import java.util.*;
import java.util.concurrent.*;
import java.util.stream.Collectors;

public abstract class MessageScheduler<M extends Message, Q extends Queue> {
    private final Logger log = LoggerFactory.getLogger(this.getClass());

    private TaskDao taskDao;
    private QueueDao<Q> queueDao;
    private MessageDao<M> messageDao;
    @Autowired
    private RetryPoliciesService retryPoliciesService;
    @Autowired
    private Signer signer;
    @Autowired
    private PostSender postSender;

    private final Set<Long> processedQueues = Collections.synchronizedSet(new HashSet<>());
    private ExecutorService executorService;


    public MessageScheduler(TaskDao taskDao, QueueDao<Q> queueDao, MessageDao<M> messageDao, int numberOfWorkers) {
        this.taskDao = taskDao;
        this.queueDao = queueDao;
        this.messageDao = messageDao;
        this.executorService = Executors.newFixedThreadPool(numberOfWorkers);
    }

    @Scheduled(fixedRateString = "${message.scheduler.delay}")
    public void loop() throws InterruptedException {
        final List<Long> currentlyProcessedQueues = new ArrayList<>(processedQueues);

        final Map<Long, List<Task>> scheduledTasks = taskDao.getScheduled(currentlyProcessedQueues);
        if (scheduledTasks.entrySet().isEmpty()) {
            return;
        }
        final Map<Long, Q> healthyQueues = queueDao.getList(scheduledTasks.keySet())
                .stream().collect(Collectors.toMap(Q::getId, v -> v));

        processedQueues.addAll(healthyQueues.keySet());

        final Set<Long> messageIdsToSend = getMessageIdsFilteredByQueues(scheduledTasks, healthyQueues.keySet());
        final Map<Long, M> messagesMap = loadMessages(messageIdsToSend);

        log.info("Schedulled tasks count = {}, after filter = {}", scheduledTasks.size(), messageIdsToSend.size());

        List<MessageSender<M, Q>> messageSenderList = new ArrayList<>(healthyQueues.keySet().size());
        for (long queueId : healthyQueues.keySet()) {
            List<Task> tasks = scheduledTasks.get(queueId);
            List<M> messagesForQueue = new ArrayList<>();
            for (Task task : tasks) {
                M e = messagesMap.get(task.getMessageId());
                if (e != null) {
                    messagesForQueue.add(e);
                } else {
                    log.error("WalletsMessage with id {} couldn't be null", task.getMessageId());
                }
            }
            MessageSender<M, Q> messageSender = getMessageSender(new MessageSender.QueueStatus<Q>(healthyQueues.get(queueId)), messagesForQueue, taskDao, signer, postSender);
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

    protected abstract MessageSender<M, Q> getMessageSender(MessageSender.QueueStatus<Q> queueStatus, List<M> messagesForQueue, TaskDao taskDao, Signer signer, PostSender postSender);

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

    private Set<Long> getMessageIdsFilteredByQueues(Map<Long, List<Task>> scheduledTasks, Collection<Long> queueIds) {
        final Set<Long> messageIds = new HashSet<>();
        for (long queueId : queueIds) {
            for (Task t : scheduledTasks.get(queueId)) {
                messageIds.add(t.getMessageId());
            }
        }
        return messageIds;
    }

    private Map<Long, M> loadMessages(Collection<Long> messageIds) {
        List<M> messages =  messageDao.getBy(messageIds);
        Map<Long, M> map = new HashMap<>();
        for(M message: messages){
            map.put(message.getId(), message);
        }
        return map;
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
