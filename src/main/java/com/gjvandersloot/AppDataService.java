package com.gjvandersloot;

import lombok.Getter;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Service
public class AppDataService {
    @Getter
    private Path mainPath;

    public void initialize(String appName) throws IOException {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        Path appDataPath;

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA");
            appDataPath = Paths.get(appData, appName);
        } else if (os.contains("mac")) {
            appDataPath = Paths.get(userHome, "Library", "Application Support", appName);
        } else { // Linux/Unix
            appDataPath = Paths.get(userHome, ".config", appName);
        }

        if (!Files.exists(appDataPath)) {
            Files.createDirectories(appDataPath);
        }

        mainPath = appDataPath;

    }
}