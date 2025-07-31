package com.gjvandersloot.mvvm.viewmodel;

import com.gjvandersloot.data.Store;
import com.gjvandersloot.service.VaultService;
import javafx.beans.property.*;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WizardViewModel {
    @Autowired private VaultService attachedVaultService;

    @Autowired private Store store;

    private final StringProperty selectedAuthMethod = new SimpleStringProperty("Secret");
    public StringProperty selectedAuthMethodProperty() {
        return selectedAuthMethod;
    }

    private final StringProperty clientId = new SimpleStringProperty();
    public Property<String> clientIdProperty() {
        return clientId;
    }

    private final StringProperty tenantId = new SimpleStringProperty();
    public Property<String> tenantIdProperty() {
        return tenantId;
    }

    private final StringProperty vaultUri = new SimpleStringProperty();
    public Property<String> vaultUriProperty() {
        return vaultUri;
    }

    private final StringProperty secret = new SimpleStringProperty();
    public Property<String> secretProperty() {
        return secret;
    }

    private final BooleanProperty success = new SimpleBooleanProperty(false);
    public Property<Boolean> successProperty() {
        return success;
    }

    private final StringProperty error = new SimpleStringProperty();
    public Property<String> errorProperty() {
        return error;
    }

    private final StringProperty certPassword = new SimpleStringProperty();
    public Property<String> certPasswordProperty() {
        return certPassword;
    }

    @Setter
    private String certificatePath;

    public void createVault() {
        var mode = selectedAuthMethod.get();

        if (store.getAttachedVaults().getOrDefault(vaultUri.get(), null) != null) {
            return;
        }

        if (mode.equals("Secret")) {
            try {
                var vault = attachedVaultService.createVaultWithSecret(vaultUri.get(), clientId.get(), tenantId.get(), secret.get());

                store.getAttachedVaults().put(vault.getVaultUri(), vault);

                success.set(true);
            } catch (Exception e) {
                error.set(e.getMessage());
            }
        } else if(mode.equals("Certificate")) {
            try {
                var vault = attachedVaultService.createVaultWithCertificate(vaultUri.get(), clientId.get(), tenantId.get(), certificatePath, certPassword.get());

                store.getAttachedVaults().put(vault.getVaultUri(), vault);

                success.set(true);
            } catch (Exception e) {
                error.set(e.getMessage());
            }
        } else {
            throw new RuntimeException("Not supported auth type");
        }
    }



}
