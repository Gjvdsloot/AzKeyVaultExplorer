package com.gjvandersloot.data;

import lombok.Getter;
import lombok.Setter;

@Getter @Setter
public class Credentials {
    private AuthType authType;

    // PCA
    private String accountName;

    // Attached
    private String clientId;
    private String tenantId;

    private String secret;

    private String certificatePath;
    private String certificatePassword;
}
