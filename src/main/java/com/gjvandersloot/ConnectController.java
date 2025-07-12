package com.gjvandersloot;

import javafx.fxml.FXML;
import javafx.scene.input.MouseEvent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import org.springframework.stereotype.Controller;

@Component
public class ConnectController {
    private final Manager manager;

    public ConnectController(Manager manager) {
        this.manager = manager;
    }

    @FXML
    public void connect(MouseEvent mouseEvent) {
        manager.SayHello();
    }
}
