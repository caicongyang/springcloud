package com.caicongyang.cloud.geteway.conf;

import com.google.common.base.Optional;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GitBranchRule extends ZoneAvoidanceRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitBranchRule.class);




    @Override
    public Server choose(Object key) {
        ILoadBalancer lb = this.getLoadBalancer();
        List<Server> allServers = filterServers(lb.getAllServers());
        Optional<Server> server = this.getPredicate().chooseRoundRobinAfterFiltering(allServers, key);
        return server.isPresent() ? (Server) server.get() : null;
    }


    private List<Server> filterServers(List<Server> serverList) {
        // todo 从DiscoveryClient拿到所有的服务实例列表，供开发人员来绑定自己的ip 与分支，并将branch 传递下去
        String branch = "master";
        if (StringUtils.isNotBlank(branch)) {
            List<Server> list = new ArrayList<>();
            for (Server server : serverList) {
                if (((DiscoveryEnabledServer) server).getInstanceInfo().getMetadata().get("branch").equalsIgnoreCase(branch)) {
                    if (LOGGER.isDebugEnabled()) {
                        LOGGER.debug("分支：" + branch + "转发成功");
                    }
                    list.add(server);
                }
            }
            return list;
        } else {
            // change masteer
            return serverList;
        }
    }


}
