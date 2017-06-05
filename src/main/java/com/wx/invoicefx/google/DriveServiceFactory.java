package com.wx.invoicefx.google;

import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.Oauth2Scopes;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.wx.invoicefx.App;
import com.wx.invoicefx.util.DesktopUtils;
import com.wx.util.Format;
import com.wx.util.log.LogHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.security.GeneralSecurityException;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

class DriveServiceFactory {

    private static final Logger LOG = LogHelper.getLogger(DriveServiceFactory.class);


    private static String userId;
    private static java.io.File dataDirectory;

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY = JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     */
    private static final List<String> SCOPES = Arrays.asList(DriveScopes.DRIVE_FILE, Oauth2Scopes.USERINFO_PROFILE);

    public static void init(java.io.File dataDirectory) throws GeneralSecurityException, IOException {
        init(dataDirectory, "user");
    }

    public static void init(java.io.File dataDirectory, String userId) throws GeneralSecurityException, IOException {
        DriveServiceFactory.userId = userId;
        DriveServiceFactory.dataDirectory = dataDirectory;

        HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
        DATA_STORE_FACTORY = new FileDataStoreFactory(dataDirectory);
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     *
     * @throws IOException
     */
    public static Credential getCredential(boolean authorizeIfNeeded) throws IOException {
        ensureIsInit();

        // Load client secrets.
        InputStream in =
                DriveServiceFactory.class.getResourceAsStream("/google/client_secret_926831742589-1s03oj41qb0k4kfcibhhpp29bcrbn3a9.apps.googleusercontent.com.json");
        GoogleClientSecrets clientSecrets =
                GoogleClientSecrets.load(JSON_FACTORY, new InputStreamReader(in));

        // Build flow and trigger user authorization request.
        GoogleAuthorizationCodeFlow flow =
                new GoogleAuthorizationCodeFlow.Builder(
                        HTTP_TRANSPORT, JSON_FACTORY, clientSecrets, SCOPES)
                        .setDataStoreFactory(DATA_STORE_FACTORY)
                        .setAccessType("offline")
                        .build();

        Credential credential = loadCredential(flow);
        if (credential == null) {
            if (authorizeIfNeeded) {
                credential = authorize(flow);
            } else {
                throw new IOException("Unauthorized user");
            }
        }

        assert credential != null;
        LOG.info("Credential loaded, expires in " + Format.formatTime(credential.getExpirationTimeMilliseconds()));
        return credential;
    }

    private static Credential loadCredential(GoogleAuthorizationCodeFlow flow) throws IOException {
        Credential credential = flow.loadCredential(userId);
        if (credential != null
                && (credential.getRefreshToken() != null || credential.getExpiresInSeconds() > 60)) {
            return credential;
        }
        return null;
    }

    private static Credential authorize(GoogleAuthorizationCodeFlow flow) throws IOException {
        VerificationCodeReceiver receiver = new CustomCodeReceiver();

        try {
            // open in browser
            String redirectUri = receiver.getRedirectUri();
            AuthorizationCodeRequestUrl authorizationUrl =
                    flow.newAuthorizationUrl().setRedirectUri(redirectUri);
            DesktopUtils.openUrl(authorizationUrl.build());
            // receive authorization code and exchange it for an access token
            String code = receiver.waitForCode();
            TokenResponse response = flow.newTokenRequest(code).setRedirectUri(redirectUri).execute();
            // store credential and return it
            return flow.createAndStoreCredential(response, userId);
        } finally {
            receiver.stop();
        }
    }

    /**
     * Build and return an authorized Drive client service.
     *
     * @return an authorized Drive client service
     *
     * @throws IOException
     */
    public static Drive getDriveService(boolean authorize) throws IOException {
        ensureIsInit();

        Credential credential = getCredential(authorize);

        return new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(App.APPLICATION_NAME)
                .build();
    }

    public static void removeCredentials() {
        ensureIsInit();

        java.io.File[] files = dataDirectory.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                file.delete();
            }
        }
    }

    public static Userinfoplus getUserInfo(boolean authorize) throws IOException {
        ensureIsInit();

        Oauth2 auth = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential(authorize))
                .setApplicationName(App.APPLICATION_NAME)
                .build();
        return auth.userinfo().get().execute();
    }



    private static void ensureIsInit() {
        if (userId == null || dataDirectory == null || HTTP_TRANSPORT == null || DATA_STORE_FACTORY == null) {
            throw new IllegalStateException("Must init first");
        }
    }

}