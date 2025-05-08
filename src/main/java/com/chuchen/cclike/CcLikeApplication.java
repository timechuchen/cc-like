package com.chuchen.cclike;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.chuchen.cclike.mapper")
public class CcLikeApplication {

    public static void main(String[] args) {
        SpringApplication.run(CcLikeApplication.class, args);
    }

}