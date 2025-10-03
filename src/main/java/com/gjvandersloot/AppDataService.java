package com.gjvandersloot;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.*;
import com.gjvandersloot.data.Store;
import com.microsoft.aad.msal4j.ITokenCacheAccessAspect;
import com.microsoft.aad.msal4j.ITokenCacheAccessContext;
import com.microsoft.aad.msal4jextensions.PersistenceSettings;
import com.microsoft.aad.msal4jextensions.PersistenceTokenCacheAccessAspect;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

@Slf4j
public class AppDataService {
    @Getter
    private Path mainPath;

    @Autowired Store store;

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

    public ITokenCacheAccessAspect getTokenCache() {
        Path cacheFile = getMainPath().resolve("msal.cache.json");

        return new ITokenCacheAccessAspect() {
            @Override
            public void beforeCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
                if (Files.exists(cacheFile)) {
                    try {
                        String data = Files.readString(cacheFile);
                        iTokenCacheAccessContext.tokenCache().deserialize(data);
                    } catch (IOException e) {
                        log.error("Failed to read token cache", e);
                    }
                }
            }

            @Override
            public void afterCacheAccess(ITokenCacheAccessContext context) {
                if (context.hasCacheChanged()) {
                    try {
                        Files.writeString(cacheFile, context.tokenCache().serialize());
                    } catch (IOException e) {
                        log.error("Failed to write token cache", e);
                    }
                }
            }
        };
    }

    public void saveStore() {
        var path = getMainPath().resolve("store.json");

        try {
            // Ensure parent directory exists
            Files.createDirectories(path.getParent());

            // Write JSON with pretty printing (optional)
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), store);
        } catch (IOException e) {
            log.error("Something went wrong serializing store", e);
        }
    }

    public void loadStore() {
        var path  = getMainPath().resolve("store.json");

        if (!Files.exists(path))
            return;

        var typeRef = new TypeReference<Store>() {};

        try {
            var loadedStore = mapper.readValue(path.toFile(), typeRef);
            store.getAccounts().putAll(loadedStore.getAccounts());
            store.getAttachedVaults().putAll(loadedStore.getAttachedVaults());
        } catch (IOException e) {
            log.error("Something went wrong deserializing store", e);
        }
    }
}