package com.gjvandersloot.service;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.gjvandersloot.AppDataService;
import com.gjvandersloot.model.Subscription;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.*;
import java.nio.file.Files;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@Service
public class Manager {
    @Autowired
    SubscriptionService subscriptionService;

    @Autowired
    AppDataService appDataService;

    @Getter
    private ArrayList<Subscription> subscriptions;
    private final ObjectMapper mapper;

    public Manager() {
        mapper = new ObjectMapper();
        subscriptions = new ArrayList<Subscription>();
    }

    public void loadSubscriptionsFromDisk() {
        var path  = appDataService.getMainPath().resolve("subs.json");

        if (!Files.exists(path))
            return;

        var typeRef = new TypeReference<ArrayList<Subscription>>() {};

        try {
            var model = mapper.readValue(path.toFile(), typeRef);
            subscriptions.addAll(model);
        } catch (IOException e) {
            //
        }
    }

    public ArrayList<Subscription> AddAzureAccount() throws IOException, ExecutionException, InterruptedException {
        var subs = subscriptionService.addNewAccount("04b07795-8ddb-461a-bbee-02f9e1bf7b46");

        for (var sub : subs) {
            boolean exists = subscriptions.stream()
                    .anyMatch(existing -> existing.getSubscriptionId().equals(sub.getSubscriptionId()));
            if (!exists) {
                subscriptions.add(sub);
            }
        }

        saveSubscriptionsToDisk(subs);

        return subs;
    }

    private void saveSubscriptionsToDisk(ArrayList<Subscription> subs) throws IOException {
        var path = appDataService.getMainPath().resolve("subs.json");

        try {
            // Ensure parent directory exists
            Files.createDirectories(path.getParent());

            // Write JSON with pretty printing (optional)
            mapper.writerWithDefaultPrettyPrinter().writeValue(path.toFile(), subs);

        } catch (IOException e) {
            e.printStackTrace(); // Replace with logger in production
        }
    }
}
