package com.wx.invoicefx.ui.views.settings.advanced;

import com.google.api.services.drive.model.FileList;
import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.config.preferences.local.LocalProperty;
import com.wx.invoicefx.config.preferences.shared.SharedProperty;
import com.wx.invoicefx.dataset.DataSet;
import com.wx.invoicefx.google.DriveManager;
import com.wx.invoicefx.ui.views.Stages;
import com.wx.invoicefx.ui.views.settings.advanced.table.DriveFilesTable;
import com.wx.invoicefx.ui.views.settings.advanced.table.IndexTable;
import com.wx.invoicefx.ui.views.settings.advanced.table.PreferencesTable;
import com.wx.invoicefx.ui.views.settings.advanced.table.UserPreferencesTable;
import com.wx.util.concurrent.Callback;
import javafx.application.Platform;
import javafx.fxml.FXML;
import javafx.scene.control.Tab;

import java.util.Optional;

import static com.wx.util.concurrent.ConcurrentUtil.executeAsync;


/**
 * Created on 08/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class AdvancedViewController {


    @FXML
    private UserPreferencesTable<SharedProperty> sharedPreferencesTable;
    @FXML
    private UserPreferencesTable<LocalProperty> localPreferencesTable;
    @FXML
    private IndexTable localIndexTable;
    @FXML
    private IndexTable remoteIndexTable;
    @FXML
    private Tab remoteIndexTab;
    @FXML
    private PreferencesTable drivePropertiesTable;
    @FXML
    private DriveFilesTable driveFilesTable;

    @FXML
    public void initialize() {
        sharedPreferencesTable.setPreferences(AppResources.sharedPreferences());
        sharedPreferencesTable.setItems(SharedProperty.values());

        localPreferencesTable.setPreferences(AppResources.localPreferences());
        localPreferencesTable.setItems(LocalProperty.values());

        localIndexTable.setIndex(AppResources.getLocalDataSet());

        Optional<DataSet> remote = AppResources.getSyncManager().getRemote();

        if (remote.isPresent()) {
            remoteIndexTable.setIndex(remote.get());
        } else {
            remoteIndexTab.setDisable(true);
        }

        drivePropertiesTable.setPreferences(DriveManager.getDriveProperties());

        executeAsync(DriveManager::executeListFiles, new Callback<FileList>() {
            @Override
            public Void success(FileList fileList) {
                Platform.runLater(() -> driveFilesTable.setItems(fileList.getFiles()));
                return null;
            }

            @Override
            public Void failure(Throwable ex) {
                ExceptionLogger.logException(ex);
                return null;
            }
        });
    }

    @FXML
    private void onClose() {
        StageManager.closeAll();
        StageManager.show(Stages.SETTINGS);
    }
}
