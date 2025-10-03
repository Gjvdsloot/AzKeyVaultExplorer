package com.gjvandersloot.viewmodel.vault;

import com.azure.security.keyvault.certificates.CertificateClient;
import com.azure.security.keyvault.certificates.models.KeyVaultCertificateWithPolicy;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.gjvandersloot.data.Certificate;
import com.gjvandersloot.data.Vault;
import com.gjvandersloot.service.KeyVaultClientProviderService;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.util.Base64;
import java.util.List;
import java.util.concurrent.CompletableFuture;

@Component
@Scope("prototype")
public class CertificateViewModel {

    @Getter
    private final ObservableList<Certificate> certificates = FXCollections.observableArrayList();

    @Autowired KeyVaultClientProviderService keyVaultClientProviderService;

    private CertificateClient certificateClient;
    private SecretClient secretClient;

    public List<Certificate> loadCertificates() {
        return certificateClient.listPropertiesOfCertificates().stream().map(c -> {
            var certificateItem = new Certificate();
            certificateItem.setName(c.getName());
            certificateItem.setThumbPrint(c.getX509ThumbprintAsString());
            certificateItem.setExpirationDate(c.getExpiresOn());
            certificateItem.setEnabled(c.isEnabled());
            return certificateItem;
        }).toList();
    }
//
//    public void loadCertificate(Certificate certificate) {
//        certificate.valueProperty().set(certificateClient.getCertificate(certificate.getCertificateName()).getValue());
//    }

    private final StringProperty error = new SimpleStringProperty();
    public Property<String> errorProperty() {
        return error;
    }

    public void setCertificateClient(Vault vault) {
        try {
            certificateClient = keyVaultClientProviderService.getOrCreateCertificateClient(vault);
        } catch (Exception e) {
            error.set(e.getMessage());
        }
    }

    public CompletableFuture<byte[]> downloadCertificateAsync(String certName, String filename) {
        return CompletableFuture.supplyAsync(() -> {
            byte[] data;
            if (filename.endsWith(".pfx")) {
                KeyVaultSecret secret = secretClient.getSecret(certName);
                data = Base64.getDecoder().decode(secret.getValue());
            } else if (filename.endsWith(".pem")) {
                KeyVaultSecret secret = secretClient.getSecret(certName);
                data = secret.getValue().getBytes(StandardCharsets.UTF_8);
            } else {
                KeyVaultCertificateWithPolicy cert = certificateClient.getCertificate(certName);
                data = cert.getCer();
            }
            return data;
        });
    }

    public void setSecretClient(Vault vault) {
        try {
            secretClient = keyVaultClientProviderService.getOrCreateSecretClient(vault);
        } catch (Exception e) {
            error.set(e.getMessage());
        }
    }

    public String getContentType(String certName) {
        var secret = secretClient.getSecret(certName);
        return secret.getProperties().getContentType();
    }

//    public void addCertificate(Certificate newCertificate) {
//        certificates.stream().filter(s -> newCertificate.getCertificateName().equals(s.getCertificateName()))
//                .findAny()
//                .ifPresentOrElse(s -> s.setValue(newCertificate.getValue()), () -> certificates.add(newCertificate));
//    }
//
//    public void deleteCertificate(Certificate selectedCertificate) {
//        String certificateName = selectedCertificate.getCertificateName();
//
//        var poller = certificateClient.beginDeleteCertificate(certificateName);
//        poller.waitForCompletion();
//
//        try {
//            certificateClient.purgeDeletedCertificate(certificateName);
//        } catch (Exception e) {
//            // Swallow
//        }
//
//        Platform.runLater(() -> certificates.remove(selectedCertificate));
//    }
}
