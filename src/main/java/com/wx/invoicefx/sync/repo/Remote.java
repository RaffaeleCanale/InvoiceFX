package com.wx.invoicefx.sync.repo;

import com.wx.invoicefx.sync.index.Index;
import com.wx.properties.page.ResourcePage;

import java.io.File;
import java.io.IOException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.05.17.
 */
public abstract class Remote {

    private final Index index;

    public Remote(ResourcePage index) {
        this.index = new Index(index);
    }

    public Index getIndex() {
        return index;
    }

    public abstract void downloadFile(String filename, File destination) throws IOException;

    public abstract void uploadFile(String filename, File file) throws IOException;

    public abstract boolean isReachable();

    public abstract void removeFile(String filename) throws IOException;
}
