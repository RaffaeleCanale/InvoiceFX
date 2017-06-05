package com.wx.invoicefx.io.util.data;


import com.wx.invoicefx.io.interfaces.DataFile;
import com.wx.invoicefx.io.interfaces.PartitionedStorage;
import com.wx.invoicefx.io.interfaces.RecordSerializer;
import com.wx.invoicefx.io.util.data.DummyData;

import java.io.DataInput;
import java.io.DataOutput;
import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 16.06.16.
 */
public class DummyStorage {

    private static final long X = 1223L;
    private static final long M = 443L;

    private static Object[][] DATA = {
            {null, null, "String", 1.0f, 'c'},
            {null, null, "      ", -1.0f, ' '},
            {null, null, "  ", 0.0f, '\t'},
            {null, null, "\nHello\n", 1.0001f, '\n'},
            {null, null, "", -99.012313f, '^'},
            {null, null, "§'^è¨éà$<,.-", 1.0f, '$'},
            {null, null, "°+\"*ç%&/()=?`ü!öä£>;:_", 1.0f, '£'},
            {null, null, "¬|@#¼½¬|¢]}~[]´{}\\─·̣", 1.0f, '}'},
    };

    private static Object[] getDataRow(long i) {
        if (i >= M) {
            throw new IllegalArgumentException("Too many rows");
        }

        return new Object[] {
                i,
                Math.floorMod(i * X, M),
                DummyData.generateClientName()
        };
    }


    public static final RecordSerializer DUMMY_SERIALIZER = new RecordSerializer() {
        @Override
        public void serialize(Object[] record, DataOutput output) throws IOException {
            output.writeLong((long) record[0]);
            output.writeLong((long) record[1]);
            output.writeUTF((String) record[2]);
        }

        @Override
        public Object[] deserialize(DataInput input) throws IOException {
            return new Object[] {
                    input.readLong(),
                    input.readLong(),
                    input.readUTF()
            };
        }
    };

    public static String dataToString(List<Object[]> data) {
        return "    " + data.stream()
                .map(Arrays::toString)
                .map(s -> s.replace("\n", "\\n"))
                .collect(Collectors.joining("\n    "));
    }

    public static void assertDataEquals(List<Object[]> expected, List<Object[]> actual) {
        String message = "Expected:\n" + dataToString(expected) + "\n\nActual:\n" + dataToString(actual) + "\n";

        assertEquals(message, expected.size(), actual.size());

        for (int i = 0; i < expected.size(); i++) {
            assertArrayEquals("Data differs at " + i + ":\n" + message, expected.get(i), actual.get(i));
        }
    }

    public static List<Object[]> generateData(int rows) {
        List<Object[]> result = new ArrayList<>(rows);

        for (int i = 0; i < rows; i++) {
            result.add(getDataRow(i));
        }
        return result;
    }

    public static List<Object[]> sort(List<Object[]> data, int key) {
        Collections.sort(data, Comparator.comparingLong(r -> (long) r[key]));
        return data;
    }

    public static List<Object[]> reverse(List<Object[]> list) {
        ArrayList<Object[]> copy = new ArrayList<>(list);
        Collections.reverse(copy);

        return copy;
    }

//    public static DummyPartitionedStorage dummyStorage() {
//        return new DummyPartitionedStorage();
//    }

//    public static DummyPartitionedStorage dummyStorage(int sortKey, List<Object[]> data, int... partitionsSize) {
//        DummyPartitionedStorage storage = new DummyPartitionedStorage();
//
//        int total = IntStream.of(partitionsSize).sum();
//        data.addAll(sort(generateData(total), sortKey));
//
//        int cursor = 0;
//
//        for (int i = 0; i < partitionsSize.length; i++) {
//            int size = partitionsSize[i];
//            if (size < 0) {
//                storage.partitions.put(i, new ExceptionDataFile());
//            } else {
//                storage.getPartition(i).table = new ArrayList<>(data.subList(cursor, cursor + size));
//                cursor += size;
//            }
//        }
//
//        return storage;
//    }

//    public static List<Object[]> generateSortedData(int sortKey, int... partitionsSize) {
//        int total = IntStream.of(partitionsSize).sum();
//        return sort(generateData(total), sortKey);
//    }

    public static List<List<Object[]>> partition(List<Object[]> data, int... partitionsSize) {
        List<List<Object[]>> partitions = new ArrayList<>();

        int cursor = 0;

        for (int i = 0; i < partitionsSize.length; i++) {
            int size = partitionsSize[i];
            if (size < 0) {
                partitions.add(null);
            } else {
                partitions.add(new ArrayList<>(data.subList(cursor, cursor + size)));
                cursor += size;
            }
        }

        return partitions;
    }

    public static DummyPartitionedStorage dummyStorage(List<List<Object[]>> partitions) {
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

        @Override
        public DummyDataFile getPartition(int partitionIndex) {
            return partitions.computeIfAbsent(partitionIndex, p -> new DummyDataFile());
        }

        @Override
        public int getPartitionsCount() {
            return partitions.keySet().stream().mapToInt(i -> i + 1).max().orElse(0);
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
        public List<Object[]> read() throws IOException {
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
        public List<Object[]> read() throws IOException {
            throw new IOException();
        }

        @Override
        public void write(List<Object[]> values) throws IOException {
            throw new IOException();
        }
    }

}
