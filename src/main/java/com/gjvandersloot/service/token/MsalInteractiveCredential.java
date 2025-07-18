package com.gjvandersloot.service.token;

import com.azure.core.credential.AccessToken;
import com.azure.core.credential.TokenCredential;
import com.azure.core.credential.TokenRequestContext;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import reactor.core.publisher.Mono;

import java.time.OffsetDateTime;

import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.PublicClientApplication;

import java.net.URI;
import java.time.ZoneOffset;
import java.util.HashSet;
import java.util.concurrent.CompletableFuture;

public class MsalInteractiveCredential implements TokenCredential {
    private final PublicClientApplication pca;

    public MsalInteractiveCredential(PublicClientApplication pca) {
        this.pca = pca;
    }

    @Override
    public Mono<AccessToken> getToken(TokenRequestContext request) {
        // Build a new interactive parameters each time, using the requested scopes
        InteractiveRequestParameters params =
                InteractiveRequestParameters.builder(URI.create("http://localhost"))
                        .prompt(Prompt.SELECT_ACCOUNT)
                        .scopes(new HashSet<>(request.getScopes()))           // ← use the scopes Azure SDK actually asked for
                        .build();

        // Kick off the MSAL interactive flow (this will open the browser)
        CompletableFuture<IAuthenticationResult> future = pca.acquireToken(params);

        return Mono.fromFuture(future)
                .map(result -> new AccessToken(
                        result.accessToken(),
                        // use UTC offset for consistency
                        OffsetDateTime.ofInstant(result.expiresOnDate().toInstant(),
                                ZoneOffset.UTC)
                ));
    }
}