package com.wx.invoicefx.io.file;

import com.wx.util.future.IoIterator;
import org.junit.Before;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.IntStream;

import static com.wx.invoicefx.io.util.DummyStorage.*;
import static org.junit.Assert.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 16.06.16.
 */
public class ClusteredIndexTest {

    private static final int DEF_PARTITION_SIZE = 100;
    private static final int SORT_KEY = 0;


    private static class TestData {

        private List<Object[]> expectedData;
        private DummyPartitionedStorage storage;
        private ClusteredIndex index;

        public void write(int... partitions) {
            int total = IntStream.of(partitions).sum();
            expectedData = sort(generateData(total), SORT_KEY);

            storage = dummyStorage(partition(expectedData, partitions));
        }

        public void write(List<Object[]>... partitions) {
            List<List<Object[]>> partitionsList = Arrays.asList(partitions);

            storage = dummyStorage(partitionsList);
        }


        public List<Object[]> read() throws IOException {
            ClusteredIndex index = new ClusteredIndex(getStorage(), DEF_PARTITION_SIZE, SORT_KEY);
            return readIt(index.iterator());
        }

        public List<Object[]> readFirst(int count) throws IOException {
            ClusteredIndex index = new ClusteredIndex(getStorage(), DEF_PARTITION_SIZE, SORT_KEY);
            List<Object[]> read = new ArrayList<>();

            IoIterator<Object[]> it = index.iterator();
            while (it.hasNext() && read.size() < count) {
                read.add(it.next());
            }


            return read;
        }

        public ClusteredIndex getIndex() {
            return new ClusteredIndex(getStorage(), DEF_PARTITION_SIZE, SORT_KEY);
        }

        private DummyPartitionedStorage getStorage() {
            if (storage == null) {
                storage = dummyStorage(Collections.emptyList());
            }
            return storage;
        }
//        private List<Object[]> readFromManager() throws IOException {
//            return read(manager.iterator());
//        }
//
//        private List<Object[]> readFromManager(int max) throws IOException {
//            List<Object[]> read = new ArrayList<>();
//
//            IoIterator<Object[]> it = manager.iterator();
//            while (it.hasNext() && read.size() < max) {
//                read.add(it.next());
//            }
//
//
//            return read;
//        }
//
        private List<Object[]> readIt(IoIterator<Object[]> it) throws IOException {
            List<Object[]> read = new ArrayList<>();
            it.forEachRemaining(read::add);
            return read;
        }
//
//        protected ClusteredIndex createManager(PartitionedStorage storage) {
//            return new ClusteredIndex(storage, DEF_PARTITION_SIZE, SORT_KEY);
//        }
//
//        private List<Object[]> generateData(int count) {
//            return sort(DummyStorage.generateData(count), SORT_KEY);
//        }
//
//        private void createManager(int... partitions) {
//            data = new ArrayList<>();
//            storage = dummyStorage(SORT_KEY, data, partitions);
//            manager = createManager(storage);
//        }
//
//        private void createManager(List<Object[]>... partitions) {
//            storage = dummyStorage(partitions);
//            manager = createManager(storage);
//        }

    }

    private TestData td;

    @Before
    public void beforeEach() {
        td = new TestData();
    }

    @Test
    public void readTestSimple() throws IOException {
        td.write(DEF_PARTITION_SIZE);

        assertDataEquals(reverse(td.expectedData), td.read());
    }

    @Test
    public void multiPartitionRead() throws IOException {
        td.write(40, 40, 20);

        assertDataEquals(reverse(td.expectedData), td.read());
    }

    @Test
    public void emptyPartitionRead() throws IOException {
        assertDataEquals(Collections.emptyList(), td.read());
    }

    @Test
    public void holePartitionRead() throws IOException {
        td.write(50, 0, 50);
        assertDataEquals(reverse(td.expectedData), td.read());
    }

    @Test
    public void lazyReadTest() throws IOException {
        td.write(25, 25, 25, 25);

        for (int i = 0; i < 4; i++) {
            td.storage.assertReadWriteCount(i, 0, 0);
        }

        List<Object[]> read30 = td.readFirst(30);
        assertDataEquals(reverse(td.expectedData).subList(0, 30), read30);
        read30 = td.readFirst(30);
        assertDataEquals(reverse(td.expectedData).subList(0, 30), read30);

        td.storage.assertReadWriteCount(0, 0, 0);
        td.storage.assertReadWriteCount(1, 0, 0);
        td.storage.assertReadWriteCount(2, 2, 0);
        td.storage.assertReadWriteCount(3, 2, 0);
    }

