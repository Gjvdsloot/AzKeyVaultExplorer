package com.gjvandersloot.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import lombok.Getter;
import lombok.Setter;

public class AttachedVault implements ILoadable {
    public AttachedVault(String vaultUri, String clientId, String tenantId, AuthType authType) {
        this.vaultUri = vaultUri;
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.authType = authType;
    }

    public AttachedVault() {}

    @Getter @Setter AuthType authType;
    @Getter @Setter String vaultUri;
    @Getter @Setter String clientId;
    @Getter @Setter String tenantId;
    @Getter @Setter String secret;

    @Getter @Setter String certificatePath;
    @Getter @Setter String certificatePassword;

    @JsonIgnore
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

    @JsonIgnore
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
