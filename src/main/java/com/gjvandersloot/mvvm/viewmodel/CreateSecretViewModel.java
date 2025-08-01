package com.gjvandersloot.mvvm.viewmodel;

import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.gjvandersloot.data.Vault;
import com.gjvandersloot.service.SecretClientService;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class CreateSecretViewModel {
    @Autowired SecretClientService secretClientService;

    @Setter
    Vault vault;

    public KeyVaultSecret createSecret(String secret, String secretValue) throws Exception {
        var client = secretClientService.getOrCreateClient(vault);
        return client.setSecret(secret, secretValue);
    }
}
