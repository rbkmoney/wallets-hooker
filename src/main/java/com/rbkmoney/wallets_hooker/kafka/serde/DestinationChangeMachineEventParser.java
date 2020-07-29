package com.rbkmoney.wallets_hooker.kafka.serde;

import com.rbkmoney.fistful.destination.TimestampedChange;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import com.rbkmoney.sink.common.serialization.BinaryDeserializer;
import org.springframework.stereotype.Service;

@Service
public class DestinationChangeMachineEventParser extends MachineEventParser<TimestampedChange> {

    public DestinationChangeMachineEventParser(BinaryDeserializer<TimestampedChange> deserializer) {
        super(deserializer);
    }
}