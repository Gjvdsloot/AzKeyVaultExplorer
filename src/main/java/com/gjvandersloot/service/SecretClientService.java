package com.gjvandersloot.service;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.gjvandersloot.data.Vault;
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

    public SecretClient getOrCreateClient(Vault vault) throws Exception {
        var secretClient = clients.getOrDefault(vault.getVaultUri(), null);
        TokenCredential tokenCredential;

        if (secretClient != null)
            return secretClient;

        tokenCredential = switch (vault.getCredentials().getAuthType()) {
            case SECRET -> new ClientSecretCredentialBuilder()
                    .tenantId(vault.getCredentials().getTenantId())
                    .clientId(vault.getCredentials().getClientId())
                    .clientSecret(vault.getCredentials().getSecret())
                    .build();
            case CERTIFICATE -> new ClientCertificateCredentialBuilder()
                    .tenantId(vault.getCredentials().getTenantId())
                    .clientId(vault.getCredentials().getClientId())
                    .pfxCertificate(vault.getCredentials().getCertificatePath())
                    .clientCertificatePassword(vault.getCredentials().getCertificatePassword())
                    .build();
            case INTERACTIVE -> new MsalInteractiveCredential(pca, vault.getCredentials().getAccountName());
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
