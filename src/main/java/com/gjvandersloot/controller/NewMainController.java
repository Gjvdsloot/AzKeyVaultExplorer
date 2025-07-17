package com.gjvandersloot.controller;

import com.gjvandersloot.data.Account;
import com.gjvandersloot.data.Store;
import com.gjvandersloot.service.AccountService;
import com.gjvandersloot.service.MainStageProvider;
import com.gjvandersloot.ui.SubscriptionItem;
import com.gjvandersloot.ui.VaultItem;
import io.netty.util.concurrent.CompleteFuture;
import javafx.application.Platform;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;

@Component
public class NewMainController {

    @FXML
    public TreeView<Object> treeView;

    @Autowired
    AccountService accountService;

    @Autowired
    Store store;

    @Autowired
    MainStageProvider stageProvider;

    public void addSubscription() throws Exception {
        var loader = new FXMLLoader(getClass().getResource("/CancelDialog.fxml"));
        Parent root = loader.load();

        CancelDialogController cancelController = loader.getController();
        var dialog = new Stage(StageStyle.UNDECORATED);
        dialog.initOwner(stageProvider.getPrimaryStage());               // your main window
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

                    var treeItem = new TreeItem<>();
                    treeItem.setValue(vaultItem);

                    obj.getChildren().add(treeItem);
                }
            });
        });
    }
}