    @Test
    public void repartition() throws IOException {
        List<Object[]> data = generateData(220);

        td.write(
                data.subList(20, 120),
                data.subList(120, 200),
                Collections.emptyList(),
                data.subList(0, 20),
                Collections.emptyList(),
                Collections.emptyList(),
                data.subList(200, 220)
        );

        ClusteredIndex index = td.getIndex();
        index.repartition();

        td.storage.assertReadWriteCount(0, 1, 1);
        td.storage.assertReadWriteCount(1, 1, 1);
        td.storage.assertReadWriteCount(2, 1, 1);
        td.storage.assertReadWriteCount(3, 1, 0);
        td.storage.assertReadWriteCount(4, 1, 0);
        td.storage.assertReadWriteCount(5, 1, 0);
        td.storage.assertReadWriteCount(6, 1, 0);
        td.storage.assertReadWriteCount(7, 0, 0);

        assertEquals(1, td.storage.getPartition(3).getDeleteCount());
        assertEquals(1, td.storage.getPartition(4).getDeleteCount());

        assertDataEquals(data.subList(0, 100), td.storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100, 200), td.storage.getPartition(1).getTable());
        assertDataEquals(data.subList(200, 220), td.storage.getPartition(2).getTable());
        assertDataEquals(Collections.emptyList(), td.storage.getPartition(3).getTable());
        assertDataEquals(Collections.emptyList(), td.storage.getPartition(29).getTable());
    }

    @Test
    public void query1() throws IOException {
        td.write(100, 100, 0, 11);

        Object[] someRow = td.expectedData.get(22);
        Optional<Object[]> query = td.getIndex().queryIndexFirst((Long) someRow[SORT_KEY]);


        assertTrue(query.isPresent());
        assertArrayEquals(query.get(), someRow);
    }

    @Test
    public void query2() throws IOException {
        List<Object[]> data = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            data.add(new Object[]{0L, i});
        }
        for (int i = 5; i < 15; i++) {
            data.add(new Object[]{1L, i});
        }
        for (int i = 15; i < 20; i++) {
            data.add(new Object[]{2L, i});
        }

        td.write(data.subList(0,10), data.subList(10,20));

        assertFalse(td.getIndex().queryIndex(-1).hasNext());
        assertDataEquals(reverse(data.subList(5, 15)), td.readIt(td.getIndex().queryIndex(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullSortKey() throws IOException {
        Object[] row = generateData(1).get(0);
        row[SORT_KEY] = null;

        td.getIndex().insert(row);
    }

    @Test
    public void addEmpty() throws IOException {

        Object[] row = generateData(1).get(0);
        td.getIndex().insert(row);

        assertDataEquals(Collections.singletonList(row), td.read());
    }

    @Test
    public void addNewSimple() throws IOException {
        List<Object[]> data = generateData(10);
        RemovedRow removedRow = new RemovedRow(data, 9);


        td.write(removedRow.remainderData);
        td.getIndex().insert(removedRow.removed);

        assertDataEquals(reverse(data), td.read());
    }

    @Test
    public void addUnique1() throws IOException {
        td.write(10);
        Object[] someRow = td.expectedData.get(0);

        td.getIndex().insert(someRow);
        td.expectedData.add(0, someRow);

        assertDataEquals(reverse(td.expectedData), td.read());
    }

    @Test(expected = IllegalArgumentException.class)
    public void addUnique2() throws IOException {
        td.write(10);
        Object[] someRow = td.expectedData.get(0);

        td.getIndex().insertUnique(someRow);
    }

    @Test
    public void addNewFull1() throws IOException {
        List<Object[]> data = generateData(101);
        RemovedRow removedRow = new RemovedRow(data, 100);

        td.write(removedRow.remainderData);
        td.getIndex().insert(removedRow.removed);

        assertEquals(2, td.storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), td.storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100,101), td.storage.getPartition(1).getTable());
    }

    @Test
    public void addNewFull2() throws IOException {
        List<Object[]> data = generateData(151);
        RemovedRow removedRow = new RemovedRow(data, 100);

        td.write(
                removedRow.remainderData.subList(0, 100),
                removedRow.remainderData.subList(100, 150)
        );
        td.getIndex().insert(removedRow.removed);

        assertEquals(2, td.storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), td.storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100,151), td.storage.getPartition(1).getTable());
    }

    @Test
    public void addNewFull3() throws IOException {
        List<Object[]> data = generateData(151);
        RemovedRow removedRow = new RemovedRow(data, 10);

        td.write(
                removedRow.remainderData.subList(0, 100),
                removedRow.remainderData.subList(100, 150)
        );
        td.getIndex().insert(removedRow.removed);

        assertEquals(2, td.storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), td.storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100,151), td.storage.getPartition(1).getTable());
    }

    @Test
    public void addNewFull4() throws IOException {
        List<Object[]> data = generateData(301);
        RemovedRow removedRow = new RemovedRow(data, 0);

        td.write(
                removedRow.remainderData.subList(0, 100),
                removedRow.remainderData.subList(100, 200),
                removedRow.remainderData.subList(200, 300)
        );
        td.getIndex().insert(removedRow.removed);

        assertEquals(4, td.storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), td.storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100, 200), td.storage.getPartition(1).getTable());
        assertDataEquals(data.subList(200, 300), td.storage.getPartition(2).getTable());
        assertDataEquals(data.subList(300, 301), td.storage.getPartition(3).getTable());
    }

    @Test
    public void addNewInHole1() throws IOException {
        List<Object[]> data = generateData(201);
        RemovedRow removedRow = new RemovedRow(data, 50);

        td.write(
                removedRow.remainderData.subList(0, 100),
                Collections.emptyList(),
                Collections.emptyList(),
                removedRow.remainderData.subList(100, 200)
        );
        td.getIndex().insert(removedRow.removed);

        assertEquals(4, td.storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), td.storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100, 101), td.storage.getPartition(1).getTable());
        assertDataEquals(Collections.emptyList(), td.storage.getPartition(2).getTable());
        assertDataEquals(data.subList(101, 201), td.storage.getPartition(3).getTable());
    }

    @Test
    public void addNewInHole2() throws IOException {
        List<Object[]> data = generateData(201);
        RemovedRow removedRow = new RemovedRow(data, 100);

        td.write(
                removedRow.remainderData.subList(0, 100),
                Collections.emptyList(),
                removedRow.remainderData.subList(100, 200)
        );
        td.getIndex().insert(removedRow.removed);

        assertEquals(3, td.storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), td.storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100, 101), td.storage.getPartition(1).getTable());
        assertDataEquals(data.subList(101, 201), td.storage.getPartition(2).getTable());
    }

    @Test
    public void remove1() throws IOException {
        td.write(10);

        Object[] row = td.expectedData.remove(3);
        assertTrue(td.getIndex().removeFirst(r -> r[0] == row[0]));
        assertDataEquals(reverse(td.expectedData), td.read());
    }

    @Test
    public void remove2() throws IOException {
        td.write(10);

        Object[] row = td.expectedData.remove(3);
        IoIterator<Object[]> it = td.getIndex().queryIndex((long) row[SORT_KEY]);
        assertTrue(it.hasNext());
        assertArrayEquals(row, it.next());
        it.remove();

        assertDataEquals(reverse(td.expectedData), td.read());
    }

    @Test
    public void remove3() throws IOException {
        td.write(10);

        Object[] row = td.expectedData.get(3);
        td.getIndex().insert(row);
        td.getIndex().insert(row);

        IoIterator<Object[]> it = td.getIndex().queryIndex((long) row[SORT_KEY]);
        assertTrue(it.hasNext());
        assertArrayEquals(row, it.next());
        it.remove();

        assertTrue(it.hasNext());
        assertArrayEquals(row, it.next());
        it.remove();

        assertDataEquals(reverse(td.expectedData), td.read());
    }

    private static class RemovedRow {
        private final List<Object[]> remainderData;
        private final Object[] removed;
//        private final long removedId;

        RemovedRow(List<Object[]> fullData, int indexToRemove) {
            remainderData = new ArrayList<>(fullData);
            removed = remainderData.remove(indexToRemove);
//            removedId = (long) removed[0];
        }

    }
}