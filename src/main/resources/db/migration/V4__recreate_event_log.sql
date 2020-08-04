DROP TABLE whook.event_log;

CREATE TYPE whook.event_topic AS ENUM ('destination', 'wallet', 'withdrawal');

CREATE TABLE whook.event_log
(
    source_id   CHARACTER VARYING NOT NULL,
    event_id    BIGINT            NOT NULL,
    event_topic whook.event_topic NOT NULL,
    CONSTRAINT event_log_pkey PRIMARY KEY (source_id, event_id, event_topic)
);