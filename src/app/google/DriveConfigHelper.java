package app.google;

import app.config.Config;
import app.util.ExceptionLogger;
import app.util.gui.AlertBuilder;
import com.google.api.services.drive.Drive;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.google.api.services.oauth2.model.Userinfoplus;
import com.wx.fx.Lang;
import com.wx.fx.util.callback.SimpleCallback;
import com.wx.util.log.LogHelper;
import javafx.application.Platform;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.*;
import javafx.concurrent.Task;
import javafx.scene.control.ButtonType;

import java.io.IOException;
import java.util.logging.Logger;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;

import static app.config.preferences.properties.LocalProperty.DRIVE_CURRENT_USER;


/**
 * // TODO: 3/1/16 Doc for all Google package Created on 14/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class DriveConfigHelper {

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

    private static final Logger LOG = LogHelper.getLogger(DriveConfigHelper.class);

    private static final Preferences driveProperties = Preferences.userNodeForPackage(DriveConfigHelper.class);
    private static final ObjectProperty<Exception> serviceException = new SimpleObjectProperty<>();
    private static final StringProperty serviceStatus = new SimpleStringProperty();
    private static final StringProperty currentUserProperty = Config.localPreferences().stringProperty(DRIVE_CURRENT_USER);
    private static final ObjectProperty<State> currentState = new SimpleObjectProperty<>(State.UNREGISTERED);

    // Local cache
    private static Drive driveService;
    private static Userinfoplus userInfo;

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
                        setCurrentState(DriveConfigHelper.State.ACTING);
                        setServiceStatus(Lang.getString("drive_status." + action.name().toLowerCase()));

                        Object[] params = executeAction(action, args);

                        setCurrentState(DriveConfigHelper.State.CONNECTED);
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
                            setCurrentState(DriveConfigHelper.State.FAILED);
                        } else {
                            setServiceStatus(Lang.getString("drive_status.error_registration", e.getMessage()));
                            setCurrentState(DriveConfigHelper.State.FAILED_REGISTRATION);
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
        Platform.runLater(() -> DriveConfigHelper.currentState.set(currentState));
    }

    public static void setServiceStatus(String serviceStatus) {
        Platform.runLater(() -> DriveConfigHelper.serviceStatus.set(serviceStatus));
    }

    private static Object[] executeAction(Action action, Object[] args) throws IOException {
        switch (action) {
            case INSERT:
                insertFile((java.io.File) args[0]);
                return null;
            case UPDATE:
                return new Object[]{updateLocal((java.io.File) args[0])};
            case LIST:
                return new Object[]{DriveFunctions.listFiles(getDriveService())};
            case REMOVE:
                DriveFunctions.removeFile(getDriveService(), (String) args[0]);
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

    public static void showExceptionAlert() {
        if (serviceException.get() != null) {
            int choice = AlertBuilder.error()
                    .key("errors.google", serviceException.get().getMessage())
                    .button("errors.google.retry")
                    .button("errors.google.disconnect")
                    .button(ButtonType.CANCEL)
                    .show();

            if (choice == 0) {
                LOG.info("Attempting a reconnection");
                performAction(Action.GET_INFO, new SimpleCallback() {
                    @Override
                    public void success(Object... objects) {
                    }

                    @Override
                    public void failure(Throwable ex) {
                        showExceptionAlert();
                    }
                });


            } else if (choice == 1) {
                removeCurrentUser();
            }
        }
    }

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
        DriveFunctions.removeCredentials();
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
        DriveFunctions.downloadFile(getDriveService(), id, file);
        driveProperties.putLong(file.getName() + ".timestamp", driveTimestamp);
        return true;
    }

    private static void insertFile(java.io.File file) throws IOException {
        String id = findFileId(file);

        File driveFile;
        if (id == null) {
            LOG.info("(upload - " + file.getName() + ") Inserting new file");
            driveFile = DriveFunctions.insertFile(getDriveService(), file);
        } else {
            LOG.info("(upload - " + file.getName() + ") Updating Drive file");
            driveFile = DriveFunctions.updateFile(getDriveService(), file, id);
        }

        driveProperties.put(file.getName() + ".id", driveFile.getId());
        driveProperties.putLong(file.getName() + ".timestamp", driveFile.getModifiedDate().getValue());
    }

    private static synchronized Userinfoplus getUserInfo() throws IOException {
        if (userInfo == null) {
            userInfo = DriveFunctions.getUserInfo(false);
            LOG.info("Loaded user info for: " + userInfo.getName());
        }

        return userInfo;
    }

    private static synchronized Drive getDriveService() throws IOException {
        if (driveService == null) {
            driveService = DriveFunctions.getDriveService(false);
        }
        return driveService;
    }

    private static void authorize() throws IOException {
        LOG.info("Connecting new Google user");
        userInfo = DriveFunctions.getUserInfo(true);

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

        FileList files = getDriveService().files().list().execute();


        for (File f : files.getItems()) {
            if (file.getName().equals(f.getTitle())) {
                id = f.getId();
                driveProperties.put(file.getName() + ".id", id);
                return id;
            }
        }

        return null;
    }

    private static long getDriveTimeStamp(String id) throws IOException {
        File file = DriveFunctions.getFile(getDriveService(), id);
        if (file != null) {
            return file.getModifiedDate().getValue();
        } else {
            return 0;
        }
    }


//    public static Drive getDriveServiceSafe() {
//        try {
//            return getDriveService();
//        } catch (IOException e) {
//            LOG.severe("Drive connection failed: " + e.getMessage());
//            LOG.severe("Drive synchronisation temporarily disabled");
//            serviceException.set(e);
//            serviceStatus.set("Exception occurred " + e.getMessage());
//
//            return null;
//        }
//    }


}
