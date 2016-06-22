package app.config.manager;

import app.config.manager.datafile.DataFile;
import app.config.manager.local.RecordSerializer;
import app.config.manager.storage.PartitionedStorage;
import org.jetbrains.annotations.NotNull;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 16.06.16.
 */
public class DummyData {



    public static String dataToString(List<Object[]> data) {
        return "    " + data.stream()
                .map(Arrays::toString)
                .collect(Collectors.joining("\n    "));
    }

    public static void assertDataEquals(List<Object[]> expected, List<Object[]> actual) {
        String message = "Data differs:\n" + dataToString(expected) + "\n\n" + dataToString(actual);

        assertEquals(message, expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertArrayEquals(message, expected.get(i), actual.get(i));
        }
    }

    public static List<Object[]> generateData(int rows, int sortKey) {
//        long x = 130000017227L;
        long x = 1223;
        long m = 443L;
        if (rows > m) {
            throw new IllegalArgumentException("Too many rows...");
        }

        List<Object[]> result = new ArrayList<>(rows);

        for (int i = 0; i < rows; i++) {
            result.add(new Object[]{
                    (long) i, "Some string " + i, Math.floorMod((long)i*x, m), (double) -i, new int[i], Arrays.asList(i, -i)
            });
        }

        if (result.stream().mapToLong(r -> (long) r[sortKey]).distinct().count() != rows) {
            throw new RuntimeException();
            // TODO: 16.06.16 Pretty sure it's correct
        }
        Collections.sort(result, Comparator.comparingLong(r -> (long) r[sortKey]));
        return result;
    }

    public static List<Object[]> reverse(List<Object[]> list) {
        ArrayList<Object[]> copy = new ArrayList<>(list);
        Collections.reverse(copy);

        return copy;
    }

//    public static DummyPartitionedStorage dummyStorage() {
//        return new DummyPartitionedStorage();
//    }

    public static DummyPartitionedStorage dummyStorage(int sortKey, List<Object[]> data, int... partitionsSize) {
        DummyPartitionedStorage storage = new DummyPartitionedStorage();

        int total = IntStream.of(partitionsSize).sum();
        data.addAll(generateData(total, sortKey));

        int cursor = 0;

        for (int i = 0; i < partitionsSize.length; i++) {
            int size = partitionsSize[i];
            if (size < 0) {
                storage.partitions.put(i, new ExceptionDataFile());
            } else {
                storage.getPartition(i).table = data.subList(cursor, cursor + size);
                cursor += size;
            }
        }

        return storage;
    }

    public static DummyPartitionedStorage dummyStorage(List<Object[]>... partitions) {
        DummyPartitionedStorage storage = new DummyPartitionedStorage();

        int i = 0;
        for (List<Object[]> partition : partitions) {
            if (partition == null) {
                storage.partitions.put(i, new ExceptionDataFile());
            } else {
                storage.getPartition(i).table = new ArrayList<>(partition);
            }
            i++;
        }

        return storage;
    }

    public static class DummyPartitionedStorage implements PartitionedStorage {

        private final Map<Integer, DummyDataFile> partitions = new HashMap<>();

        public long getMaxId() {
            return partitions.values().stream()
                    .flatMap(d -> d.table.stream())
                    .mapToLong(r -> (long) r[0])
                    .max().orElse(0L);
        }

        public Map<Integer, DummyDataFile> getPartitions() {
            return partitions;
        }

        public void assertReadWriteCount(int partition, int read, int write) {
            assertEquals("Reads for " + partition + " do not match", read, getPartition(partition).readCount);
            assertEquals("Writes for " + partition + " do not match", write, getPartition(partition).writeCount);
        }

        @NotNull
        @Override
        public DummyDataFile getPartition(int partitionIndex) {
            return partitions.computeIfAbsent(partitionIndex, p -> new DummyDataFile());
        }

        @Override
        public int getPartitionsCount() {
            return partitions.keySet().stream().mapToInt(i -> i+1).max().orElse(0);
        }

        @Override
        public void removePartition(int partitionIndex) {
            getPartition(partitionIndex).delete();
        }

        @Override
        public String toString() {
            return partitions.entrySet().stream()
                    .map(e -> e.getKey() + ":\n" + e.getValue())
                    .collect(Collectors.joining("\n"));
        }
    }

    public static class DummyDataFile implements DataFile {

        private int readCount = 0;
        private int writeCount = 0;
        private int deleteCount = 0;
        private List<Object[]> table = new ArrayList<>();

        public List<Object[]> getTable() {
            return table;
        }

        public int getReadCount() {
            return readCount;
        }

        public int getWriteCount() {
            return writeCount;
        }

        public int getDeleteCount() {
            return deleteCount;
        }

        public void delete() {
            deleteCount++;
            table = new ArrayList<>();
        }

        @Override
        public @NotNull List<Object[]> read() throws IOException {
            readCount++;
            return table;
        }

        @Override
        public void write(List<Object[]> values) throws IOException {
            writeCount++;
            table = Objects.requireNonNull(values);
        }

        @Override
        public String toString() {
            return dataToString(table);
        }
    }

    public static class ExceptionDataFile extends DummyDataFile {

        @Override
        public @NotNull List<Object[]> read() throws IOException {
            throw new IOException();
        }

        @Override
        public void write(List<Object[]> values) throws IOException {
            throw new IOException();
        }
    }

}
