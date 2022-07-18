package com.caicongyang.springcloudapp;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.EnableAutoConfiguration;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

/**
 * Spring could web程序主入口
 * @author Administrator
 *
 */

@EnableDiscoveryClient
@EnableEurekaClient
@SpringBootApplication
public class Application {
	public static void main(String[] args) {   
        //第一个简单的应用，   
        SpringApplication.run(Application.class,args);   
    }   
}
