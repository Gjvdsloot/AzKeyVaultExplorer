package com.gjvandersloot.controller;

import com.gjvandersloot.service.ContextProvider;
import com.gjvandersloot.service.MainStageProvider;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;

import java.net.URI;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.Optional;

@Component
public class ConnectController {

    @Autowired
    MainStageProvider mainStageProvider;

    @Autowired
    ContextProvider contextProvider;

    public ConnectController() {
    }

    public void addSubscription() throws IOException {
        var entered = GetKeyVaultUrl();

        if (entered.isEmpty()) {
            new Alert(Alert.AlertType.ERROR, "Invalid URL").show();
            return;
        }

        openAuthModal(entered.get());
    }

    private void openAuthModal(String url) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/AzureAuthDialog.fxml"));
        loader.setControllerFactory(contextProvider.getApplicationContext()::getBean);
        Parent root = loader.load();

        AzureAuthController azureAuthController = loader.getController();
        azureAuthController.authenticate(url);

        // Create dialog stage
        Stage dialogStage = new Stage();
        dialogStage.setTitle("Login");
        dialogStage.initModality(Modality.WINDOW_MODAL);
        dialogStage.initOwner(mainStageProvider.getPrimaryStage());
        dialogStage.setScene(new Scene(root));
    }

    private static Optional<String> GetKeyVaultUrl() {
        var dialog = new TextInputDialog("https://gjvandersloot.vault.azure.net/");
        dialog.setTitle("Enter key vault URI");
        dialog.setHeaderText("Please enter the key vault URI");

        var entered = dialog.showAndWait();

        if (entered.isEmpty())
            return entered;

        var content = entered.get();

        try {
            new URI(content).toURL();
            return content.describeConstable();
        } catch (Exception e) {
            return Optional.empty();
        }
    }
}
