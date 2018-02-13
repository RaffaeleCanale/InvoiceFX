package com.wx.invoicefx.update;

import com.wx.invoicefx.dataset.DataSet;
import com.wx.invoicefx.sync.AbstractFileSystem;
import com.wx.invoicefx.sync.index.Index;
import com.wx.invoicefx.util.io.InvalidDataException;
import com.wx.properties.page.ResourcePage;
import com.wx.servercomm.http.HttpRequest;
import com.wx.util.representables.string.StringRepr;

import java.io.IOException;
import java.io.InputStream;
import java.util.Map;
import java.util.Optional;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 01.08.17.
 */
class RemoteUpdateDataSet extends DataSet {

    private static final String INDEX_URL = "https://drive.google.com/uc?export=download&id=0B6LgrYnciPdhc2lKbW1pOWxWRUk";

    private final ResourcePage indexPage = ResourcePage.builder()
            .fromLinks(() -> HttpRequest.createGET().executeAsStream(INDEX_URL),
                    () -> {
                        throw new UnsupportedOperationException();
                    }).get();
    private Map<String, String> urlMap;

    @Override
    protected void loadDataSetContent() throws IOException {
        StringRepr stringCaster = new StringRepr();
        urlMap = indexPage.getMap("url_map", stringCaster, stringCaster);
    }

    @Override
    public void testDataSetContent() throws InvalidDataException {
        if (urlMap == null) {
            throw new InvalidDataException("url_map not found");
        }
    }

    @Override
    protected ResourcePage getIndexPage() {
        return indexPage;
    }

    @Override
    protected void populateIndex(Index index) throws IOException {
    }

    @Override
    protected boolean hasContent() {
        return true;
    }

    @Override
    public boolean isReachable() {
        return true;
    }

    @Override
    protected AbstractFileSystem accessFileSystem0() {
        return new HttpFileSystem();
    }

    private String getUrlFor(String filename) {
        String url = urlMap.get(filename);
        if (url == null) {
            throw new RuntimeException("url for '" + filename + "' not found");
        }

        return url;
    }

    @Override
    public Optional<String> getProperty(String key) {
        return null;
    }

    private class HttpFileSystem implements AbstractFileSystem {

        private final HttpRequest httpGet = HttpRequest.createGET();

        @Override
        public void clear() throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public InputStream read(String filename) throws IOException {
            return httpGet.executeAsStream(getUrlFor(filename));
        }

        @Override
        public void write(String filename, InputStream input) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void remove(String filename) throws IOException {
            throw new UnsupportedOperationException();
        }

        @Override
        public void close() {}
    }
}
