package com.gjvandersloot;

import com.microsoft.aad.msal4j.PublicClientApplication;
import javafx.application.HostServices;
import javafx.stage.Stage;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class Config {
    private static final String AUTHORITY = "https://login.microsoftonline.com/common";
    private static final String CLIENT_ID = "04b07795-8ddb-461a-bbee-02f9e1bf7b46";
    private static final String APPDATA_FOLDER_NAME = "KvExplorer";

    @Bean
    public PublicClientApplication publicClientApplication(AppDataService appDataService) throws IOException {
        return PublicClientApplication.builder(CLIENT_ID)
                .authority(AUTHORITY)
                .setTokenCacheAccessAspect(appDataService.getTokenCache())
                .build();
    }

    @Bean
    public AppDataService appDataService() throws IOException {
        var appDataService = new AppDataService();
        appDataService.initialize(APPDATA_FOLDER_NAME);
        return appDataService;
    }
}
