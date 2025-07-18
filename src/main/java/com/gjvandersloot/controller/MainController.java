package com.gjvandersloot.controller;

import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.models.SecretProperties;
import com.gjvandersloot.data.Account;
import com.gjvandersloot.data.Store;
import com.gjvandersloot.service.AccountService;
import com.gjvandersloot.service.MainStageProvider;
import com.gjvandersloot.service.SecretClientService;
import com.gjvandersloot.ui.SubscriptionItem;
import com.gjvandersloot.ui.VaultItem;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TableColumn;
import javafx.scene.control.TableView;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.scene.control.cell.PropertyValueFactory;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.stream.Collectors;

@Component
public class MainController {

    @FXML
    public TreeView<Object> treeView;

    @FXML
    public TableView<SecretProperties> secretsTable;

    @FXML
    public TableColumn<SecretProperties, String> secretsColumn;
    @FXML
    public TableColumn<SecretProperties, String> emailColumn;

    @Autowired
    AccountService accountService;

    @Autowired
    Store store;

    @Autowired
    SecretClientService secretClientService;

    @Autowired
    MainStageProvider mainStageProvider;

    @FXML
    public void initialize() {
        // 1) Tell the name‑column to call getName() on each SecretProperties
        secretsColumn.setCellValueFactory(
                new PropertyValueFactory<>("name")
        );

//        // 2) (Optional) value‑column — fetch the actual secret value
//        secretsColumn.setCellValueFactory(cell ->
//                new SimpleStringProperty(
//                        secretClient.getSecret(cell.getValue().getName())
//                                .getValue()
//                )
//        );
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
        var root = new TreeItem<>();
        treeView.setRoot(root);
        treeView.setShowRoot(false);

        var accounts = store.getAccounts();

        for (var account : accounts.values()) {
            for (var tenant : account.getTenants().values()) {
                for (var subscription : tenant.getSubscriptions().values()) {
                    var subscriptionItem = new SubscriptionItem();
                    subscriptionItem.setId(subscription.getId());
                    subscriptionItem.setName(subscription.getName());
                    subscriptionItem.setAccountName(account.getUsername());

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

        if (!(obj instanceof VaultItem)) return;

        var vaultItem = (VaultItem) obj;
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
                            .collect(Collectors.toList());

            ObservableList<SecretProperties> rows =
                    FXCollections.observableArrayList(secretList);
            secretsTable.setItems(rows);
        } catch(Exception e) {
            e.printStackTrace();
        }


        // 4) Push your list of SecretProperties in as the rows

    }
}
