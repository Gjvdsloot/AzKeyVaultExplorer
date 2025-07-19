package com.gjvandersloot.ui;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class SecretItem {
    private String secretName;
    private String vaultUri;
    private String accountName;

    private final StringProperty value = new SimpleStringProperty(this, "value");
    public String getValue() {
        return value.get();
    }
    public StringProperty valueProperty() { return value; }


    private final BooleanProperty hidden = new SimpleBooleanProperty(this, "hidden");
    public boolean isHidden() {
        return hidden.get();
    }
    public BooleanProperty hiddenProperty() { return hidden; }
}
