package com.gjvandersloot.utils;

import javafx.scene.control.TextField;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;

public final class FxExtensions {
    public static void clearOnEscape(TextField field) {
        field.addEventFilter(KeyEvent.KEY_PRESSED, event -> {
            if (event.getCode() == KeyCode.ESCAPE && field.isFocused()) {
                field.clear();
                event.consume();
            }
        });
    }
}