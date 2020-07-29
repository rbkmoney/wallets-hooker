package com.rbkmoney.wallets_hooker.kafka.serde;

import com.rbkmoney.fistful.withdrawal.TimestampedChange;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import com.rbkmoney.sink.common.serialization.BinaryDeserializer;
import org.springframework.stereotype.Service;

@Service
public class WithdrawalChangeMachineEventParser extends MachineEventParser<TimestampedChange> {

    public WithdrawalChangeMachineEventParser(BinaryDeserializer<TimestampedChange> deserializer) {
        super(deserializer);
    }
}