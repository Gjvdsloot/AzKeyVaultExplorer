package com.gjvandersloot.service;

import com.microsoft.aad.msal4j.PublicClientApplication;

public class PcaProvider {
    private final PublicClientApplication pcas;

    public PcaProvider(PublicClientApplication pcas) {
        this.pcas = pcas;
    }
}
