package com.rbkmoney.wallets_hooker.dao.withdrawal;

import com.rbkmoney.mapper.RecordRowMapper;
import com.rbkmoney.wallets_hooker.dao.AbstractDao;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WithdrawalIdentityWalletReference;
import com.rbkmoney.wallets_hooker.domain.tables.records.WithdrawalIdentityWalletReferenceRecord;
import lombok.extern.slf4j.Slf4j;
import org.jooq.InsertReturningStep;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static com.rbkmoney.wallets_hooker.domain.tables.WithdrawalIdentityWalletReference.WITHDRAWAL_IDENTITY_WALLET_REFERENCE;

@Component
@Slf4j
public class WithdrawalReferenceDaoImpl extends AbstractDao implements WithdrawalReferenceDao {

    private final RowMapper<WithdrawalIdentityWalletReference> listRecordRowMapper;

    public WithdrawalReferenceDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.listRecordRowMapper = new RecordRowMapper<>(WITHDRAWAL_IDENTITY_WALLET_REFERENCE, WithdrawalIdentityWalletReference.class);
    }

    @Override
    public void create(WithdrawalIdentityWalletReference reference) {
        InsertReturningStep<WithdrawalIdentityWalletReferenceRecord> insertReturningStep = getDslContext()
                .insertInto(WITHDRAWAL_IDENTITY_WALLET_REFERENCE)
                .set(getDslContext()
                        .newRecord(WITHDRAWAL_IDENTITY_WALLET_REFERENCE, reference))
                .onConflict(WITHDRAWAL_IDENTITY_WALLET_REFERENCE.WITHDRAWAL_ID)
                .doNothing();
        execute(insertReturningStep);

        log.info("withdrawalIdentityWalletReference has been created, withdrawalIdentityWalletReference={} ", reference.toString());
    }

    @Override
    public WithdrawalIdentityWalletReference get(String id) {
        WithdrawalIdentityWalletReference withdrawalIdentityWalletReference = fetchOne(getDslContext()
                        .select(WITHDRAWAL_IDENTITY_WALLET_REFERENCE.WITHDRAWAL_ID,
                                WITHDRAWAL_IDENTITY_WALLET_REFERENCE.WALLET_ID,
                                WITHDRAWAL_IDENTITY_WALLET_REFERENCE.IDENTITY_ID,
                                WITHDRAWAL_IDENTITY_WALLET_REFERENCE.EVENT_ID,
                                WITHDRAWAL_IDENTITY_WALLET_REFERENCE.SEQUENCE_ID,
                                WITHDRAWAL_IDENTITY_WALLET_REFERENCE.EXTERNAL_ID)
                        .from(WITHDRAWAL_IDENTITY_WALLET_REFERENCE)
                        .where(WITHDRAWAL_IDENTITY_WALLET_REFERENCE.WITHDRAWAL_ID.eq(id)),
                listRecordRowMapper);

        if (withdrawalIdentityWalletReference != null) {
            log.info("withdrawalIdentityWalletReference has been got, withdrawalIdentityWalletReference={}", withdrawalIdentityWalletReference.toString());
        }

        return withdrawalIdentityWalletReference;
    }
}
