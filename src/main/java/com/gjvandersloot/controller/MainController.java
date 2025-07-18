package com.gjvandersloot.controller;

import com.gjvandersloot.model.Subscription;
import com.gjvandersloot.service.Manager;
import com.gjvandersloot.service.ContextProvider;
import com.gjvandersloot.service.MainStageProvider;
import javafx.application.Platform;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Node;
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
import java.util.concurrent.CompletableFuture;

@Component
public class MainController {

    @FXML
    public Accordion accordion;

    @Autowired
    MainStageProvider mainStageProvider;

    @Autowired
    ContextProvider contextProvider;

    @Autowired
    Manager manager;

    @FXML
    public void initialize() {
        accordion.expandedPaneProperty().addListener((obs, oldPane, newPane) -> {
            if (newPane == null) return;

            var subscriptionId = newPane.getId();

            Node content = newPane.getContent();

            if (!(content instanceof ListView<?> rawList)) {
                return;
            }

            @SuppressWarnings("unchecked")
            ListView<Object> listView = (ListView<Object>) rawList;

            // only load once
            if (!listView.getItems().isEmpty()) return;

            // show a “Loading” placeholder
            listView.getItems().setAll("Loading");

            // do the fetch off the FX thread
            CompletableFuture
                    .supplyAsync(() -> {
                        try {
                            return manager.getKeyVaultsForSubscriptionId(subscriptionId);
                        } catch (Exception e) {
                            e.printStackTrace();
                            return null;
                        }
                    })
                    .thenAccept(vaults -> {
                        // back on the FX thread to update the UI
                        Platform.runLater(() -> {
                            listView.getItems().clear();
                            if (vaults != null) {
                                listView.getItems().addAll(vaults);
                            } else {
                                listView.getItems().add("Failed to load vaults");
                            }
                        });
                    });
        });

        manager.loadSubscriptionsFromDisk();

        for (var sub : manager.getSubscriptions()) {
            addSubscriptionToAccordion(sub);
        }
    }

    public MainController() {
    }

    public void addSubscription() throws Exception {
        var subscriptions = manager.AddAzureAccount();

        for(var subscription : subscriptions)
            addSubscriptionToAccordion(subscription);
    }

    public void addSubscriptionToAccordion(Subscription subscription) {
        var pane = new TitledPane();
        pane.setText(subscription.getName());
        pane.setId(subscription.getSubscriptionId());
        pane.setExpanded(false);

        ListView<Object> listView = new ListView<>();

        var items = FXCollections.observableArrayList();
        items.addAll(subscription.getKeyVaults());

        listView.setItems(items);
        pane.setContent(listView);

        accordion.getPanes().add(pane);
//        accordion.setExpandedPane(pane);
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
