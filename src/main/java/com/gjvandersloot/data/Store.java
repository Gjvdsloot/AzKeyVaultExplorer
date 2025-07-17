package com.gjvandersloot.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Getter
@Setter
@Service
public class Store {
    private Map<String, Account> accounts = new HashMap<String, Account>();
}
