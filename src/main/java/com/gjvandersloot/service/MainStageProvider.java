package com.gjvandersloot.service;

import javafx.stage.Stage;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Setter
@Getter
@Service
public class MainStageProvider {
    private Stage primaryStage;
}
