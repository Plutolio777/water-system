package com.qingyuan.water;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@MapperScan("com.qingyuan.water.mapper")
@SpringBootApplication
public class WaterSystemApplication {
    public static void main(String[] args) {
        SpringApplication.run(WaterSystemApplication.class, args);
    }
}
