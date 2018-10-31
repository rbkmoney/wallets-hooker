package com.rbkmoney.wallets_hooker.configuration;


import org.jooq.Schema;
import org.jooq.impl.SchemaImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

@Configuration
public class AppConfiguration {

    @Bean
    public Schema dbSchema() {
        return new SchemaImpl("whook");
    }
}
