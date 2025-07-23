package com.gjvandersloot.controller;

import javafx.fxml.FXML;
import javafx.scene.Node;
import org.springframework.stereotype.Component;

@Component
public class WireframeController{
    @FXML private Node mainView;
    @FXML private Node settingsView;

    @FXML
    public void initialize() {
        mainView.setVisible(true);
        mainView.setManaged(true);

        settingsView.setVisible(false);
        settingsView.setManaged(false);
    }

    @FXML
    private void onHome() {
        mainView.setVisible(true);
        mainView.setManaged(true);

        settingsView.setVisible(false);
        settingsView.setManaged(false);
    }

    @FXML
    private void onSettings() {
        settingsView.setVisible(true);
        settingsView.setManaged(true);

        mainView.setVisible(false);
        mainView.setManaged(false);
    }
}