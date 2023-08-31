package com.caicongyang.cloud.geteway.conf;

import com.caicongyang.cloud.geteway.utils.IPUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cloud.client.ServiceInstance;
import org.springframework.cloud.client.discovery.DiscoveryClient;
import org.springframework.cloud.client.loadbalancer.LoadBalancerClient;
import org.springframework.cloud.gateway.config.LoadBalancerProperties;
import org.springframework.cloud.gateway.filter.LoadBalancerClientFilter;
import org.springframework.http.server.reactive.ServerHttpRequest;
import org.springframework.stereotype.Component;
import org.springframework.util.CollectionUtils;
import org.springframework.web.server.ServerWebExchange;

import java.net.URI;
import java.util.List;
import java.util.stream.Collectors;

import static org.springframework.cloud.gateway.support.ServerWebExchangeUtils.GATEWAY_REQUEST_URL_ATTR;

@Component
public class GitBranchFilter extends LoadBalancerClientFilter {


    @Autowired
    DiscoveryClient client;


    public GitBranchFilter(LoadBalancerClient loadBalancer, LoadBalancerProperties properties) {
        super(loadBalancer, properties);
    }

    @Override
    protected ServiceInstance choose(ServerWebExchange exchange) {
        ServerHttpRequest request = exchange.getRequest();
        // todo 开发测试人员已经将branch 塞到http header
        // List<String> branchList = request.getHeaders().get("branch");
        //  if (CollectionUtils.isEmpty(branchList)) {
        // return super.choose(exchange);
        //  }

        String ipAddress = IPUtil.getIpAddress(request);
        // todo 从DiscoveryClient拿到所有的服务实例列表，供开发人员来绑定自己的ip 与分支，并将branch 传递下去
        String branch = "master";

        URI uri = exchange.getAttribute(GATEWAY_REQUEST_URL_ATTR);
        String host = uri.getHost();
        List<ServiceInstance> instances = client.getInstances(host);

        List<ServiceInstance> branchList = instances.stream().filter(a -> a.getMetadata().get("branch").equalsIgnoreCase(branch)).collect(Collectors.toList());
        if (CollectionUtils.isEmpty(branchList)) {
            //默认master
            branchList = instances.stream().filter(a -> a.getMetadata().get("branch").equalsIgnoreCase("master")).collect(Collectors.toList());
        }
        // 往下传递
        exchange.mutate().request(request.mutate().header("branch", branch).build()).build();
        return branchList.get(0);
    }
}
