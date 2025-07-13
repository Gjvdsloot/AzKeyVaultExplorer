package com.gjvandersloot.service;

import javafx.application.HostServices;
import org.springframework.stereotype.Service;

@Service
public class HostedServiceProvider {
    private HostServices hostServices;

    public HostServices getHostServices() {
        return hostServices;
    }

    public void setHostServices(HostServices hostServices) {
        this.hostServices = hostServices;
    }
}
