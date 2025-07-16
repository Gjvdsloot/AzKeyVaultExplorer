package com.gjvandersloot.model;

import lombok.Getter;
import lombok.Setter;

@Setter
@Getter
public class KeyVault {
    private String name;
    private String vaultUri;

    @Override
    public String toString() {
        return name;
    }
}
