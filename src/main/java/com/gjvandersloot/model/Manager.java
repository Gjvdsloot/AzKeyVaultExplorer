package com.gjvandersloot.model;

import java.util.ArrayList;

public class Manager {
    private ArrayList<Subscription> subscriptions;

    public ArrayList<Subscription> getSubscriptions() {
        return subscriptions;
    }

    public void setSubscriptions(ArrayList<Subscription> subscriptions) {
        this.subscriptions = subscriptions;
    }
}
