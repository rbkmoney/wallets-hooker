create schema if not exists whook;

CREATE TYPE whook.message_type AS ENUM (
    'WITHDRAWAL',
    'IDENTITY',
    'WALLET');

CREATE TYPE whook.event_type AS ENUM (
    'WITHDRAWAL_CREATED',
    'WITHDRAWAL_SUCCEEDED',
    'WITHDRAWAL_FAILED',
    'IDENTITY_CREATED',
    'WALLET_CREATED',
    'WALLET_ACCOUNT_CREATED');

-- Table: hook.webhook
CREATE TABLE whook.webhook
(
    id bigserial NOT NULL,
    party_id character varying NOT NULL,
    url character varying NOT NULL,
    enabled boolean NOT NULL DEFAULT true,
    CONSTRAINT pk_webhook PRIMARY KEY (id)
);

create index webhook_party_id_key on whook.webhook(party_id);

-- Table: hook.webhook_to_events
CREATE TABLE whook.webhook_to_events
(
    hook_id bigint NOT NULL,
    message_type whook.message_type NOT NULL,
    event_type whook.event_type NOT NULL,
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

CREATE TABLE whook.withdrawal_message
(
    id bigserial NOT NULL,
    event_type whook.event_type NOT NULL,
    event_id bigint NOT NULL,
    party_id character varying NOT NULL,
    occured_at character varying NOT NULL,
    withdrawal_id character varying NOT NULL,
    withdrawal_created_at character varying NOT NULL,
    withdrawal_wallet_id character varying,
    withdrawal_destination_id character varying,
    withdrawal_amount bigint NOT NULL,
    withdrawal_currency_code character varying NOT NULL,
    withdrawal_metadata character varying,
    withdrawal_status character varying NOT NULL,
    withdrawal_failure_code character varying,
    CONSTRAINT withdrawal_message_pkey PRIMARY KEY (id)
);

CREATE TABLE whook.scheduled_task
(
    message_id bigint NOT NULL,
    queue_id bigint NOT NULL,
    message_type whook.message_type NOT NULL,
    CONSTRAINT scheduled_task_pkey PRIMARY KEY (message_id, queue_id, message_type)
);

CREATE TABLE whook.withdrawal_queue
(
    id bigserial NOT NULL,
    hook_id bigint NOT NULL,
    withdrawal_id CHARACTER VARYING NOT NULL,
    fail_count int NOT NULL DEFAULT 0,
    last_fail_time BIGINT,
    next_time BIGINT,
    enabled boolean NOT NULL DEFAULT true,
    CONSTRAINT withdrawal_queue_pkey PRIMARY KEY (id),
    CONSTRAINT withdrawal_queue_pkey_by_hook_and_withdrawal_id UNIQUE (hook_id, withdrawal_id),
    CONSTRAINT fk_withdrawal_queue FOREIGN KEY (hook_id) REFERENCES whook.webhook(id)
);

CREATE INDEX hook_idx ON whook.webhook(party_id, id) WHERE enabled;
CREATE index message_event_id_idx ON whook.withdrawal_message(event_id);
CREATE index message_wallet_id_idx ON whook.withdrawal_message(withdrawal_id);

CREATE TABLE whook.identity_message (
  id bigserial NOT NULL,
  event_type whook.event_type NOT NULL,
  event_id bigint NOT NULL,
  party_id character varying NOT NULL,
  occured_at character varying NOT NULL,
  identity_id CHARACTER VARYING NOT NULL,
  CONSTRAINT identity_pkey PRIMARY KEY (id)
);

CREATE INDEX identity_event_id_idx on whook.identity_message(event_id);
CREATE INDEX identity_id_idx on whook.identity_message(identity_id);
CREATE INDEX identity_party_id_idx on whook.identity_message(party_id);

CREATE TABLE whook.wallet_message (
  id bigserial NOT NULL,
  event_type whook.event_type NOT NULL,
  event_id bigint NOT NULL,
  occured_at character varying NOT NULL,
  wallet_id CHARACTER VARYING NOT NULL,
  party_id character varying,
  identity_id CHARACTER VARYING,
  CONSTRAINT wallet_pkey PRIMARY KEY (id)
);

CREATE INDEX wallet_event_id_idx on whook.wallet_message(event_id);
CREATE INDEX wallet_id_idx on whook.wallet_message(wallet_id);

