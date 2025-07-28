package com.gjvandersloot.data;

import lombok.Getter;
import lombok.Setter;

public class AttachedVault extends Vault {
    public AttachedVault(String vaultUrl, String clientId, String tenantId, AuthType authType) {
        this.vaultUrl = vaultUrl;
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.authType = authType;
    }

    public AttachedVault() {}

    @Getter @Setter AuthType authType;
    @Getter @Setter String vaultUrl;
    @Getter @Setter String clientId;
    @Getter @Setter String tenantId;
    @Getter @Setter String secret;

    public String getName() {
        return vaultUrl.replaceFirst(
                "^(?:.*://)?(.*?)\\.vault.*$",
                "$1"
        );
    }

    @Override
    public String toString() {
        return getName();
    }

    private boolean loadFailed = false;
    @Override
    public boolean getLoadFailed() {
        return loadFailed;
    }

    @Override
    public void setLoadFailed(boolean loadFailed) {
        this.loadFailed = loadFailed;
    }
}
