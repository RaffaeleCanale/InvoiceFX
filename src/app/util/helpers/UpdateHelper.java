package app.util.helpers;


import app.config.Config;
import app.util.ExceptionLogger;
import com.wx.io.Accessor;
import com.wx.util.log.LogHelper;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import updater.Updater;
import updater.implementations.UpdatersImpl;
import updater.util.UpdaterApi;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.Locale;
import java.util.logging.Logger;

/**
 * Utility class that manages the App state regarding any updates.
 * <p>
 * Created on 12/08/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public class UpdateHelper {

    public enum State {
        UNINITIALIZED,
        LOADING_INDEX,
        //        DOWNLOADING,
        UP_TO_DATE,
        UPDATE_AVAILABLE,
        ERROR
    }

    private static final String DRIVE_INDEX_FILE_ID = "0B6LgrYnciPdhWWFfdzFFTko2aUk";
    private static final String UPDATE_DIR = "Update";
    private static final String UPDATE_INDEX_FILE = "index.properties";
    private static final Logger LOG = LogHelper.getLogger(UpdateHelper.class);

    private static final ObjectProperty<State> state = new SimpleObjectProperty<>(State.UNINITIALIZED);

    private static IOException error;
    private static double currentVersion;
    private static double updateVersion;

    /**
     * The updater is an external app that is included inside this App resources. This method allows to export the
     * updater from the resources (usually contained in the same JAR) to an external file.
     * <p>
     * The updater is exported into the {@link #UPDATE_DIR} directory.
     * <p>
     * If the updater already existed, it is overridden.
     *
     * @return File (JAR executable) of the updater
     *
     * @throws IOException
     */
    private static File exportUploader() throws IOException {
        String updaterFileName = "WXUpdater.jar";

        File updater = Config.getConfigFile(UPDATE_DIR, updaterFileName);
        InputStream resource = UpdateHelper.class.getResourceAsStream("/updater/" + updaterFileName);

        try (Accessor accessor = new Accessor().setIn(resource).setOut(updater)) {
            accessor.pourInOut();
        }

        return updater;
    }

    private static void loadUpdater() {
        if (currentStateIs(State.LOADING_INDEX)) {
            LOG.warning("Action ignored");
            return;
        }
        setState(State.LOADING_INDEX);

        // Asynchronous loading
        new Thread(() -> {
            try {
                File indexFile = Config.getConfigFile(UPDATE_DIR, UPDATE_INDEX_FILE);
                File programRoot = Config.getAppLauncher().getParentFile();

                UpdaterApi.initializeIndexFile(indexFile, programRoot, DRIVE_INDEX_FILE_ID, UpdatersImpl.Implementations.DRIVE);
                Updater updater = UpdatersImpl.loadFromConfigFile(indexFile);

                updater.load();

                currentVersion = updater.getCurrentVersion();
                updateVersion = updater.getUpdateVersion();

                setState(updater.isUpdateAvailable() ? State.UPDATE_AVAILABLE : State.UP_TO_DATE);
            } catch (IOException e) {
                setErrorState(e);
            }
        }).start();
    }

    /**
     * Initializes the updater only if it is uninitialized. This operation is performed in a separated thread and, at
     * the end, the state will be updated accordingly.
     * <p>
     * The updater initialization may take some time (to perform server requests).
     */
    public static void tryInitialize() {
        if (currentStateIs(State.UNINITIALIZED)) {
            loadUpdater();
        }
    }

    /**
     * Reload the updater and re-verify the current state
     */
    public static void reload() {
        loadUpdater();
    }

    /**
     * Perform the update. This will terminate the current app and start an external software.
     */
    public static void update() {
        if (!currentStateIs(State.UPDATE_AVAILABLE)) {
            LOG.warning("Action ignored (current state: " + state.get() + ")");
            return;
        }

        new Thread(() -> {
            setState(State.LOADING_INDEX); // TODO: 3/11/16 Necessary? Correct?

            try {
                File updater = exportUploader(); // TODO: 3/11/16 Export every time?

                File configFile = Config.getConfigFile(UPDATE_DIR, UPDATE_INDEX_FILE);

                String relaunchCmd = "java -jar " + Config.getAppLauncher().getAbsolutePath().replaceAll(" ", "\\ ");
                UpdaterApi.runUpdaterAndExecute(updater, configFile, relaunchCmd, Locale.getDefault().toLanguageTag());

            } catch (IOException e) {
                setErrorState(e);
            }
        }).start();
    }

    /**
     * @return Current local App version
     */
    public static double getCurrentVersion() {
        ensureIsLoaded();

        return currentVersion;
    }

    /**
     * @return Remote version
     */
    public static double getUpdateVersion() {
        ensureIsLoaded();

        return updateVersion;
    }

    /**
     * If the current state is {@link State#ERROR}, returns the source exception of the error.
     *
     * @return Last exception encountered or {@code null} if the state is not {@link State#ERROR}
     */
    public static IOException getError() {
        return error;
    }

    /**
     * @return The updater state
     *
     * @see {@link State}
     */
    public static ObjectProperty<State> stateProperty() {
        return state;
    }

    private static synchronized void setState(State s) {
        state.set(s);
    }

    private static synchronized boolean currentStateIs(State s) {
        return state.get().equals(s);
    }

    private static void ensureIsLoaded() {
        if (currentStateIs(State.UNINITIALIZED)) {
            throw new IllegalArgumentException("Unitialized");
        }
    }

    private static void setErrorState(IOException e) {
        error = e;
        ExceptionLogger.logException(e);
        setState(State.ERROR);
    }

}
