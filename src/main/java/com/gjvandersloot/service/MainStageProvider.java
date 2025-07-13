package com.gjvandersloot.service;

import javafx.stage.Stage;
import org.springframework.stereotype.Service;

@Service
public class MainStageProvider {
    private Stage primaryStage;


    public Stage getPrimaryStage() {
        return primaryStage;
    }

    public void setPrimaryStage(Stage primaryStage) {
        this.primaryStage = primaryStage;
    }
}
