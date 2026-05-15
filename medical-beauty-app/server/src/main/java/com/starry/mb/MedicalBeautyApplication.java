package com.starry.mb;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

@SpringBootApplication
@EnableScheduling
@MapperScan("com.starry.mb.**.mapper")
public class MedicalBeautyApplication {
    public static void main(String[] args) {
        SpringApplication.run(MedicalBeautyApplication.class, args);
    }
}
