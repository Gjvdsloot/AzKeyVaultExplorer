package com.gjvandersloot.controller;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.gjvandersloot.AppDataService;
import com.gjvandersloot.data.Account;
import com.gjvandersloot.data.Store;
import com.gjvandersloot.service.AccountService;
import com.gjvandersloot.service.MainStageProvider;
import com.gjvandersloot.service.SecretClientService;
import com.gjvandersloot.ui.SecretItem;
import com.gjvandersloot.ui.SubscriptionItem;
import com.gjvandersloot.ui.VaultItem;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.Clipboard;
import javafx.scene.input.ClipboardContent;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;
import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Component
public class MainController {

    @FXML
    public TreeView<Object> treeView;

    @FXML
    public TableView<SecretItem> secretsTable;

    @FXML
    public TableColumn<SecretItem, String> secretsColumn;
    @FXML
    public TableColumn<SecretItem, String> secretValueColumn;

    @FXML
    public Button show;

    @Autowired
    AccountService accountService;

    @Autowired
    Store store;

    @Autowired
    SecretClientService secretClientService;

    @Autowired
    MainStageProvider mainStageProvider;

    @Autowired
    AppDataService appDataService;
    private TreeItem<Object> root;

    @FXML
    public void initialize() {
        this.root = new TreeItem<>();
        treeView.setRoot(root);
        treeView.setShowRoot(false);
        loadTree();

        secretValueColumn.setCellValueFactory(cell -> cell.getValue().displayProperty());

        secretsTable.getSelectionModel().selectedItemProperty()
                        .addListener((obs, o, n) -> {
                            var secret = secretsTable.getSelectionModel().getSelectedItem();

                            if (secret == null) {
                                show.setDisable(true);
                                return;
                            }

                            show.setDisable(false);

                            if (secret.valueProperty().get() == null) {
                                show.setText("Show");
                                return;
                            }

                            show.setText(secret.isHidden() ? "Show" : "Hide");
                        });
    }

    public void addSubscription() throws IOException {
        var loader = new FXMLLoader(getClass().getResource("/CancelDialog.fxml"));
        Parent root = loader.load();

        CancelDialogController cancelController = loader.getController();
        var dialog = new Stage(StageStyle.UNDECORATED);
        dialog.initOwner(mainStageProvider.getPrimaryStage());               // your main window
        dialog.initModality(Modality.APPLICATION_MODAL);
        dialog.setScene(new Scene(root));
        cancelController.setDialogStage(dialog);

        var future = CompletableFuture.runAsync(() -> {
                    Account account;
                    try {
                        account = accountService.addAccount();
                        store.getAccounts().put(account.getUsername(), account);
                        appDataService.saveStore();
                    } catch (Exception e) {
                        showError(e.getMessage());
                    }
                })
                .thenAccept((v) -> Platform.runLater(this::loadTree))
                .whenComplete((r, e) -> Platform.runLater(dialog::close));

        cancelController.setOnCancel(() -> {
            future.cancel(true);
            Platform.runLater(dialog::close);
        });

        dialog.showAndWait();
    }

    public void loadTree() {
        var root = this.root;

        var accounts = store.getAccounts();

        for (var account : accounts.values()) {
            for (var tenant : account.getTenants().values()) {
                for (var subscription : tenant.getSubscriptions().values()) {
                    var subscriptionItem = new SubscriptionItem();
                    subscriptionItem.setId(subscription.getId());
                    subscriptionItem.setName(subscription.getName());
                    subscriptionItem.setAccountName(account.getUsername());

                    if (root.getChildren().stream().anyMatch(t -> {
                        var obj = (SubscriptionItem) t.getValue();
                        return subscription.getId().equals(obj.getId());
                    })) continue;

                    var treeItem = new TreeItem<>();
                    treeItem.setValue(subscriptionItem);

                    var loadingItem = new TreeItem<>();
                    loadingItem.setValue("Loading");

                    treeItem.getChildren().add(loadingItem);
                    treeItem.setExpanded(false);

                    root.getChildren().add(treeItem);

                    treeItem.expandedProperty().addListener((obs, o, n) -> {
                        if (!n) return;

                        @SuppressWarnings("unchecked")
                        var obj = (TreeItem<Object>) ((ReadOnlyBooleanProperty)obs).getBean();

                        if (obj.getChildren().stream().findFirst().get().getValue() instanceof String) {
                            loadVaults(obj);
                        }
                    });
                }
            }
        }
    }

