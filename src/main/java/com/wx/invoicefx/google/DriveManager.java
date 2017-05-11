package com.wx.invoicefx.google;

import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.wx.fx.Lang;
import com.wx.fx.util.callback.SimpleCallback;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.util.log.LogHelper;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;



/**
 * // TODO: 3/1/16 Doc for all Google package Created on 14/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class DriveManager {

    public enum Action {
        LIST,
        UPDATE,
        INSERT,
        REMOVE,
        AUTHORIZE,
        GET_INFO
    }

    public enum State {
        CONNECTED,
        ACTING,
        UNREGISTERED,
        FAILED_REGISTRATION,
        FAILED
    }

    private static final Logger LOG = LogHelper.getLogger(DriveManager.class);

    private static final Preferences driveProperties = Preferences.userNodeForPackage(DriveManager.class);
    private static final ObjectProperty<Exception> serviceException = new SimpleObjectProperty<>();
    private static final StringProperty serviceStatus = new SimpleStringProperty();
    private static final ObjectProperty<State> currentState = new SimpleObjectProperty<>(State.UNREGISTERED);

    // Local cache
    private static DriveServiceHelper driveService;
    private static Userinfoplus userInfo;
    private static StringProperty currentUserProperty;

    public static void init(java.io.File dataStoreDirectory, StringProperty userProperty) throws GeneralSecurityException, IOException {
        currentUserProperty = userProperty;

        DriveServiceFactory.init(dataStoreDirectory);
    }

    public static Thread performAction(Action action, SimpleCallback callback, Object... args) {
        if (action == Action.AUTHORIZE) {
            serviceException.set(null); // Only command able to erase reset state

        } else if (!isUserRegistered()) {
            return null;
        }

        if (serviceException.get() == null) {
            Task<Void> task = new Task<Void>() {
                @Override
                protected Void call() {
                    try {
                        setCurrentState(DriveManager.State.ACTING);
                        setServiceStatus(Lang.getString("drive_status." + action.name().toLowerCase()));

                        Object[] params = executeAction(action, args);

                        setCurrentState(DriveManager.State.CONNECTED);
                        setServiceStatus("");
                        if (callback != null) {
                            Platform.runLater(() -> callback.success(params));
                        }
                    } catch (IOException e) {
                        LOG.severe("Drive action failed (" + action.name().toLowerCase() + "): [" + e.getClass().getSimpleName() + "]" + e.getMessage());
                        LOG.severe("Drive synchronisation temporarily disabled");
                        ExceptionLogger.logException(e);
                        serviceException.set(e);
                        if (isUserRegistered()) {
                            setServiceStatus(Lang.getString("drive_status.error", e.getMessage()));
                            setCurrentState(DriveManager.State.FAILED);
                        } else {
                            setServiceStatus(Lang.getString("drive_status.error_registration", e.getMessage()));
                            setCurrentState(DriveManager.State.FAILED_REGISTRATION);
                        }


                        if (callback != null) {
                            Platform.runLater(() -> callback.failure(e));
                        }
                    }

                    return null;
                }
            };
            Thread thread = new Thread(task);
            thread.start();
            return thread;
        } else {
            LOG.warning("Action ignored: " + action.name().toLowerCase());
            return null;
        }
    }

    private static void setCurrentState(State currentState) {
        Platform.runLater(() -> DriveManager.currentState.set(currentState));
    }

    public static void setServiceStatus(String serviceStatus) {
        Platform.runLater(() -> DriveManager.serviceStatus.set(serviceStatus));
    }

    private static Object[] executeAction(Action action, Object[] args) throws IOException {
        switch (action) {
            case INSERT:
                insertFile((java.io.File) args[0]);
                return null;
            case UPDATE:
                return new Object[]{updateLocal((java.io.File) args[0])};
            case LIST:
                return new Object[]{getDriveService().listFiles()};
            case REMOVE:
                getDriveService().removeFile((String) args[0]);
                return null;
            case AUTHORIZE:
                authorize();
                return null;
            case GET_INFO:
                return new Object[]{getUserInfo()};
            default:
                throw new AssertionError();
        }
    }

    public static Preferences getDriveProperties() {
        return driveProperties;
    }

//    public static void showExceptionAlert() {
//        // TODO: 11.05.17 Move this elsewhere
//        if (serviceException.get() != null) {
//            int choice = AlertBuilder.error()
//                    .key("errors.google", serviceException.get().getMessage())
//                    .button("errors.google.retry")
//                    .button("errors.google.disconnect")
//                    .button(ButtonType.CANCEL)
//                    .show();
//
//            if (choice == 0) {
//                LOG.info("Attempting a reconnection");
//                performAction(Action.GET_INFO, new SimpleCallback() {
//                    @Override
//                    public void success(Object... objects) {
//                    }
//
//                    @Override
//                    public void failure(Throwable ex) {
//                        showExceptionAlert();
//                    }
//                });
//
//
//            } else if (choice == 1) {
//                removeCurrentUser();
//            }
//        }
//    }

    public static void removeCurrentUser() {
        LOG.info("Removing current Google user");
        serviceException.set(null);
        setCurrentState(State.UNREGISTERED);
        currentUserProperty.set("");
        try {
            for (String key : driveProperties.keys()) {
                driveProperties.remove(key);
            }
        } catch (BackingStoreException e) {
            ExceptionLogger.logException(e);
            LOG.warning("Couldn't remove Drive properties: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
        }
        DriveServiceFactory.removeCredentials();
        userInfo = null;
        driveService = null;
    }

    public static boolean isUserRegistered() {
        return currentUserProperty.isNotEmpty().get();
    }

    public static ReadOnlyStringProperty currentUserProperty() {
        return currentUserProperty;
    }

    public static ReadOnlyObjectProperty<Exception> serviceExceptionProperty() {
        return serviceException;
    }

    public static ReadOnlyStringProperty serviceStatusProperty() {
        return serviceStatus;
    }

    public static ReadOnlyObjectProperty<State> currentStateProperty() {
        return currentState;
    }

    public static BooleanBinding serviceOnlineBinding() {
        return serviceException.isNull().and(currentUserProperty.isNotEmpty());
    }

    private static boolean updateLocal(java.io.File file) throws IOException {
        String id = findFileId(file);

        if (id == null) {
            LOG.info("(update - " + file.getName() + ") File not present on Drive, action cancelled");
            return false;
        }

        long localTimestamp = driveProperties.getLong(file.getName() + ".timestamp", 0);
        long driveTimestamp = getDriveTimeStamp(id);
        if (localTimestamp >= driveTimestamp) {
            LOG.info("(update - " + file.getName() + ") Local file more recent, action cancelled");
            return false;
        }

        LOG.info("(update - " + file.getName() + ") Updating from Drive...");
        getDriveService().downloadFile(id, file);
        driveProperties.putLong(file.getName() + ".timestamp", driveTimestamp);
        return true;
    }

    private static void insertFile(java.io.File file) throws IOException {
        String id = findFileId(file);

        File driveFile;
        if (id == null) {
            LOG.info("(upload - " + file.getName() + ") Inserting new file");
            driveFile = getDriveService().insertFile(file);
        } else {
            LOG.info("(upload - " + file.getName() + ") Updating Drive file");
            driveFile = getDriveService().updateFile(file, id);
        }

        driveProperties.put(file.getName() + ".id", driveFile.getId());
        driveProperties.putLong(file.getName() + ".timestamp", driveFile.getModifiedTime().getValue());
    }

    private static synchronized Userinfoplus getUserInfo() throws IOException {
        if (userInfo == null) {
            userInfo = DriveServiceFactory.getUserInfo(false);
            LOG.info("Loaded user info for: " + userInfo.getName());
        }

        return userInfo;
    }

    private static synchronized DriveServiceHelper getDriveService() throws IOException {
        if (driveService == null) {
            driveService = new DriveServiceHelper(DriveServiceFactory.getDriveService(false));
        }
        return driveService;
    }

    private static void authorize() throws IOException {
        LOG.info("Connecting new Google user");
        userInfo = DriveServiceFactory.getUserInfo(true);

        if (userInfo != null) {
            currentUserProperty.set(userInfo.getName());
        } else {
            LOG.severe("No info returned");
        }
    }

    private static String findFileId(java.io.File file) throws IOException {
        String id = driveProperties.get(file.getName() + ".id", "");
        if (!id.isEmpty()) {
            return id;
        }

        id = getDriveService().findIdByName(file.getName());
        driveProperties.put(file.getName() + ".id", id);

        return id;
    }

    private static long getDriveTimeStamp(String id) throws IOException {
        File file = getDriveService().getFile(id);
        if (file != null) {
            return file.getModifiedTime().getValue();
        } else {
            return 0;
        }
    }


}
