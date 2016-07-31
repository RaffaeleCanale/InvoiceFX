package app.gui.config.properties;

import app.Stages;
import app.config.Config;
import app.legacy.config.manager.DefaultModelManager;
import com.wx.fx.preferences.UserPreferences;
import com.wx.fx.preferences.properties.ConfigProperty;
import com.wx.fx.preferences.properties.LocalProperty;
import com.wx.fx.preferences.properties.SharedProperty;
import app.google.DriveConfigHelper;
import app.legacy.model.invoice.InvoiceList;
import app.legacy.model.invoice.InvoiceModel;
import app.util.ExceptionLogger;
import app.util.backup.BackUpFile;
import app.util.backup.BackUpManager;
import com.google.api.services.drive.model.File;
import com.google.api.services.drive.model.FileList;
import com.wx.fx.gui.window.StageController;
import com.wx.fx.gui.window.StageManager;
import com.wx.util.Format;
import javafx.beans.binding.BooleanBinding;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleStringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.*;

import java.io.IOException;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.prefs.BackingStoreException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import static app.google.DriveConfigHelper.Action.LIST;

/**
 * Created on 08/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class PropertiesViewerController implements StageController {


    @FXML
    private PropertiesTableController<SharedProperty> sharedTableController;
    @FXML
    private PropertiesTableController<LocalProperty> localTableController;
    @FXML
    private PropertiesTableController<String> driveTableController;
    @FXML
    private PropertiesTableController<String> filesTableController;

    @FXML
    private PropertiesTableController<BackUpFile> backUpController;

    @FXML
    private Tab filesTab;
    @FXML
    private Label driveInfoLabel;

    public void initialize() {
        loadProperties(SharedProperty.values(), Config.sharedPreferences(), sharedTableController);
        loadProperties(LocalProperty.values(), Config.localPreferences(), localTableController);
        loadProperties(DriveConfigHelper.getDriveProperties(), driveTableController);
        loadBackUpFiles(getAllBackUpFiles(), backUpController);

        BooleanBinding serviceOnlineBinding = DriveConfigHelper.serviceOnlineBinding();
        serviceOnlineBinding.addListener((observable, oldValue, newValue) -> initDriveFilesTable(newValue));
        filesTab.disableProperty().bind(serviceOnlineBinding.not());

        initDriveFilesTable(serviceOnlineBinding.get());
    }

    private List<BackUpFile> getAllBackUpFiles() {
        try {
            return BackUpManager.getAllBackUpFiles();
        } catch (IOException e) {
            ExceptionLogger.logException(e);
            return Collections.emptyList();
        }
    }

    private void initDriveFilesTable(boolean isServiceOnline) {
        if (isServiceOnline) {
            loadDriveFiles(filesTableController);
        }
    }

    @Override
    public void closing() {
        Config.saveSharedPreferences();
    }

    private <E extends ConfigProperty> void loadProperties(E[] values,
                                                           UserPreferences<E> prefs,
                                                           PropertiesTableController<E> table) {
        table.setFunctionsEditable(
                ConfigProperty::key,
                prefs::stringProperty,
                prefs::isSet,
                (k, v) -> {
                    prefs.remove(k);
                    return false;
                }
        );
        table.setItems(values);
    }

    private void loadProperties(Preferences prefs, PropertiesTableController<String> table) {
        table.setFunctionsEditable(
                Function.identity(),
                k -> {
                    SimpleStringProperty prop = new SimpleStringProperty(prefs.get(k, ""));
                    prop.addListener((observable, oldValue, newValue) -> {
                        prefs.put(k, newValue);
                    });
                    return prop;
                },
                null,
                (k, v) -> {
                    prefs.remove(k);
                    return true;
                }
        );
        try {
            table.setItems(prefs.keys());
        } catch (BackingStoreException e) {
            e.printStackTrace();
        }
    }

    private void loadDriveFiles(PropertiesTableController<String> table) {
        DriveConfigHelper.performAction(LIST, args -> {
            Map<String, File> files = toMap((FileList) args[0]);

            long sum = files.values().stream().mapToLong(File::getFileSize).sum();
            driveInfoLabel.setText(Format.formatSize(sum));

            table.setFunctionsEditable(
                    Function.identity(),
                    id -> new ReadOnlyStringWrapper(files.get(id).getModifiedDate().getValue() + " - " + files.get(id).getTitle()),
                    null,
                    (id, file) -> {
                        DriveConfigHelper.performAction(DriveConfigHelper.Action.REMOVE, null, id);
                        return true;
                    }
            );
            table.setItems(files.keySet());
        });
    }

    private void loadBackUpFiles(List<BackUpFile> files, PropertiesTableController<BackUpFile> table) {
        table.setFunctions(
                f -> f.getFile().getName(),
                f -> formatDate(f.getBackUpDate()),
                f -> false,
                (f, v) -> f.getFile().delete()
        );

        table.setAction(f -> {
            final String invoiceFileName = Config.localPreferences().getPathProperty(LocalProperty.STORED_INVOICES_PATH).getName();
            final String itemFileName = Config.localPreferences().getPathProperty(LocalProperty.STORED_ITEMS_PATH).getName();

            try {
                if (f.getBaseName().equals(invoiceFileName)) {

                    DefaultModelManager<InvoiceModel, InvoiceList> manager = new DefaultModelManager<>(InvoiceList.class, f.getFile());
                    manager.load();
                    StageManager.show(Stages.INVOICES_ARCHIVE, manager);

                } else if (f.getBaseName().equals(itemFileName)) {
                    // TODO: 11/9/15 Add this feature
                } else {
                    throw new AssertionError("Invalid basename: " + f.getBaseName());
                }
            } catch (IOException e) {
                ExceptionLogger.logException(e);
            }
        });

        table.setItems(files);
    }

    private String formatDate(LocalDateTime date) {
        return date.format(DateTimeFormatter.ISO_LOCAL_DATE) + "   " + date.format(DateTimeFormatter.ISO_LOCAL_TIME);
    }

    private Map<String, File> toMap(FileList list) {
        return list.getItems().stream().collect(
                Collectors.toMap(File::getId, Function.<File>identity()));
    }


}
