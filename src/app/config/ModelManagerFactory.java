package app.config;

import app.config.manager.GoogleDriveManager;
import app.config.manager.ModelManager;
import app.config.manager.RuntimeModelManager;
import app.config.preferences.properties.LocalProperty;
import app.model_legacy.invoice.InvoiceList;
import app.model_legacy.invoice.InvoiceModel;
import app.model_legacy.item.ItemModel;
import app.model_legacy.item.ItemsList;

/**
 * Created on 15/03/2016
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public class ModelManagerFactory {

    public enum Impl {
        DRIVE,
        RUNTIME
    }

    private static Impl implementation;

    public static void setImplementation(Impl implementation) {
        ModelManagerFactory.implementation = implementation;
    }

    public static ModelManager<InvoiceModel> createInvoiceManager() {
        checkInit();
        switch (implementation) {
            case DRIVE:
                return new GoogleDriveManager<>(InvoiceList.class, Config.localPreferences().getPathProperty(LocalProperty.STORED_INVOICES_PATH));
            case RUNTIME:
                return new RuntimeModelManager<>();
            default:
                throw new AssertionError();
        }
    }

    public static ModelManager<ItemModel> createItemsManager() {
        checkInit();
        switch (implementation) {
            case DRIVE:
                return new GoogleDriveManager<>(ItemsList.class, Config.localPreferences().getPathProperty(LocalProperty.STORED_ITEMS_PATH));
            case RUNTIME:
                return new RuntimeModelManager<>();
            default:
                throw new AssertionError();
        }
    }

    private static void checkInit() {
        if (implementation == null) {
            throw new IllegalStateException("Must init this factory first");
        }
    }

}
