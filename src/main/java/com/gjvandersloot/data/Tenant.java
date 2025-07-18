package com.gjvandersloot.data;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class Tenant {
    private String id;
    private String tenantName;
    private Map<String, Subscription> subscriptions = new HashMap<>();
}
