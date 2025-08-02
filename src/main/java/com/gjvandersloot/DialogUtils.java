package com.gjvandersloot;

import com.gjvandersloot.controller.ErrorDialogController;
import com.gjvandersloot.controller.MainController;
import com.gjvandersloot.service.MainStageProvider;
import javafx.application.Platform;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Modality;
import javafx.stage.Stage;
import javafx.stage.StageStyle;
import javafx.stage.Window;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class DialogUtils {
    @Autowired MainStageProvider mainStageProvider;

    public void showError(String message) {
        Window owner = getActiveWindow();
        showError(message, owner);
    }

    private Window getActiveWindow() {
        return Window.getWindows().stream()
                .filter(Window::isFocused)
                .findFirst().orElseGet(mainStageProvider::getPrimaryStage);
    }

    public void showError(String message, Window window) {
        Platform.runLater(() -> {
            var loader = new FXMLLoader(MainController.class.getResource("/ErrorDialog.fxml"));

            Parent root;
            try {
                root = loader.load();
            } catch (IOException ex) {
                throw new RuntimeException(ex);
            }

            ErrorDialogController errorCtr = loader.getController();

            var dialog = new Stage(StageStyle.DECORATED);
            dialog.initOwner(window);
            dialog.initModality(Modality.APPLICATION_MODAL);
            dialog.setScene(new Scene(root));

            errorCtr.setDialogStage(dialog);
            errorCtr.setMessage(message);
            dialog.showAndWait();
        });
    }
}