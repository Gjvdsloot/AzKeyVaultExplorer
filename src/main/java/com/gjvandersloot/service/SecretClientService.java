package com.gjvandersloot.service;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
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

    public SecretClient getOrCreateClient(String vaultUrl, String accountName) {
        var secretClient = clients.getOrDefault(vaultUrl, null);
        if (secretClient != null) return secretClient;

        secretClient = new SecretClientBuilder()
                .vaultUrl(vaultUrl)
                .credential(new MsalInteractiveCredential(pca, accountName))
                .buildClient();

                clients.put(vaultUrl, secretClient);

        return secretClient;
    }

//    public SecretClient getOrCreate
}
