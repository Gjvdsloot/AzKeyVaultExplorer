package com.gjvandersloot.service;

public class Manager {
    public Manager() {
//        var vaultUrl = "https://gjvandersloot.vault.azure.net/";
//
//        InteractiveBrowserCredential credential = new InteractiveBrowserCredentialBuilder()
//                .redirectUrl("http://localhost") // default works for most cases
//                .build();
//
//        SecretClient secretClient = new SecretClientBuilder()
//                .vaultUrl(vaultUrl)
//                .credential(credential)
//                .buildClient();
    }

    public void sayHello() {
        System.out.println("Hello world!");
    }

    public void addSubscription() {
//        TokenCredential credential = new DefaultAzureCredentialBuilder().build();
//
//        // Use empty profile to allow listing
//        AzureProfile dummyProfile = new AzureProfile(AzureEnvironment.AZURE);
//
//        // Authenticate temporarily just to get subscription
//        var tempAzure = AzureResourceManager
//                .authenticate(credential, dummyProfile);
//
//        Subscription firstSub = tempAzure.subscriptions().list().stream()
//                .findFirst()
//                .orElseThrow(() -> new RuntimeException("No subscription found."));
//
//        String subscriptionId = firstSub.subscriptionId();
//        String tenantId = firstSub.innerModel().tenantId(); // tenantId may be null in some cases, fallback from CLI if needed
//
//        System.out.println("Found subscription: " + firstSub.displayName());
//        System.out.println("Subscription ID: " + subscriptionId);
//        System.out.println("Tenant ID: " + tenantId);
//
//        // Now build a proper profile
//        AzureProfile fullProfile = new AzureProfile(tenantId, subscriptionId, AzureEnvironment.AZURE);
//
//        // Re-authenticate with full context
//        AzureResourceManager azure = AzureResourceManager
//                .authenticate(credential, fullProfile)
//                .withSubscription(subscriptionId);
//
//        System.out.println("Azure authenticated with full profile.");
    }
}
