package com.wx.invoicefx;

import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.legacy.converter.LegacyConverter;
import com.wx.invoicefx.ui.views.Stages;
import com.wx.invoicefx.update.UpdateHelper;
import com.wx.util.concurrent.Callback;
import javafx.application.Application;
import javafx.application.Platform;
import javafx.stage.Stage;

import java.io.File;
import java.io.IOException;

/**
 * Starting point of the App. This class will get all the necessary components and show the main frame when it's ready
 */
public class App extends Application {

    public static final double APPLICATION_VERSION = 2.20;
    public static final String APPLICATION_NAME = "InvoiceFX";

    // TODO: 3/11/16 See if there is a better way to access the Application (better than just storing it here)
    public static App app;

    public static void main(String[] args) {
        Platform.setImplicitExit(false);
        if (args.length > 0 ) {
            if (args[0].equals("--create-index")) {
                Bootstrap.bootstrapForUpdater(o -> {
                    try {
                        UpdateHelper.createIndex();
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                });

            } else if (args[0].equals("--add-url")) {
                Bootstrap.bootstrapForUpdater(o -> {
                    try {
                        UpdateHelper.addUrl(args[1], args[2]);
                    } catch (IOException e) {
                        e.printStackTrace();
                    }
                    return null;
                });


            } else if (args[0].equals("--convert")) {
                try {
                    LegacyConverter.convertDataSet(new File(args[1]), new File(args[2]));
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }

            System.exit(0);
        } else {
            launch(args);
        }


    }

    @Override
    public void start(Stage primaryStage) {
        app = this;
        Platform.setImplicitExit(false);

        Bootstrap.bootstrap(new Callback<Object>() {
            @Override
            public Void success(Object o) {
                Platform.runLater(() -> {
                    StageManager.show(Stages.OVERVIEW, AppResources.getLocalDataSet());
                    Platform.setImplicitExit(true);
                });
                return null;
            }

            @Override
            public Void failure(Throwable ex) {
                ExceptionLogger.logException(ex, "Bootstrap failed");
                return cancelled();
            }

            @Override
            public Void cancelled() {
                System.exit(1);
                return null;
            }
        });
    }


}