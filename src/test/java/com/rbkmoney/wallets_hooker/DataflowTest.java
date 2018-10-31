package com.rbkmoney.wallets_hooker;

/*
@TestPropertySource(properties = {"message.scheduler.delay=500"})
public class DataflowTest extends AbstractIntegrationTest {
    private static Logger log = LoggerFactory.getLogger(DataflowTest.class);

    @Autowired
    HookDao hookDao;

    @Autowired
    WithdrawalMessageDao messageDao;

    private final BlockingQueue<Event> hook1queue = new LinkedBlockingDeque<>(10);
    private final BlockingQueue<Event> hook2queue = new LinkedBlockingDeque<>(10);
    private final BlockingQueue<Event> hook3queue = new LinkedBlockingDeque<>(10);

    final List<Hook> hooks = new ArrayList<>();
    private static final  String HOOK_1 = "/hook1";
    private static final  String HOOK_2 = "/hook2";
    private static final  String HOOK_3 = "/hook3";

    private final Map<String, BlockingQueue> hookQueuesMap = new HashMap<>();
    {
        hookQueuesMap.put(HOOK_1, hook1queue);
        hookQueuesMap.put(HOOK_2, hook2queue);
        hookQueuesMap.put(HOOK_3, hook3queue);
    }


    String baseServerUrl;

    @Before
    public void setUp() throws Exception {
        //start mock web server
        //createByMessageId hooks
        if (baseServerUrl == null) {
            baseServerUrl = webserver(dispatcher());
            log.info("Mock server url: " + baseServerUrl);

            hooks.add(hookDao.create(hook("partyId1", "http://" + baseServerUrl + HOOK_1, EventType.WITHDRAWAL_CREATED)));
            hooks.add(hookDao.create(hook("partyId1", "http://" + baseServerUrl + HOOK_2, EventType.WITHDRAWAL_CREATED, EventType.WITHDRAWAL_SUCCEEDED)));
            hooks.add(hookDao.create(hook("partyId2", "http://" + baseServerUrl + HOOK_3, EventType.WITHDRAWAL_SUCCEEDED, EventType.WITHDRAWAL_FAILED)));
        }
    }

    @Test
    public void testMessageSend() throws InterruptedException {
        List<WithdrawalMessage> sourceMessages = new ArrayList<>();
        WithdrawalMessage message;
        message = ConverterUtils.buildWithdrawalMessage("WITHDRAWAL_CREATED", "partyId1", "walletId1", "2016-03-22T06:12:27Z", 123);
        messageDao.create(message);
        sourceMessages.add(message);
        message = ConverterUtils.buildWithdrawalMessage("WITHDRAWAL_SUCCEEDED", "partyId1", "walletId1", "2016-03-22T06:12:27Z", 124);
        messageDao.create(message);
        sourceMessages.add(message);
        message = ConverterUtils.buildWithdrawalMessage("WITHDRAWAL_CREATED", "partyId1", "walletId3", "2016-03-22T06:12:27Z", 125);
        messageDao.create(message);
        sourceMessages.add(message);
        message = ConverterUtils.buildWithdrawalMessage("WITHDRAWAL_CREATED", "partyId", "walletId4", "2016-03-22T06:12:27Z", 126);
        messageDao.create(message);
        sourceMessages.add(message);
        message = ConverterUtils.buildWithdrawalMessage("WITHDRAWAL_CREATED", "partyId2", "walletId5", "2016-03-22T06:12:27Z", 127);
        messageDao.create(message);
        sourceMessages.add(message);
        message = ConverterUtils.buildWithdrawalMessage("WITHDRAWAL_SUCCEEDED", "partyId2", "walletId5", "2016-03-22T06:12:27Z", 128);
        messageDao.create(message);
        sourceMessages.add(message);
        message = ConverterUtils.buildWithdrawalMessage("WITHDRAWAL_FAILED", "partyId2", "walletId5", "2016-03-22T06:12:27Z", 129);
        messageDao.create(message);
        sourceMessages.add(message);

        List<Event> hook1 = new ArrayList<>();
        List<Event> hook2 = new ArrayList<>();
        List<Event> hook3 = new ArrayList<>();

        Thread.currentThread().sleep(1000);

        for (int i = 0; i < 2; i++) {
            hook1.add(hook1queue.poll(1, TimeUnit.SECONDS));
        }
        assertEquals(hook1.stream().map(Event::getEventID).collect(Collectors.toSet()), new HashSet<>(Arrays.asList(123, 125)));

        for (int i = 0; i < 3; i++) {
            hook2.add(hook2queue.poll(1, TimeUnit.SECONDS));
        }
        assertEquals(hook2.stream().map(Event::getEventID).collect(Collectors.toSet()), new HashSet<>(Arrays.asList(123, 124, 125)));

        for (int i = 0; i < 2; i++) {
            hook3.add(hook3queue.poll(1, TimeUnit.SECONDS));
        }
        assertEquals(hook3.stream().map(Event::getEventID).collect(Collectors.toSet()), new HashSet<>(Arrays.asList(128, 129)));

        assertTrue(hook1queue.isEmpty());
        assertTrue(hook2queue.isEmpty());
        assertTrue(hook3queue.isEmpty());
    }

    private static Hook hook(String partyId, String url, EventType... types) {
        Hook hook = new Hook();
        hook.setPartyId(partyId);
        hook.setTopic(Event.TopicEnum.WALLETSTOPIC.getValue());
        hook.setUrl(url);

        Set<WebhookAdditionalFilter> webhookAdditionalFilters = new HashSet<>();
        for (EventType type : types) {
            webhookAdditionalFilters.add(new WebhookAdditionalFilter(type));
        }
        hook.setFilters(webhookAdditionalFilters);

        return hook;
    }

    public static Event extractMessage(RecordedRequest request) {
        try {
            String body = request.getBody().readUtf8();
            log.info("body: {}", body);
            Class<? extends Event> clazz;
            if (body.contains(Event.EventTypeEnum.WALLETWITHDRAWALSTARTED.getValue())) {
                clazz = WalletWithdrawalStarted.class;
            } else if (body.contains(Event.EventTypeEnum.WALLETWITHDRAWALSUCCEEDED.getValue())) {
                clazz = WalletWithdrawalSucceeded.class;
            } else if (body.contains(Event.EventTypeEnum.WALLETWITHDRAWALFAILED.getValue())){
                clazz = WalletWithdrawalFailed.class;
            } else {
                throw new RuntimeException();
            }
            return ConverterUtils.getObjectMapper()
                    .readValue(body, clazz);

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    private Dispatcher dispatcher() {
        final Dispatcher dispatcher = new Dispatcher() {

            @Override
            public MockResponse dispatch(RecordedRequest request) throws InterruptedException {
                log.info("requestPath = "+request.getPath());
                Event mockMessage = extractMessage(request);
                String path = request.getPath();

                BlockingQueue blockingQueue = hookQueuesMap.get(path);
                if (blockingQueue != null) {
                    blockingQueue.put(mockMessage);
                    Thread.sleep(100);
                    return new MockResponse().setBody("OK").setResponseCode(200);
                } else {
                    Thread.sleep(100);
                    return new MockResponse().setBody("FAIL").setResponseCode(500);
                }
            }
        };
        return dispatcher;
    }

    private String webserver(Dispatcher dispatcher) {
        final MockWebServer server = new MockWebServer();
        server.setDispatcher(dispatcher);
        try {
            server.start();
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        log.info("Mock Hook Server started on port: " + server.getPort());

        // process request
        new Thread(() -> {
            while (true) {
                try {
                    server.takeRequest();
                } catch (InterruptedException e) {
                    try {
                        server.shutdown();
                    } catch (IOException e1) {
                        new RuntimeException(e1);
                    }
                }
            }
        }).start();


        return server.getHostName() + ":" + server.getPort();
    }


}
*/
