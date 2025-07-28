package com.gjvandersloot.data;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.collections.FXCollections;
import lombok.Getter;
import lombok.Setter;

public class Tenant {
    @Getter @Setter
    private String id;

    @Getter @Setter
    private String tenantName;

    @JsonIgnore
    @Getter @Setter
    private Map<String, Subscription> subscriptions = new HashMap<>();

    @JsonProperty("subscriptions")
    public HashMap<String, Subscription> getSubscriptionsMap() {
        return new HashMap<>(subscriptions);
    }
    @JsonProperty("subscriptions")
    public void setSubscriptionsMap(Map<String,Subscription> map) {
        // wrap it back into your observable map
        this.subscriptions = FXCollections.observableMap(map);
    }
}
