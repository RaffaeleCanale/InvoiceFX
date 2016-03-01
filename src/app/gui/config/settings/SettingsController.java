package app.gui.config.settings;

import app.App;
import app.Stages;
import app.config.Config;
import app.util.helpers.InvoiceHelper;
import app.gui.config.currency.CurrencyPanelController;
import app.util.gui.AlertBuilder;
import app.util.helpers.Common;
import app.util.helpers.UpdateHelper;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.io.file.FileUtil;
import com.wx.properties.PropertiesManager;
import com.wx.util.log.LogHelper;
import javafx.application.Platform;
import javafx.beans.binding.Bindings;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;
import javafx.scene.control.*;
import javafx.stage.DirectoryChooser;
import javafx.stage.Stage;
import javafx.util.StringConverter;

import java.io.File;
import java.io.IOException;
import java.util.Locale;
import java.util.logging.Logger;

import static app.config.preferences.properties.LocalProperty.INVOICE_DIRECTORY;
import static app.config.preferences.properties.SharedProperty.*;

/**
 * Created on 12/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class SettingsController implements StageController {

    private static final Logger LOG = LogHelper.getLogger(SettingsController.class);


    @FXML
    private ProgressIndicator versionProgress;
    @FXML
    private ProgressBar versionUpdateProgress;

    @FXML
    private Label versionLabel;
    @FXML
    private Button versionActionButton;

    @FXML
    private CurrencyPanelController currencyPanelController;

    @FXML
    private CheckBox showCountBox;

    @FXML
    private Label invoiceDirLabel;
    @FXML
    private ChoiceBox<Locale> languageBox;
    @FXML
    private CheckBox roundVatCheckbox;

    private PropertiesManager lang;

    public void initialize() {
        // INVOICE DIR
        StringProperty dirName = Config.localPreferences().stringProperty(INVOICE_DIRECTORY);
        invoiceDirLabel.textProperty().bind(Bindings.createStringBinding(() -> new File(dirName.get()).getName(), dirName));

        // LANGUAGES
        languageBox.setItems(FXCollections.observableArrayList(App.supportedLanguages()));
        languageBox.setConverter(new StringConverter<Locale>() {
            @Override
            public String toString(Locale object) {
                return object.getDisplayName();
            }

            @Override
            public Locale fromString(String string) {
                return null;
            }
        });
        languageBox.getSelectionModel().select(findCurrentLocale());
        languageBox.getSelectionModel().selectedItemProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue.equals(Locale.getDefault())) {
                Config.sharedPreferences().setProperty(LANGUAGE, newValue.toLanguageTag());
                Locale.setDefault(newValue);
//                App.initLang();

                StageManager.closeAll();
                StageManager.show(Stages.OVERVIEW);
                StageManager.show(Stages.SETTINGS);
            }
        });

        // ROUND VAT
        roundVatCheckbox.selectedProperty().bindBidirectional(Config.sharedPreferences().booleanProperty(VAT_ROUND));

        // SHOW ITEM COUNT
        showCountBox.selectedProperty().bindBidirectional(Config.sharedPreferences().booleanProperty(SHOW_ITEM_COUNT));


        // VERSION
        UpdateHelper.progressPropertyProperty().addListener((observable, oldValue, newValue) -> {
            double p = newValue.doubleValue();
            Platform.runLater(() -> versionUpdateProgress.setProgress(p == 0.0 ? -1 : p));
        });
        UpdateHelper.stateProperty().addListener((observable, oldValue, newValue) -> {
            Platform.runLater(() -> showUpdaterState(newValue));
        });
        UpdateHelper.tryInitialize();
    }

    @Override
    public void setContext(Stage stage, PropertiesManager bundle) {
        this.lang = bundle;

        currencyPanelController.setContext(stage);
        showUpdaterState(UpdateHelper.stateProperty().get());
    }

    private void showUpdaterState(UpdateHelper.State state) {
        versionLabel.setId("");
        versionActionButton.setVisible(false);
        versionProgress.setVisible(false);
        versionUpdateProgress.setVisible(false);

        switch (state) {
            case UNINITIALIZED:
            case LOADING_INDEX:
                versionLabel.setText(lang.getString("settings.version.getting"));
                versionProgress.setVisible(true);
                break;

            case DOWNLOADING:
                versionLabel.setText(lang.getString("settings.version.downloading"));
                versionUpdateProgress.setVisible(true);
                break;

            case UP_TO_DATE:
                double currentVersion = UpdateHelper.getCurrentVersion();
                versionLabel.setText(lang.getString("settings.version.current", currentVersion));

                versionActionButton.setText(lang.getString("settings.version.check"));
                versionActionButton.setVisible(true);
                break;

            case UPDATE_AVAILABLE:
                double newVersion = UpdateHelper.getUpdateVersion();

                versionLabel.setText(lang.getString("settings.version.available", newVersion));

                versionActionButton.setText(lang.getString("settings.update"));
                versionActionButton.setVisible(true);
                break;

            case ERROR:
                versionLabel.setText(lang.getString("settings.version.error"));
                versionLabel.setId("error");

                versionActionButton.setText(lang.getString("settings.version.retry"));
                versionActionButton.setVisible(true);
                break;
        }
    }

    private Locale findCurrentLocale() {
        Locale current = Locale.getDefault();
        for (Locale locale : App.supportedLanguages()) {
            if (locale.getLanguage().equals(current.getLanguage())) {
                return locale;
            }
        }

        return null;
    }

    public void chooseInvoiceDir() {
        DirectoryChooser chooser = new DirectoryChooser();
        chooser.setTitle(lang.getString("settings.file_chooser.title"));

        File currentDir = Config.localPreferences().getPathProperty(INVOICE_DIRECTORY);
        if (currentDir != null && currentDir.getParentFile() != null && currentDir.getParentFile().exists()) {
            chooser.setInitialDirectory(currentDir.getParentFile());
        }


        File result = chooser.showDialog(null);

        if (result == null || result.equals(currentDir)) {
            return;
        }

        try {
            FileUtil.autoCreateDirectory(result);
        } catch (IOException e) {
            AlertBuilder.error()
                    .key("settings.file_chooser.error")
                    .plainContent(result.getAbsolutePath())
                    .show();
            return;
        }

        File[] pdfFiles = currentDir == null ? null : currentDir.listFiles(f -> {
            return f.getName().endsWith(".pdf");
        });

        if (pdfFiles != null && pdfFiles.length > 0) {
            int choice = AlertBuilder.confirmation()
                    .key("settings.file_chooser.migrate", pdfFiles.length)
                    .show();
            if (choice == 0) { // Migrate
                InvoiceHelper.moveFiles(pdfFiles, result);
            }
        }

        Config.localPreferences().setProperty(INVOICE_DIRECTORY, result.getAbsolutePath());
    }

    public void versionAction() {
        switch (UpdateHelper.stateProperty().get()) {
            case UP_TO_DATE:
                UpdateHelper.reload();
                break;
            case UPDATE_AVAILABLE:
                Common.clearDirectoryFiles(Config.getConfigFile("template"));

//                UpdateHelper.progressPropertyProperty().addListener(new ChangeListener<Number>() {
//                    @Override
//                    public void changed(ObservableValue<? extends Number> observable, Number oldValue, Number newValue) {
//                        System.out.println(newValue);
//                    }
//                });
                UpdateHelper.update();

                break;
            case ERROR:
                UpdateHelper.reload();
                break;
            default:
                break;
        }

//        versionActionButton.setVisible(false);
//        versionProgress.setVisible(true);
//
//        new Thread(() -> {
//            try {
//                Platform.runLater(() -> {
//                    versionProgress.setProgress(0.0);
//                    versionProgress.setPrefHeight(45);
//                    versionProgress.setPrefWidth(45);
//                    versionLabel.setText(Lang.get("settings.version.downloading"));
//                });
//                UpdateHelper.update(p -> Platform.runLater(() -> versionProgress.setProgress(p)));
//                Platform.runLater(() -> versionProgress.setProgress(1.0));
//
//
//                File templateDir = Config.getConfigFile("template");
//                if (templateDir.isDirectory() && templateDir.exists()) {
//                    for (File file : templateDir.listFiles()) {
//                        if (file.isFile()) {
//                            file.delete();
//                        }
//                    }
//                }
//                Platform.runLater(StageManager::closeAll);
//
//            } catch (IOException e) {
//                ExceptionLogger.logException(e);
//
//                Platform.runLater(() -> {
//                    versionLabel.setText(Lang.get("settings.version.error"));
//                    versionLabel.setId("error");
//
//                    versionProgress.setProgress(0.0);
//                    versionProgress.setPrefHeight(50);
//                    versionProgress.setPrefWidth(50);
//                    versionProgress.setVisible(false);
//                });
//            }
//
//        }).start();
    }

    public void showAdvanced() {
        StageManager.show(Stages.PREFS_EDITOR);
    }

    @Override
    public void closing() {
        currencyPanelController.unbindVariables();
        Config.saveSharedPreferences();
    }

    public void close() {
        StageManager.close(Stages.SETTINGS);
    }


}
