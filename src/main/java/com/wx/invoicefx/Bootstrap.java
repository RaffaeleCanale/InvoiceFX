package com.wx.invoicefx;

import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.backup.BackupManager;
import com.wx.invoicefx.command.CommandRunner;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.config.Places;
import com.wx.invoicefx.currency.ECBRetriever;
import com.wx.invoicefx.google.DriveManager;
import com.wx.invoicefx.ui.animation.Animator;
import com.wx.invoicefx.ui.animation.DisabledAnimator;
import com.wx.invoicefx.util.view.AlertBuilder;
import com.wx.io.file.FileUtil;
import com.wx.properties.page.ResourcePage;
import com.wx.util.concurrent.Callback;
import com.wx.util.log.LogHelper;
import javafx.application.Platform;
import javafx.scene.control.ButtonType;
import javafx.scene.control.Label;
import javafx.scene.image.Image;

import java.io.File;
import java.io.IOException;
import java.security.GeneralSecurityException;
import java.util.*;
import java.util.logging.Level;

import static com.wx.invoicefx.config.Places.Dirs.*;
import static com.wx.invoicefx.config.Places.Files.UPDATER;
import static com.wx.invoicefx.config.Places.Files.UPDATER_CONFIG;
import static com.wx.invoicefx.config.preferences.local.LocalProperty.*;
import static com.wx.invoicefx.config.preferences.shared.SharedProperty.AUTO_UPDATE_CURRENCY;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 11.05.17.
 */
public class Bootstrap {

    private enum Step {
        CHECK_VERSION {
            @Override
            Void execute(BootstrapFlags flags, Callback<?> callback) {
                double lastVersion = AppResources.localPreferences().getDouble(LAST_KNOWW_VERSION);
                double currentVersion = App.APPLICATION_VERSION;

                if (lastVersion != currentVersion) {
                    flags.isNewVersionStart = true;
                    flags.onSuccess.add(() -> {
                        AppResources.localPreferences().setProperty(LAST_KNOWW_VERSION, App.APPLICATION_VERSION);
                    });
                }

                return callback.success(null);
            }
        },

        INIT_LOGGER {
            @Override
            Void execute(BootstrapFlags flags, Callback<?> callback) {
                LogHelper.setupLogger(LogHelper.consoleHandlerShort(Level.ALL));

                return callback.success(null);
            }
        },

        LOAD_UI_RESOURCES {
            @Override
            Void execute(BootstrapFlags flags, Callback<?> callback) {
                StageManager.setStyleSheet(App.class.getResource("/style.css").toExternalForm());
                StageManager.setAppIcon(new Image(App.class.getResourceAsStream("/icons/icon.png")));

                String tag = AppResources.localPreferences().getString(LANGUAGE);
                Lang.setLocale(tag, AppResources.supportedLanguages());
                Lang.initLanguageResource("text");

                return callback.success(null);
            }
        },

        CREATE_FOLDER_STRUCTURE {
            @Override
            Void execute(BootstrapFlags flags, Callback<?> callback) {
                try {
                    Places.init();
                } catch (IOException e) {
                    // Cannot log

                    AlertBuilder.error(e)
                            .key("bootstrap.errors.create_folder_structure")
                            .show();

                    return callback.failure(e);
                }

                try {
                    for (Places.Dirs dir : Places.Dirs.values()) {
                        File directory = Places.getDir(dir);

                        FileUtil.autoCreateDirectories(directory);
                    }
                } catch (IOException e) {
                    ExceptionLogger.logException(e);

                    AlertBuilder.error(e)
                            .key("bootstrap.errors.create_folder_structure")
                            .show();

                    return callback.failure(e);
                }

                File updater = Places.getFile(UPDATER);
                if (!updater.isFile() || flags.isNewVersionStart) {
                    try {
                        AppResources.extractResource("/update/cpupdater.jar", updater);
                        updater.setExecutable(true);
                    } catch (IOException e) {
                        ExceptionLogger.logException(e);

                        AlertBuilder.error(e)
                                .key("bootstrap.errors.create_folder_structure")
                                .show();
                        return callback.failure(e);
                    }
                }

                File updaterConfig = Places.getFile(UPDATER_CONFIG);
                if (!updaterConfig.isFile() || flags.isNewVersionStart) {
                    try {
                        ResourcePage page = ResourcePage.builder().fromFile(updaterConfig).create();
                        String mainJar = new File(Places.getConfigDir().getParentFile(), "InvoiceFX.jar").getAbsolutePath();
                        List<String> restartCmd = CommandRunner.getInstance(null, null, "java -jar \"" + mainJar + "\"").getCmd();

                        page.setProperty("file.src.main", Places.getCustomFile(UPDATE, "InvoiceFX.jar").getAbsolutePath());
                        page.setProperty("file.dst.main", mainJar);
                        page.setProperty("file.backup.main", "true");
                        page.setProperty("after_cmd", String.join(";", restartCmd));
                        page.save();

                        flags.ignoredSteps.add(UPDATE_CHECK);
                    } catch (IOException e) {
                        ExceptionLogger.logException(e);

                        AlertBuilder.error(e)
                                .key("bootstrap.errors.create_folder_structure")
                                .show();
                        return callback.failure(e);
                    }
                }
//                File updateScript = Places.getFile(UPDATE_SCRIPT);
//                if (!updateScript.exists()) {
//                    try {
//                        AppResources.extractResource("/update/" + updateScript.getName(), updateScript);
//                    } catch (IOException e) {
//                        ExceptionLogger.logException(e);
//
//                        AlertBuilder.error(e)
//                                .key("bootstrap.errors.create_folder_structure")
//                                .show();
//                        return callback.failure(e);
//                    }
//                }

                return callback.success(null);
            }
        },

