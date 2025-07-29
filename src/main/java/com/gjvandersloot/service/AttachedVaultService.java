package com.gjvandersloot.service;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.gjvandersloot.AppDataService;
import com.gjvandersloot.data.AttachedVault;
import com.gjvandersloot.data.AuthType;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.io.FileInputStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.security.KeyStore;
import java.security.PrivateKey;
import java.security.cert.X509Certificate;
import java.util.UUID;

@Service
public class AttachedVaultService {
    @Autowired AppDataService appDataService;

    public AttachedVault createVaultWithSecret(String vaultUri, String clientId, String tenantId, String secret) throws Exception {
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
                credential.getToken(request).block();
            } catch (RuntimeException ex) {
                throw new Exception(ex);
            }

            return vault;
    }

    public AttachedVault createVaultWithCertificate(String vaultUri, String clientId, String tenantId, String sourceCertificatePath, String certificatePassword) throws Exception {
        var vault = new AttachedVault(vaultUri, clientId, tenantId, AuthType.CERTIFICATE);

        ClientCertificateCredential credential = new ClientCertificateCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId)
                .pfxCertificate(sourceCertificatePath)
                .clientCertificatePassword(certificatePassword)
                .build();

        TokenRequestContext request = new TokenRequestContext()
                .addScopes("https://vault.azure.net/.default");

        try {
            credential.getToken(request).block();
        } catch (RuntimeException ex) {
            throw new Exception(ex);
        }

        var guid = UUID.randomUUID().toString();
        Path certFolder = appDataService.getMainPath().resolve("certs");
        Files.createDirectories(certFolder);

        var targetPath = certFolder.resolve(guid + ".pfx");
        Files.copy(Paths.get(sourceCertificatePath), targetPath, StandardCopyOption.REPLACE_EXISTING);

        vault.setCertificatePath(targetPath.toAbsolutePath().toString());
        vault.setCertificatePassword(certificatePassword);

        return vault;
    }
}
