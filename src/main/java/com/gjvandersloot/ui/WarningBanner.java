package com.gjvandersloot.ui;

import javafx.geometry.Insets;
import javafx.scene.control.Button;
import javafx.scene.control.Label;
import javafx.scene.layout.HBox;

public class WarningBanner extends HBox {
    public WarningBanner(String message, boolean showCloseButton, Runnable onClose) {
        var label = new Label(message);
        label.setWrapText(true);
        label.setStyle("-fx-text-fill: black; -fx-font-weight: bold;");

        setStyle("""
            -fx-background-color: #fff8c4;
            -fx-padding: 8;
            -fx-spacing: 10;
            -fx-border-color: #f2c200;
            -fx-border-width: 0 0 1 0;
        """);
        setPadding(new Insets(10));
        getChildren().add(label);

        if (showCloseButton) {
            var closeButton = new Button("✕");
            closeButton.setStyle("-fx-background-color: transparent; -fx-text-fill: black; -fx-font-size: 14;");
            closeButton.setOnAction(e -> {
                this.setVisible(false);
                if (onClose != null) onClose.run();
            });
            getChildren().add(closeButton);
        }
    }
}