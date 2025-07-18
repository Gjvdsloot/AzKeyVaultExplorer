package com.gjvandersloot.data;

import java.util.ArrayList;
import lombok.Getter;
import lombok.Setter;
import net.minidev.json.annotate.JsonIgnore;

@Getter
@Setter
public class Subscription {
    private String name;
    private String id;
    @JsonIgnore
    private ArrayList<Vault> vaults;
}
