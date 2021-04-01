package com.rbkmoney.wallets.hooker.dao.identity;

import com.rbkmoney.mapper.RecordRowMapper;
import com.rbkmoney.wallets.hooker.dao.AbstractDao;
import com.rbkmoney.wallets.hooker.domain.tables.pojos.IdentityKey;
import com.rbkmoney.wallets.hooker.domain.tables.records.IdentityKeyRecord;
import org.jooq.InsertReturningStep;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static com.rbkmoney.wallets.hooker.domain.tables.IdentityKey.IDENTITY_KEY;

@Component
public class IdentityKeyDaoImpl extends AbstractDao implements IdentityKeyDao {

    private final RowMapper<IdentityKey> listRecordRowMapper;

    public IdentityKeyDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.listRecordRowMapper = new RecordRowMapper<>(IDENTITY_KEY, IdentityKey.class);
    }

    @Override
    public void create(IdentityKey identityKey) {
        InsertReturningStep<IdentityKeyRecord> identityKeyRecordInsertReturningStep = getDslContext()
                .insertInto(IDENTITY_KEY)
                .set(getDslContext()
                        .newRecord(IDENTITY_KEY, identityKey))
                .onConflict(IDENTITY_KEY.IDENTITY_ID)
                .doNothing();
        execute(identityKeyRecordInsertReturningStep);
    }

    @Override
    public IdentityKey get(Long id) {
        return fetchOne(getDslContext()
                        .select(IDENTITY_KEY.ID, IDENTITY_KEY.IDENTITY_ID, IDENTITY_KEY.PUB_KEY, IDENTITY_KEY.PRIV_KEY)
                        .from(IDENTITY_KEY)
                        .where(IDENTITY_KEY.ID.eq(id)),
                listRecordRowMapper);
    }

    @Override
    public IdentityKey getByIdentity(String identityId) {
        return fetchOne(getDslContext()
                        .select(IDENTITY_KEY.ID, IDENTITY_KEY.IDENTITY_ID, IDENTITY_KEY.PUB_KEY, IDENTITY_KEY.PRIV_KEY)
                        .from(IDENTITY_KEY)
                        .where(IDENTITY_KEY.IDENTITY_ID.eq(identityId)),
                listRecordRowMapper);
    }
}
