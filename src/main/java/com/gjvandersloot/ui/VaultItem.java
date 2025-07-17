package com.gjvandersloot.ui;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class VaultItem {
    private String name;
    private String vaultUri;

    @Override
    public String toString() {
        return name;
    }
}
