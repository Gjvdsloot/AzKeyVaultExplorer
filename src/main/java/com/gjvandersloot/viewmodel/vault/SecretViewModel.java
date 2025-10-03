package com.gjvandersloot.viewmodel.vault;

import com.azure.security.keyvault.secrets.SecretClient;
import com.gjvandersloot.data.Secret;
import com.gjvandersloot.data.Vault;
import com.gjvandersloot.service.KeyVaultClientProviderService;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.List;

@Component
@Scope("prototype")
@Slf4j
public class SecretViewModel {

    @Getter
    private final ObservableList<Secret> secrets = FXCollections.observableArrayList();

    @Autowired KeyVaultClientProviderService keyVaultClientProviderService;

    private SecretClient secretClient;

    public List<Secret> loadSecrets() {
        return secretClient.listPropertiesOfSecrets().stream()
                .filter(s -> s.isManaged() == null || !s.isManaged())
                .map(s -> {
                    var secretItem = new Secret();
                    secretItem.setSecretName(s.getName());
                    secretItem.setEnabled(s.isEnabled());
                    secretItem.setExpirationDate(s.getExpiresOn());
                    return secretItem;
                }).toList();
    }

    public void loadSecret(Secret secret) {
        secret.valueProperty().set(secretClient.getSecret(secret.getSecretName()).getValue());
    }

    private final StringProperty error = new SimpleStringProperty();
    public Property<String> errorProperty() {
        return error;
    }

    public void setSecretClient(Vault vault) {
        try {
            secretClient = keyVaultClientProviderService.getOrCreateSecretClient(vault);
        } catch (Exception e) {
            error.set(e.getMessage());
        }
    }

    public void addSecret(Secret newSecret) {
        secrets.stream().filter(s -> newSecret.getSecretName().equals(s.getSecretName()))
                .findAny()
                .ifPresentOrElse(s -> s.setValue(newSecret.getValue()), () -> secrets.add(newSecret));
    }

    public void deleteSecret(Secret selectedSecret) {
        String secretName = selectedSecret.getSecretName();

        var poller = secretClient.beginDeleteSecret(secretName);
        poller.waitForCompletion();

        try {
            secretClient.purgeDeletedSecret(secretName);
        } catch (Exception e) {
            if (e.getMessage().toLowerCase().contains("soft delete")) {
                log.info("Cannot purge secret when soft-delete is disabled, skipping");
            } else {
                error.set(e.getMessage());
            }
        }

        Platform.runLater(() -> secrets.remove(selectedSecret));
    }
}
