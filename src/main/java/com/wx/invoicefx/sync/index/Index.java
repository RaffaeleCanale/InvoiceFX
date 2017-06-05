package com.wx.invoicefx.sync.index;

import com.wx.invoicefx.util.InvalidDataException;
import com.wx.properties.page.ResourcePage;

import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import static com.wx.invoicefx.sync.index.Index.IndexProperties.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.05.17.
 */
public class Index {

    public static final double VERSION_INCREMENT = 0.1;

    enum IndexProperties {
        VERSION,
        BRANCH_VERSION,
        FILES_COUNT,
        FILES;

        public String key(Object... properties) {
            String suffix = "";

            if (properties.length > 0) {
                suffix = "." + Stream.of(properties).map(Object::toString).collect(Collectors.joining("."));
            }
            return name().toLowerCase() + suffix;
        }
    }


    final ResourcePage page;
    private final Map<String, Integer> filenameToIndexMap = new HashMap<>();

    public Index(ResourcePage page) {
        this.page = page;

        reloadNamesMap();
    }


    public void copyTo(Index other) {
        other.page.clear();

        other.filenameToIndexMap.clear();
        other.filenameToIndexMap.putAll(filenameToIndexMap);

        page.keySet().stream()
                .filter(key -> !key.equals(BRANCH_VERSION.key()))
                .forEach(key -> other.page.setProperty(key, page.getString(key).get()));
    }

    public double getVersion() {
        return page.getDouble(VERSION.key()).get();
    }

    public void setVersion(double version) {
        if (version < getBranchVersion()) throw new IllegalArgumentException("Version cannot be smaller than branch version");
        page.setProperty(VERSION.key(), version);
    }

    public void incrementVersion() {
        setVersion(getVersion() + VERSION_INCREMENT);
    }

    public double getBranchVersion() {
        return page.getDouble(BRANCH_VERSION.key()).orElse(0.0);
    }

    public void setBranchVersion(double version) {
        if (version > getVersion()) throw new IllegalArgumentException("Version cannot be smaller than branch version");

        page.setProperty(BRANCH_VERSION.key(), version);
    }

    public int getFilesCount() {
        return page.getInt(FILES_COUNT.key()).get();
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

    public void testIntegrity() throws InvalidDataException {
        if (isEmpty()) return;

        try {
            page.getDouble(VERSION.key()).orElseThrow(() -> new InvalidDataException("Version is invalid"));
            page.getDouble(BRANCH_VERSION.key()).orElse(0.0);
            page.getInt(FILES_COUNT.key()).orElseThrow(() -> new InvalidDataException("Files count is invalid"));

            int filesCount = getFilesCount();
            for (int i = 0; i < filesCount; i++) {
                page.getString(Index.IndexProperties.FILES.key(i, "filename")).orElseThrow(() -> new InvalidDataException("Filename is invalid"));
                page.getLong(Index.IndexProperties.FILES.key(i, "timestamp")).orElseThrow(() -> new InvalidDataException("Timestamp is invalid"));
                page.getBytes(Index.IndexProperties.FILES.key(i, "check_sum")).orElseThrow(() -> new InvalidDataException("Checksum is invalid"));
            }

            int expectedKeysCount = 2 + 3*getFilesCount();
            if (page.getDouble(BRANCH_VERSION.key()).isPresent()) {
                expectedKeysCount++;
            }

            if (expectedKeysCount != page.keySet().size()) {
                throw new InvalidDataException("Too many or too few keys, expected " + expectedKeysCount + " but got " + page.keySet().size());
            }

            if (getBranchVersion() > getVersion()) {
                throw new InvalidDataException("Branch version is greater than version");
            }

        } catch (ClassCastException e) {
            throw new InvalidDataException(e);
        }
    }

    public boolean isEmpty() {
        return page.keySet().isEmpty();
    }

    public void save() throws IOException {
        page.save();
    }

    public List<String> getAllFilenames() {
        int filesCount = getFilesCount();
        List<String> filenames = new ArrayList<>(filesCount);

        for (int i = 0; i < filesCount; i++) {
            FileInfo fileInfo = getFileInfo(i);

            filenames.add(fileInfo.getFilename());
        }

        return filenames;
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
