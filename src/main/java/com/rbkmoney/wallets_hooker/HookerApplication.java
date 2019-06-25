package com.rbkmoney.wallets_hooker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;
import org.springframework.kafka.annotation.EnableKafka;

@EnableKafka
@ServletComponentScan
@SpringBootApplication
public class HookerApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(HookerApplication.class, args);
    }

}
