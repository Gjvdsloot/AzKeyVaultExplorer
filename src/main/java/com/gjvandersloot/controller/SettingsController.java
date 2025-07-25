package com.gjvandersloot.controller;

import com.gjvandersloot.data.Account;
import com.gjvandersloot.data.Store;
import com.gjvandersloot.ui.settings.Wrapper;
import com.sun.source.tree.Tree;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.layout.HBox;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettingsController {

    public TreeView<Object> treeView;

    private final TreeItem<Object> root = new TreeItem<>();

    @Autowired
    Store store;

    @FXML
    public void initialize() {
        treeView.setCellFactory(tv -> new TreeCell<>() {
            @Override
            protected void updateItem(Object item, boolean empty) {
                super.updateItem(item, empty);
                if (empty || item == null) {
                    setText(null);
                    setGraphic(null);
                } else {
                    TreeItem<Object> treeItem = getTreeItem();
                    setText(null); // Don't show value.toString()
                    setGraphic(treeItem.getGraphic());
                }
            }
        });

        treeView.setRoot(root);
        treeView.setShowRoot(false);

        store.getAccounts().addListener((MapChangeListener<String, Account>) change -> {
            if (change.wasAdded() && !change.wasRemoved())
                createAccountItem(change.getValueAdded());
        });

        for (var a : store.getAccounts().values()) {
            createAccountItem(a);
            for (var t : a.getTenants().values()) {
                for (var s : t.getSubscriptions().values()) {
                    var x = new TreeItem<Boolean>();
//                    x.setGraphic();
//                    s.visibleProperty().bindBidirectional(x.valueProperty());
                }
            }
        }
    }

    private void createAccountItem(Account a) {
        var treeItem = new TreeItem<>();

        var aw = new Wrapper<>(a, a.getUsername());

        var label = new Label(a.getUsername());
        var removeLink = new Hyperlink("Remove");


        var box = new HBox(10, label, removeLink);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(2));

        treeItem.setGraphic(box);
        treeItem.setValue(aw);

        root.getChildren().add(treeItem);

        removeLink.setOnAction(e -> {
            root.getChildren().remove(treeItem);
            store.getAccounts().remove(a.getUsername());
        });
    }
}
