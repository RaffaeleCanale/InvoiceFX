package com.wx.invoicefx.google;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.util.log.LogHelper;
import javafx.beans.property.StringProperty;

import java.io.IOException;
import java.io.InputStream;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;


/**
 * High level Drive manager
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class DriveManager {

    public enum Action {
        LIST,
        GET_STREAM,
        INSERT,
        REMOVE,
        AUTHORIZE,
        GET_INFO
    }

    private static final Logger LOG = LogHelper.getLogger(DriveManager.class);
    private static final Preferences driveProperties = Preferences.userNodeForPackage(DriveManager.class);

    // Local cache
    private static DriveServiceHelper driveService;
    private static Userinfoplus userInfo;
    private static StringProperty currentUserProperty;




    public static void init(java.io.File dataStoreDirectory, StringProperty userProperty) throws GeneralSecurityException, IOException {
        currentUserProperty = userProperty;

        DriveServiceFactory.init(dataStoreDirectory);
    }

    public static boolean isInit() {
        return currentUserProperty != null && DriveServiceFactory.isInit();
    }


    public static void executeClearFiles() throws IOException {
        FileList files = executeListFiles();

        for (File file : files.getFiles()) {
            getDriveService().removeFile(file.getId());
        }

        try {
            String[] keys = driveProperties.keys();
            for (String key : keys) {
                if (key.endsWith(".id")) {
                    driveProperties.remove(key);
                }
            }
        } catch (BackingStoreException e) {
            throw new IOException(e);
        }

    }

    public static FileList executeListFiles() throws IOException {
        ensureIsInit();
        ensureIsRegistered();

        return getDriveService().listFiles();
    }

    public static void executeRemoveFile(String filename) throws IOException {
        ensureIsInit();
        ensureIsRegistered();

        String id = findFileId(filename);

        if (id != null) {
            LOG.finer("(remove - " + filename + ")");
            getDriveService().removeFile(id);
        }
    }

    public static void executeAuthorize() throws IOException {
        ensureIsInit();

        LOG.info("Connecting new Google user");
        userInfo = DriveServiceFactory.getUserInfo(true);

        if (userInfo != null) {
            currentUserProperty.set(userInfo.getName());
        } else {
            LOG.severe("No info returned");
        }
    }

    public static void executeInsertFile(String filename, InputStream in) throws IOException {
        ensureIsInit();
        ensureIsRegistered();

        String id = findFileId(filename);

        File driveFile;
        if (id == null) {
            LOG.finer("(insert - " + filename + ")");
            driveFile = getDriveService().insertFile(null, filename, in);
        } else {
            LOG.finer("(update - " + filename + ")");
            driveFile = getDriveService().updateFile(id, in);
        }

        driveProperties.put(filename + ".id", driveFile.getId());
    }

    public static synchronized Userinfoplus executeGetUserInfo() throws IOException {
        ensureIsRegistered();

        if (userInfo == null) {
            userInfo = DriveServiceFactory.getUserInfo(false);
            LOG.fine("Loaded user info for: " + userInfo.getName());
        }

        return userInfo;
    }

    public static InputStream executeGetFileStream(String filename) throws IOException {
        ensureIsRegistered();
        ensureIsInit();

        String id = findFileId(filename);

        if (id == null) {
            return null;
        }

        return getDriveService().downloadFile(id);
    }

    public static void removeCurrentUser() {
        LOG.info("Removing current Google user");

        currentUserProperty.set("");
        try {
            for (String key : driveProperties.keys()) {
                driveProperties.remove(key);
            }
        } catch (BackingStoreException e) {
            ExceptionLogger.logException(e);
        }
        DriveServiceFactory.removeCredentials();
        userInfo = null;
        driveService = null;
    }

    public static String getCurrentUser() {
        return currentUserProperty.get();
    }

    public static boolean isUserRegistered() {
        ensureIsInit();
        return currentUserProperty.isNotEmpty().get();
    }

    public static Preferences getDriveProperties() {
        return driveProperties;
    }

    private static synchronized DriveServiceHelper getDriveService() throws IOException {
        if (driveService == null) {
            driveService = new DriveServiceHelper(DriveServiceFactory.getDriveService(false));
        }
        return driveService;
    }

    private static String findFileId(String filename) throws IOException {
        String id = driveProperties.get(filename + ".id", "");
        if (!id.isEmpty()) {
            return id;
        }

        id = getDriveService().findIdByName(filename);
        if (id == null) {
            return null;
        }

        driveProperties.put(filename + ".id", id);

        return id;
    }

    private static void ensureIsInit() {
        if (!isInit()) {
            throw new IllegalStateException("Manager is not initialized");
        }
    }

    private static void ensureIsRegistered() {
        if (!isUserRegistered()) {
            throw new IllegalStateException("User is not registered");
        }
    }
}
