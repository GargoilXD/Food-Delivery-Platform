package com.restaurant.service;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

@SpringBootApplication
@EnableDiscoveryClient
@EnableFeignClients(basePackages = "com.restaurant.service.client")
public class RestaurantMicroServiceApplication {
    public static void main(String[] args) {
        SpringApplication.run(RestaurantMicroServiceApplication.class, args);
    }
}