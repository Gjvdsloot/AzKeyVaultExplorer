package com.gjvandersloot.model;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Setter
@Getter
public class Subscription {
    private String tenantId;
    private String subscriptionId;
    private String name;
    private String state;
    private String accountId;
    private ArrayList<KeyVault> keyVaults = new ArrayList<>();

    public Subscription copyWithoutVaults() {
        var s = new Subscription();
        s.setTenantId(tenantId);
        s.setSubscriptionId(subscriptionId);
        s.setName(name);
        s.setState(state);
        s.setAccountId(accountId);
        s.setKeyVaults(new ArrayList<>());
        return s;
    }
}

