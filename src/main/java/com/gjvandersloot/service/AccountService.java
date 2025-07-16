package com.gjvandersloot.service;

import com.gjvandersloot.data.Account;
import com.gjvandersloot.data.Tenant;
import com.gjvandersloot.data.Subscription;
import com.microsoft.aad.msal4j.IAuthenticationResult;
import com.microsoft.aad.msal4j.InteractiveRequestParameters;
import com.microsoft.aad.msal4j.Prompt;
import com.microsoft.aad.msal4j.PublicClientApplication;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutionException;
import java.util.function.Function;
import java.util.stream.Collectors;

public class AccountService {
    private static final String AUTHORITY = "https://login.microsoftonline.com/common";

    public static final String CLIENT_ID = "04b07795-8ddb-461a-bbee-02f9e1bf7b46";

    Set<String> ARM_SCOPE = Set.of("https://management.azure.com/.default");
    Set<String> VAULT_SCOPE = Set.of("https://vault.azure.net/.default");
    Set<String> GRAPH_SCOPE = Set.of("https://vault.azure.net/.default");

    public Account AddAccount() throws Exception {
        PublicClientApplication pca;
        pca = PublicClientApplication.builder(CLIENT_ID)
                .authority(AUTHORITY)
                .setTokenCacheAccessAspect(Manager.getTokenCache())
                .build();

        InteractiveRequestParameters params = InteractiveRequestParameters.builder(URI.create("http://localhost"))
                .prompt(Prompt.SELECT_ACCOUNT)
                .scopes(ARM_SCOPE)
                .build();

        IAuthenticationResult token = pca.acquireToken(params).get();

        var acc = pca.getAccounts().get().stream().findFirst().get();
        var tenantIds = acc.getTenantProfiles().keySet();

        var account = new Account();
        account.setUsername(acc.username());

        var tenants = tenantIds.stream().map(i -> {
            var t = new Tenant();
            t.setId(i);
            t.setTenantName(i);
            return t;
        }).collect(Collectors.toMap(Tenant::getId, Function.identity()));

        account.setTenants(tenants);

        HttpClient client = HttpClient.newHttpClient();

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create("https://management.azure.com/subscriptions?api-version=2020-01-01"))
                .header("Authorization", "Bearer " + token.accessToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to list subscriptions: " + response.body());
        }

        // Simple parsing of subscription IDs from JSON (naive, you may want to use a JSON lib)
        var subs = new ArrayList<Subscription>();
        var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.body());

        for (var sub : json.get("value")) {
            var subscription = new Subscription();
            subscription.setId(sub.get("subscriptionId").asText());
            subscription.setName(sub.get("displayName").asText());

            var tenantId = sub.get("tenantId").asText();
            tenants.get(tenantId).getSubscriptions().put(subscription.getId(), subscription);
        }

        return account;
    }
}
