package com.wx.invoicefx.io.file;


import com.wx.invoicefx.io.interfaces.DataFile;
import com.wx.invoicefx.io.interfaces.PartitionedStorage;
import com.wx.invoicefx.io.interfaces.RecordSerializer;
import com.wx.io.Accessor;

import java.io.EOFException;
import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Stream;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.16.
 */
public class DirectoryStorage implements PartitionedStorage {

    public static final String PARTITION_FILE_EXTENSION = ".part";

    private final RecordSerializer serializer;
    private final File directory;
    private final String partitionFilePrefix;

    private int partitionsCountBuffer;

    public DirectoryStorage(RecordSerializer serializer, File directory, String partitionFilePrefix) {
        this.serializer = serializer;
        this.directory = directory;
        this.partitionFilePrefix = partitionFilePrefix + "_";
        partitionsCountBuffer = -1;
    }

    @Override
    public DataFile getPartition(int partitionIndex) {
        if (partitionIndex < 0) {
            throw new IllegalArgumentException();
        }

        partitionsCountBuffer = Math.max(partitionsCountBuffer, partitionIndex + 1);
        return new PartitionFile(getFile(partitionIndex), serializer);
    }

    @Override
    public int getPartitionsCount() {
        if (partitionsCountBuffer < 0) {
            partitionsCountBuffer = getFilesCount();
        }

        return partitionsCountBuffer;
    }

    private int getFilesCount() {
        File[] files = directory.listFiles((dir, name) -> name.startsWith(partitionFilePrefix) && name.endsWith(PARTITION_FILE_EXTENSION));
        if (files == null) {
            return 0;
        } else {
            return Stream.of(files)
                    .mapToInt(this::getPartitionIndex)
                    .max().orElse(-1) + 1;
        }
    }

    private int getPartitionIndex(File file) {
        String fileName = file.getName();

        int p = partitionFilePrefix.length();
        int e = PARTITION_FILE_EXTENSION.length();
        int n = fileName.length();
        assert n > p + e;

        String value = fileName.substring(p, n - e);
        try {
            return Integer.parseInt(value);
        } catch (NumberFormatException e1) {
            return -1;
        }
    }

    private File getFile(int partitionIndex) {
        return new File(directory, partitionFilePrefix + partitionIndex + PARTITION_FILE_EXTENSION);
    }

    public static class PartitionFile implements DataFile {

        private final File file;
        private final RecordSerializer serializer;

        public PartitionFile(File file, RecordSerializer serializer) {
            this.file = file;
            this.serializer = serializer;
        }

        @Override
        public List<Object[]> read() throws IOException {
            List<Object[]> read = new ArrayList<>();

            if (!file.isFile()) {
                return read;
            }

            try (Accessor accessor = new Accessor().setIn(file)) {
                while (true) {
                    read.add(serializer.deserialize(accessor));
                }
            } catch (EOFException eof) {
                // Do nothing
            }

            return read;
        }

        @Override
        public void write(List<Object[]> values) throws IOException {
            try (Accessor accessor = new Accessor().setOut(file)) {
                for (Object[] record : values) {
                    serializer.serialize(record, accessor);
                }
            }
        }

        @Override
        public void append(List<Object[]> currentValues, Object[] newRow) throws IOException {
            try (Accessor accessor = new Accessor().setOut(file, true)) {
                serializer.serialize(newRow, accessor);

                currentValues.add(newRow);
            }
        }

        @Override
        public void delete() {
            if (file.isFile()) {
                file.delete();
            }
        }
    }
}
