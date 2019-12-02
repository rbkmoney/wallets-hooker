ALTER TABLE whook.destination_identity_reference
    ADD COLUMN external_id character varying;

ALTER TABLE whook.withdrawal_identity_wallet_reference
    ADD COLUMN external_id character varying;
