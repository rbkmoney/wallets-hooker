package com.rbkmoney.wallets_hooker.dao.destination;

import com.rbkmoney.mapper.RecordRowMapper;
import com.rbkmoney.wallets_hooker.dao.AbstractDao;
import com.rbkmoney.wallets_hooker.domain.tables.pojos.DestinationIdentityReference;
import com.rbkmoney.wallets_hooker.domain.tables.records.DestinationIdentityReferenceRecord;
import lombok.extern.slf4j.Slf4j;
import org.jooq.InsertReturningStep;
import org.springframework.jdbc.core.RowMapper;
import org.springframework.stereotype.Component;

import javax.sql.DataSource;

import static com.rbkmoney.wallets_hooker.domain.tables.DestinationIdentityReference.DESTINATION_IDENTITY_REFERENCE;

@Component
@Slf4j
public class DestinationReferenceDaoImpl extends AbstractDao implements DestinationReferenceDao {

    private final RowMapper<DestinationIdentityReference> listRecordRowMapper;

    public DestinationReferenceDaoImpl(DataSource dataSource) {
        super(dataSource);
        this.listRecordRowMapper = new RecordRowMapper<>(DESTINATION_IDENTITY_REFERENCE, DestinationIdentityReference.class);
    }

    @Override
    public void create(DestinationIdentityReference reference) {
        InsertReturningStep<DestinationIdentityReferenceRecord> insertReturningStep = getDslContext()
                .insertInto(DESTINATION_IDENTITY_REFERENCE)
                .set(getDslContext()
                        .newRecord(DESTINATION_IDENTITY_REFERENCE, reference))
                .onConflict(DESTINATION_IDENTITY_REFERENCE.DESTINATION_ID)
                .doNothing();
        execute(insertReturningStep);

        log.info("destinationIdentityReference has been created, destinationIdentityReference={} ", reference.toString());
    }

    @Override
    public DestinationIdentityReference get(String id) {
        DestinationIdentityReference reference = fetchOne(getDslContext()
                        .select(DESTINATION_IDENTITY_REFERENCE.DESTINATION_ID,
                                DESTINATION_IDENTITY_REFERENCE.IDENTITY_ID,
                                DESTINATION_IDENTITY_REFERENCE.EVENT_ID,
                                DESTINATION_IDENTITY_REFERENCE.EXTERNAL_ID)
                        .from(DESTINATION_IDENTITY_REFERENCE)
                        .where(DESTINATION_IDENTITY_REFERENCE.DESTINATION_ID.eq(id)),
                listRecordRowMapper);

        if (reference != null) {
            log.info("destinationIdentityReference has been got, destinationIdentityReference={}", reference.toString());
        }

        return reference;
    }
}
