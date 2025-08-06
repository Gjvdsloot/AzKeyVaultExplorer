package com.gjvandersloot.service;

import com.azure.core.credential.TokenRequestContext;
import com.azure.identity.ClientCertificateCredential;
import com.azure.identity.ClientCertificateCredentialBuilder;
import com.azure.identity.ClientSecretCredential;
import com.azure.identity.ClientSecretCredentialBuilder;
import com.gjvandersloot.AppDataService;
import com.gjvandersloot.data.AuthType;
import com.gjvandersloot.data.Vault;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.nio.file.StandardCopyOption;
import java.util.UUID;

@Service
public class VaultService {
    @Autowired AppDataService appDataService;

    public Vault createVaultWithSecret(String vaultUri, String clientId, String tenantId, String secret) throws Exception {
            var vault = new Vault(vaultUri, clientId, tenantId, AuthType.SECRET);
            vault.getCredentials().setSecret(secret);

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

    public Vault createVaultWithCertificate(String vaultUri, String clientId, String tenantId, String sourceCertificatePath, String certificatePassword) throws Exception {
        var vault = new Vault(vaultUri, clientId, tenantId, AuthType.CERTIFICATE);

        var isPkcs12 = sourceCertificatePath.endsWith(".pfx") || sourceCertificatePath.endsWith(".p12");

        var builder = new ClientCertificateCredentialBuilder()
                .tenantId(tenantId)
                .clientId(clientId);
        if (isPkcs12) {
            builder = builder.pfxCertificate(sourceCertificatePath);

            if (certificatePassword != null && !certificatePassword.isBlank()) {
                builder = builder.clientCertificatePassword(certificatePassword);
            }
        } else {
            builder = builder.pemCertificate(sourceCertificatePath);
        }

        ClientCertificateCredential credential = builder.build();

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

        vault.getCredentials().setCertificatePath(targetPath.toAbsolutePath().toString());
        vault.getCredentials().setCertificatePassword(certificatePassword);

        return vault;
    }
}
