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
    /**
     * -- GETTER --
     * Bind the progress bar to a Task’s progress if you like
     */
    @Getter
    @FXML private ProgressBar progressBar;
    @FXML private Button cancelBtn;

    @Setter
    private Stage dialogStage;

    /** Set the text in the Label */
    public void setMessage(String msg) {
        message.setText(msg);
    }

    /** Hook your cancel action (e.g. task.cancel()) here */
    public void setOnCancel(Runnable onCancel) {
        cancelBtn.setOnAction(e -> onCancel.run());
    }

    /** Close the dialog */
    public void close() {
        if (dialogStage != null) dialogStage.close();
    }
}