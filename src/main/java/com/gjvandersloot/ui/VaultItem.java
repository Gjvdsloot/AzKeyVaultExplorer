package com.gjvandersloot.ui;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;

public class VaultItem {
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
}
