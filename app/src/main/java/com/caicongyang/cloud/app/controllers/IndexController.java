package com.caicongyang.cloud.app.controllers;

import com.netflix.client.http.HttpHeaders;
import com.netflix.client.http.HttpRequest;
import nonapi.io.github.classgraph.json.JSONUtils;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.info.GitProperties;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestMethod;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.Map;

@RestController
public class IndexController {

    @Autowired
    private GitProperties gitProperties;


    @Autowired
    DiscoveryClient discoveryClient ;

    @RequestMapping("/hello")
    public String hellow() {
        String getConf = "hello, sping Cloud!";
        return getConf;
    }


    @RequestMapping(value = "/git-info", method = RequestMethod.GET)
    public String gitinfo() {
        String gitInfo = gitProperties.getBranch() + ":" + gitProperties.getCommitId();
        List<ServiceInstance> app = discoveryClient.getInstances("app");
        return gitInfo;
    }

}
