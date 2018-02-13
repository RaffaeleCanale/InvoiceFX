package com.wx.invoicefx.ui.views.settings.advanced.table;

import com.wx.properties.page.ResourcePage;
import javafx.util.converter.DefaultStringConverter;

import java.io.IOException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 20.06.17.
 */
public class ResourcePageTable extends AbstractKeyValueTable<String, String> {

    private ResourcePage page;

    public ResourcePageTable() {
        super(new DefaultStringConverter());
    }

    public void setPage(ResourcePage page) {
        this.page = page;

        setItems(page.keySet());
    }

    @Override
    protected String get(String key) {
        return page.getString(key).orElse("");
    }

    @Override
    protected boolean set(String key, String value) {
        page.setProperty(key, value);
        return trySave();
    }

    @Override
    protected boolean remove(String key, String value) {
        page.removeProperty(key);
        return trySave();
    }

    private boolean trySave() {
        try {
            page.save();
            return true;
        } catch (IOException e) {
            e.printStackTrace();
        }

        return false;
    }
}
