package com.gjvandersloot.data;

import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import lombok.Getter;
import lombok.Setter;


public class Account {
    @Getter @Setter
    private String username;

    @JsonIgnore
    @Getter @Setter
    private ObservableMap<String, Tenant> tenants = FXCollections.observableHashMap();

    @JsonProperty("tenants")
    public HashMap<String, Tenant> getTenantsMap() {
        return new HashMap<>(tenants);
    }
    @JsonProperty("tenants")
    public void setTenantsMap(Map<String,Tenant> map) {
        // wrap it back into your observable map
        this.tenants = FXCollections.observableMap(map);
    }
}
