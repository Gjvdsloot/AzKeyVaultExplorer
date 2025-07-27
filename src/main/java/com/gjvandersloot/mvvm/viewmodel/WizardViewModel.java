package com.gjvandersloot.mvvm.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import org.springframework.stereotype.Component;

@Component
public class WizardViewModel {
    private final StringProperty selectedAuthMethod = new SimpleStringProperty("Secret");

    public StringProperty selectedAuthMethodProperty() {
        return selectedAuthMethod;
    }
}
