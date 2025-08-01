package com.gjvandersloot.service;

import com.gjvandersloot.data.Vault;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.springframework.stereotype.Service;

@Service
public class TabManagerService {
    public ObservableList<Vault> tabsProperty() {
        return tabs;
    }

    private final ObservableList<Vault> tabs = FXCollections.observableArrayList();

    public ObjectProperty<Vault> selectedTabProperty() {
        return selectedTab;
    }

    private final ObjectProperty<Vault> selectedTab = new SimpleObjectProperty<>(null);

    public TabManagerService() {

    }

    public void openVault(Vault vault) {
        var name = vault.getName();
        var authType = vault.getCredentials().getAuthType();
        var tab = tabs.stream().filter(t -> t.getCredentials().getAuthType() == authType && t.getName().equals(name)).findAny();

        if (tab.isPresent()) {
            selectedTab.set(tab.get());
            return;
        }

        tabs.add(vault);
        selectedTab.set(vault);
    }

}
