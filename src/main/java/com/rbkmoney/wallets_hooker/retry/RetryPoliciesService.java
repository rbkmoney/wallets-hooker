package com.rbkmoney.wallets_hooker.retry;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class RetryPoliciesService {

    @Autowired
    private List<RetryPolicy> retryPolicies;

    public RetryPolicy getRetryPolicyByType(RetryPolicyType type) {
        return retryPolicies.stream()
                .filter(rp -> rp.getType().equals(type))
                .findFirst()
                .orElseThrow(() -> new UnsupportedOperationException("Retry policy for retryPolicyType: " + type.toString() + " not found"));
    }
}
