package com.gjvandersloot.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import com.sun.javafx.collections.ObservableMapWrapper;
import javafx.collections.FXCollections;
import javafx.collections.ObservableMap;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class Store {
    private Map<String, Account> accounts = new HashMap<>();
}
