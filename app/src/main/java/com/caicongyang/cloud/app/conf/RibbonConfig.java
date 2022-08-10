package com.caicongyang.cloud.app.conf;

import com.caicongyang.cloud.app.rule.GitBranchRule;
import com.netflix.loadbalancer.IRule;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * @author caicongyang
 */
@Configuration
public class RibbonConfig {

    /**
     * 全局配置
     * 指定负载均衡策略
     *
     * @return
     */
    @Bean
    public IRule ribbonRule() {
        return new GitBranchRule();
    }
}