    private void loadVaults(TreeItem<Object> obj) {
        var subscriptionItem = (SubscriptionItem) obj.getValue();

        CompletableFuture.supplyAsync(() -> {
                    try {
                        return accountService.addKeyVaults(subscriptionItem.getId(), subscriptionItem.getAccountName());
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
        }).thenAccept(vaults -> Platform.runLater(() -> {
            obj.getChildren().clear();

            for (var vault : vaults) {
                var vaultItem = new VaultItem();
                vaultItem.setVaultUri(vault.getVaultUri());
                vaultItem.setName(vault.getName());
                vaultItem.setAccountName(subscriptionItem.getAccountName());

                var treeItem = new TreeItem<>();
                treeItem.setValue(vaultItem);

                obj.getChildren().add(treeItem);
            }
        })).exceptionally(e -> {
            showError(e.getMessage());
            return null;
        });
    }

    private CompletableFuture<List<SecretProperties>> listPropertySecretsFuture = null;
    public void treeViewClicked() {
        TreeItem<Object> clickedItem = treeView.getSelectionModel().getSelectedItem();
        if (clickedItem == null) return;

        var obj = clickedItem.getValue();

        if (!(obj instanceof VaultItem vaultItem)) return;

        var url = vaultItem.getVaultUri();
        var accountName = vaultItem.getAccountName();

        if (listPropertySecretsFuture != null && !listPropertySecretsFuture.isDone() && !listPropertySecretsFuture.isCancelled())
            listPropertySecretsFuture.cancel(true);

        CompletableFuture<List<SecretProperties>> fetchFuture = CompletableFuture.supplyAsync(() -> {
            var secretClient = secretClientService.getOrCreateClient(url, accountName);

            return secretClient.listPropertiesOfSecrets()
                    .stream()
                    .toList();
        });

        listPropertySecretsFuture = fetchFuture;

        fetchFuture.thenAccept(secretProperties -> Platform.runLater(() -> {
            if (fetchFuture.isCancelled())
                return;

            var secretItems = secretProperties.stream().map(s -> {
                var secretItem = new SecretItem();
                secretItem.setSecretName(s.getName());
                secretItem.setAccountName(accountName);
                secretItem.setVaultUri(vaultItem.getVaultUri());
                return secretItem;
            }).toList();

            ObservableList<SecretItem> rows =
                    FXCollections.observableArrayList(secretItems);
            secretsTable.setItems(rows);
        }));
    }

    public void showSecret() {
        var secret = secretsTable.getSelectionModel().getSelectedItem();
        if (secret == null)
            return;

        if (secret.getValue() == null)
            CompletableFuture
                    .runAsync(() -> lazyLoadSecret(secret))
                    .thenAccept((v) -> Platform.runLater(() -> {
                        secret.hiddenProperty().setValue(!secret.hiddenProperty().getValue());
                        show.setText(secret.isHidden() ? "Show" : "Hide");
                    }));
        else {
            secret.hiddenProperty().setValue(!secret.hiddenProperty().getValue());
            show.setText(secret.isHidden() ? "Show" : "Hide");
        }
    }

    public void copySecret() {
        var secret = secretsTable.getSelectionModel().getSelectedItem();
        if (secret == null)
            return;

        if (secret.getValue() == null)
            CompletableFuture.runAsync(() -> lazyLoadSecret(secret))
                    .thenAccept(v -> Platform.runLater(() -> copyToClipBoard(secret.getValue())))
                    .exceptionally(e -> {
                        showError(e.getMessage());
                        return null;
                    });
        else
            copyToClipBoard(secret.getValue());
    }

    public static void copyToClipBoard(String value) {
        Clipboard clipboard = Clipboard.getSystemClipboard();

        ClipboardContent content = new ClipboardContent();
        content.putString(value);

        clipboard.setContent(content);
    }

    private void lazyLoadSecret(SecretItem secret) {
        SecretClient client;
        client = secretClientService.getOrCreateClient(secret.getVaultUri(), secret.getAccountName());
        var val = client.getSecret(secret.getSecretName());
        Platform.runLater(() -> {
            secret.valueProperty().setValue((val.getValue()));
            secret.hiddenProperty().set(true);
        });
    }

    private void showError(String e) {
        Platform.runLater(() -> {
            var loader = new FXMLLoader(getClass().getResource("/ErrorDialog.fxml"));

            Parent root;
            try {
                root = loader.load();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            ErrorDialogController errorCtr = loader.getController();

            var dialog = new Stage(StageStyle.DECORATED);
            dialog.initOwner(mainStageProvider.getPrimaryStage());
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));

            errorCtr.setDialogStage(dialog);
            errorCtr.setMessage(e);
            dialog.showAndWait();
        });
    }

    public void addSecret(ActionEvent actionEvent) {
        showError("OhOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOOO\nNo\nOh\nNo\nNo\nOh\nNo\nNo\nOh\nNo\nNo\nOh\nNo\nNo\nOh\nNo\nNo\nOh\nNo");
    }
}
