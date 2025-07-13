package com.gjvandersloot;

import com.azure.core.exception.ResourceNotFoundException;
import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.identity.DeviceCodeInfo;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Hyperlink;
import javafx.scene.control.Label;
import javafx.scene.control.TextField;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;
import java.awt.Desktop;
import java.io.IOException;
import java.net.URI;
import java.net.URISyntaxException;
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

//        var credentials = new InteractiveBrowserCredentialBuilder()
//                .clientId("04b07795-8ddb-461a-bbee-02f9e1bf7b46")
//                .tenantId("common")
//                .additionallyAllowedTenants("*")
//                .build();

        var secretClient = new SecretClientBuilder()
                .vaultUrl(url)
                .credential(credentials)
                .buildClient();

        CompletableFuture.runAsync(() -> {
            triggerAuth(secretClient);

            for (SecretProperties sp : secretClient.listPropertiesOfSecrets()) {
                KeyVaultSecret secret = secretClient.getSecret(sp.getName());
                var version = sp.getVersion();
                System.out.println(secret.getName() + " (" + version + ") = " + secret.getValue());
            }

            Platform.runLater(() -> {
                var stage = (Stage) cancelBtn.getScene().getWindow();
                if (stage != null) stage.close();
            });
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
