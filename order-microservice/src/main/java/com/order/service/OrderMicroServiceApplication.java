package com.order.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.order.service.client")
public class OrderMicroServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(OrderMicroServiceApplication.class, args);
    }
}