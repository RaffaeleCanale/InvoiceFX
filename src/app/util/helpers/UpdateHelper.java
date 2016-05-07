package app.util.helpers;


import app.App;
import app.config.Config;
import app.config.preferences.properties.LocalProperty;
import app.tex.TexFileCreator;
import com.wx.io.Accessor;
import com.wx.io.ProgressInputStream;
import com.wx.servercomm.http.HttpRequest;
import javafx.application.Platform;

import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.util.function.Consumer;

/**
 * Utility class that manages the App state regarding any updates.
 * <p>
 * Created on 12/08/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public class UpdateHelper {

    private static final String UPDATE_URL = "https://drive.google.com/uc?id=0B6LgrYnciPdhbUkzNV92U0ZEVjg";

    public static void update(Consumer<Double> progress, long interval, Consumer<IOException> errorConsumer) {

        new Thread(() -> {
            try {
                File appDir = Config.getAppLauncher().getParentFile();

                File templateDir = Config.getConfigFile(TexFileCreator.TEMPLATE_DIR);
                Common.clearDirectoryFiles(templateDir);

                File downloadFile = new File(appDir, App.APPLICATION_NAME + "_downloading.jar");
                File finalFile = new File(appDir, App.APPLICATION_NAME + "_update.jar");

                File updateScript = exportScript(appDir);
                if (finalFile.exists() && !finalFile.delete()) {
                    throw new IOException("Cannot delete update file " + finalFile);
                }

                downloadUpdate(progress, interval, downloadFile);
                if (!downloadFile.renameTo(new File(appDir, App.APPLICATION_NAME + "_update.jar"))) {
                    throw new IOException("Cannot rename update file");
                }

                runScript(updateScript);
            } catch (IOException e) {
                errorConsumer.accept(e);
            }
        }
        ).start();
    }


    private static void runScript(File scriptFile) throws IOException {
        String[] cmd = {scriptFile.getAbsolutePath(), scriptFile.getParentFile().getAbsolutePath()};
        Runtime.getRuntime().exec(cmd);


        System.exit(0);
    }

    private static File exportScript(File appDir) throws IOException {
        String scriptName = Config.localPreferences().getProperty(LocalProperty.UPDATE_SCRIPT_NAME);

        File scriptFile = new File(appDir, scriptName);

        if (!scriptFile.exists()) {
            try (Accessor accessor = new Accessor()
                    .setIn(UpdateHelper.class.getResourceAsStream("/" + scriptName))
                    .setOut(scriptFile)) {
                accessor.pourInOut();
            }

            scriptFile.setExecutable(true);
        }

        return scriptFile;
    }

    private static void downloadUpdate(Consumer<Double> progress, long interval, File downloadFile) throws IOException {
        try (Accessor accessor = new Accessor().setIn(getDownloadStream(progress, interval)).setOut(downloadFile)) {
            accessor.pourInOut();
        }
    }

    private static InputStream getDownloadStream(Consumer<Double> progress, long interval) throws IOException {
        double estimatedSize = Config.getAppLauncher().length();
        Consumer<Long> progressL = l -> Platform.runLater(() -> progress.accept(Math.min(1.0, (double) l / estimatedSize)));

        HttpRequest get = HttpRequest.createGET();
        return new ProgressInputStream(get.executeDirect(UPDATE_URL), progressL, interval);
    }


}
