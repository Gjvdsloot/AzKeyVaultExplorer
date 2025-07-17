package com.gjvandersloot.controller;

import com.gjvandersloot.data.Account;
import com.gjvandersloot.data.Store;
import com.gjvandersloot.service.AccountService;
import com.gjvandersloot.ui.SubscriptionItem;
import com.gjvandersloot.ui.VaultItem;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TreeItem;
import javafx.scene.control.TreeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class NewMainController {

    @FXML
    public TreeView<Object> treeView;

    @Autowired
    AccountService accountService;

    @Autowired
    Store store;

    public void addSubscription() {
        Account account = null;
        try {
            account = accountService.addAccount();
        } catch (Exception e) {
            throw new RuntimeException(e);
        }

        store.getAccounts().put(account.getUsername(), account);
        loadTree();
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
                                loadVaults(obj, subscription.getId());
                            } catch (Exception e) {
                                throw new RuntimeException(e);
                            }
                        }
                    });
                }
            }
        }
    }

    private void loadVaults(TreeItem<Object> obj, String id) throws Exception {
        var vaults = accountService.addKeyVaults(id);

        obj.getChildren().clear();

        for (var vault : vaults) {
            var vaultItem = new VaultItem();
            vaultItem.setVaultUri(vault.getVaultUri());
            vaultItem.setName(vault.getName());

            var treeItem = new TreeItem<>();
            treeItem.setValue(vaultItem);

            obj.getChildren().add(treeItem);
        }
    }
}
