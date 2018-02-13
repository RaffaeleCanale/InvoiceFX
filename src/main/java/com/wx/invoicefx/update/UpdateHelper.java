package com.wx.invoicefx.update;

import com.wx.invoicefx.command.CommandRunner;
import com.wx.invoicefx.config.Places;
import com.wx.invoicefx.dataset.LocalDataSet;
import com.wx.invoicefx.sync.PushPullSync;
import com.wx.invoicefx.util.DesktopUtils;
import com.wx.invoicefx.util.io.InvalidDataException;
import com.wx.properties.page.ResourcePage;
import com.wx.properties.structures.map.MapResource;
import com.wx.properties.structures.map.RefHashMap;
import com.wx.util.representables.string.StringRepr;
import javafx.application.Platform;
import javafx.beans.property.IntegerProperty;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.util.HashMap;
import java.util.List;
import java.util.Optional;

import static com.wx.invoicefx.App.APPLICATION_VERSION;
import static com.wx.invoicefx.config.Places.Dirs.UPDATE;
import static com.wx.invoicefx.config.Places.Files.UPDATER;
import static com.wx.invoicefx.config.Places.Files.UPDATER_CONFIG;


/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 01.08.17.
 */
public class UpdateHelper {

    private static RemoteUpdateDataSet remoteUpdateDataSet;


    public static double getVersion() {
        return APPLICATION_VERSION;
    }

    public static void createIndex() throws IOException {
        LocalUpdateDataSet dataSet = new LocalUpdateDataSet();
        dataSet.loadData();
        dataSet.updateIndex();

        dataSet.getIndex().setVersion(getVersion());
        dataSet.getIndex().save();
    }

    public static void addUrl(String filename, String url) throws IOException {
        StringRepr stringCaster = new StringRepr();

        LocalUpdateDataSet dataSet = new LocalUpdateDataSet();
        dataSet.loadData();

        MapResource<String, String> urlMap = dataSet.getIndex().getPage().getMap("url_map", stringCaster, stringCaster);
        if (urlMap == null) {
            urlMap = dataSet.getIndex().getPage().setMap("url_map", new HashMap<>(), stringCaster, stringCaster, RefHashMap.class);
        }

        urlMap.put(filename, url);

        dataSet.getIndex().getPage().save();
    }

    public static Optional<Double> getUpdateVersion() throws IOException {
        RemoteUpdateDataSet server = getRemoteUpdateDataSet();
        server.reload();
        double remoteVersion = server.getIndex().getVersion();

        if (remoteVersion > getVersion()) {
            return Optional.of(remoteVersion);
        } else {
            return Optional.empty();
        }
    }

    public static void downloadUpdate(IntegerProperty progressProperty) throws IOException {
        File updateDir = Places.getDir(UPDATE);

        DesktopUtils.deleteDirContent(updateDir);

        LocalDataSet localDataSet = new EmptyDataSet(updateDir);
        localDataSet.loadData();

        PushPullSync pushPullSync = new PushPullSync(localDataSet, getRemoteUpdateDataSet());
        if (progressProperty != null) {
            Platform.runLater(() -> progressProperty.bind(pushPullSync.progressProperty().multiply(100.0)));
        }

        pushPullSync.pull();
    }

    public static void executeUpdateScript() throws IOException {
        File updater = Places.getFile(UPDATER);
        File updaterConfig = Places.getFile(UPDATER_CONFIG);
        if (!updater.isFile()) {
            throw new FileNotFoundException("Missing script file: " + updater);
        }
        if (!updaterConfig.isFile()) {
            throw new FileNotFoundException("Missing script file: " + updaterConfig);
        }

//        File jarParent = Places.getConfigDir().getParentFile();
//        File updateDir = Places.getDir(UPDATE);

//        String[] cmd = {updater.getAbsolutePath(), jarParent.getAbsolutePath(), updateDir.getAbsolutePath()};
//        String[] cmd = {"java" ,"-jar", updater.getAbsolutePath(), updaterConfig.getAbsolutePath()};
        List<String> cmd = CommandRunner.getInstance(null, null, "java -jar \"" + updater.getAbsolutePath() + "\" \"" + updaterConfig.getAbsolutePath() + "\"").getCmd();
        Runtime.getRuntime().exec(cmd.toArray(new String[cmd.size()]));


        System.exit(0);
    }

    private static RemoteUpdateDataSet getRemoteUpdateDataSet() throws IOException {
        if (remoteUpdateDataSet == null) {
            remoteUpdateDataSet = new RemoteUpdateDataSet();
            remoteUpdateDataSet.loadData();
        }

        if (remoteUpdateDataSet.isCorrupted()) {
            throw new IOException(remoteUpdateDataSet.getException());
        }

        return remoteUpdateDataSet;
    }

    private static class EmptyDataSet extends LocalDataSet {

        public EmptyDataSet(File dataDirectory) {
            super(dataDirectory);
        }

        @Override
        protected void loadDataSetContent() throws IOException {}

        @Override
        public void testDataSetContent() throws InvalidDataException {}

        @Override
        protected boolean hasContent() {
            return true;
        }

        @Override
        public Optional<String> getProperty(String key) {
            return Optional.empty();
        }
    }



    private static class LocalUpdateDataSet extends LocalDataSet {

        public LocalUpdateDataSet() {
            super(Places.getConfigDir().getParentFile());
        }

        @Override
        protected ResourcePage getIndexPage() {
            File indexFile = getFile("UpdateIndex.properties");

            return ResourcePage.builder().fromFile(indexFile).get();
        }

        @Override
        protected File[] getDataSetFiles() {
            return new File[] {
                    getFile("InvoiceFX.jar")
            };
        }

        @Override
        protected void loadDataSetContent() throws IOException {

        }

        @Override
        public void testDataSetContent() throws InvalidDataException {

        }

        @Override
        protected boolean hasContent() {
            return true;
        }

        @Override
        public Optional<String> getProperty(String key) {
            return Optional.empty();
        }
    }

}
