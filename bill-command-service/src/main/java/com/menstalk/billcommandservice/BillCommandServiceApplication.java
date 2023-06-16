package com.menstalk.billcommandservice;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableFeignClients
public class BillCommandServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(BillCommandServiceApplication.class, args);
    }

}

