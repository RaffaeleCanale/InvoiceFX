package app.config;

import app.legacy.config.ModelManagerFactory;
import app.legacy.config.manager.ModelManager;
import app.config.preferences.UserPreferences;
import app.config.preferences.properties.LocalProperty;
import app.config.preferences.properties.SharedProperty;
import app.google.DriveConfigHelper;
import app.legacy.model.invoice.InvoiceModel;
import app.legacy.model.item.ClientItem;
import app.legacy.model.item.ItemModel;
import app.util.ExceptionLogger;
import app.util.gui.AlertBuilder;
import app.legacy.model.ValidationModel;
import com.wx.io.Accessor;
import com.wx.io.AccessorUtil;
import com.wx.io.TextAccessor;
import com.wx.io.file.FileUtil;
import com.wx.util.log.LogHelper;
import javafx.collections.ObservableList;
import javafx.collections.transformation.FilteredList;

import java.io.*;
import java.net.URISyntaxException;
import java.util.*;
import java.util.function.Function;
import java.util.logging.Logger;
import java.util.prefs.InvalidPreferencesFormatException;
import java.util.prefs.Preferences;
import java.util.stream.Collectors;

import static app.config.preferences.properties.LocalProperty.*;
import static app.google.DriveConfigHelper.Action.UPDATE;

