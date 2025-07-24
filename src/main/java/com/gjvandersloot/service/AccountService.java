package com.gjvandersloot.service;

import com.gjvandersloot.data.Account;
import com.gjvandersloot.data.Tenant;
import com.gjvandersloot.data.Subscription;
import com.gjvandersloot.data.Vault;
import com.microsoft.aad.msal4j.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Set;

@Service
public class AccountService {
    private static final String AUTHORITY = "https://login.microsoftonline.com/common";
    private static final String CLIENT_ID = "04b07795-8ddb-461a-bbee-02f9e1bf7b46";

    private static final Set<String> ARM_SCOPE = Set.of("https://management.azure.com/.default");
    private static final Set<String> VAULT_SCOPE = Set.of("https://vault.azure.net/.default");
    private static final Set<String> GRAPH_SCOPE = Set.of("https://vault.azure.net/.default");

    @Autowired
    private PublicClientApplication pca;

    public Account addAccount() throws Exception {
        InteractiveRequestParameters params = InteractiveRequestParameters.builder(URI.create("http://localhost"))
                .prompt(Prompt.SELECT_ACCOUNT)
                .scopes(ARM_SCOPE)
                .build();

        IAuthenticationResult token = pca.acquireToken(params).get();

        var acc = token.account();

        var account = new Account();
        account.setUsername(acc.username());

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

        var tenants = new HashMap<String, Tenant>();

        var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.body());

        for (var sub : json.get("value")) {
            var subscription = new Subscription();
            subscription.setId(sub.get("subscriptionId").asText());
            subscription.setName(sub.get("displayName").asText());

            var tenantId = sub.get("tenantId").asText();
            var tenant = tenants.getOrDefault(tenantId, null);

            if (tenant == null) {
                tenant = new Tenant();
                tenant.setTenantName(tenantId);
                tenant.setId(tenantId);
                tenants.put(tenantId, tenant);
            }

            tenant.getSubscriptions().put(subscription.getId(), subscription);
        }

        account.getTenants().putAll(tenants);

        return account;
    }

    public ArrayList<Vault> addKeyVaults(String subscriptionId, String accountName) throws Exception {
        var account = pca.getAccounts().get().stream().filter(a -> accountName.equals(a.username())).findFirst().get();

        var params = SilentParameters.builder(ARM_SCOPE, account).build();

        InteractiveRequestParameters interactiveParams = InteractiveRequestParameters.builder(URI.create("http://localhost"))
                .prompt(Prompt.SELECT_ACCOUNT)
                .scopes(ARM_SCOPE)
                .build();

        IAuthenticationResult token;
        try {
            token = pca.acquireTokenSilently(params).get();
        } catch(Exception e) {
            token = pca.acquireToken(interactiveParams).get();
        }

        var kvs = new ArrayList<Vault>();

        String url = String.format("https://management.azure.com/subscriptions/%s/providers/Microsoft.KeyVault/vaults?api-version=2022-07-01", subscriptionId);
        kvs = getVaultsFromApi(url, token, HttpClient.newHttpClient());
        return kvs;
    }

    private ArrayList<Vault> getVaultsFromApi(String url, IAuthenticationResult token, HttpClient client) throws IOException, InterruptedException {
        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token.accessToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to list keyvaults: " + response.body());
        }


        var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.body());

        var kvs = new ArrayList<Vault>();
        for (var kv : json.get("value")) {
            var vault = new Vault();
            vault.setVaultUri(kv.get("properties").get("vaultUri").asText());
            vault.setName(kv.get("name").asText());
            kvs.add(vault);
        }

        var nextUrl = json.get("nextLink");
        if (nextUrl != null)
            kvs.addAll(getVaultsFromApi(nextUrl.asText(), token, client));

        return kvs;
    }
}
