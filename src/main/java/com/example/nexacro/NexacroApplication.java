package com.example.nexacro;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.nexacro.mapper")
public class NexacroApplication {
    public static void main(String[] args) {
        SpringApplication.run(NexacroApplication.class, args);
    }
}
