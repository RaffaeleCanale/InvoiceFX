package com.wx.invoicefx.sync.index;

import com.wx.invoicefx.util.io.InvalidDataException;
import com.wx.invoicefx.util.math.CurrencyUtils;
import com.wx.properties.page.ResourcePage;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wx.invoicefx.sync.index.Index.IndexProperties.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.05.17.
 */
public class Index {

    public static final double VERSION_INCREMENT = 0.1;

    private static final String KEY_PREFIX = "index.";


    enum IndexProperties {
        LAST_MODIFIED_DATE,
        LAST_MODIFIED_AUTHOR,
        VERSION,
        BASE_VERSION,
        FILES_COUNT,
        FILES;

        public String key(Object... properties) {
            String suffix = "";

            if (properties.length > 0) {
                suffix = "." + Stream.of(properties).map(Object::toString).collect(Collectors.joining("."));
            }
            return KEY_PREFIX + name().toLowerCase() + suffix;
        }
    }


    private final ResourcePage page;
    private final Map<String, Integer> filenameToIndexMap = new HashMap<>();

    public Index(ResourcePage page) {
        this.page = page;

        reloadNamesMap();
    }

    public ResourcePage getPage() {
        return page;
    }

    public void notifyPageChanged() {
        reloadNamesMap();
    }



    public double getVersion() {
        return page.getDouble(VERSION.key()).orElse(0.0);
    }

    public void setVersion(double version) {
        if (version < getBaseVersion())
            throw new IllegalArgumentException("Version cannot be smaller than branch version");
        page.setProperty(VERSION.key(), CurrencyUtils.roundToTwoPlaces(version));
    }

    public void incrementVersion() {
        setVersion(getVersion() + VERSION_INCREMENT);
    }

    public double getBaseVersion() {
        return page.getDouble(BASE_VERSION.key()).orElse(0.0);
    }

    public void setBaseVersion(double version) {
        if (version > getVersion()) throw new IllegalArgumentException("Version cannot be smaller than branch version");

        page.setProperty(BASE_VERSION.key(), CurrencyUtils.roundToTwoPlaces(version));
    }

    public int getFilesCount() {
        return page.getInt(FILES_COUNT.key()).orElse(0);
    }

    public FileInfo getFileInfo(int i) {
        return new FileInfo(page, i);
    }

    public FileInfo getFileInfo(String filename) {
        Integer i = filenameToIndexMap.get(filename);
        if (i == null) {
            return null;
        }

        return getFileInfo(i);
    }

    public void setFiles(FileInfo[] infos) {
        filenameToIndexMap.clear();

        for (int i = 0; i < infos.length; i++) {
            FileInfo info = infos[i];

            info.setTo(page, i);
            filenameToIndexMap.put(info.getFilename(), i);
        }

        int filesCount = page.getInt(FILES_COUNT.key()).orElse(0);
        for (int i = infos.length; i < filesCount; i++) {
            FileInfo.remove(page, i);
        }

        setFilesCount(infos.length);
    }

    public void setLastModified(String author, Date time) {
        page.setProperty(LAST_MODIFIED_AUTHOR.key(), author);
        page.setProperty(LAST_MODIFIED_DATE.key(), time.getTime());
    }

    public String getLastModifiedAuthor() {
        return page.getString(LAST_MODIFIED_AUTHOR.key()).orElse(null);
    }

    public Date getLastModifiedDate() {
        return page.getLong(LAST_MODIFIED_DATE.key())
                .map(Date::new)
                .orElse(null);
    }

    public void testIntegrity() throws InvalidDataException {
        if (isEmpty()) return;

        try {
            page.getDouble(VERSION.key()).orElseThrow(() -> new InvalidDataException("Version is invalid"));
            page.getDouble(BASE_VERSION.key()).orElse(0.0);
            page.getInt(FILES_COUNT.key()).orElseThrow(() -> new InvalidDataException("Files count is invalid"));

            int filesCount = getFilesCount();
            for (int i = 0; i < filesCount; i++) {
                page.getString(Index.IndexProperties.FILES.key(i, "filename")).orElseThrow(() -> new InvalidDataException("Filename is invalid"));
                page.getLong(Index.IndexProperties.FILES.key(i, "timestamp")).orElseThrow(() -> new InvalidDataException("Timestamp is invalid"));
                page.getBytes(Index.IndexProperties.FILES.key(i, "check_sum")).orElseThrow(() -> new InvalidDataException("Checksum is invalid"));
            }

//            int expectedKeysCount = 2 + 3 * getFilesCount();
//            if (page.getDouble(BASE_VERSION.key()).isPresent()) {
//                expectedKeysCount++;
//            }
//            if (page.getString(LAST_MODIFIED_AUTHOR.key()).isPresent()) {
//                expectedKeysCount++;
//            }
//            if (page.getLong(LAST_MODIFIED_DATE.key()).isPresent()) {
//                expectedKeysCount++;
//            }
//
//            int keysCount = (int) keySet().count();
//            if (expectedKeysCount != keysCount) {
//                throw new InvalidDataException("Too many or too few keys, expected " + expectedKeysCount + " but got " + keysCount);
//            }

            if (getBaseVersion() > getVersion()) {
                throw new InvalidDataException("Branch version is greater than version");
            }

        } catch (ClassCastException e) {
            throw new InvalidDataException(e);
        }
    }

    public boolean isEmpty() {
        return !keySet().findAny().isPresent();
    }

    public void save() throws IOException {
        page.save();
    }

    public Map<String, FileInfo> getAllFiles() {
        int filesCount = getFilesCount();
        Map<String, FileInfo> files = new HashMap<>(filesCount);

        for (int i = 0; i < filesCount; i++) {
            FileInfo fileInfo = getFileInfo(i);
            files.put(fileInfo.getFilename(), fileInfo);
        }

        return files;
    }

    public void clear() {
        page.removeProperties(keySet().collect(Collectors.toList()));
    }

    public void copyTo(Index other) {
        other.clear();

        other.filenameToIndexMap.clear();
        other.filenameToIndexMap.putAll(filenameToIndexMap);

        keySet().filter(key -> !key.equals(BASE_VERSION.key()))
                .forEach(key -> other.page.setProperty(key, page.getString(key).get()));
    }

    private Stream<String> keySet() {
        return page.keySet().stream().filter(s -> s.startsWith(KEY_PREFIX));
    }

    private void setFilesCount(int n) {
        page.setProperty(FILES_COUNT.key(), n);
    }

    private void reloadNamesMap() {
        filenameToIndexMap.clear();

        if (isEmpty()) return;

        int filesCount = getFilesCount();
        for (int i = 0; i < filesCount; i++) {
            FileInfo info = getFileInfo(i);

            filenameToIndexMap.put(info.getFilename(), i);
        }
    }


}
