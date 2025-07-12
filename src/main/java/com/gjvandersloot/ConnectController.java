package com.gjvandersloot;

import com.azure.identity.DeviceCodeCredentialBuilder;
import com.azure.identity.DeviceCodeInfo;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.azure.security.keyvault.secrets.models.KeyVaultSecret;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import io.netty.util.concurrent.CompleteFuture;
import javafx.application.HostServices;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.VBox;
import javafx.stage.Modality;
import javafx.stage.Stage;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.net.URI;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

@Component
public class ConnectController {
    private Manager manager;
    private final HostedServiceProvider provider;

    public ConnectController(HostedServiceProvider provider) {
        this.provider = provider;
        manager = new Manager();
    }

    @FXML
    private Stage dialogStage;

    @FXML
    private Hyperlink link;

    @FXML
    private TextField msgCode;

    @FXML
    private Label message;

    @FXML
    private Button cancelBtn;

    private Stage dlgStage;

    public void connect(MouseEvent mouseEvent) {
        manager = new Manager();
    }

    public void addSubscription(MouseEvent mouseEvent) throws IOException {
//        FXMLLoader loader = new FXMLLoader(getClass().getResource("/CustomDialog.fxml"));
//        Parent root = loader.load();
//
//        Stage dialogStage = new Stage();
//        dialogStage.initModality(Modality.APPLICATION_MODAL); // block input to other windows
//        dialogStage.setTitle("My Custom Dialog");
//        dialogStage.setScene(new Scene(root));
//        dialogStage.showAndWait();

        var dialog = new TextInputDialog("https://gjvandersloot.vault.azure.net/");
        dialog.setTitle("Enter key vault URI");
        dialog.setHeaderText("Please enter the key vault URI");

        Optional<String> result = dialog.showAndWait();

        if (result.isEmpty()) return;

        var entered = result.get();

        registerKeyVault(entered);
    }

    private void registerKeyVault(String uri) {
        var credentials = new DeviceCodeCredentialBuilder()
                .challengeConsumer(info ->
                    Platform.runLater(() -> {
                        try {
                            showDeviceCodeWindow(info);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                    }))
                .build();

        var client = new SecretClientBuilder()
                .vaultUrl(uri)
                .credential(credentials)
                .buildClient();

        CompletableFuture.runAsync(() -> {
            for (SecretProperties secretProps : client.listPropertiesOfSecrets()) {
                KeyVaultSecret secret = client.getSecret(secretProps.getName());
                var version = secretProps.getVersion();
                System.out.println(secret.getName() + " (" + version +  ") = " + secret.getValue());
            }

            Platform.runLater(() -> {
                if (dlgStage != null)
                    dlgStage.close();
            });
        });
    }

    void showDeviceCodeWindow(DeviceCodeInfo info) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/CustomDialog.fxml"));
        loader.setController(this);
        Parent root = loader.load();

        msgCode.setText(info.getUserCode());
        link.setText(info.getVerificationUrl());
        link.setOnAction(e -> provider.getHostServices().showDocument(link.getText()));
        message.setText(info.getMessage());

        dlgStage = new Stage();
        dlgStage.initModality(Modality.APPLICATION_MODAL); // block input to other windows
        dlgStage.setTitle("Authentication prompt");
        dlgStage.setScene(new Scene(root));

        cancelBtn.setOnAction(e -> dlgStage.close());
        dlgStage.showAndWait();
    }
}
