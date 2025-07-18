package com.gjvandersloot.service;

import com.azure.core.credential.AccessToken;
import com.azure.identity.InteractiveBrowserCredential;
import com.azure.identity.InteractiveBrowserCredentialBuilder;
import com.azure.identity.TokenCachePersistenceOptions;
import com.azure.security.keyvault.secrets.SecretClient;
import com.azure.security.keyvault.secrets.SecretClientBuilder;
import com.gjvandersloot.service.token.MsalInteractiveCredential;
import com.microsoft.aad.msal4j.*;
import org.springframework.stereotype.Service;
import reactor.core.publisher.Mono;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URI;
import java.nio.file.Paths;
import java.time.OffsetDateTime;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
public class SecretClientService {
    private static final String AUTHORITY = "https://login.microsoftonline.com/common";
    private static final String CLIENT_ID = "04b07795-8ddb-461a-bbee-02f9e1bf7b46";
    private static final Set<String> VAULT_SCOPE = Set.of("https://vault.azure.net/.default");

    private Map<String, SecretClient> clients = new HashMap<>();

//    private final PublicClientApplication pca;
//
//    public SecretClientService() throws IOException {
//        pca =  PublicClientApplication.builder(CLIENT_ID)
//                .authority(AUTHORITY)
//                .setTokenCacheAccessAspect(Manager.getTokenCache())
//                .build();
//    }

    public SecretClient getOrCreateClient(String vaultUrl) throws Exception {
        var secretClient = clients.getOrDefault(vaultUrl, null);
        if (secretClient != null) return secretClient;

        TokenCachePersistenceOptions persistenceOptions =
                new TokenCachePersistenceOptions()
                        .setName("my-app-msal-cache")     // file will be something like ~/.identity/my-app-msal-cache.json
                        .setUnencryptedStorageAllowed(true);

        InteractiveBrowserCredential credential = new InteractiveBrowserCredentialBuilder()
                .clientId(CLIENT_ID)
                .tokenCachePersistenceOptions(persistenceOptions)
                .build();

        var client = new SecretClientBuilder()
        .vaultUrl(vaultUrl)
        .credential(credential)
        .buildClient();

        clients.put(vaultUrl, client);

        return client;
    }

//    public SecretClient getOrCreateClient(String vaultUrl) throws Exception {
////        ClientCredentialParameters parameters =
////                ClientCredentialParameters.builder(VAULT_SCOPE).build();
//
//
//        InteractiveRequestParameters params = InteractiveRequestParameters.builder(URI.create("http://localhost"))
//                .prompt(Prompt.SELECT_ACCOUNT)
//                .scopes(VAULT_SCOPE)
//                .build();
//
//        SecretClient secretClient = new SecretClientBuilder()
//                .vaultUrl(vaultUrl)
////                .credential(request -> Mono.just(new AccessToken(result.accessToken(),
////                        OffsetDateTime.ofInstant(result.expiresOnDate().toInstant(), OffsetDateTime.now().getOffset()))))
//                .credential(new MsalInteractiveCredential(pca))
//                .buildClient();
//
//        return secretClient;
//    }
}
