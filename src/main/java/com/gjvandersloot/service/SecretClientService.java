package com.gjvandersloot.service;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.gjvandersloot.data.AttachedVault;
import com.gjvandersloot.data.AuthType;
import com.gjvandersloot.service.token.MsalInteractiveCredential;
import com.microsoft.aad.msal4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.Map;

@Service
public class SecretClientService {
    private final Map<String, SecretClient> clients = new HashMap<>();

    @Autowired
    private PublicClientApplication pca;


    public SecretClient getClient(String vaultUri) {
        return clients.get(vaultUri);
    }

    public SecretClient getOrCreateClient(String vaultUri, String accountName) {
        var secretClient = clients.getOrDefault(vaultUri, null);
        if (secretClient != null) return secretClient;

        secretClient = new SecretClientBuilder()
                .vaultUrl(vaultUri)
                .credential(new MsalInteractiveCredential(pca, accountName))
                .buildClient();

        clients.put(vaultUri, secretClient);

        return secretClient;
    }

    public SecretClient getOrCreateClient(AttachedVault vault) throws Exception {
        var secretClient = clients.getOrDefault(vault.getVaultUri(), null);
        TokenCredential tokenCredential;

        if (secretClient != null)
            return secretClient;

        tokenCredential = switch (vault.getAuthType()) {
            case SECRET -> new ClientSecretCredentialBuilder()
                    .tenantId(vault.getTenantId())
                    .clientId(vault.getClientId())
                    .clientSecret(vault.getSecret())
                    .build();
            case CERTIFICATE -> new ClientCertificateCredentialBuilder()
                    .tenantId(vault.getTenantId())
                    .clientId(vault.getClientId())
                    .pfxCertificate(vault.getCertificatePath())
                    .clientCertificatePassword(vault.getCertificatePassword())
                    .build();
            case null -> throw new Exception("Not implemented");
        };

        secretClient = new SecretClientBuilder()
                .vaultUrl(vault.getVaultUri())
                .credential(tokenCredential)
                .buildClient();

        clients.put(vault.getVaultUri(), secretClient);

        return secretClient;
    }
}
