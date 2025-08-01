package com.gjvandersloot.mvvm.view;

import com.gjvandersloot.data.AuthType;
import com.gjvandersloot.data.Vault;
import com.gjvandersloot.service.TabManagerService;
import javafx.application.Platform;
import javafx.collections.ListChangeListener;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.control.Tab;
import javafx.scene.control.TabPane;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class TabView {
    public TabPane tabPane;
    @Autowired TabManagerService tabManagerService;

    @Autowired
    private ApplicationContext context;

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
                        var tab = createVaultTab(vault);
                        tabPane.getTabs().add(tab);

                        Platform.runLater(() -> tabPane.getSelectionModel().select(tab)); // ✅ here
                    }
                }
            }
        });
    }

    private Tab createVaultTab(Vault vault) {
        var tab = new Tab(vault.getName() + (
                (vault.getCredentials().getAuthType() == AuthType.INTERACTIVE) ? "" : " (a)"
        ));
        String vaultId = vault.getName() + "@" + vault.getCredentials().getAuthType();
        tab.setId(vaultId);
        tab.setOnClosed(e -> tabManagerService.tabsProperty().remove(vault));

        var vaultPane = new TabPane();

        var secretTab = new Tab("Secrets");
        var keysTab = new Tab("Keys");
        var certsTab = new Tab("Certificates");
        try {
            FXMLLoader loader = new FXMLLoader(getClass().getResource("/SecretView.fxml"));
            loader.setControllerFactory(context::getBean);
            Parent content = loader.load();

            SecretView ctr = loader.getController();
            ctr.init(vault);

            secretTab.setContent(content);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        secretTab.setClosable(false);
        keysTab.setClosable(false);
        certsTab.setClosable(false);

        vaultPane.getTabs().add(secretTab);
        vaultPane.getTabs().add(keysTab);
        vaultPane.getTabs().add(certsTab);
        tab.setContent(vaultPane);

        return tab;
    }
}
