package app.config.manager.local;

import app.config.manager.datafile.DataFile;
import app.config.manager.storage.PartitionedStorage;
import com.wx.io.Accessor;
import org.jetbrains.annotations.NotNull;

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

    private static final String PARTITION_FILE_EXTENSION = ".part";

    private final RecordSerializer serializer;
    private final File directory;

    private final String partitionFilePrefix;
    private int filesCountBuffer;

    public DirectoryStorage(RecordSerializer serializer, File directory, String partitionFilePrefix) {
        this.serializer = serializer;
        this.directory = directory;
        this.partitionFilePrefix = partitionFilePrefix + "_";
        filesCountBuffer = -1;
    }

    @Override
    public @NotNull DataFile getPartition(int partitionIndex) {
        if (partitionIndex < 0) {
            throw new IllegalArgumentException();
        }

        filesCountBuffer = Math.max(filesCountBuffer, partitionIndex + 1);
        return new PartitionFile(getFile(partitionIndex), serializer);
    }

    @Override
    public int getPartitionsCount() {
        if (filesCountBuffer < 0) {

            File[] files = directory.listFiles((dir, name) -> name.startsWith(partitionFilePrefix) && name.endsWith(PARTITION_FILE_EXTENSION));
            if (files == null) {
                filesCountBuffer = 0;
            } else {
                filesCountBuffer = Stream.of(files)
                        .mapToInt(this::getPartitionIndex)
                        .max().orElse(-1) + 1;
            }
        }

        return filesCountBuffer;
    }

    private int getPartitionIndex(File file) {
        int p = partitionFilePrefix.length();
        int e = PARTITION_FILE_EXTENSION.length();
        int n = file.getName().length();
        assert n > p + e;

        String value = file.getName().substring(p, n - e);
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
        public @NotNull List<Object[]> read() throws IOException {
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
