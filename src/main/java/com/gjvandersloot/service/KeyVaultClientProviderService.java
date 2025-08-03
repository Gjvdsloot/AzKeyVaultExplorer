package com.gjvandersloot.service;

import com.azure.core.credential.TokenCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.CertificateClientBuilder;
import com.azure.security.keyvault.keys.KeyClient;
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
public class KeyVaultClientProviderService {
    private final Map<VaultKey, SecretClient> secretClients = new HashMap<>();
    private final Map<VaultKey, CertificateClient> certClients = new HashMap<>();
    private final Map<VaultKey, KeyClient> keyClients = new HashMap<>();

    private final Map<VaultKey, TokenCredential> tokenCredentials = new HashMap<>();


    @Autowired private PublicClientApplication pca;


    public SecretClient getOrCreateSecretClient(Vault vault) throws Exception {
        var key = vault.getVaultKey();

        var secretClient = secretClients.getOrDefault(key, null);
        TokenCredential tokenCredential;

        if (secretClient != null)
            return secretClient;

        tokenCredential = getCredential(vault);

        secretClient = new SecretClientBuilder()
                .vaultUrl(vault.getVaultUri())
                .credential(tokenCredential)
                .buildClient();

        secretClients.put(key, secretClient);

        return secretClient;
    }

    public CertificateClient getOrCreateCertificateClient(Vault vault) throws Exception {
        var key = vault.getVaultKey();

        var certClient = certClients.getOrDefault(key, null);
        TokenCredential tokenCredential;

        if (certClient != null)
            return certClient;

        tokenCredential = getCredential(vault);

        certClient = new CertificateClientBuilder()
                .vaultUrl(vault.getVaultUri())
                .credential(tokenCredential)
                .buildClient();

        certClients.put(key, certClient);

        return certClient;
    }

    private TokenCredential getCredential(Vault vault) throws Exception {
        TokenCredential tokenCredential = tokenCredentials.getOrDefault(vault.getVaultKey(), null);

        if (tokenCredential != null)
            return tokenCredential;

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

        tokenCredentials.put(vault.getVaultKey(), tokenCredential);

        return tokenCredential;
    }
}
