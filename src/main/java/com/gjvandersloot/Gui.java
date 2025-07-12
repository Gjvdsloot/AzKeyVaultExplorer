package com.gjvandersloot;

import javafx.application.Application;
import javafx.application.Platform;
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
        // Initialize Spring context before JavaFX starts
        springContext = new SpringApplicationBuilder(Main.class).run();

        HostedServiceProvider hsp = springContext.getBean(HostedServiceProvider.class);
        hsp.setHostServices(getHostServices());

        Platform.runLater(() -> {
            System.out.println("Inside Platform.runLater");
        });
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        loader.setControllerFactory(type -> {
            System.out.println("Looking up controller for: " + type);
            return springContext.getBean(type);
        });

        Parent root = loader.load();
        Scene scene = new Scene(root, 300, 275);

        stage.setTitle("FXML Welcome");
        stage.setScene(scene);
        stage.show();
    }

    @Override
    public void stop() {
        // Close Spring context when app closes
        springContext.close();
    }
}
