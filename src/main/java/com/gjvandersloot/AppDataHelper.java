package com.gjvandersloot;

import java.nio.file.Path;
import java.nio.file.Paths;

public class AppDataHelper {
    public static Path getAppDataFolder(String appName) {
        String os = System.getProperty("os.name").toLowerCase();
        String userHome = System.getProperty("user.home");

        if (os.contains("win")) {
            String appData = System.getenv("APPDATA"); // usually C:\Users\User\AppData\Roaming
            return Paths.get(appData, appName);
        } else if (os.contains("mac")) {
            return Paths.get(userHome, "Library", "Application Support", appName);
        } else { // Linux/Unix
            return Paths.get(userHome, ".config", appName);
        }
    }
}