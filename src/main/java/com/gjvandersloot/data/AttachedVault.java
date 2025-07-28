package com.gjvandersloot.data;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class AttachedVault extends Vault implements Loadable {
    public AttachedVault(String vaultUri, String clientId, String tenantId, AuthType authType) {
        this.vaultUri = vaultUri;
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.authType = authType;
    }

    public AttachedVault() {}

    AuthType authType;
    String vaultUri;
    String clientId;
    String tenantId;
    String secret;

    public String getName() {
        return vaultUri.replaceFirst(
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
