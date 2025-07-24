package com.gjvandersloot.controller;

import com.gjvandersloot.data.Store;
import javafx.fxml.FXML;
import javafx.scene.control.TreeView;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class SettingsController {

    public TreeView<Object> treeView;

    @Autowired
    Store store;

    @FXML
    public void initialize() {
        for (var a : store.getAccounts().values()) {
            for (var t : a.getTenants().values()) {
                for (var s : t.getSubscriptions().values()) {

                }
            }
        }
    }
}
