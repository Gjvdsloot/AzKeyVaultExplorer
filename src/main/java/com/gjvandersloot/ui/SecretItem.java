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

    private final BooleanProperty isVisible = new SimpleBooleanProperty(this, "visible");
    private final StringProperty secretValue = new SimpleStringProperty(this, "secretValue");
}
