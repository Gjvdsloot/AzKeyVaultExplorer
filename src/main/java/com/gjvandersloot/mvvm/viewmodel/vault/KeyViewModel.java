package com.gjvandersloot.mvvm.viewmodel.vault;

import com.azure.security.keyvault.keys.KeyClient;
import com.azure.security.keyvault.keys.models.JsonWebKey;
import com.azure.security.keyvault.keys.models.KeyVaultKey;
import com.gjvandersloot.data.KeySecret;
import com.gjvandersloot.data.Vault;
import com.gjvandersloot.service.KeyVaultClientProviderService;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.math.BigInteger;
import java.security.KeyFactory;
import java.security.NoSuchAlgorithmException;
import java.security.PublicKey;
import java.security.spec.InvalidKeySpecException;
import java.security.spec.RSAPublicKeySpec;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Scope("prototype")
public class KeyViewModel {
    @Autowired KeyVaultClientProviderService keyVaultClientProviderService;

    private KeyClient keyClient;

    @Getter private final ObservableList<KeySecret> keys = FXCollections.observableArrayList();

    public void setKeyClient(Vault vault) throws Exception {
        keyClient = keyVaultClientProviderService.getOrCreateKeyClient(vault);
    }

    public List<KeySecret> loadKeys() {
        return keyClient.listPropertiesOfKeys().stream()
                .filter(s -> s.isManaged() == null || !s.isManaged())
                .map(s -> {
                    var keyItem = new KeySecret();
                    keyItem.setName(s.getName());
                    keyItem.setEnabled(s.isEnabled());
                    keyItem.setExpirationDate(s.getExpiresOn());
                    return keyItem;
                }).toList();
    }

    public CompletableFuture<String> downloadKeyAsync(String name) {
        return CompletableFuture.supplyAsync(() -> {
            KeyVaultKey keyVaultKey = keyClient.getKey(name);
            JsonWebKey jwk = keyVaultKey.getKey();

            if (!jwk.getKeyType().toString().startsWith("RSA")) {
                throw new IllegalStateException("Only RSA keys are supported in this example.");
            }

            BigInteger modulus = new BigInteger(1, jwk.getN());
            BigInteger exponent = new BigInteger(1, jwk.getE());

            RSAPublicKeySpec publicKeySpec = new RSAPublicKeySpec(modulus, exponent);
            KeyFactory keyFactory = null;
            PublicKey publicKey = null;
            try {
                keyFactory = KeyFactory.getInstance("RSA");
                publicKey = keyFactory.generatePublic(publicKeySpec);
            } catch (InvalidKeySpecException | NoSuchAlgorithmException e) {
                throw new RuntimeException(e);
            }

            byte[] encoded = publicKey.getEncoded();
            String pem = convertToPem(encoded);

            return pem;
        });
    }

    private static String convertToPem(byte[] encodedKey) {
        String base64 = Base64.getEncoder().encodeToString(encodedKey);
        StringBuilder pem = new StringBuilder();
        pem.append("-----BEGIN PUBLIC KEY-----\n");
        for (int i = 0; i < base64.length(); i += 64) {
            pem.append(base64, i, Math.min(i + 64, base64.length())).append("\n");
        }
        pem.append("-----END PUBLIC KEY-----\n");
        return pem.toString();
    }

}
