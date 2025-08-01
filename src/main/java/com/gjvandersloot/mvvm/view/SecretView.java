package com.gjvandersloot.mvvm.view;

import com.gjvandersloot.data.Vault;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;

public class SecretView implements Initializable {
    private final Vault vault;
    private boolean isLoaded = false;

    public SecretView(Vault vault) {
        this.vault = vault;
    }

    @FXML
    public void initialize() {

    }

    public void copySecret(ActionEvent actionEvent) {
    }

    public void showSecret(ActionEvent actionEvent) {
    }

    public void addSecret(ActionEvent actionEvent) {
    }

    public void delete(ActionEvent actionEvent) {
    }

    @Override
    public void init() {
        if (isLoaded) return;

        System.out.println("Loaded");
        isLoaded = true;
    }
}
