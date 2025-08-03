package com.gjvandersloot.data;

import com.fasterxml.jackson.annotation.JsonIgnore;
import com.gjvandersloot.service.VaultKey;
import lombok.Getter;
import lombok.Setter;

public class Vault implements ILoadable {
    public Vault(String vaultUri, String clientId, String tenantId, AuthType authType) {
        this.vaultUri = vaultUri;
        credentials = new Credentials();
        credentials.setClientId(clientId);
        credentials.setTenantId(tenantId);
        credentials.setAuthType(authType);
    }

    public Vault(String vaultUri, String accountName) {
        this.vaultUri = vaultUri;
        credentials = new Credentials();
        credentials.setAccountName(accountName);
        credentials.setAuthType(AuthType.INTERACTIVE);
    }

    public Vault() {}

    @Getter @Setter
    private String vaultUri;

    @Getter @Setter
    Credentials credentials;

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

    private boolean loadFailed = false;
    @Override
    public boolean getLoadFailed() {
        return loadFailed;
    }

    @Override
    public void setLoadFailed(boolean loadFailed) {
        this.loadFailed = loadFailed;
    }

    @JsonIgnore
    public VaultKey getVaultKey() {
        return new VaultKey(this.vaultUri, this.credentials.getAuthType());
    }
}
