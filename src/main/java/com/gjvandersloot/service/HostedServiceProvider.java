package com.gjvandersloot.service;

import javafx.application.HostServices;
import lombok.Getter;
import lombok.Setter;
import org.springframework.stereotype.Service;

@Setter
@Getter
@Service
public class HostedServiceProvider {
    private HostServices hostServices;
}
