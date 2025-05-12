package com.caicongyang.cloud.app.conf;

import com.netflix.appinfo.ApplicationInfoManager;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.ApplicationArguments;
import org.springframework.boot.ApplicationRunner;
import org.springframework.boot.info.GitProperties;
import org.springframework.stereotype.Component;

import java.util.HashMap;
import java.util.Map;

@Component
public class CommonApplicationRunner implements ApplicationRunner {

    @Autowired
    ApplicationInfoManager manager;

    @Autowired
    private GitProperties gitProperties;


    @Override
    public void run(ApplicationArguments args) throws Exception {
        Map<String, String> branch = new HashMap<>();
        branch.put("branch", gitProperties.getBranch());
        manager.registerAppMetadata(branch);
    }
}
