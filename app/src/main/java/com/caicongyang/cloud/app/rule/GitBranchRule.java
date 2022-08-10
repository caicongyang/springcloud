package com.caicongyang.cloud.app.rule;

import com.caicongyang.cloud.app.conf.RequestContextHolder;
import com.google.common.base.Optional;
import com.netflix.loadbalancer.ILoadBalancer;
import com.netflix.loadbalancer.Server;
import com.netflix.loadbalancer.ZoneAvoidanceRule;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.List;

public class GitBranchRule extends ZoneAvoidanceRule {

    private static final Logger LOGGER = LoggerFactory.getLogger(GitBranchRule.class);

//
//    private CompositePredicate compositePredicate;
//
//
//
//    public GitBranchRule() {
//        //用于判断该服务的分区是否是一个坏的分区，需要避免使用该分区
//        ZoneAvoidancePredicate zonePredicate = new ZoneAvoidancePredicate(this, null);
//        GitBranchServerPredicate gitBranchServerPredicate = new GitBranchServerPredicate();
//        //用于判断该服务的压力是否过大
//        AvailabilityPredicate availabilityPredicate = new AvailabilityPredicate(this, null);
//        compositePredicate = CompositePredicate.withPredicates(zonePredicate, availabilityPredicate, gitBranchServerPredicate)
//                .build();
//    }
//
//    @Override
//    public AbstractServerPredicate getPredicate() {
//        return this.compositePredicate;
//    }

    @Override
    public Server choose(Object key) {
        ILoadBalancer lb = this.getLoadBalancer();
        List<Server> allServers = filterServers(lb.getAllServers());
        Optional<Server> server = this.getPredicate().chooseRoundRobinAfterFiltering(allServers, key);
        return server.isPresent() ? (Server) server.get() : null;
    }


    private List<Server> filterServers(List<Server> serverList) {
        String branch = RequestContextHolder.get("branch");
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
