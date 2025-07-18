package com.gjvandersloot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gjvandersloot.data.Store;
import com.microsoft.aad.msal4jextensions.PersistenceSettings;
import com.microsoft.aad.msal4jextensions.PersistenceTokenCacheAccessAspect;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class AppDataService {
    @Getter
    private Path mainPath;

    @Autowired
    Store store;

    private final ObjectMapper mapper = new ObjectMapper();

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

    public PersistenceTokenCacheAccessAspect getTokenCache() throws IOException {
        PersistenceSettings settings = PersistenceSettings
                .builder("msal.cache", getMainPath())
                .setLockRetry(500, 10)
                .build();

        return new PersistenceTokenCacheAccessAspect(settings);
    }

    public void saveStore() {
        var path = getMainPath().resolve("store.json");

        try {
            // Ensure parent directory exists
            Files.createDirectories(path.getParent());

            // Write JSON with pretty printing (optional)
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), store);
        } catch (IOException e) {
            e.printStackTrace(); // Replace with logger in production
        }
    }

    public void loadStore() {
        var path  = getMainPath().resolve("store.json");

        if (!Files.exists(path))
            return;

        var typeRef = new TypeReference<Store>() {};

        try {
            var loadedStore = mapper.readValue(path.toFile(), typeRef);
            store.setAccounts(loadedStore.getAccounts());
        } catch (IOException e) {
            e.printStackTrace();
        }
    }
}