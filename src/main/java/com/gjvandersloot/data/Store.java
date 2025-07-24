package com.gjvandersloot.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.sun.javafx.collections.ObservableMapWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;


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
}
