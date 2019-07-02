package com.rbkmoney.wallets_hooker.dao.wallet;

import com.rbkmoney.mapper.RecordRowMapper;
import com.rbkmoney.wallets_hooker.dao.AbstractDao;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.WalletIdentityReference;
import com.rbkmoney.wallets_hooker.domain.tables.records.WalletIdentityReferenceRecord;
import org.jooq.InsertReturningStep;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static com.rbkmoney.wallets_hooker.domain.tables.WalletIdentityReference.WALLET_IDENTITY_REFERENCE;

@Component
public class WalletReferenceDaoImpl extends AbstractDao implements WalletReferenceDao {

    private final RowMapper<WalletIdentityReference> listRecordRowMapper;

    public WalletReferenceDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.listRecordRowMapper = new RecordRowMapper<>(WALLET_IDENTITY_REFERENCE, WalletIdentityReference.class);
    }

    @Override
    public void create(WalletIdentityReference reference) {
        InsertReturningStep<WalletIdentityReferenceRecord> insertReturningStep = getDslContext()
                .insertInto(WALLET_IDENTITY_REFERENCE)
                .set(getDslContext()
                        .newRecord(WALLET_IDENTITY_REFERENCE, reference))
                .onConflict(WALLET_IDENTITY_REFERENCE.WALLET_ID)
                .doNothing();
        execute(insertReturningStep);
    }

    @Override
    public WalletIdentityReference get(String id) {
        return fetchOne(getDslContext()
                        .select(WALLET_IDENTITY_REFERENCE.WALLET_ID, WALLET_IDENTITY_REFERENCE.IDENTITY_ID)
                        .from(WALLET_IDENTITY_REFERENCE)
                        .where(WALLET_IDENTITY_REFERENCE.WALLET_ID.eq(id)),
                listRecordRowMapper);
    }
}
