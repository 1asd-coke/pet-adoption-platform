package com.clawpet;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@MapperScan("com.clawpet.mapper")
@EnableScheduling
public class ClawPetApplication {
    public static void main(String[] args) {
        SpringApplication.run(ClawPetApplication.class, args);
    }
}
