package app.google;

import app.App;
import app.config.Config;
import com.google.api.client.auth.oauth2.AuthorizationCodeRequestUrl;
import com.google.api.client.auth.oauth2.Credential;
import com.google.api.client.auth.oauth2.TokenResponse;
import com.google.api.client.extensions.java6.auth.oauth2.VerificationCodeReceiver;
import com.google.api.client.googleapis.auth.oauth2.GoogleAuthorizationCodeFlow;
import com.google.api.client.googleapis.auth.oauth2.GoogleClientSecrets;
import com.google.api.client.googleapis.javanet.GoogleNetHttpTransport;
import com.google.api.client.http.FileContent;
import com.google.api.client.http.HttpTransport;
import com.google.api.client.json.JsonFactory;
import com.google.api.client.json.jackson2.JacksonFactory;
import com.google.api.client.util.store.FileDataStoreFactory;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.DriveScopes;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.drive.model.ParentReference;
import com.google.api.services.oauth2.Oauth2;
import com.google.api.services.oauth2.Oauth2Scopes;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.wx.io.Accessor;
import com.wx.util.Format;
import com.wx.util.log.LogHelper;

import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Logger;

public class DriveFunctions {

    private static final Logger LOG = LogHelper.getLogger(DriveFunctions.class);

    private static String userId = "user";

    /**
     * Directory to store user credentials for this application.
     */
    private static final java.io.File DATA_STORE_DIR =
            Config.getConfigFile("credentials");

    /**
     * Global instance of the {@link FileDataStoreFactory}.
     */
    private static FileDataStoreFactory DATA_STORE_FACTORY;

    /**
     * Global instance of the JSON factory.
     */
    private static final JsonFactory JSON_FACTORY =
            JacksonFactory.getDefaultInstance();

    /**
     * Global instance of the HTTP transport.
     */
    private static HttpTransport HTTP_TRANSPORT;

    /**
     * Global instance of the scopes required by this quickstart.
     */
    private static final List<String> SCOPES =
            Arrays.asList(DriveScopes.DRIVE_FILE, Oauth2Scopes.USERINFO_PROFILE);

    static {
        try {
            HTTP_TRANSPORT = GoogleNetHttpTransport.newTrustedTransport();
            DATA_STORE_FACTORY = new FileDataStoreFactory(DATA_STORE_DIR);
        } catch (Throwable t) {
            // TODO: 14.06.16 Not good!
            t.printStackTrace();
            System.exit(1);
        }
    }

    /**
     * Creates an authorized Credential object.
     *
     * @return an authorized Credential object.
     *
     * @throws IOException
     */
    public static Credential getCredential(boolean autoAuthorize) throws IOException {
        // Load client secrets.
        InputStream in =
                DriveFunctions.class.getResourceAsStream("/google/client_secret_926831742589-1s03oj41qb0k4kfcibhhpp29bcrbn3a9.apps.googleusercontent.com.json");
        if (in == null) throw new IOException("Internal error: Client secret not found");

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
            if (autoAuthorize) {
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
            App.openUrl(authorizationUrl.build());
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
        Credential credential = getCredential(authorize);

        return new Drive.Builder(
                HTTP_TRANSPORT, JSON_FACTORY, credential)
                .setApplicationName(App.APPLICATION_NAME)
                .build();
    }

    public static void removeCredentials() {
        java.io.File[] files = DATA_STORE_DIR.listFiles();
        if (files != null) {
            for (java.io.File file : files) {
                file.delete();
            }
        }
    }

    public static Userinfoplus getUserInfo(boolean authorize) throws IOException {
        Oauth2 auth = new Oauth2.Builder(HTTP_TRANSPORT, JSON_FACTORY, getCredential(authorize))
                .setApplicationName(App.APPLICATION_NAME)
                .build();
        return auth.userinfo().get().execute();
    }

    public static File insertFile(Drive service, java.io.File fileContent) throws IOException {
        return insertFile(service, null, fileContent);
    }

    public static File updateFile(Drive service, java.io.File fileContent, String id) throws IOException {
        // First retrieve the file from the API.
        File file = service.files().get(id).execute();
        file.setTitle(fileContent.getName());

        // File's new content.
        FileContent mediaContent = new FileContent(null, fileContent);

        // Send the request to the API.
        return service.files().update(id, file, mediaContent).execute();
    }

    public static FileList listFiles(Drive service) throws IOException {
        return service.files().list().execute();
    }

    public static File insertFile(Drive service, String parentId, java.io.File fileContent) throws IOException {
        // File's metadata.
        File body = new File();
        body.setTitle(fileContent.getName());

        // Set the parent folder.
        if (parentId != null && parentId.length() > 0) {
            body.setParents(
                    Arrays.asList(new ParentReference().setId(parentId)));
        }

        // File's content.
        FileContent mediaContent = new FileContent(null, fileContent);
        return service.files().insert(body, mediaContent).execute();
    }

    public static File getFile(Drive service, String id) throws IOException {
        return service.files().get(id).execute();
    }

    public static void removeFile(Drive service, String id) throws IOException {
        service.files().delete(id).execute();
    }

    public static InputStream downloadFile(Drive service, String id) throws IOException {
        return service.files().get(id).executeMediaAsInputStream();
    }

    public static void downloadFile(Drive service, String id, java.io.File destination) throws IOException {
        try (Accessor accessor = new Accessor()
                .setIn(downloadFile(service, id))
                .setOut(destination, false)) {
            accessor.pourInOut();
        }
    }

    public static void printFiles(Drive service) throws IOException {
        FileList result = service.files().list()
                .setMaxResults(10)
                .execute();
        List<File> files = result.getItems();
        if (files == null || files.size() == 0) {
            System.out.println("No files found.");
        } else {
            System.out.println("Files:");
            for (File file : files) {
                System.out.printf("%s (%s)\n", file.getTitle(), file.getId());
            }
        }
    }

}