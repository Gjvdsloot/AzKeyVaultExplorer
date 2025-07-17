package com.gjvandersloot.ui;

import lombok.Getter;
import lombok.Setter;

import java.util.ArrayList;

@Getter
@Setter
public class SubscriptionItem {
    private String id;
    private String accountName;
    private String name;

    private ArrayList<VaultItem> vaults;

    @Override
    public String toString() {
        return name;
    }
}
