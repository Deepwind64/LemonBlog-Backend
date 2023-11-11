package com.bluemsun.lemonserver;

import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.autoconfigure.security.servlet.SecurityAutoConfiguration;
import org.springframework.cache.annotation.EnableCaching;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@EnableTransactionManagement //开启注解方式的事务管理
@SpringBootApplication(exclude = { SecurityAutoConfiguration.class })
@EnableCaching
@Slf4j
public class LemonApplication {
    public static void main(String[] args) {SpringApplication.run(LemonApplication.class, args);}
}
