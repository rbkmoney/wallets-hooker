create schema if not exists whook;

CREATE TYPE whook.RetryPolicyType AS ENUM ('SIMPLE');

CREATE TYPE whook.EventType AS ENUM (
    'WALLET_WITHDRAWAL_CREATED',
    'WALLET_WITHDRAWAL_SUCCEEDED',
    'WALLET_WITHDRAWAL_FAILED');

CREATE TYPE whook.message_topic AS ENUM ('WalletsTopic');

-- Table: hook.webhook
CREATE TABLE whook.webhook
(
    id bigserial NOT NULL,
    topic whook.message_topic,
    party_id character varying NOT NULL,
    url character varying NOT NULL,
    retry_policy whook.RetryPolicyType NOT NULL DEFAULT 'SIMPLE',
    enabled boolean NOT NULL DEFAULT true,
    CONSTRAINT pk_webhook PRIMARY KEY (id)
);

create index webhook_party_id_key on whook.webhook(party_id);

-- Table: hook.webhook_to_events
CREATE TABLE whook.webhook_to_events
(
    hook_id bigint NOT NULL,
    event_type whook.EventType NOT NULL,
    CONSTRAINT fk_webhook_to_events FOREIGN KEY (hook_id) REFERENCES whook.webhook(id)
);

CREATE TABLE whook.party_key
(
    id bigserial NOT NULL,
    party_id character varying NOT NULL,
    pub_key character VARYING NOT NULL,
    priv_key character VARYING NOT NULL,
    CONSTRAINT pk_party_key PRIMARY KEY (id),
    CONSTRAINT key_party_id_key UNIQUE (party_id)
);

CREATE TABLE whook.wallets_message
(
    id bigserial NOT NULL,
    event_type whook.EventType NOT NULL,
    event_id int NOT NULL,
    event_time character varying NOT NULL,
    party_id character varying NOT NULL,
    wallet_id character varying NOT NULL,
    CONSTRAINT message_pkey PRIMARY KEY (id)
);

CREATE TABLE whook.scheduled_task
(
    message_id bigint NOT NULL,
    queue_id bigint NOT NULL,
    message_type whook.message_topic,
    CONSTRAINT scheduled_task_pkey PRIMARY KEY (message_id, queue_id, message_type)
);

CREATE TABLE whook.wallets_queue
(
    id bigserial NOT NULL,
    hook_id bigint NOT NULL,
    wallet_id CHARACTER VARYING NOT NULL,
    fail_count int NOT NULL DEFAULT 0,
    last_fail_time BIGINT,
    next_time BIGINT,
    enabled boolean NOT NULL DEFAULT true,
    CONSTRAINT wallets_queue_pkey PRIMARY KEY (id),
    CONSTRAINT wallets_queue_pkey2 UNIQUE (hook_id, wallet_id)
);

CREATE INDEX hook_idx ON whook.webhook(party_id, id) WHERE enabled;
CREATE index message_event_id_idx ON whook.wallets_message(event_id);
CREATE index message_wallet_id_idx ON whook.wallets_message(wallet_id);
