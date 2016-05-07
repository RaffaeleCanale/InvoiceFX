package app.gui.config.settings;

import app.App;
import app.Stages;
import app.config.Config;
import app.util.ExceptionLogger;
import app.util.helpers.InvoiceHelper;
import app.gui.config.currency.CurrencyPanelController;
import app.util.gui.AlertBuilder;
import app.util.helpers.Common;
import app.util.helpers.UpdateHelper;
import com.sun.javaws.progress.Progress;
import com.wx.fx.Lang;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.io.Accessor;
import com.wx.io.ProgressInputStream;
import com.wx.io.file.FileUtil;
import com.wx.properties.PropertiesManager;
import com.wx.servercomm.http.HttpRequest;
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
import java.util.function.Consumer;
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

    private static final long PROGRESS_INTERVAL = 1024 * 100;

    @FXML
    private ProgressBar versionProgress;

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


        // UPDATER
        // TODO: 29.04.16 Updater button here
    }

    @Override
    public void setContext(Stage stage) {
        currencyPanelController.setContext(stage);
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
        chooser.setTitle(Lang.getString("settings.file_chooser.title"));

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
                    .key("settings.file_chooser.error", result.getAbsolutePath())
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
                Common.moveFiles(pdfFiles, result);
            }
        }

        Config.localPreferences().setProperty(INVOICE_DIRECTORY, result.getAbsolutePath());
    }

    public void versionAction() {
        // TODO: 29.04.16 update
        versionActionButton.setVisible(false);
        versionProgress.setVisible(true);

        versionProgress.setProgress(-1);


        UpdateHelper.update(versionProgress::setProgress, PROGRESS_INTERVAL, e -> Platform.runLater(() -> {
            ExceptionLogger.logException(e);
            AlertBuilder.error(e).show();

            versionProgress.setVisible(false);
            versionActionButton.setVisible(true);
        }));


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
