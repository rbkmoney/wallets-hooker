create schema if not exists whook;

CREATE TYPE whook.event_type AS ENUM (
    'WITHDRAWAL_CREATED',
    'WITHDRAWAL_SUCCEEDED',
    'WITHDRAWAL_FAILED',
    'DESTINATION_CREATED',
    'DESTINATION_AUTHORIZED',
    'DESTINATION_UNAUTHORIZED'
    );

-- Table: hook.webhook
CREATE TABLE whook.webhook
(
    id bigserial NOT NULL,
    identity_id character varying NOT NULL,
    wallet_id character varying,
    url character varying NOT NULL,
    enabled boolean NOT NULL DEFAULT true,
    CONSTRAINT pk_webhook PRIMARY KEY (id)
);

create index webhook_identity_id_key on whook.webhook(identity_id);

CREATE TABLE whook.webhook_to_events
(
    hook_id bigint NOT NULL,
    event_type whook.event_type NOT NULL,
    CONSTRAINT pk_webhook_to_events PRIMARY KEY (hook_id, event_type),
    CONSTRAINT fk_webhook_to_events FOREIGN KEY (hook_id) REFERENCES whook.webhook(id)
);

CREATE TABLE whook.identity_key
(
    id bigserial NOT NULL,
    identity_id character varying NOT NULL,
    pub_key character VARYING NOT NULL,
    priv_key character VARYING NOT NULL,
    CONSTRAINT pk_identity_key PRIMARY KEY (id),
    CONSTRAINT key_identity_id_key UNIQUE (identity_id)
);

CREATE TABLE whook.wallet_identity_reference (
  wallet_id character varying NOT NULL,
  identity_id character NOT NULL,
  CONSTRAINT wallet_identity_reference_pkey PRIMARY KEY (wallet_id)
);

CREATE TABLE whook.destination_identity_reference (
  destination_id character varying NOT NULL,
  identity_id character NOT NULL,
  CONSTRAINT destination_identity_reference_pkey PRIMARY KEY (destination_id)
);

CREATE TABLE whook.withdrawal_identity_wallet_reference (
  withdrawal_id character varying NOT NULL,
  identity_id character NOT NULL,
  wallet_id character NOT NULL,
  CONSTRAINT withdrawal_identity_wallet_reference_pkey PRIMARY KEY (withdrawal_id)
);