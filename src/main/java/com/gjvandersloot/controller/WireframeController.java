package com.gjvandersloot.controller;

import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.Node;
import org.springframework.stereotype.Component;

@Component
public class WireframeController{
    @FXML private Node mainView;
    @FXML private Node settingsView;

    @FXML
    public void initialize() {
        mainView.toFront();
        onHome();

        mainView.setManaged(true);
        settingsView.setManaged(true);
    }

    @FXML
    private void onHome() {
        mainView.toFront();
        mainView.setVisible(true);
        settingsView.setVisible(false);
    }

    @FXML
    private void onSettings() {
        settingsView.toFront();
        mainView.setVisible(false);
        settingsView.setVisible(true);
    }

    public void closeApp() {
        Platform.exit();
    }
}