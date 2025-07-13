package com.gjvandersloot;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.identity.AzureCliCredentialBuilder;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.identity.DeviceCodeInfo;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Optional;
import java.util.UUID;
import java.util.concurrent.CompletableFuture;

@Component
public class AzureAuthController {
    @FXML public Label message;
    @FXML public Hyperlink link;
    @FXML public TextField msgCode;
    @FXML public Button cancelBtn;

    private final HostedServiceProvider hostedServiceProvider;

    public AzureAuthController(HostedServiceProvider hostedServiceProvider) {
        this.hostedServiceProvider = hostedServiceProvider;
    }

    public void authenticate(String url) {
        var credentials = new DeviceCodeCredentialBuilder()
                .challengeConsumer(info -> Platform.runLater(() -> showForm(info)))
                .build();

        var secretClient = new SecretClientBuilder()
                .vaultUrl(url)
                .credential(credentials)
                .buildClient();

        CompletableFuture.runAsync(() -> {
            try {
                triggerAuth(secretClient);

                for (SecretProperties sp : secretClient.listPropertiesOfSecrets()) {
                    KeyVaultSecret secret = secretClient.getSecret(sp.getName());
                    var version = sp.getVersion();
                    System.out.println(secret.getName() + " (" + version + ") = " + secret.getValue());
                }
            } catch(Exception e) {
                Platform.runLater(() -> {
                    Alert alert = new Alert(Alert.AlertType.ERROR, e.getMessage());
                    alert.showAndWait();
                });
            } finally {
                Platform.runLater(() -> {
                    if (cancelBtn != null && cancelBtn.getScene() != null) {
                        var stage = (Stage) cancelBtn.getScene().getWindow();
                        if (stage != null) stage.close();
                    }
                });
            }
        });
    }

    private void triggerAuth(SecretClient secretClient) {
        try {
            secretClient.getSecret(UUID.randomUUID().toString());
        } catch (ResourceNotFoundException e) {
            //
        }
    }

    private void showForm(DeviceCodeInfo info) {
        var stage = (Stage) cancelBtn.getScene().getWindow();

        msgCode.setText(info.getUserCode());
        link.setText(info.getVerificationUrl());
        link.setOnAction(e -> hostedServiceProvider.getHostServices().showDocument(link.getText()));
        message.setText(info.getMessage());

        cancelBtn.setOnAction(e -> stage.close());

        stage.showAndWait();
    }
}
