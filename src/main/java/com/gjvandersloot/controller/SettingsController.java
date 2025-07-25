package com.gjvandersloot.controller;

import com.gjvandersloot.data.Account;
import com.gjvandersloot.data.Store;
import com.gjvandersloot.ui.settings.Wrapper;
import javafx.collections.MapChangeListener;
import javafx.fxml.FXML;
import javafx.geometry.Insets;
import javafx.geometry.Pos;
import javafx.scene.control.*;
import javafx.scene.control.cell.CheckBoxTreeCell;
import javafx.scene.layout.HBox;
import javafx.scene.text.Text;
import javafx.scene.text.TextFlow;
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

                    if (treeItem instanceof CheckBoxTreeItem<Object> cbItem) {
                        // This will give you the standard checkbox behavior
                        CheckBox checkBox = new CheckBox();
                        checkBox.selectedProperty().bindBidirectional(cbItem.selectedProperty());

                        // Set text next to it
                        Label label = new Label(item.toString());

                        HBox hBox = new HBox(5, checkBox, label);
                        hBox.setAlignment(Pos.CENTER_LEFT);
                        setGraphic(hBox);
                        setText(null);
                    } else {
                        setText(item.toString());
                        setGraphic(getTreeItem().getGraphic());
                    }
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
            var ai = createAccountItem(a);
            for (var t : a.getTenants().values()) {
                var ti = new TreeItem<>();
                ti.setValue(new Wrapper<>(t, "Tenant: " + t.getId()));
                ai.getChildren().add(ti);

                CheckBoxTreeItem<Object> checkAll = new CheckBoxTreeItem<>("Select all subscriptions");
                checkAll.setIndependent(false);
                ti.getChildren().add(checkAll);

                for (var s : t.getSubscriptions().values()) {
                    var si = new CheckBoxTreeItem<Object>(new Wrapper<>(s, s.getName()));
                    si.selectedProperty().bindBidirectional(s.visibleProperty());
                    checkAll.getChildren().add(si);
                }
            }
        }

        expandAll(root);
    }

    private void expandAll(TreeItem<?> item) {
        if (item == null) return;

        item.setExpanded(true);
        for (TreeItem<?> child : item.getChildren()) {
            expandAll(child);
        }
    }

    private TreeItem<Object> createAccountItem(Account a) {
        var treeItem = new TreeItem<>();

//        var aw = new Wrapper<>(a, a.getUsername());

        Label prefix = new Label("Account: ");
        prefix.setStyle("-fx-font-weight: bold;");

        Label username = new Label(a.getUsername());
        var removeLink = new Hyperlink("Remove");


        var box = new HBox(10, prefix, username, removeLink);
        box.setAlignment(Pos.CENTER_LEFT);
        box.setPadding(new Insets(2));

        treeItem.setGraphic(box);
        treeItem.setValue("");

        root.getChildren().add(treeItem);

        removeLink.setOnAction(e -> {
            root.getChildren().remove(treeItem);
            store.getAccounts().remove(a.getUsername());
        });

        return treeItem;
    }
}