/**
 * This is the main configuration manager. This class manages the {@link ModelManager}s and the user properties.
 * <p>
 * To use the configuration, it must first be initialized ({@link #initConfig(ModelManagerFactory.Impl)}), then, the managers and preferences
 * can be loaded ({@link #loadManagers()}, {@link #loadPreferences()}).
 * <p>
 * <p>
 * Created on 03/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class Config {

    private static final Logger LOG = LogHelper.getLogger(Config.class);
    private static final String CONFIG_DIR_NAME = "Config";
    // TODO: 3/15/16 Add all Config-related constants here! (eg. update, managers locations, etc...)

    private static File configDir;
    private static final UserPreferences<LocalProperty> localPreferences =
            new UserPreferences<>(Preferences.userNodeForPackage(ItemModel.class));
    private static final UserPreferences<SharedProperty> sharedPreferences =
            new UserPreferences<>(Preferences.userNodeForPackage(SharedProperty.class));

    private static ModelManager<ItemModel> itemsManager;
    private static ModelManager<InvoiceModel> invoicesManager;

    private static final Map<Double, ObservableList<ItemModel>> itemGroups = new HashMap<>();

    /**
     * Initialize this manager, this must be the first method called.
     *
     * @throws IOException If the config directory is not found or cannot be created
     * @param modelManagerImplementation
     */
    public static void initConfig(ModelManagerFactory.Impl modelManagerImplementation) throws IOException {
        configDir = loadConfigDirectory(CONFIG_DIR_NAME);
        LOG.info("Configuration directory located: " + configDir.getAbsolutePath());

        ModelManagerFactory.setImplementation(modelManagerImplementation);
        itemsManager = ModelManagerFactory.createItemsManager();
        invoicesManager = ModelManagerFactory.createInvoiceManager();
    }

    /**
     * Load the items and invoices managers.
     *
     * @throws IOException
     */
    public static void loadManagers() throws IOException {
        LOG.info("Loading managers");

        itemsManager.load();
        invoicesManager.load();

        removeInvalidElements(itemsManager);
        removeInvalidElements(invoicesManager);

        ObservableList<ItemModel> items = itemsManager.get();
        try(TextAccessor accessor = new TextAccessor().setOut(new File("Items.txt"), false)) {
            Set<String> names = items.stream().map(ItemModel::getItemName)
                    .collect(Collectors.toSet());
            accessor.write(names);
        }

        ObservableList<InvoiceModel> invoices = invoicesManager.get();
        try(TextAccessor accessor = new TextAccessor().setOut(new File("Addresses.txt"), false)) {
            Set<String> addresses = invoices.stream().map(InvoiceModel::getAddress)
                    .collect(Collectors.toSet());
            accessor.write(addresses);
        }

        try(TextAccessor accessor = new TextAccessor().setOut(new File("Clients.txt"), false)) {
            Set<String> clients = invoices.stream().flatMap(i -> i.getItems().stream())
                    .map(ClientItem::getClientName)
                    .collect(Collectors.toSet());
            accessor.write(clients);
        }

    }

    /**
     * Load all preferences, synchronizing from Google Drive if necessary
     */
    public static void loadPreferences() {
        File prefs = localPreferences.getPathProperty(EXPORTED_PREFERENCES);
        DriveConfigHelper.performAction(UPDATE, params -> {
            boolean updated = (boolean) params[0];
            if (updated) {
                LOG.info("Importing Drive preferences");
                sharedPreferences.clearPreferences();
                try (InputStream is = new BufferedInputStream(new FileInputStream(prefs))) {
                    Preferences.importPreferences(is);
                    sharedPreferences.resyncProperties();
                } catch (IOException | InvalidPreferencesFormatException e) {
                    ExceptionLogger.logException(e);
                    LOG.severe("Importation failed: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
                }
            }
        }, prefs);
    }

    /**
     * Save the shared preferences (if changed) to Google Drive
     */
    public static void saveSharedPreferences() {
        if (sharedPreferences.propertiesChanged()) {
            File prefs = localPreferences.getPathProperty(EXPORTED_PREFERENCES);
            LOG.info("Saving shared preferences at: " + prefs.getAbsolutePath());

            try {
                sharedPreferences.saveToFile(prefs);
                DriveConfigHelper.performAction(DriveConfigHelper.Action.INSERT, null, prefs);
            } catch (IOException e) {
                ExceptionLogger.logException(e);
                LOG.severe("Saving failed: [" + e.getClass().getSimpleName() + "] " + e.getMessage());
            }
        }
    }

    /**
     * Utility method that merges two lists according to a identity function (no duplicates).
     *
     * @param source           Source list that will contain all the merged elements
     * @param toAdd            List to add to the source
     * @param getId            Identity function (should provide a unique identifier for each element of the lists)
     * @param prioritizeSource If set to {@code true}, when elements are present on both lists, only those from the
     *                         source list are kept. Else, those from the second list are kept
     * @param <E>              Type of the list elements
     * @param <F>              Type of the elements identifier
     *
     * @return {@code true} if the source list changed.
     */
    public static <E, F> boolean mergeLists(List<E> source, List<E> toAdd, Function<E, F> getId, boolean prioritizeSource) {
        Map<F, E> idsMap = source.stream().collect(Collectors.toMap(getId, Function.identity(), (i1, i2) -> {
            LOG.severe("Duplicates items detected, the following item might be lost: " + i2);
            return i1;
        }));

        boolean changed = false;

        for (E e : toAdd) {
            F eId = getId.apply(e);
            E existing = idsMap.get(eId);

            if (prioritizeSource) {
                if (existing == null) {
                    source.add(e);
                    changed = true;
                }

            } else {
                if (existing != null) {
                    source.remove(existing);
                }

                source.add(e);
                changed = true;
            }

            idsMap.put(eId, e);
        }

        return changed;
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
    public static UserPreferences<SharedProperty> sharedPreferences() {
        return sharedPreferences;
    }

    /**
     * Save the given manager. Any exception is dealt locally.
     * <p>
     * More specifically, in case of exception, a dialog is started with the user to determine whether to retry or
     * ignore the error.
     *
     * @param manager Manager to save
     */
    public static void saveSafe(ModelManager<?> manager) {
        try {
            LOG.info("Saving manager");
            manager.save();
        } catch (IOException ex) {
            ExceptionLogger.logException(ex);
            int choice = AlertBuilder.error(ex)
                    .key("errors.saving_failed")
                    .button("dialog.retry")
                    .button("dialog.ignore")
                    .show();

            if (choice == 0) {
                saveSafe(manager);
            }
        }
    }

    /**
     * Get a file contained in the config directory.
     *
     * @param name Name (or sub-path) of the file
     *
     * @return File in the config directory
     */
    public static File getConfigFile(String... name) {
        String path = String.join(File.separator, name);
        File file = new File(configDir, path);
        AccessorUtil.createParent(file);

        return file;
    }

    /**
     * Get a file contained in the config directory, ensuring that this file does not exist.
     *
     * @param name      Name of the file
     * @param extension Extension of the file
     *
     * @return File in the config directory
     */
    public static File getFreshConfigFile(String name, String extension) {
        return FileUtil.getFreshFile(configDir, name, extension);
    }

    /**
     * Suggest an invoice id that has not been used.
     *
     * @return A new invoice id
     */
    public static int suggestId() {
        return invoicesManager.get().stream()
                .mapToInt(InvoiceModel::getId)
                .max().orElse(0) + 1;
    }

    /**
     * Get an invoice model by id.
     *
     * @param id Id of the invoice
     *
     * @return The corresponding invoice or {@code null} if no invoice has that id
     */
    public static InvoiceModel getInvoiceById(int id) {
        return invoicesManager.get().stream()
                .filter(i -> i.getId() == id)
                .findAny().orElse(null);
    }

    /**
     * Return the invoice model manager.
     *
     * @return The invoice model manager
     */
    public static ModelManager<InvoiceModel> invoicesManager() {
        return invoicesManager;
    }

    /**
     * Return the item model manager.
     *
     * @return The item model manager
     */
    public static ModelManager<ItemModel> itemsManager() {
        return itemsManager;
    }

    /**
     * Return a sub-list of the item models containing only those with the given VAT.
     *
     * @param vat VAT the item models
     *
     * @return A list containing only the items with the given VAT
     */
    public static ObservableList<ItemModel> getItemGroup(double vat) {
        ObservableList<ItemModel> group = itemGroups.get(vat);
        if (group == null) {
            group = new FilteredList<>(itemsManager.get(), i -> i.getTva() == vat);
            itemGroups.put(vat, group);
        }

        return group;
    }

    /**
     * Return the config directory.
     *
     * @return The config directory
     */
    public static File getConfigDirectory() {
        return configDir;
    }

    private static <E extends ValidationModel> void removeInvalidElements(ModelManager<E> manager) {
        for (Iterator<E> iterator = manager.get().iterator(); iterator.hasNext(); ) {
            E element = iterator.next();

            if (!element.isValid()) {
                LOG.warning("Invalid element removed: " + element);
                element.diagnosis(LOG);
                iterator.remove();
            }
        }
    }

    private static File loadConfigDirectory(String CONFIG_DIR) throws IOException {
        String programDir;
        try {
            programDir = Config.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        File dir = new File(programDir);
        if (dir.isFile()) {
            dir = dir.getParentFile();
        }


        dir = new File(dir, CONFIG_DIR);
        if (!dir.exists() && !dir.mkdir()) {
            throw new FileNotFoundException(dir.getAbsolutePath());
        }

        return dir;
    }

    /**
     * Find the application launcher, that is, the executable Jar file.
     * <p>
     * If this application has not been compiled in a .jar file, then a {@code RuntimeException} is thrown.
     *
     * @return The executable Jar file
     */
    public static File getAppLauncher() throws IOException {
        try {
            File file = new File(Config.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
            if (!file.getName().endsWith(".jar")) {
                throw new IOException("Not a jar file: " + file.getAbsolutePath());
            }
            return file;
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }
    }

    private Config() {
    }

}
