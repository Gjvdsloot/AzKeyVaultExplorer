package com.gjvandersloot;

import com.gjvandersloot.service.Manager;
import com.microsoft.aad.msal4j.PublicClientApplication;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

import java.io.IOException;

@Configuration
public class Config {
    private static final String AUTHORITY = "https://login.microsoftonline.com/common";
    private static final String CLIENT_ID = "04b07795-8ddb-461a-bbee-02f9e1bf7b46";

    @Bean
    public PublicClientApplication publicClientApplication() throws IOException {
        return PublicClientApplication.builder(CLIENT_ID)
                .authority(AUTHORITY)
                .setTokenCacheAccessAspect(Manager.getTokenCache())
                .build();
    }
}
