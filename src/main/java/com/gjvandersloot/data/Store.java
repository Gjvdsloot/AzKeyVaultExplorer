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
    private ObservableMap<String, AttachedVault> attachedVaults = FXCollections.observableHashMap();
    @JsonProperty("attachedVaults")
    public Map<String, AttachedVault> getAttachedVaultsMap() {
        return new HashMap<>(attachedVaults);
    }

    @JsonProperty("attachedVaults")
    public void setAttachedVaultsMap(Map<String, AttachedVault> map) {
        this.attachedVaults = FXCollections.observableMap(map);
    }
}
