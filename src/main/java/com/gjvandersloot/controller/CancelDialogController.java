package com.gjvandersloot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

public class CancelDialogController {
//    @FXML private Label message;

    @Getter
    @FXML private ProgressBar progressBar;
    @FXML private Button cancelBtn;

    @Setter @Getter
    private Stage stage;

//    public void setMessage(String msg) {
//        message.setText(msg);
//    }

    public void setOnCancel(Runnable onCancel) {
        cancelBtn.setOnAction(e -> onCancel.run());
    }
}