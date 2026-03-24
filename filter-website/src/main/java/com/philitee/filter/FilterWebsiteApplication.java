package com.philitee.filter;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cache.annotation.EnableCaching;

/**
 * Philitee Filter B2B 企业官网启动类
 *
 * @author philitee
 */
@SpringBootApplication
@MapperScan("com.philitee.filter.mapper")
@EnableCaching
public class FilterWebsiteApplication {

    public static void main(String[] args) {
        SpringApplication.run(FilterWebsiteApplication.class, args);
    }

}
