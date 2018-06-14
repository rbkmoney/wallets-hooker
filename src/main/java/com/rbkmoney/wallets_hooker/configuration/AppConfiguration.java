package com.rbkmoney.wallets_hooker.configuration;

import com.rbkmoney.wallets_hooker.dao.HookDao;
import com.rbkmoney.wallets_hooker.dao.WalletsMessageDao;
import com.rbkmoney.wallets_hooker.dao.impl.*;
import org.jooq.Schema;
import org.jooq.impl.SchemaImpl;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.DependsOn;
import org.springframework.jdbc.core.namedparam.NamedParameterJdbcTemplate;

import javax.sql.DataSource;

@Configuration
public class AppConfiguration {

    @Bean
    @DependsOn("dbInitializer")
    public HookDao webhookDao(NamedParameterJdbcTemplate jdbcTemplate) {
        return new HookDaoImpl(jdbcTemplate);
    }

    @Bean
    @DependsOn("dbInitializer")
    public WalletsMessageDao messageDao(DataSource dataSource) {
        return new CacheableWalletsMessageDaoImpl(dataSource);
    }

    @Bean
    @DependsOn("dbInitializer")
    public WalletsTaskDao walletsTaskDao(DataSource dataSource) {
        return new WalletsTaskDao(dataSource);
    }

    @Bean
    @DependsOn("dbInitializer")
    public WalletsQueueDao walletsQueueDao(DataSource dataSource) {
        return new CacheableWalletsQueueDao(dataSource);
    }

    @Bean
    public Schema dbSchema() {
        return new SchemaImpl("whook");
    }
}
