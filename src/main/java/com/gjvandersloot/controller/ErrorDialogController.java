package com.gjvandersloot.controller;

import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.TextArea;
import javafx.stage.Stage;
import lombok.Setter;

public class ErrorDialogController {
    @FXML
    public TextArea errorField;
    @Setter
    private Stage dialogStage;

    public void setMessage(String e) {
        errorField.setText(e);
    }

    public void confirm(ActionEvent actionEvent) {
        dialogStage.close();
    }

    public void copy(ActionEvent actionEvent) {
        MainController.copyToClipBoard(errorField.getText());
    }
}
