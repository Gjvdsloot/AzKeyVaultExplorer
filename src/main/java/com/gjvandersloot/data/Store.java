package com.gjvandersloot.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;


@Service
public class Store {

    @Getter
    @Setter
    @JsonIgnore
    private ObservableMap<String, Account> accounts = FXCollections.observableHashMap();
    @JsonProperty("accounts")
    public Map<String,Account> getAccountsMap() {
        return new HashMap<>(accounts);
    }
    @JsonProperty("accounts")
    public void setAccountsMap(Map<String,Account> map) {
        this.accounts = FXCollections.observableMap(map);
    }

    @Getter @Setter
    @JsonIgnore
    private ObservableMap<String, Vault> attachedVaults = FXCollections.observableHashMap();
    @JsonProperty("attachedVaults")
    public Map<String, Vault> getAttachedVaultsMap() {
        return new HashMap<>(attachedVaults);
    }

    @JsonProperty("attachedVaults")
    public void setVaultsMap(Map<String, Vault> map) {
        this.attachedVaults = FXCollections.observableMap(map);
    }
}
