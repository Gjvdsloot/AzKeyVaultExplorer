package com.gjvandersloot;

import javafx.collections.FXCollections;
import javafx.collections.ObservableList;

public class ConnectViewModel
{
    private final ObservableList<Object> secretPeople = FXCollections.observableArrayList();

    public ObservableList<Object> getSecretPeople() {
        return null;
    }

    public void loadSecretPeople() {
//        secretPeople.setAll(
//                new string("Agent 47", "hitman@ica.com"),
//                new Person("Ethan Hunt", "imf@gov.us"),
//                new Person("Natasha Romanoff", "black.widow@shield.org")
//        );
    }
}
