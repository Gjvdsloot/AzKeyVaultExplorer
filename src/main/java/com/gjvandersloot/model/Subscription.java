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

    private ArrayList<KeyVault> keyVaults = new ArrayList<>();

}

