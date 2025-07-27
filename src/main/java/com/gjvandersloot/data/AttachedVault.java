package com.gjvandersloot.data;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AttachedVault extends Vault {
    public AttachedVault(String vaultUri, String clientId, String tenantId, AuthType authType) {
        this.vaultUri = vaultUri;
        this.clientId = clientId;
        this.tenantId = tenantId;
        this.authType = authType;
    }

    AuthType authType;
    String vaultUri;
    String clientId;
    String tenantId;
    String secret;
}
