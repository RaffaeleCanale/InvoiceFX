package com.wx.invoicefx.sync.index;

import com.wx.io.file.FileUtil;
import com.wx.properties.page.ResourcePage;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.05.17.
 */
public class FileInfo {

    static void remove(ResourcePage index, int i) {
        index.removeProperty(Index.IndexProperties.FILES.key(i, "filename"));
        index.removeProperty(Index.IndexProperties.FILES.key(i, "timestamp"));
        index.removeProperty(Index.IndexProperties.FILES.key(i, "check_sum"));
        index.removeProperty(Index.IndexProperties.FILES.key(i, "size"));
    }

    private final String filename;
    private final long timestamp;
    private final byte[] checkSum;
    private final long fileSize;

    public FileInfo(File file) throws IOException {
        this.filename = file.getName();
        this.timestamp = file.lastModified();
        this.checkSum = FileUtil.checkSum(file);
        this.fileSize = file.length();
    }

    FileInfo(String filename, long timestamp, byte[] checkSum, long fileSize) {
        this.filename = filename;
        this.timestamp = timestamp;
        this.checkSum = checkSum;
        this.fileSize = fileSize;
    }

    FileInfo(ResourcePage index, int i) {
        this.filename = index.getString(Index.IndexProperties.FILES.key(i, "filename")).orElse("");
        this.timestamp = index.getLong(Index.IndexProperties.FILES.key(i, "timestamp")).orElse(0L);
        this.checkSum = index.getBytes(Index.IndexProperties.FILES.key(i, "check_sum")).orElse(new byte[0]);
        this.fileSize = index.getLong(Index.IndexProperties.FILES.key(i, "size")).orElse(0L);
    }

    public boolean matches(File file) {
        return file.getName().equals(filename) && timestamp == file.lastModified() && fileSize == file.length();
    }

    void setTo(ResourcePage index, int i) {
        index.setProperty(Index.IndexProperties.FILES.key(i, "filename"), filename);
        index.setProperty(Index.IndexProperties.FILES.key(i, "timestamp"), timestamp);
        index.setProperty(Index.IndexProperties.FILES.key(i, "check_sum"), checkSum);
        index.setProperty(Index.IndexProperties.FILES.key(i, "size"), fileSize);
    }

    public String getFilename() {
        return filename;
    }

    public long getTimestamp() {
        return timestamp;
    }

    public byte[] getCheckSum() {
        return checkSum;
    }

    public long getFileSize() {
        return fileSize;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;

        FileInfo fileInfo = (FileInfo) o;

        if (timestamp != fileInfo.timestamp) return false;
        if (!filename.equals(fileInfo.filename)) return false;
        return Arrays.equals(checkSum, fileInfo.checkSum);

    }

    @Override
    public int hashCode() {
        int result = filename.hashCode();
        result = 31 * result + (int) (timestamp ^ (timestamp >>> 32));
        result = 31 * result + Arrays.hashCode(checkSum);
        return result;
    }

    @Override
    public String toString() {
        return "FileInfo{" +
                "filename='" + filename + '\'' +
                ", timestamp=" + timestamp +
                ", checkSum=" + Arrays.toString(checkSum) +
                ", fileSize=" + fileSize +
                '}';
    }
}
