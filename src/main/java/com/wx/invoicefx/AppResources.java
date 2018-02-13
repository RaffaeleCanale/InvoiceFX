package com.wx.invoicefx;

import com.wx.fx.preferences.AbstractPreferences;
import com.wx.fx.preferences.UserPreferences;
import com.wx.invoicefx.backup.BackupManager;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.config.Places;
import com.wx.invoicefx.config.preferences.CachedUserPreferences;
import com.wx.invoicefx.config.preferences.local.LocalProperty;
import com.wx.invoicefx.config.preferences.shared.SharedProperty;
import com.wx.invoicefx.currency.ECBRetriever;
import com.wx.invoicefx.currency.Rates;
import com.wx.invoicefx.dataset.DataSet;
import com.wx.invoicefx.dataset.impl.DriveDataSet;
import com.wx.invoicefx.dataset.impl.InvoiceFxDataSet;
import com.wx.invoicefx.dataset.impl.event.ChangeEvent;
import com.wx.invoicefx.dataset.impl.event.ModelEvent;
import com.wx.invoicefx.google.DriveManager;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Vat;
import com.wx.invoicefx.model.entities.item.Vats;
import com.wx.invoicefx.sync.SyncManager;
import com.wx.invoicefx.util.concurrent.ThrottledTask;
import com.wx.invoicefx.util.string.VatsConverter;
import com.wx.io.Accessor;
import com.wx.util.concurrent.ConcurrentUtil;
import com.wx.util.log.LogHelper;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.Collection;
import java.util.Locale;
import java.util.logging.Logger;

import static com.wx.invoicefx.config.Places.Dirs.DATA_DIR;
import static com.wx.invoicefx.config.preferences.local.LocalProperty.COMPUTER_NAME;
import static com.wx.invoicefx.config.preferences.local.LocalProperty.INVOICE_DIRECTORY;
import static com.wx.invoicefx.config.preferences.shared.SharedProperty.*;
import static com.wx.util.concurrent.ConcurrentUtil.executeAsync;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 11.05.17.
 */
public class AppResources {

    private static boolean UPDATE_ITEMS_ON_VAT_CHANGED = true;

    private static final String DEFAULT_TEX_TEMPLATE = "invoice.tex";
    private static final Logger LOG = LogHelper.getLogger(AppResources.class);

    private static final int PREFERENCES_SAVE_THROTTLE = 3000;

    private static final UserPreferences<LocalProperty> localPreferences = new CachedUserPreferences<>(LocalProperty.class);

    private static SyncManager syncManager;
    private static InvoiceFxDataSet localDataSet;
    private static ThrottledSaveAndSync saveAndSyncRunner = new  ThrottledSaveAndSync();


    static void initLocalDataSet() {
        if (localDataSet != null) {
            throw new IllegalStateException("Local repo is already initialized");
        }

        final File dataDir = Places.getDir(DATA_DIR);
        localDataSet = new InvoiceFxDataSet(dataDir, "local");
        localDataSet.loadData();
        localDataSet.addDataChangedListener((o, source) -> onLocalDataSetChanged((Collection<ChangeEvent>) source));
    }

    public static void triggerSync() {
        saveAndSyncRunner.execute();
    }

    public static InvoiceFxDataSet getLocalDataSet() {
        if (localDataSet == null) throw new IllegalStateException("Must init repo first");
        return localDataSet;
    }

    static void initSyncManager() {
        syncManager = new SyncManager(getLocalDataSet()) {
            @Override
            protected DataSet initRemote() throws IOException {
                DriveDataSet driveDataSet = new DriveDataSet();
                driveDataSet.loadData();

                return driveDataSet;
            }
        };
    }

    public static SyncManager getSyncManager() {
        if (syncManager == null) {
            throw new IllegalStateException("Must init sync manager first");
        }
        return syncManager;
    }

    public static File getTexTemplate() throws IOException {
        String texTemplateName = sharedPreferences().getString(TEX_TEMPLATE);
        boolean isDefault = false;

        if (texTemplateName.isEmpty()) {
            isDefault = true;
            texTemplateName = DEFAULT_TEX_TEMPLATE;
        }

        File texTemplate = Places.getCustomFile(DATA_DIR, texTemplateName);

        if (!texTemplate.isFile()) {
            if (isDefault) {
                extractResource("/tex_template/" + DEFAULT_TEX_TEMPLATE, texTemplate);
            } else {
                throw new FileNotFoundException("Tex template not found");
            }
        }

        return texTemplate;
    }


