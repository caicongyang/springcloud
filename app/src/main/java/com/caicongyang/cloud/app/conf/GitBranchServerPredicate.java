package com.caicongyang.cloud.app.conf;

import com.netflix.loadbalancer.AbstractServerPredicate;
import com.netflix.loadbalancer.PredicateKey;
import com.netflix.niws.loadbalancer.DiscoveryEnabledServer;
import org.apache.commons.lang3.StringUtils;
import org.checkerframework.checker.nullness.qual.Nullable;

import java.util.Map;

public class GitBranchServerPredicate extends AbstractServerPredicate {


    @Override
    public boolean apply(@Nullable PredicateKey predicateKey) {
        DiscoveryEnabledServer server = (DiscoveryEnabledServer) predicateKey.getServer();
        Map<String, String> metaDataMap = server.getInstanceInfo().getMetadata();
        String branch = RequestContextHolder.get("branch");
        if (StringUtils.isNotBlank(branch)) {
            return metaDataMap.get("branch").equals(branch);
        }
        return Boolean.TRUE;
    }
}
