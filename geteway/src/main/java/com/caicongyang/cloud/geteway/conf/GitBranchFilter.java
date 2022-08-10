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
        List<String> branch = exchange.getRequest().getHeaders().get("branch");
        if (CollectionUtils.isEmpty(branch)) {
            return chain.filter(exchange);
        }
        // todo  提供一个页面，从内存或者数据中拿
        exchange = exchange.mutate().request(exchange.getRequest().mutate().header("branch", "master").build()).build();
        return chain.filter(exchange);
    }
}
