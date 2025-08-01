package com.gjvandersloot.mvvm.view;

import com.gjvandersloot.data.AuthType;
import com.gjvandersloot.data.Vault;
import com.gjvandersloot.service.TabManagerService;
import javafx.collections.ListChangeListener;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

@Component
public class TabView {
    public TabPane tabPane;
    @Autowired TabManagerService tabManagerService;

    @FXML
    public void initialize() {
        tabManagerService.selectedTabProperty().addListener((obs, o, n) -> {
            if (n == null) return;

            String tabId = n.getName() + "@" + n.getCredentials().getAuthType();

            tabPane.getTabs().stream()
                    .filter(tab -> tabId.equals(tab.getId()))
                    .findFirst()
                    .ifPresent(tab -> tabPane.getSelectionModel().select(tab));
        });

        tabManagerService.tabsProperty().addListener((ListChangeListener<Vault>) c -> {
            while (c.next()) {
                if (c.wasAdded()) {
                    for (Vault vault : c.getAddedSubList()) {
                        var tab = new Tab(vault.getName() + ((vault.getCredentials().getAuthType() == AuthType.INTERACTIVE) ? "" : " (a)"));
                        tab.setId(vault.getName() + "@" + vault.getCredentials().getAuthType());
                        tab.setOnClosed((e) -> tabManagerService.tabsProperty().remove(vault));
                        tabPane.getTabs().add(tab);
                        tabPane.getSelectionModel().select(tab);
                    }
                }
            }
        });
    }

    public void copySecret(ActionEvent actionEvent) {
    }

    public void showSecret(ActionEvent actionEvent) {
    }

    public void addSecret(ActionEvent actionEvent) {
    }

    public void delete(ActionEvent actionEvent) {
    }
}
