package com.gjvandersloot.mvvm.viewmodel;

import com.gjvandersloot.data.AuthType;
import com.gjvandersloot.data.Store;
import com.gjvandersloot.service.AttachedVaultService;
import javafx.beans.property.*;
import lombok.Setter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class WizardViewModel {
    @Autowired private AttachedVaultService attachedVaultService;

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

    @Setter
    private String certificatePath;

    public void createAttachedVault() {
        var mode = selectedAuthMethod.get();

        if (store.getAttachedVaults().getOrDefault(vaultUri.get(), null) != null) {
            return;
        }

        if (mode.equals("Secret")) {
            try {
                var vault = attachedVaultService.createVaultWithSecret(vaultUri.get(), clientId.get(), tenantId.get(), secret.get());
                success.set(true);
            } catch (Exception e) {
                error.set(e.getMessage());
            }
        } else {
            // Swallow
        }
    }


}
