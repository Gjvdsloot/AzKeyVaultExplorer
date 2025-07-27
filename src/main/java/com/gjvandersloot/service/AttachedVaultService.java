package com.gjvandersloot.service;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.gjvandersloot.data.AttachedVault;
import com.gjvandersloot.data.AuthType;
import com.gjvandersloot.data.Vault;
import org.springframework.stereotype.Service;

import java.util.concurrent.CompletableFuture;

@Service
public class AttachedVaultService {
    public Vault createVaultWithSecret(String vaultUri, String clientId, String tenantId, String secret) throws Exception {
            var vault = new AttachedVault(vaultUri, clientId, tenantId, AuthType.SECRET);
            vault.setSecret(secret);

            ClientSecretCredential credential = new ClientSecretCredentialBuilder()
                    .tenantId(tenantId)
                    .clientId(clientId)
                    .clientSecret(secret)
                    .build();

            TokenRequestContext request = new TokenRequestContext()
                    .addScopes("https://vault.azure.net/.default");

            try {
                AccessToken token = credential.getToken(request).block();
            } catch (RuntimeException ex) {
                throw new Exception(ex);
            }

            return vault;
    }
}