        UPDATE_CHECK {
            @Override
            Void execute(BootstrapFlags flags, Callback<?> callback) {
                try {
                    ResourcePage updaterConfig = ResourcePage.builder().fromFile(Places.getFile(UPDATER_CONFIG)).load();
                    Optional<String> error = updaterConfig.getString("error");
                    if (error.isPresent()) {
                        AlertBuilder.error()
                                .key("bootstrap.errors.update")
                                .expandableContent(new Label(error.get()))
                                .show();

                        updaterConfig.removeProperty("error");
                        updaterConfig.save();
                    }

                } catch (IOException e) {
                    ExceptionLogger.logException(e);
                }


                return callback.success(null);
            }
        },

        LOAD_LOCAL_DATA_SET {
            @Override
            Void execute(BootstrapFlags flags, Callback<?> callback) {
                AppResources.initLocalDataSet();

                return callback.success(null);
            }
        },

        INIT_FACTORIES {
            @Override
            Void execute(BootstrapFlags flags, Callback<?> callback) {
                try {
                    DriveManager.init(Places.getDir(GOOGLE_CREDENTIALS_FILE),
                            AppResources.localPreferences().stringProperty(DRIVE_CURRENT_USER));
                } catch (GeneralSecurityException | IOException e) {
                    ExceptionLogger.logException(e);

                    int choice = AlertBuilder.error(e)
                            .key("bootstrap.errors.drive_service_failed")
                            .button(ButtonType.CANCEL)
                            .button("bootstrap.errors.drive_service_failed.ignore")
                            .show();

                    if (choice == 0) {
                        return callback.failure(e);
                    }
                }

                if (!AppResources.localPreferences().getBoolean(ENABLE_ANIMATIONS)) {
                    Animator.setInstance(new DisabledAnimator());
                }

                AppResources.initSyncManager();

                return callback.success(null);
            }
        },

        ENSURE_DATA_SANITY {
            @Override
            Void execute(BootstrapFlags flags, Callback<?> callback) {
                if (AppResources.getLocalDataSet().isCorrupted()) {
                    ExceptionLogger.logException(AppResources.getLocalDataSet().getException(), "Local data set is corrupted");

                    Platform.runLater(() -> BackupManager.solveCorrupt(callback));
                    return null;
                } else {
                    return callback.success(null);
                }
            }
        },

        SYNC_ECB {
            @Override
            Void execute(BootstrapFlags flags, Callback<?> callback) {
                ECBRetriever.initialize(Places.getDir(CACHE));

                if (AppResources.sharedPreferences().getBoolean(AUTO_UPDATE_CURRENCY)) {
                    AppResources.updateCurrencyRate();
                }

                return callback.success(null);
            }
        },

        SYNC {
            @Override
            Void execute(BootstrapFlags flags, Callback<?> callback) {
                AppResources.triggerSync();
                return callback.success(null);
            }
        };


        abstract Void execute(BootstrapFlags flags, Callback<?> callback);

        Step next() {
            int next = ordinal() + 1;
            Step[] steps = Step.values();

            return next < steps.length ? steps[next] : null;
        }
    }

    public static void bootstrap(Callback<?> callback) {
        new Thread(() -> {
            executeStep(callback, new BootstrapFlags(), Step.values()[0]);
        }).start();
    }

    public static void bootstrapWithoutUI(Callback<?> callback) {
        new Thread(() -> {
            BootstrapFlags flags = new BootstrapFlags();
            flags.ignoredSteps.addAll(Arrays.asList(Step.LOAD_UI_RESOURCES, Step.SYNC));

            executeStep(callback, flags, Step.values()[0]);
        }).start();
    }

    public static void bootstrapForUpdater(Callback<?> callback) {
        Step[] steps = {
                Step.INIT_LOGGER,
                Step.CREATE_FOLDER_STRUCTURE
        };

        new Thread(() -> {
            BootstrapFlags flags = new BootstrapFlags();
            flags.setOnlySteps(steps);

            executeStep(callback, flags, Step.values()[0]);
        }).run();
    }

    private static Void executeStep(Callback<?> callback, BootstrapFlags flags, Step step) {
        if (step == null) {
            flags.onSuccess.forEach(Runnable::run);
            return callback.success(null);
        }

        if (flags.ignoredSteps.contains(step)) {
            return executeStep(callback, flags, step.next());
        }

        return step.execute(flags, new Callback<Object>() {
            @Override
            public Void success(Object result) {
                return executeStep(callback, flags, step.next());
            }

            @Override
            public Void failure(Throwable ex) {
                ex.printStackTrace();
                return callback.failure(ex);
            }

            @Override
            public Void cancelled() {
                return callback.cancelled();
            }
        });
    }

    private static class BootstrapFlags {
        private boolean isNewVersionStart = false;
        private Set<Step> ignoredSteps = new HashSet<>();

        private Set<Runnable> onSuccess = new HashSet<>();

        void setOnlySteps(Step[] steps) {
            Set<Step> includedSteps = new HashSet<>(Arrays.asList(steps));

            ignoredSteps = new HashSet<>(Arrays.asList(Step.values()));
            ignoredSteps.removeAll(includedSteps);
        }
    }

    private Bootstrap() {
    }
}
