package com.wx.invoicefx.dataset.impl;

import com.wx.fx.preferences.AbstractPreferences;
import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.invoicefx.config.preferences.CachedResourcePagePreferences;
import com.wx.invoicefx.config.preferences.shared.SharedProperty;
import com.wx.invoicefx.dataset.LocalDataSet;
import com.wx.invoicefx.dataset.impl.event.ChangeEvent;
import com.wx.invoicefx.dataset.impl.event.ModelEvent;
import com.wx.invoicefx.dataset.impl.event.PreferencesEvent;
import com.wx.invoicefx.io.file.DirectoryStorage;
import com.wx.invoicefx.model.entities.client.Client;
import com.wx.invoicefx.model.entities.invoice.Invoice;
import com.wx.invoicefx.model.entities.item.Item;
import com.wx.invoicefx.model.save.ModelSaver;
import com.wx.invoicefx.model.save.table.ClientsTable;
import com.wx.invoicefx.model.save.table.ItemsTable;
import com.wx.invoicefx.util.concurrent.ThrottledTask;
import com.wx.invoicefx.util.io.InvalidDataException;
import com.wx.util.future.IoIterator;
import com.wx.util.log.LogHelper;

import java.io.File;
import java.io.IOException;
import java.util.*;
import java.util.logging.Logger;
import java.util.stream.Collectors;

import static java.util.Collections.singleton;
import static org.apache.pdfbox.pdmodel.documentinterchange.taggedpdf.StandardStructureTypes.types;


/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 08.07.17.
 */
public class InvoiceFxDataSet extends LocalDataSet {

    private static final Logger LOG = LogHelper.getLogger(InvoiceFxDataSet.class);

    private static final String PREFERENCES_FILENAME = "Preferences.xml";


//    private final ThrottledPreferencesSave throttledPreferencesSaveTask = new ThrottledPreferencesSave();
    private ModelSaver modelSaver;
    private CachedResourcePagePreferences<SharedProperty> preferences;
    private final String setType;

    public InvoiceFxDataSet(File dataDirectory, String setType) {
        super(dataDirectory);
        this.setType = setType;
    }

    public AbstractPreferences<SharedProperty> getPreferences() {
        return preferences;
    }

    public void savePreferences() throws IOException {
        try {
            writeLock(this);
            preferences.save();
        } finally {
            releaseLock(this);
        }
    }

    @Override
    protected void onFilesChanged(Set<String> filenames) {
        super.onFilesChanged(filenames);


        Set<ChangeEvent> changes = new HashSet<>();


        try {
            if (filenames.contains(PREFERENCES_FILENAME)) {
                preferences.load();
                changes.add(new PreferencesEvent(null));
            }

            Optional<ModelEvent> modelEvent = getModelEvent(filenames);
            if (modelEvent.isPresent()) {
                modelSaver.invalidate();
                changes.add(modelEvent.get());
            }
        } catch (IOException e) {
            ExceptionLogger.logException(e);
        }

        notifyChanged(changes);
    }

    private Optional<ModelEvent> getModelEvent(Set<String> filenames) {
        boolean invoiceChange = false, clientChange = false, itemChange = false;

        for (String filename : filenames) {
            if (filename.endsWith(DirectoryStorage.PARTITION_FILE_EXTENSION)) {
                if (filename.startsWith(ClientsTable.PARTITION_FILE_PREFIX)) {
                    clientChange = true;
                } else if (filename.startsWith(ItemsTable.PARTITION_FILE_PREFIX)) {
                    itemChange = true;
                } else {
                    invoiceChange = true;
                }
            }
        }

        if (invoiceChange || clientChange || itemChange) {
            return Optional.of(new ModelEvent(itemChange, clientChange, invoiceChange));
        } else {
            return Optional.empty();
        }
    }

    public ModelSaver getModelSaver() {
        return modelSaver;
    }

    @Override
    public Optional<String> getProperty(String key) {
        if (key.equals("type")) {
            return Optional.of(setType);
        }

        return getModelSaver().getMetadataPage().getString(key);
    }

    @Override
    public boolean isReachable() {
        return super.isReachable() && modelSaver != null;
    }

