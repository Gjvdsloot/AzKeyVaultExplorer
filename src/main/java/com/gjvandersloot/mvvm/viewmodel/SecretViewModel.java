package com.gjvandersloot.mvvm.viewmodel;

import com.azure.security.keyvault.secrets.SecretClient;
import com.gjvandersloot.data.Secret;
import com.gjvandersloot.data.Vault;
import com.gjvandersloot.service.SecretClientService;
import javafx.application.Platform;
import javafx.beans.property.Property;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import lombok.Getter;

import java.util.concurrent.CompletableFuture;

public class SecretViewModel {

    @Getter
    private final ObservableList<Secret> secrets = FXCollections.observableArrayList();
    private final Vault vault;
    private final SecretClient secretClient;

    public SecretViewModel(Vault vault, SecretClientService secretClientService) {
        this.vault = vault;
        try {
            secretClient = secretClientService.getOrCreateClient(vault);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }
    }

    public void refresh() {
        CompletableFuture.supplyAsync(() -> secretClient.listPropertiesOfSecrets()
                .stream()
                .toList()).thenAccept(secretProperties -> Platform.runLater(() -> {
            var secretItems = secretProperties.stream().map(s -> {
                var secretItem = new Secret();
                secretItem.setSecretName(s.getName());
                return secretItem;
            }).toList();
                secrets.clear();
                secrets.addAll(secretItems);
        })).exceptionally((e) -> {
            error.set(e.getMessage());
            secrets.clear();
            return null;
        });
    }

    public void loadSecret(Secret secret) {
        secret.valueProperty().set(secretClient.getSecret(secret.getSecretName()).getValue());
    }

    private final StringProperty error = new SimpleStringProperty();
    public Property<String> errorProperty() {
        return error;
    }
}
