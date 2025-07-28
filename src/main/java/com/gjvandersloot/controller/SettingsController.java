package com.gjvandersloot.controller;

import com.gjvandersloot.AppDataService;
import com.gjvandersloot.data.Account;
import com.gjvandersloot.data.AttachedVault;
import com.gjvandersloot.data.Store;
import com.gjvandersloot.ui.settings.Wrapper;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettingsController {

    public TreeView<Object> treeView;

    private final TreeItem<Object> root = new TreeItem<>();

    private final TreeItem<Object> attachedRoot = new TreeItem<>("");

    @Autowired
    AppDataService appDataService;

    @Autowired
    Store store;

    @FXML
    public void initialize() {
        treeView.setRoot(root);
        treeView.setShowRoot(false);

        var label = new Label("Attached");
        label.setStyle("-fx-font-weight: bold;");
        attachedRoot.setGraphic(label);
        root.getChildren().add(attachedRoot);
        attachedRoot.setExpanded(true);

        store.getAccounts().addListener((MapChangeListener<String, Account>) change -> {
            if (change.wasAdded() && !change.wasRemoved())
                createAccountItem(change.getValueAdded());
        });

        store.getAttachedVaults().addListener((MapChangeListener<String, AttachedVault>) change -> {
            if (change.wasAdded() && !change.wasRemoved())
                createAttachedVaultItem(change.getValueAdded());
        });

        for (var a : store.getAccounts().values()) {
            createAccountItem(a);
        }

        for (var a : store.getAttachedVaults().values()) {
            createAttachedVaultItem(a);
        }

        expandAll(root);
    }

    private void createAttachedVaultItem(AttachedVault vault) {
        var ti = new TreeItem<Object>("");

        Label vaultName = new Label(vault.getName());
        var removeLink = new Hyperlink("Remove");

        removeLink.setOnAction(e -> {
            attachedRoot.getChildren().remove(ti);
            store.getAttachedVaults().remove(vault.getVaultUri());
        });

        var box = new HBox(10, vaultName, removeLink);
        box.setAlignment(Pos.CENTER_LEFT);

        ti.setGraphic(box);

        attachedRoot.getChildren().add(ti);
    }

    private void createAccountItem(Account a) {
        var ai = new TreeItem<>();

        Label prefix = new Label("Account:");
        prefix.setStyle("-fx-font-weight: bold;");

        Label username = new Label(a.getUsername());
        var removeLink = new Hyperlink("Remove");

        var box = new HBox(10, prefix, username, removeLink);
        box.setAlignment(Pos.CENTER_LEFT);

        ai.setGraphic(box);
        ai.setValue("");

        root.getChildren().add(ai);

        removeLink.setOnAction(e -> {
            root.getChildren().remove(ai);
            store.getAccounts().remove(a.getUsername());
        });

        for (var t : a.getTenants().values()) {
            var ti = new TreeItem<Object>("");
            Label tPrefix = new Label("Tenant:");
            tPrefix.setStyle("-fx-font-weight: bold;");
            Label tText = new Label(t.getId());

            var tBox = new HBox(10, tPrefix, tText);
            tBox.setAlignment(Pos.CENTER_LEFT);
            ti.setGraphic(tBox);

            ai.getChildren().add(ti);

            TreeItem<Object> checkAllItem = new TreeItem<>("Select all subscriptions");
            var checkAll = new CheckBox();

            checkAll.setOnAction((e) -> {
                var isSelected = checkAll.isSelected();
                var isIndeterminate = checkAll.isIndeterminate();
                for (var c : checkAllItem.getChildren()) {
                    var sel = (CheckBox) c.getGraphic();

                    sel.setSelected(isSelected && !isIndeterminate);
                }
            });

            checkAllItem.setGraphic(checkAll);
            ti.getChildren().add(checkAllItem);

            for (var s : t.getSubscriptions().values()) {
                var si = new TreeItem<Object>(new Wrapper<>(s, s.getName()));

                var sCheckBox = new CheckBox();
                sCheckBox.selectedProperty().bindBidirectional(s.visibleProperty());
                sCheckBox.setOnAction((e) -> refreshSelectAll(checkAllItem));
                checkAllItem.getChildren().add(si);

                s.visibleProperty().addListener(obs -> appDataService.saveStore());

                si.setGraphic(sCheckBox);
            }

            refreshSelectAll(checkAllItem);
        }
    }

    private void refreshSelectAll(TreeItem<Object> checkAll) {
        boolean any = false, all = true;
        for (TreeItem<?> c : checkAll.getChildren()) {
            boolean sel = ((CheckBox) c.getGraphic()).isSelected();
            any |= sel; all &= sel;
        }
        var gfx = (CheckBox) checkAll.getGraphic();
        gfx.setSelected(all);
        gfx.setIndeterminate(any && !all);
    }

    private void expandAll(TreeItem<?> item) {
        if (item == null) return;

        item.setExpanded(true);
        for (TreeItem<?> child : item.getChildren()) {
            expandAll(child);
        }
    }
}
