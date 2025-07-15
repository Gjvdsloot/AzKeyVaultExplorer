package com.gjvandersloot;

import com.gjvandersloot.service.HostedServiceProvider;
import com.gjvandersloot.service.MainStageProvider;
import javafx.application.Application;
import javafx.fxml.FXMLLoader;
import javafx.scene.Parent;
import javafx.scene.Scene;
import javafx.stage.Stage;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.boot.builder.SpringApplicationBuilder;
import org.springframework.context.ConfigurableApplicationContext;

import java.io.IOException;

@SpringBootApplication
public class Main extends Application {
    private ConfigurableApplicationContext springContext;

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    @Override
    public void init() throws IOException {
        springContext = new SpringApplicationBuilder(Main.class).run();

        HostedServiceProvider hsp = springContext.getBean(HostedServiceProvider.class);
        hsp.setHostServices(getHostServices());

        AppDataService appDataService = springContext.getBean(AppDataService.class);
        appDataService.initialize("kvexplorer");
    }

    @Override
    public void start(Stage stage) throws IOException {
        FXMLLoader loader = new FXMLLoader(getClass().getResource("/MainWindow.fxml"));
        loader.setControllerFactory(springContext::getBean);

        Parent root = loader.load();
        Scene scene = new Scene(root, 800, 600);

        MainStageProvider stageProvider = springContext.getBean(MainStageProvider.class);
        stageProvider.setPrimaryStage(stage);


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