    @Override
    protected boolean hasContent() {
        return getDataSetFiles().length > 1;
    }

    @Override
    protected void loadDataSetContent() throws IOException {
        if (modelSaver == null) {
            writeLock(this);
            modelSaver = new CoupledModelSaver();
            updateIndex();
            releaseLock(this);
        }

        File preferencesFile = getFile(PREFERENCES_FILENAME);
        if (preferences == null) {
            preferences = new CachedResourcePagePreferences<SharedProperty>(preferencesFile) {

                @Override
                protected void fireChanged(SharedProperty key) {
                    super.fireChanged(key);

                    notifyChanged(singleton(new PreferencesEvent(key)));
                }
            };
        }

        if (preferencesFile.exists()) {
            preferences.load();
        }
    }

    @Override
    public void testDataSetContent() throws InvalidDataException {
        testDataIntegrityThroughIndex();

//        try {
//            modelSaver.testDataCorrupted();
//        } catch (IOException e) {
//            throw new InvalidDataException(e);
//        }
    }

    private class CoupledModelSaver extends ModelSaver {

        CoupledModelSaver() throws IOException {
            super(dataDirectory);
        }

        @Override
        public boolean invoiceExists(long id) throws IOException {
            try {
                readLock(this);
                return super.invoiceExists(id);
            } finally {
                releaseLock(this);
            }
        }

        @Override
        public long getNextInvoiceId() {
            return super.getNextInvoiceId();
        }

        @Override
        public IoIterator<Invoice> getAllInvoices() throws IOException {
            try {
                readLock(this);
                return super.getAllInvoices();
            } finally {
                releaseLock(this);
            }
        }

        @Override
        public void addInvoices(Collection<Invoice> invoices) throws IOException {
            try {
                writeLock(this);
                super.addInvoices(invoices);
            } finally {
                releaseLock(this);
                notifyChanged(singleton(new ModelEvent(true, true, true)));
            }
        }

        @Override
        public boolean removeInvoicesById(Collection<Long> invoicesId) throws IOException {
            try {
                writeLock(this);
                return super.removeInvoicesById(invoicesId);
            } finally {
                releaseLock(this);
                notifyChanged(singleton(ModelEvent.invoiceChange()));
            }
        }

        @Override
        public IoIterator<Client> getAllClients() throws IOException {
            try {
                readLock(this);
                return super.getAllClients();
            } finally {
                releaseLock(this);
            }
        }

        @Override
        public IoIterator<Client> getAllClients(int limit) throws IOException {
            try {
                readLock(this);
                return super.getAllClients(limit);
            } finally {
                releaseLock(this);
            }
        }

        @Override
        public IoIterator<Item> getAllActiveItems() throws IOException {
            try {
                readLock(this);
                return super.getAllActiveItems();
            } finally {
                releaseLock(this);
            }
        }

        @Override
        public IoIterator<Item> getAllItems() throws IOException {
            try {
                readLock(this);
                return super.getAllItems();
            } finally {
                releaseLock(this);
            }
        }

        @Override
        public IoIterator<Item> getAllItems(int limit) throws IOException {
            try {
                readLock(this);
                return super.getAllItems(limit);
            } finally {
                releaseLock(this);
            }
        }

        @Override
        public boolean isReferenced(long itemId) throws IOException {
            try {
                readLock(this);
                return super.isReferenced(itemId);
            } finally {
                releaseLock(this);
            }
        }

        @Override
        public void addItems(Collection<Item> items) throws IOException {
            try {
                writeLock(this);
                super.addItems(items);
            } finally {
                releaseLock(this);
                notifyChanged(singleton(ModelEvent.itemChange()));
            }
        }

        @Override
        public boolean updateItems(Collection<Item> items) throws IOException {
            try {
                writeLock(this);
                return super.updateItems(items);
            } finally {
                releaseLock(this);
                notifyChanged(singleton(ModelEvent.itemChange()));
            }
        }

        @Override
        public boolean removeItemsById(Collection<Long> itemsId) throws IOException {
            try {
                writeLock(this);
                return super.removeItemsById(itemsId);
            } finally {
                releaseLock(this);
                notifyChanged(singleton(ModelEvent.itemChange()));
            }
        }
    }


}
