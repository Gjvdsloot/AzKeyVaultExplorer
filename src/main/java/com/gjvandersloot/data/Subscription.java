package com.gjvandersloot.data;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Subscription {
    private String name;
    private String id;
    private ArrayList<Vault> vaults;
}
