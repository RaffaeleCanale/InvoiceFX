package com.wx.invoicefx.dataset;

import com.wx.invoicefx.sync.AbstractFileSystem;
import com.wx.invoicefx.sync.index.Index;
import com.wx.invoicefx.util.SimpleObservable;
import com.wx.invoicefx.util.io.InvalidDataException;
import com.wx.properties.page.ResourcePage;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ReadOnlyBooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 08.07.17.
 */
public abstract class DataSet {



    protected static final String DEFAULT_INDEX_FILENAME = "Index.properties";

    private final Observable dataChangedObservable = new SimpleObservable();

    private final BooleanProperty isCorrupted = new SimpleBooleanProperty(false);
    private Throwable exception;

    private final Set<Object> readLocks = Collections.newSetFromMap(new IdentityHashMap<>());
    private Object writeLock;

    private Index index;

    public ReadOnlyBooleanProperty isCorruptedProperty() {
        return isCorrupted;
    }

    public boolean isCorrupted() {
        return isCorrupted.get();
    }

    public Throwable getException() {
        return exception;
    }

    public void addDataChangedListener(Observer listener) {
        dataChangedObservable.addObserver(listener);
    }

    public void removeDataChangedListener(Observer listener) {
        dataChangedObservable.deleteObserver(listener);
    }

    public Index getIndex() {
        return index;
    }

    public void reload() {
        index = null;
        loadData();
    }

    public void loadData() {
        try {
            if (index == null) {
                ResourcePage indexPage = getIndexPage();
                index = new Index(indexPage);

                if (hasContent()) {
                    indexPage.load();
                    index.notifyPageChanged();

                    if (index.isEmpty()) {
                        throw new InvalidDataException("Index is empty");
                    }
                }
            }

            index.testIntegrity();

            loadDataSetContent();
            testDataSetContent();

        } catch (IOException | InvalidDataException e) {
            setCorrupted(e);
        }
    }

    public void clear() throws IOException {
        try (AbstractFileSystem fs = accessFileSystem()) {
            fs.clear();
        }

        index.clear();
    }


    protected abstract void loadDataSetContent() throws IOException;

    public abstract void testDataSetContent() throws InvalidDataException;

    protected abstract ResourcePage getIndexPage();

    protected abstract void populateIndex(Index index) throws IOException;

    protected abstract boolean hasContent();

    public abstract boolean isReachable();

    public final AbstractFileSystem accessFileSystem() {
        return new LockedFileSystem(accessFileSystem0());
    }

    protected abstract AbstractFileSystem accessFileSystem0();

    //    public abstract PropertiesManager getProperties();
    public abstract Optional<String> getProperty(String key);

    protected synchronized void writeLock(Object source) throws BusyException {
        if (writeLock != null) {
            if (writeLock == source) {
                return;
            }
            throw new BusyException(writeLock);
        }
        if (!readLocks.isEmpty()) {
            throw new BusyException(readLocks);
        }

        writeLock = source;
    }

    protected synchronized void readLock(Object source) throws BusyException {
        if (writeLock != null) {
            throw new BusyException(writeLock);
        }

        readLocks.add(source);
    }

    protected synchronized void releaseLock(Object source) {
        if (writeLock == source) {
            writeLock = null;
        } else {
            readLocks.remove(source);
        }
    }

    private void setCorrupted(Throwable exception) {
        this.exception = exception;

        isCorrupted.set(true);

        double version = index.getVersion();
        index.clear();
        try {
            populateIndex(index);
        } catch (Exception e) {
            e.printStackTrace();
        }
        index.setVersion(version);
    }

    protected void notifyChanged(Object source) {
        dataChangedObservable.notifyObservers(source);
    }

    protected void onFilesChanged(Set<String> filenames) {
    }

    private class LockedFileSystem implements AbstractFileSystem {

        private final AbstractFileSystem subSystem;
        private final Set<String> filesChanged = new HashSet<>();
        private boolean isLocked = true;

        public LockedFileSystem(AbstractFileSystem subSystem) {
            this.subSystem = subSystem;
        }

        @Override
        public void clear() throws IOException {
            writeLock(this);
            isLocked = true;

            subSystem.clear();
        }

        @Override
        public InputStream read(String filename) throws IOException {
            readLock(this);
            isLocked = true;

            return subSystem.read(filename);
        }

        @Override
        public void write(String filename, InputStream input) throws IOException {
            writeLock(this);
            isLocked = true;

            subSystem.write(filename, input);

            filesChanged.add(filename);
        }

        @Override
        public void remove(String filename) throws IOException {
            writeLock(this);
            isLocked = true;

            subSystem.remove(filename);

            filesChanged.add(filename);
        }

        @Override
        public void close() {
            if (isLocked) {
                releaseLock(this);
            }

            if (!filesChanged.isEmpty()) {
                onFilesChanged(filesChanged);
            }

            subSystem.close();
        }
    }
}
