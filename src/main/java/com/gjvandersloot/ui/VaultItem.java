package com.gjvandersloot.ui;

import com.gjvandersloot.data.Loadable;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;

public class VaultItem implements Loadable {
    private final StringProperty name = new SimpleStringProperty();
    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.setValue(name); };

    @Getter @Setter
    private String accountName;

    @Getter @Setter
    private String vaultUri;

    @Override
    public String toString() {
        return getName();
    }


    private boolean loadFailed = false;
    @Override
    public boolean getLoadFailed() {
        return loadFailed;
    }

    @Override
    public void setLoadFailed(boolean loadFailed) {
        this.loadFailed = loadFailed;
    }
}
