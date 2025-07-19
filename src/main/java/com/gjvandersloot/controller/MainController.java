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
import javafx.beans.binding.Bindings;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

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

        secretsTable.getSelectionModel().selectedItemProperty()
                        .addListener((obs, o, n) -> {
                            var secret = secretsTable.getSelectionModel().getSelectedItem();

                            if (secret == null)
                                return;

                            SecretClient client = null;
                            try {
                                client = secretClientService.getOrCreateClient(secret.getVaultUri(), secret.getAccountName());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }

                            var secretValue = client.getSecret(secret.getSecretName()).getValue();

                            secret.getIsVisible().setValue(false);
                            secret.getSecretValue().setValue(secretValue);
                        });

        secretValueColumn.setCellValueFactory(cell -> {
            SecretItem item = cell.getValue();

            return Bindings.createStringBinding(
                    () -> {
                        var secretValue = item.getSecretValue().getValue();
                        if (secretValue == null)
                            return null;

                        var isVisible = item.getIsVisible().getValue();

                        if (isVisible)
                            return item.getSecretValue().getValue();

                        return "*".repeat(secretValue.length());
                    }, item.getIsVisible(), item.getSecretValue()
            );
        });

//        secretValueColumn.setCellFactory(col -> {
//            return new TableCell<>() {
//                @Override
//                public void updateItem(Node item, boolean empty) {
//    //                super.updateItem(item, empty);
//    //                if (empty || item == null) {
//    //                    setGraphic(null);
//    //                } else {
//    //                    setGraphic(deleteBtn);
//    //                }
//                    super.updateItem(item, empty);
//                    setGraphic(empty ? null : deleteBtn);
//                }
//            };
//        });

//        secretValueColumn.setCellFactory(col -> {
//            return new TableCell<>() {
//                private final Button deleteBtn = new Button("Delete");
//
//                {
//                    // Button event handler: get the current row’s item:
//                    deleteBtn.setOnAction(e -> {
//    //                    MyModel rowData = getTableView().getItems().get(getIndex());
//                        // ... perform delete or other action ...
//                    });
//                }
//
//                @Override
//                public void updateItem(Node item, boolean empty) {
//    //                super.updateItem(item, empty);
//    //                if (empty || item == null) {
//    //                    setGraphic(null);
//    //                } else {
//    //                    setGraphic(deleteBtn);
//    //                }
//                    super.updateItem(item, empty);
//                    setGraphic(empty ? null : deleteBtn);
//                }
//            };
//        });
    }

    public void addSubscription() throws Exception {
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
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }

                    store.getAccounts().put(account.getUsername(), account);
                    appDataService.saveStore();
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

                        if (obj.getChildren().getFirst().getValue() instanceof String) {
                            try {
                                loadVaults(obj);
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            }
        }
    }

    private void loadVaults(TreeItem<Object> obj) throws Exception {
        var subscriptionItem = (SubscriptionItem) obj.getValue();

        CompletableFuture.supplyAsync(() -> {
                    try {
                        return accountService.addKeyVaults(subscriptionItem.getId(), subscriptionItem.getAccountName());
                    } catch (Exception e) {
                        throw new CompletionException(e);
                    }
        }).thenAccept(vaults -> {
            Platform.runLater(() -> {
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
            });
        });
    }

    public void treeViewClicked() {
        TreeItem<Object> clickedItem;
        try {
            clickedItem = treeView.getSelectionModel().getSelectedItem();
        } catch (Exception e) {
            return;
        }
        if (clickedItem == null) return;

        var obj = clickedItem.getValue();

        if (!(obj instanceof VaultItem vaultItem)) return;

        var url = vaultItem.getVaultUri();
        var accountName = vaultItem.getAccountName();

        SecretClient secretClient = null;
        try {
            secretClient = secretClientService.getOrCreateClient(url, accountName);
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        try {
            // 1) Suppose you already have your list of SecretProperties:
            List<SecretProperties> secretList =
                    secretClient.listPropertiesOfSecrets()
                            .stream()
                            .toList();

            var secretItems = secretList.stream().map(s -> {
                var secretItem = new SecretItem();
                secretItem.setSecretName(s.getName());
                secretItem.getSecretValue().setValue(null);
                secretItem.setAccountName(accountName);
                secretItem.setVaultUri(vaultItem.getVaultUri());
                return secretItem;
            }).toList();

            ObservableList<SecretItem> rows =
                    FXCollections.observableArrayList(secretItems);
            secretsTable.setItems(rows);
        } catch(Exception e) {
            e.printStackTrace();
        }
    }

    public void showSecret() throws Exception {
        var secret = secretsTable.getSelectionModel().getSelectedItem();

        if (secret == null)
            return;

        secret.getIsVisible().setValue(!secret.getIsVisible().getValue());
    }
}
