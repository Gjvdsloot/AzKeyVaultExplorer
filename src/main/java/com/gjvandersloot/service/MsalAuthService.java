package com.gjvandersloot.service;

import com.microsoft.aad.msal4j.*;
import org.springframework.stereotype.Service;

import java.io.*;
import java.net.URI;
import java.util.Arrays;
import java.util.Set;
import java.util.function.Consumer;

@Service
public class MsalAuthService {
//    private static final String CLIENT_ID = "6a5a594c-91d7-45f9-a996-d800a7dc5343";
    private static final String CLIENT_ID = "04b07795-8ddb-461a-bbee-02f9e1bf7b46";
//    private static final String AUTHORITY = "https://login.microsoftonline.com/77c437ca-1810-4f73-bcd9-a99ce83ce14f/"; // common for multi-tenant
    private static final String AUTHORITY = "https://login.microsoftonline.com/common/"; // common for multi-tenant
    private static final Set<String> SCOPES = Set.of(
            "https://vault.azure.net/user_impersonation",
            "openid", "profile", "offline_access"
    );

    private static final String TOKEN_CACHE_FILE = "msal_cache.json";

    public static void main(String[] args) throws Exception {
        ITokenCacheAccessAspect persistence = new FileTokenCache(TOKEN_CACHE_FILE);
        PublicClientApplication pca = PublicClientApplication.builder(CLIENT_ID)
                .authority(AUTHORITY)
                .setTokenCacheAccessAspect(persistence)
                .build();

        IAuthenticationResult result = null;
        result = pca.acquireToken(InteractiveRequestParameters
                        .builder(new URI("http://localhost"))
                        .scopes(SCOPES)
                        .prompt(Prompt.SELECT_ACCOUNT)
                        .build())
                .get();

//        // Try silent token acquisition (from cache)
//        IAuthenticationResult result = null;
//        try {
//            result = pca.acquireTokenSilently(SilentParameters.builder(SCOPES).build()).get();
//            System.out.println("Token acquired silently from cache.");
//        } catch (ExecutionException e) {
//            System.out.println("Silent token acquisition failed, requesting device code flow...");
//            // Fall back to device code flow
//            result = pca.acquireToken(InteractiveRequestParameters
//                            .builder(new URI("http://localhost"))
//                            .scopes(SCOPES)
//                            .prompt(Prompt.SELECT_ACCOUNT)
//                            .build())
//                    .get();
//        }

        System.out.println("Access Token: " + result.accessToken());
        // Use this token in Azure SDK or REST calls...
    }

    private static Consumer<DeviceCode> deviceCodeCallback() {
        return (DeviceCode deviceCode) -> {
            System.out.println(deviceCode.message());
        };
    }

    // Helper class to persist token cache to file
    static class FileTokenCache implements ITokenCacheAccessAspect {

        private final File file;

        FileTokenCache(String fileName) {
            this.file = new File(fileName);
        }

        @Override
        public void beforeCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
            if (file.exists()) {
                try (InputStream is = new FileInputStream(file)) {
                    byte[] data = is.readAllBytes();
                    iTokenCacheAccessContext.tokenCache().deserialize(Arrays.toString(data));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }

        @Override
        public void afterCacheAccess(ITokenCacheAccessContext iTokenCacheAccessContext) {
            if (iTokenCacheAccessContext.hasCacheChanged()) {
                try (OutputStream os = new FileOutputStream(file)) {
                    os.write(iTokenCacheAccessContext.tokenCache().serialize().getBytes());
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
    }
}
