package com.gjvandersloot.mvvm.viewmodel;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.net.DatagramSocket;

public class WizardViewModel {
    private final StringProperty selectedAuthMethod = new SimpleStringProperty("Secret");

    public StringProperty selectedAuthMethodProperty() {
        return selectedAuthMethod;
    }
}