    /**
     * Return the local preferences. See {@link LocalProperty}.
     *
     * @return The local preferences
     */
    public static UserPreferences<LocalProperty> localPreferences() {
        return localPreferences;
    }

    /**
     * Return the shared preferences. See {@link SharedProperty}.
     *
     * @return The shared preferences
     */
    public static AbstractPreferences<SharedProperty> sharedPreferences() {
        return localDataSet.getPreferences();
    }


    public static File getInvoiceFile(Invoice invoice) {
        File invoiceDirectory = localPreferences().getPath(INVOICE_DIRECTORY);

        return new File(invoiceDirectory, invoice.getPdfFilename());
    }

    public static Vats getAllVats() {
        String encodedVats = sharedPreferences().getString(VAT);

        try {
            return VatsConverter.toVats(encodedVats);
        } catch (ClassCastException e) {
            ExceptionLogger.logException(e);
            return new Vats(new Vat[0]);
        }
    }

    public static void setAllVats(Vats vats) throws IOException {
        Vats oldVats = getAllVats();

        String encodedVats = VatsConverter.toString(vats);
        sharedPreferences().setProperty(VAT, encodedVats);

        if (UPDATE_ITEMS_ON_VAT_CHANGED) {
            localDataSet.getModelSaver().duplicateActiveItems(oldVats.difference(vats));
        }
    }

    public static String getComputerName() {
        return localPreferences.getString(COMPUTER_NAME);
    }

    /**
     * @return List of Locale available for this App
     */
    public static Locale[] supportedLanguages() {
        return new Locale[]{Locale.FRENCH, Locale.ENGLISH};
    }

    public static void updateCurrencyRate() {
        if (sharedPreferences().getBoolean(AUTO_UPDATE_CURRENCY)) {
            executeAsync(() -> {
                Rates rates = ECBRetriever.instance().retrieveRates();

                if (rates.getEuroToChf() != sharedPreferences().getDouble(EURO_TO_CHF_CURRENCY)) {
                    sharedPreferences().setProperty(EURO_TO_CHF_CURRENCY, rates.getEuroToChf());
                }
            }, ConcurrentUtil.NO_OP);
        }
    }

    public static void extractResource(String resource, File destination) throws IOException {
        try (Accessor accessor = new Accessor()
                .setOut(destination, false)
                .setIn(AppResources.class.getResourceAsStream(resource))) {
            accessor.pourInOut();
        }
    }

    private static void onLocalDataSetChanged(Collection<ChangeEvent> sources) {
        for (ChangeEvent change : sources) {
            if (change.getType() == ChangeEvent.Type.MODEL && ((ModelEvent) change).isInvoiceChange()) {
                saveAndSyncRunner.backupNeeded = true;
            }
        }

        saveAndSyncRunner.execute();
    }

    private AppResources() {
    }


    static class ThrottledSaveAndSync extends ThrottledTask {

        private boolean backupNeeded = false;

        ThrottledSaveAndSync() {
            super(PREFERENCES_SAVE_THROTTLE);
        }

        @Override
        public void run() {
            try {
                InvoiceFxDataSet localDataSet = getLocalDataSet();
                SyncManager syncManager = getSyncManager();

                if (localDataSet.getPreferences().propertiesChanged()) {
                    LOG.fine("Saving preferences");
                    localDataSet.savePreferences();
                }

                if (backupNeeded) {
                    BackupManager.executeBackup();
                    backupNeeded = false;
                }

                if (localDataSet.updateIndex()) {
                    localDataSet.getIndex().save();
                }

                if (!syncManager.isEnabled() &&  DriveManager.isInit() && localPreferences().getBoolean(LocalProperty.ENABLE_SYNC)) {
                    syncManager.enableSync();
                }

                if (syncManager.isEnabled()) {
                    syncManager.synchronizeWithRemote(ConcurrentUtil.NO_OP);
                }
            } catch (IOException e) {
                ExceptionLogger.logException(e);
            }
        }
    }
}
