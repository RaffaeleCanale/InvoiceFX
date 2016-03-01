package app.util.helpers;


import app.config.Config;
import app.util.ExceptionLogger;
import com.wx.io.Accessor;
import com.wx.util.log.LogHelper;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleDoubleProperty;
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
 * Created on 12/08/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class UpdateHelper {

    public enum State {
        UNINITIALIZED,
        LOADING_INDEX,
        DOWNLOADING,
        UP_TO_DATE,
        UPDATE_AVAILABLE,
        ERROR
    }

//    // Create index file
//    public static void main(String[] args) throws IOException {
//        File[] files = {
//                new File("out/artifacts/InvoiceFX_jar/InvoiceFX.jar")
//        };
//
//        new IndexFileBuilder()
//                .files(files)
//                .increment(0.01)
//                .createIndexFile();
//    }

    private static final String DRIVE_INDEX_FILE_ID = "0B6LgrYnciPdhWWFfdzFFTko2aUk";
    private static final String UPDATE_DIR = "Update";
    private static final String UPDATE_INDEX_FILE = "index.properties";
    private static final Logger LOG = LogHelper.getLogger(UpdateHelper.class);

    private static final ObjectProperty<State> state = new SimpleObjectProperty<>(State.UNINITIALIZED);
    private static final DoubleProperty progressProperty = new SimpleDoubleProperty();

    private static Updater updater;
    private static IOException error;

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

        if (checkState(State.LOADING_INDEX)) {
            LOG.warning("Action ignored");
            return;
        }


        new Thread(() -> {
            setState(State.LOADING_INDEX);
            //
//            try {
//                Thread.sleep(6000);
//            } catch (InterruptedException e) {
//                e.printStackTrace();
//            }

            try {
                if (updater == null) {
                    File indexFile = Config.getConfigFile(UPDATE_DIR, UPDATE_INDEX_FILE);
                    File programRoot = Config.getAppLauncher().getParentFile();

                    UpdaterApi.initializeIndexFile(indexFile, programRoot, DRIVE_INDEX_FILE_ID, UpdatersImpl.Implementations.DRIVE);
                    updater = UpdatersImpl.loadFromConfigFile(indexFile);
                }

                updater.load();
                setState(updater.isUpdateAvailable() ? State.UPDATE_AVAILABLE : State.UP_TO_DATE);
            } catch (IOException e) {
                setErrorState(e);
            }
        }).start();
    }

    public static void tryInitialize() {
        if (checkState(State.UNINITIALIZED)) {
            loadUpdater();
        }
    }

    public static void reload() {
        loadUpdater();
    }

    public static void update() {
        if (!checkState(State.UPDATE_AVAILABLE)) {
            LOG.warning("Action ignored (current state: " + state.get() + ")");
            return;
        }

        new Thread(() -> {
            setState(State.DOWNLOADING);
            try {
                File updater = exportUploader();
                File configFile = Config.getConfigFile(UPDATE_DIR, UPDATE_INDEX_FILE);
//                Common.clearDirectoryFiles(Config.getConfigFile(UPDATE_DIR));

                String relaunchCmd = "java -jar " + Config.getAppLauncher().getAbsolutePath().replaceAll(" ", "\\ ");
                UpdaterApi.runUpdaterAndExecute(updater, configFile, relaunchCmd, Locale.getDefault().toLanguageTag());
//
//                updater.downloadUpdateFiles(updateDir, progressProperty::setValue);
//
//                setState(State.UP_TO_DATE);
//
//
//                Platform.runLater(() ->
//                        StageManager.show(TransferController.STAGE_INFO,
//                        new TransferTask.Builder()
//                                .action(MOVE, updateDir.listFiles(), Config.getAppLauncher().getParentFile())
//                                .build(),
//                        (LazyCallback) () -> System.exit(0)
//                ));

            } catch (IOException e) {
                setErrorState(e);
            }
        }).start();
    }

    public static double getCurrentVersion() {
        ensureIsLoaded();

        return updater.getCurrentVersion();
    }

    public static double getUpdateVersion() {
        ensureIsLoaded();

        return updater.getUpdateVersion();
    }

    public static IOException getError() {
        return error;
    }

    public static DoubleProperty progressPropertyProperty() {
        return progressProperty;
    }

    public static ObjectProperty<State> stateProperty() {
        return state;
    }

    private static synchronized void setState(State s) {
        state.set(s);
    }

    private static synchronized boolean checkState(State s) {
        return state.get().equals(s);
    }

    private static void ensureIsLoaded() {
        if (checkState(State.UNINITIALIZED)) {
            throw new IllegalArgumentException("Unitialized");
        }
    }

    private static void setErrorState(IOException e) {
        error = e;
        ExceptionLogger.logException(e);
        setState(State.ERROR);
    }

}
