package com.example.word;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

@SpringBootApplication
@MapperScan("com.example.word.mapper")
public class WordTreeApplication {
    public static void main(String[] args) {
        SpringApplication.run(WordTreeApplication.class, args);
        System.out.println("------------------------------------------");
        System.out.println("   项目启动成功！访问: http://localhost:8082");
        System.out.println("------------------------------------------");
    }
}