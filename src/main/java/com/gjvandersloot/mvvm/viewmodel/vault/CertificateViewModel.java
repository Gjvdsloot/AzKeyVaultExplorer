package com.gjvandersloot.mvvm.viewmodel.vault;

import com.azure.security.keyvault.certificates.CertificateClient;
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

import java.util.List;

@Component
@Scope("prototype")
public class CertificateViewModel {

    @Getter
    private final ObservableList<Certificate> certificates = FXCollections.observableArrayList();

    @Autowired KeyVaultClientProviderService keyVaultClientProviderService;

    private CertificateClient certificateClient;

    public List<Certificate> loadCertificates() {
        return certificateClient.listPropertiesOfCertificates().stream().map(c -> {
            var certificateItem = new Certificate();
            certificateItem.setName(c.getName());
            certificateItem.setThumbPrint(c.getX509ThumbprintAsString());
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
