package com.caicongyang.cloud.app;

import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.cloud.client.discovery.EnableDiscoveryClient;
import org.springframework.cloud.netflix.eureka.EnableEurekaClient;
import org.springframework.cloud.openfeign.EnableFeignClients;

/**
 * Spring could web程序主入口
 * @author Administrator
 *
 * swagger地址
 * http://localhost:8080/swagger-ui/index.html
 *
 */

@SpringBootApplication
@EnableDiscoveryClient
@EnableEurekaClient
@EnableFeignClients(basePackages = "com.caicongyang")
public class Application {
	public static void main(String[] args) {   
        //第一个简单的应用，   
        SpringApplication.run(Application.class,args);   
    }   
}
