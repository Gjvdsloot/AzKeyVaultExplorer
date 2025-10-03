package com.gjvandersloot.viewmodel;

import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.gjvandersloot.data.Vault;
import com.gjvandersloot.service.KeyVaultClientProviderService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateSecretViewModel {
    @Autowired KeyVaultClientProviderService keyVaultClientProviderService;

    @Setter
    Vault vault;

    public KeyVaultSecret createSecret(String secret, String secretValue) throws Exception {
        var client = keyVaultClientProviderService.getOrCreateSecretClient(vault);
        return client.setSecret(secret, secretValue);
    }
}
