package com.gjvandersloot.model;

import java.util.ArrayList;

public class Subscription {
    private String tenantId;
    private String subscriptionId;
    private String name;
    private String state;
    private ArrayList<KeyVault> keyVaults;

    public ArrayList<KeyVault> getKeyVaults() {
        return keyVaults;
    }

    public void setKeyVaults(ArrayList<KeyVault> keyVaults) {
        this.keyVaults = keyVaults;
    }

    public String getState() {
        return state;
    }

    public void setState(String state) {
        this.state = state;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public String getSubscriptionId() {
        return subscriptionId;
    }

    public void setSubscriptionId(String subscriptionId) {
        this.subscriptionId = subscriptionId;
    }

    public String getTenantId() {
        return tenantId;
    }

    public void setTenantId(String tenantId) {
        this.tenantId = tenantId;
    }
}

