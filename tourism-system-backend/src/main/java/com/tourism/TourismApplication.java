package com.tourism;

import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;

/**
 * 全国旅游景点信息管理系统 - 启动类
 * SpringBoot 2.7.18 + JDK17
 */
@SpringBootApplication
@MapperScan("com.tourism.mapper")
public class TourismApplication {

    public static void main(String[] args) {
        SpringApplication.run(TourismApplication.class, args);
        System.out.println("\n================================================");
        System.out.println("  全国旅游景点信息管理系统启动成功!");
        System.out.println("  访问地址: http://localhost:8080");
        System.out.println("  测试账号: admin / 123456");
        System.out.println("================================================\n");
    }
}
