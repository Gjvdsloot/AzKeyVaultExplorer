package com.gjvandersloot.service.token;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.aad.msal4j.*;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import java.net.URI;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;
import java.util.concurrent.CompletionException;
import java.util.concurrent.ExecutionException;

public class MsalInteractiveCredential implements TokenCredential {
    private final PublicClientApplication pca;
    private final String accountName;

    public MsalInteractiveCredential(PublicClientApplication pca, String accountName) {
        this.pca = pca;
        this.accountName = accountName;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        CompletableFuture<IAuthenticationResult> future = CompletableFuture.supplyAsync(() -> {
            try {
                var account = pca.getAccounts().get().stream().filter(a -> accountName.equals(a.username())).findFirst().get();

                SilentParameters params = SilentParameters.builder(new HashSet<>(request.getScopes()), account).build();

                return pca.acquireTokenSilently(params).get();
            } catch (Exception e) {
                InteractiveRequestParameters params =
                        InteractiveRequestParameters.builder(URI.create("http://localhost"))
                                .prompt(Prompt.SELECT_ACCOUNT)
                                .scopes(new HashSet<>(request.getScopes()))           // ← use the scopes Azure SDK actually asked for
                                .build();

                try {
                    return pca.acquireToken(params).get();
                } catch (InterruptedException | ExecutionException ex) {
                    throw new CompletionException(ex);
                }
            }
        });

        return Mono.fromFuture(future)
                .map(result -> new AccessToken(
                        result.accessToken(),
                        // use UTC offset for consistency
                        OffsetDateTime.ofInstant(result.expiresOnDate().toInstant(),
                                ZoneOffset.UTC)
                ));
    }
}