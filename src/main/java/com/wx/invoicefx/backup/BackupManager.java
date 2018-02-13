package com.wx.invoicefx.backup;

import com.wx.fx.gui.window.StageManager;
import com.wx.invoicefx.AppResources;
import com.wx.invoicefx.config.Places;
import com.wx.invoicefx.dataset.DataSet;
import com.wx.invoicefx.dataset.LocalDataSet;
import com.wx.invoicefx.dataset.impl.InvoiceFxDataSet;
import com.wx.invoicefx.ui.views.Stages;
import com.wx.invoicefx.ui.views.sync.DataSetChooserController;
import com.wx.io.file.FileUtil;
import com.wx.util.concurrent.Callback;
import com.wx.util.log.LogHelper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;

import static com.wx.invoicefx.config.Places.Dirs.BACKUP_DIRECTORY;
import static com.wx.invoicefx.config.preferences.local.LocalProperty.BACKUP_LENGTH;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 27.06.17.
 */
public class BackupManager {

    private static final Logger LOG = LogHelper.getLogger(BackupManager.class);

    private static final Map<Double, LocalDataSet> backupBuffer = new HashMap<>();

    public static void solveCorrupt(Callback<?> callback) {
        if (!AppResources.getLocalDataSet().isCorrupted()) {
            throw new IllegalArgumentException("Can only be used to solve local dataset");
        }

        StageManager.show(Stages.DATA_SET_CHOOSER, DataSetChooserController.DialogType.SOLVE_CORRUPTED, callback);
    }


    public static void executeBackup() throws IOException {
        LocalDataSet localDataSet = AppResources.getLocalDataSet();
        double version = localDataSet.getIndex().getVersion();
        File backupDir = getBackupDirFor(version);

        LOG.info("Creating backup for " + version);

        if (backupDir.isDirectory()) {
            LOG.warning("Backup for " + version + " already exists");
            return;
        }


        transferData(localDataSet, backupDir);

        removeOldBackupIfNeeded();
    }

    public static List<DataSet> getAllBackups() throws IOException {
        Set<Double> backupsVersion = getBackupsVersion();
        List<DataSet> result = new ArrayList<>();

        for (Double version : backupsVersion) {
            DataSet dataSet = loadDataSet(version);

            if (dataSet != null) {
                result.add(dataSet);
            }
        }

        Comparator<DataSet> comparator = Comparator.comparingDouble(r -> r.getIndex().getVersion());
        Collections.sort(result, comparator.reversed());

        return result;
    }

//    public static Optional<DataSet> getLatestBackup() throws IOException {
//        Set<Double> backupsVersion = getBackupsVersion();
//        OptionalDouble max = backupsVersion.stream().mapToDouble(Double::doubleValue).max();
//
//        if (!max.isPresent()) {
//            return Optional.empty();
//        }
//
//        double version = max.getAsDouble();
//        return Optional.ofNullable(loadDataSet(version));
//    }

    private static DataSet loadDataSet(double version) throws IOException {
        LocalDataSet backup = backupBuffer.get(version);
        if (backup == null) {
            File dataDir = getBackupDirFor(version);

            backup = new InvoiceFxDataSet(dataDir, "backup");
            backupBuffer.put(version, backup);
        }

        backup.loadData();

        return backup;
    }


    private static void transferData(LocalDataSet dataSet, File destinationDir) throws IOException {
        FileUtil.copyFile(dataSet.getDataDirectory(), destinationDir);
    }

    private static void removeOldBackupIfNeeded() {
        Set<Double> backupsVersion = getBackupsVersion();


        if (backupsVersion.size() > AppResources.localPreferences().getInt(BACKUP_LENGTH)) {
            OptionalDouble min = backupsVersion.stream().mapToDouble(Double::doubleValue)
                    .min();
            assert min.isPresent();

            double version = min.getAsDouble();
            FileUtil.deleteDir(getBackupDirFor(version));
        }
    }

    private static Set<Double> getBackupsVersion() {
        File backupDir = Places.getDir(BACKUP_DIRECTORY);
        Set<Double> versions = new HashSet<>();

        File[] files = backupDir.listFiles();
        if (files != null) {
            for (File file : files) {
                if (file.isDirectory()) {
                    try {
                        versions.add(Double.parseDouble(file.getName()));
                    } catch (NumberFormatException e) {
                        LOG.warning("Invalid backup directory: " + file.getName());
                    }
                }
            }
        }

        return versions;
    }

    private static File getBackupDirFor(double version) {
        return Places.getCustomFile(BACKUP_DIRECTORY, String.valueOf(version));
    }



}
