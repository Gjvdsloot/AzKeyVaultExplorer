package com.gjvandersloot;

import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.ApplicationContext;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class FxmlViewLoader {
    @Autowired
    private ApplicationContext context;

    public Parent load(String path) throws IOException, IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource(path));
        loader.setControllerFactory(context::getBean);
        return loader.load();
    }
}