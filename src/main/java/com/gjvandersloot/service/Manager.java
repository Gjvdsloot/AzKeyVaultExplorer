package com.gjvandersloot.service;

import com.gjvandersloot.model.Subscription;
import lombok.Getter;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;

import java.io.IOException;
import java.util.ArrayList;
import java.util.concurrent.ExecutionException;

@Service
public class Manager {
    @Autowired
    SubscriptionService subscriptionService;

    @Getter
    private ArrayList<Subscription> subscriptions;

    public Manager() {
        subscriptions = new ArrayList<Subscription>();
    }

    public void init() {

    }

    public ArrayList<Subscription> AddSubscription() throws IOException, ExecutionException, InterruptedException {
            var subscription = subscriptionService.newSubscription("6a5a594c-91d7-45f9-a996-d800a7dc5343");
            subscriptions.addAll(subscription);
            return subscription;
    }


    //    public void setSubscriptions(ArrayList<Subscription> subscriptions) {
//        this.subscriptions = subscriptions;
//    }
}
