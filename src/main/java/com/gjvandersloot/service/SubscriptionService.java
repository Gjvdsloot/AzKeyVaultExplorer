package com.gjvandersloot.service;

import com.azure.identity.InteractiveBrowserCredential;
import com.azure.identity.implementation.PersistentTokenCacheImpl;
import com.gjvandersloot.model.KeyVault;
import com.gjvandersloot.model.Subscription;
import com.microsoft.aad.msal4j.*;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.net.URI;
import java.net.http.HttpClient;
import java.net.http.HttpRequest;
import java.net.http.HttpResponse;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutionException;

@Service
public class SubscriptionService {

    private static final String AUTHORITY = "https://login.microsoftonline.com/common";

    Set<String> ARM_SCOPE = Set.of("https://management.azure.com/.default");
    Set<String> VAULT_SCOPE = Set.of("https://vault.azure.net/.default");

    public ArrayList<Subscription> addNewAccount(String clientId) throws IOException, ExecutionException, InterruptedException {
        PublicClientApplication pca;
        pca = PublicClientApplication.builder(clientId)
                .authority(AUTHORITY)
                .setTokenCacheAccessAspect(Manager.getTokenCache())
                .build();

        InteractiveRequestParameters params = InteractiveRequestParameters.builder(URI.create("http://localhost"))
                .prompt(Prompt.SELECT_ACCOUNT)
                .scopes(ARM_SCOPE)
                .build();

        IAuthenticationResult token = pca.acquireToken(params).get();

        var accountId = pca.getAccounts().get().stream().findFirst().get().homeAccountId();

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

            var subscriptionId = sub.get("subscriptionId").asText();
            subscription.setSubscriptionId(subscriptionId);
            subscription.setName(sub.get("displayName").asText());
            subscription.setState(sub.get("state").asText());
            subscription.setTenantId(sub.get("tenantId").asText());
            subscription.setAccountId(accountId);

            var keyVaults = listKeyVaults(subscriptionId, clientId, pca);
            subscription.setKeyVaults(keyVaults);

            subs.add(subscription);
        }

        return subs;
    }

    private void test() {
        var credential = new InteractiveBrowserCredential(clientId: "your-client-id");

// 2) Create an ArmClient
        var armClient = new ArmClient(credential);

// 3) List tenants
        await foreach (var tenantResource in armClient.GetTenantsAsync())
        {
            Console.WriteLine($"Tenant ID: {tenantResource.Data.TenantId}");
        }
    }

    public ArrayList<KeyVault> listKeyVaults(String subscriptionId, String clientId, PublicClientApplication pca) throws ExecutionException, InterruptedException, IOException {
        var account = pca.getAccounts().get().stream().findFirst().get();

        var params = SilentParameters.builder(ARM_SCOPE, account).build();

        IAuthenticationResult token = pca.acquireTokenSilently(params).get();

        HttpClient client = HttpClient.newHttpClient();

        String url = String.format("https://management.azure.com/subscriptions/%s/providers/Microsoft.KeyVault/vaults?api-version=2022-07-01", subscriptionId);

        HttpRequest request = HttpRequest.newBuilder()
                .uri(URI.create(url))
                .header("Authorization", "Bearer " + token.accessToken())
                .GET()
                .build();

        HttpResponse<String> response = client.send(request, HttpResponse.BodyHandlers.ofString());

        if (response.statusCode() != 200) {
            throw new RuntimeException("Failed to list keyvaults: " + response.body());
        }

        var kvs = new ArrayList<KeyVault>();
        var json = new com.fasterxml.jackson.databind.ObjectMapper().readTree(response.body());

        for (var kv : json.get("value")) {
            var keyVault = new KeyVault();
            keyVault.setVaultUri(kv.get("properties").get("vaultUri").asText());
            keyVault.setName(kv.get("name").asText());
            kvs.add(keyVault);
        }
        return kvs;
    }

    private void listSecrets(String keyVaultUri) {

    }
}