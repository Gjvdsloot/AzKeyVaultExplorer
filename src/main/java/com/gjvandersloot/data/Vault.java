package com.gjvandersloot.data;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Vault implements ILoadable {
    private final StringProperty name = new SimpleStringProperty();
    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public void setName(String name) { this.name.setValue(name); }

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
