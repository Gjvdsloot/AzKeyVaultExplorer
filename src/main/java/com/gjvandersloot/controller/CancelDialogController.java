package com.gjvandersloot.controller;

import javafx.fxml.FXML;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.control.ProgressBar;
import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;

public class CancelDialogController {
    @FXML private Label message;

    @Getter
    @FXML private ProgressBar progressBar;
    @FXML private Button cancelBtn;

    @Setter
    private Stage dialogStage;

    public void setMessage(String msg) {
        message.setText(msg);
    }

    /** Hook your cancel action (e.g. task.cancel()) here */
    public void setOnCancel(Runnable onCancel) {
        cancelBtn.setOnAction(e -> onCancel.run());
    }

    public void close() {
        if (dialogStage != null) dialogStage.close();
    }
}