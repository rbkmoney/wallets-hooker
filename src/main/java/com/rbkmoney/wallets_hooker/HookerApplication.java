package com.rbkmoney.wallets_hooker;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.web.servlet.ServletComponentScan;

@ServletComponentScan
@SpringBootApplication(scanBasePackages = {"com.rbkmoney.wallets_hooker", "com.rbkmoney.dbinit"})
public class HookerApplication {
    public static void main(String[] args) throws Exception {
        SpringApplication.run(HookerApplication.class, args);
    }
}
