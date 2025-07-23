package com.gjvandersloot;

import com.gjvandersloot.service.MainStageProvider;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

public class Gui extends Application {
    private ConfigurableApplicationContext springContext;

    @Override
    public void init() {
        springContext = new SpringApplicationBuilder(Main.class).headless(false).run();
    }

    @Override
    public void start(Stage stage) throws IOException {
        springContext.getBean(AppDataService.class).loadStore();

        springContext.getBean(MainStageProvider.class).setPrimaryStage(stage);

        FXMLLoader loader = new FXMLLoader(getClass().getResource("/Wireframe.fxml"));
        loader.setControllerFactory(springContext::getBean);

        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600);

        stage.setTitle("FXML Welcome");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        springContext.close();
    }
}
