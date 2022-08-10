package com.caicongyang.cloud.geteway.conf;

import org.springframework.cloud.gateway.filter.GatewayFilterChain;
import org.springframework.cloud.gateway.filter.GlobalFilter;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;
import reactor.core.publisher.Mono;

import java.util.List;

@Component
public class GitBranchFilter implements GlobalFilter {

    @Override
    public Mono<Void> filter(ServerWebExchange exchange, GatewayFilterChain chain) {
        // 开发测试人员已经将branch 塞到http header
        List<String> branch = exchange.getRequest().getHeaders().get("branch");
        if (CollectionUtils.isEmpty(branch)) {
            return chain.filter(exchange);
        }
        // todo 从DiscoveryClient拿到所有的服务实例列表，供开发人员来绑定自己的ip 与分支，并将branch 传递下去
        exchange = exchange.mutate().request(exchange.getRequest().mutate().header("branch", "master").build()).build();
        return chain.filter(exchange);
    }
}
