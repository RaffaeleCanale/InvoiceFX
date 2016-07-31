package app.legacy.config;

import app.config.Config;
import app.legacy.config.manager.GoogleDriveManager;
import app.legacy.config.manager.ModelManager;
import app.legacy.config.manager.RuntimeModelManager;
import app.legacy.model.invoice.InvoiceList;
import app.legacy.model.invoice.InvoiceModel;
import app.legacy.model.item.ItemModel;
import app.legacy.model.item.ItemsList;

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
