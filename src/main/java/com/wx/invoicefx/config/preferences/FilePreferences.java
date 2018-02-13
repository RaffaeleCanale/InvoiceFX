package com.wx.invoicefx.config.preferences;

import com.wx.fx.preferences.properties.UserProperty;
import com.wx.invoicefx.util.SimpleObservable;

import java.io.File;
import java.io.IOException;
import java.util.Observable;
import java.util.Observer;
import java.util.prefs.InvalidPreferencesFormatException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 05.07.17.
 */
public class FilePreferences<E extends UserProperty> extends CachedUserPreferences<E> {

    private final Observable onLoadObservable = new SimpleObservable();

    private File file;
    private long lastLoaded = -1;

    public FilePreferences(Class<E> cls) {
        super(cls);
    }

    public void addOnLoadListener(Observer o) {
        onLoadObservable.addObserver(o);
    }

    public void removeOnLoadListener(Observer o) {
        onLoadObservable.deleteObserver(o);
    }

    public void setFile(File file) {
        this.file = file;
    }

    public void load() throws IOException, InvalidPreferencesFormatException {
        loadFromFile(file);
        lastLoaded = file.lastModified();

        onLoadObservable.notifyObservers();
    }

    public void save() throws IOException {
        saveToFile(file);
        lastLoaded = file.lastModified();
    }

    public boolean fileHasChanged() {
        return lastLoaded != file.lastModified();
    }

}
