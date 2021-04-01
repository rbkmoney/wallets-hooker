package com.rbkmoney.wallets.hooker.kafka.serde;

import com.rbkmoney.fistful.wallet.TimestampedChange;
import com.rbkmoney.sink.common.parser.impl.MachineEventParser;
import com.rbkmoney.sink.common.serialization.BinaryDeserializer;
import org.springframework.stereotype.Service;

@Service
public class WalletChangeMachineEventParser extends MachineEventParser<TimestampedChange> {

    public WalletChangeMachineEventParser(BinaryDeserializer<TimestampedChange> deserializer) {
        super(deserializer);
    }
}