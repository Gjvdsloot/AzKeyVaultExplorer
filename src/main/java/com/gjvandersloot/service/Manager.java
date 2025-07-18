package com.gjvandersloot.service;

import com.azure.identity.implementation.PersistentTokenCacheImpl;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gjvandersloot.AppDataService;
import com.gjvandersloot.model.KeyVault;
import com.gjvandersloot.model.Subscription;
import com.microsoft.aad.msal4j.PublicClientApplication;
import com.microsoft.aad.msal4jextensions.PersistenceSettings;
import com.microsoft.aad.msal4jextensions.PersistenceTokenCacheAccessAspect;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Set;
import java.util.concurrent.ExecutionException;

public class Manager {
    public static final String CLIENT_ID = "04b07795-8ddb-461a-bbee-02f9e1bf7b46";
//    @Autowired
//    SubscriptionService subscriptionService;
//
//    @Autowired
//    AppDataService appDataService;

    @Getter
    private ArrayList<Subscription> subscriptions;
    private final ObjectMapper mapper;

    Set<String> VAULT_SCOPE = Set.of("https://vault.azure.net/.default");
    private static final String AUTHORITY = "https://login.microsoftonline.com/common";

    public Manager() {
        mapper = new ObjectMapper();
        subscriptions = new ArrayList<Subscription>();
    }

//    public void loadSubscriptionsFromDisk() {
//        var path  = appDataService.getMainPath().resolve("subs.json");
//
//        if (!Files.exists(path))
//            return;
//
//        var typeRef = new TypeReference<ArrayList<Subscription>>() {};
//
//        try {
//            var model = mapper.readValue(path.toFile(), typeRef);
//            subscriptions.addAll(model);
//        } catch (IOException e) {
//            //
//        }
//    }

//    public ArrayList<Subscription> AddAzureAccount() throws IOException, ExecutionException, InterruptedException {
//        var subs = subscriptionService.addNewAccount(CLIENT_ID);
//
//        for (var sub : subs) {
//            boolean exists = subscriptions.stream()
//                    .anyMatch(existing -> existing.getSubscriptionId().equals(sub.getSubscriptionId()));
//            if (!exists) {
//                subscriptions.add(sub);
//            }
//        }
//
//        saveSubscriptionsToDisk(subs);
//
//        return subs;
//    }

//    public ArrayList<KeyVault> getKeyVaultsForSubscriptionId(String id) throws IOException, ExecutionException, InterruptedException {
//        var sub = subscriptions.stream().filter(s -> id.equals(s.getSubscriptionId())).findFirst().get();
//
//        if (!sub.getKeyVaults().isEmpty())
//            return sub.getKeyVaults();
//
//        var pca = PublicClientApplication.builder(CLIENT_ID)
//                .authority("https://login.microsoftonline.com/common")
//                .setTokenCacheAccessAspect(getTokenCache())
//                .build();
//
//        return subscriptionService.listKeyVaults(id, CLIENT_ID, pca);
//    }

    public static PersistenceTokenCacheAccessAspect getTokenCache() throws IOException {
        Path cacheDir = Paths.get(System.getProperty("user.home"), ".msalcache");
        PersistenceSettings settings = PersistenceSettings
                .builder("msal.cache", cacheDir)
                // optional: .setLockRetry(500, 50)
                .build();

        return new PersistenceTokenCacheAccessAspect(settings);
    }

//    private void saveSubscriptionsToDisk(ArrayList<Subscription> subs) throws IOException {
//        var path = appDataService.getMainPath().resolve("subs.json");
//
//        var ss = new ArrayList<Subscription>();
//        for (var s : subs) {
//            ss.add(s.copyWithoutVaults());
//        }
//
//        try {
//            // Ensure parent directory exists
//            Files.createDirectories(path.getParent());
//
//            // Write JSON with pretty printing (optional)
//            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), ss);
//        } catch (IOException e) {
//            e.printStackTrace(); // Replace with logger in production
//        }
//    }
}
