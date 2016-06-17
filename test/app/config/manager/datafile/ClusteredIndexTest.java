package app.config.manager.datafile;

import app.config.manager.TestsHelper.*;
import com.wx.util.future.IoIterator;
import org.junit.Test;

import java.io.IOException;
import java.util.*;
import java.util.stream.Collectors;

import static app.config.manager.TestsHelper.*;
import static org.junit.Assert.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 16.06.16.
 */
public class ClusteredIndexTest {

    private static final int DEF_PARTITION_SIZE = 100;
    private static final int SORT_KEY = 2;

    private List<Object[]> readFromManager() throws IOException {
        return read(manager.iterator());
    }

    private List<Object[]> readFromManager(int max) throws IOException {
        List<Object[]> read = new ArrayList<>();

        IoIterator<Object[]> it = manager.iterator();
        while (it.hasNext() && read.size() < max) {
            read.add(it.next());
        }


        return read;
    }

    private List<Object[]> read(IoIterator<Object[]> it) throws IOException {
        List<Object[]> read = new ArrayList<>();
        it.forEachRemaining(read::add);
        return read;
    }

    ClusteredIndex createManager(DummyPartitionedStorage storage) {
        return new ClusteredIndex(storage, DEF_PARTITION_SIZE, SORT_KEY);
    }

    private void createManager(int... partitions) {
        data = new ArrayList<>();
        storage = dummyStorage(SORT_KEY, data, partitions);
        manager = createManager(storage);
    }

    private void createManager(List<Object[]>... partitions) {
        storage = dummyStorage(partitions);
        manager = createManager(storage);
    }

    private List<Object[]> data;
    private DummyPartitionedStorage storage;
    private ClusteredIndex manager;

    @Test
    public void readTestSimple() throws IOException {
        createManager(DEF_PARTITION_SIZE);

        assertDataEquals(reverse(data), readFromManager());
    }

    @Test
    public void multiPartitionRead() throws IOException {
        createManager(40, 40, 20);

        assertDataEquals(reverse(data), readFromManager());
    }

    @Test
    public void emptyPartitionRead() throws IOException {
        createManager(0);
        assertDataEquals(Collections.emptyList(), readFromManager());
    }

    @Test
    public void holePartitionRead() throws IOException {
        createManager(50, 0, 50);
        assertDataEquals(reverse(data), readFromManager());
    }

    @Test
    public void lazyReadTest() throws IOException {
        createManager(25, 25, 25, 25);

        for (int i = 0; i < 4; i++) {
            storage.assertReadWriteCount(i, 0, 0);
        }

        List<Object[]> read30 = readFromManager(30);
        assertDataEquals(reverse(data).subList(0, 30), read30);
        read30 = readFromManager(30);
        assertDataEquals(reverse(data).subList(0, 30), read30);

        storage.assertReadWriteCount(0, 0, 0);
        storage.assertReadWriteCount(1, 0, 0);
        storage.assertReadWriteCount(2, 1, 0);
        storage.assertReadWriteCount(3, 1, 0);
    }

    @Test
    public void repartition() throws IOException {
        data = generateData(220, SORT_KEY);

        createManager(
                data.subList(20, 120),
                data.subList(120, 200),
                Collections.emptyList(),
                data.subList(0, 20),
                Collections.emptyList(),
                Collections.emptyList(),
                data.subList(200, 220)
        );
        manager.repartition();

        storage.assertReadWriteCount(0, 1, 1);
        storage.assertReadWriteCount(1, 1, 1);
        storage.assertReadWriteCount(2, 1, 1);
        storage.assertReadWriteCount(3, 1, 0);
        storage.assertReadWriteCount(4, 1, 0);
        storage.assertReadWriteCount(5, 1, 0);
        storage.assertReadWriteCount(6, 1, 0);
        storage.assertReadWriteCount(7, 0, 0);

        assertEquals(1, storage.getPartition(3).getDeleteCount());
        assertEquals(1, storage.getPartition(4).getDeleteCount());

        assertDataEquals(data.subList(0, 100), storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100, 200), storage.getPartition(1).getTable());
        assertDataEquals(data.subList(200, 220), storage.getPartition(2).getTable());
        assertDataEquals(Collections.emptyList(), storage.getPartition(3).getTable());
        assertDataEquals(Collections.emptyList(), storage.getPartition(29).getTable());
    }

    @Test
    public void query1() throws IOException {
        createManager(100, 100, 0, 11);

        Object[] someRow = data.get(22);
        Optional<Object[]> query = manager.queryIndexFirst((Long) someRow[SORT_KEY]);


        assertTrue(query.isPresent());
        assertArrayEquals(query.get(), someRow);
    }

    @Test
    public void query2() throws IOException {
        data = new ArrayList<>();

        for (int i = 0; i < 5; i++) {
            data.add(new Object[]{i, 0L});
        }
        for (int i = 5; i < 15; i++) {
            data.add(new Object[]{i, 1L});
        }
        for (int i = 15; i < 20; i++) {
            data.add(new Object[]{i, 2L});
        }

        storage = dummyStorage(data.subList(0,10), data.subList(10,20));
        manager = new ClusteredIndex(storage, 10, 1);

        assertFalse(manager.queryIndex(-1).hasNext());
        assertDataEquals(reverse(data.subList(5, 15)), read(manager.queryIndex(1)));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullSortKey() throws IOException {
        createManager(0);
        data = generateData(1, SORT_KEY);
        data.get(0)[SORT_KEY] = null;

        manager.insert(this.data.get(0));
    }

    @Test
    public void addEmpty() throws IOException {
        createManager(new int[0]);

        data = generateData(1, SORT_KEY);
        manager.insert(data.get(0));

        assertDataEquals(data, readFromManager());
    }

    @Test
    public void addNewSimple() throws IOException {
        data = generateData(10, SORT_KEY);
        RemovedRow removedRow = new RemovedRow(data, 9);


        createManager(removedRow.remainderData);
        manager.insert(removedRow.removed);

        assertDataEquals(reverse(data), readFromManager());
    }

    @Test
    public void addNewFull1() throws IOException {
        data = generateData(101, SORT_KEY);
        RemovedRow removedRow = new RemovedRow(data, 100);

        createManager(removedRow.remainderData);
        manager.insert(removedRow.removed);

        assertEquals(2, storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100,101), storage.getPartition(1).getTable());
    }

    @Test
    public void addNewFull2() throws IOException {
        data = generateData(151, SORT_KEY);
        RemovedRow removedRow = new RemovedRow(data, 100);

        createManager(
                removedRow.remainderData.subList(0, 100),
                removedRow.remainderData.subList(100, 150)
        );
        manager.insert(removedRow.removed);

        assertEquals(2, storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100,151), storage.getPartition(1).getTable());
    }

    @Test
    public void addNewFull3() throws IOException {
        data = generateData(151, SORT_KEY);
        RemovedRow removedRow = new RemovedRow(data, 10);

        createManager(
                removedRow.remainderData.subList(0, 100),
                removedRow.remainderData.subList(100, 150)
        );
        manager.insert(removedRow.removed);

        assertEquals(2, storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100,151), storage.getPartition(1).getTable());
    }

    @Test
    public void addNewFull4() throws IOException {
        data = generateData(301, SORT_KEY);
        RemovedRow removedRow = new RemovedRow(data, 0);

        createManager(
                removedRow.remainderData.subList(0, 100),
                removedRow.remainderData.subList(100, 200),
                removedRow.remainderData.subList(200, 300)
        );
        manager.insert(removedRow.removed);

        assertEquals(4, storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100, 200), storage.getPartition(1).getTable());
        assertDataEquals(data.subList(200, 300), storage.getPartition(2).getTable());
        assertDataEquals(data.subList(300, 301), storage.getPartition(3).getTable());
    }

    @Test
    public void addNewInHole1() throws IOException {
        data = generateData(201, SORT_KEY);
        RemovedRow removedRow = new RemovedRow(data, 50);

        createManager(
                removedRow.remainderData.subList(0, 100),
                Collections.emptyList(),
                Collections.emptyList(),
                removedRow.remainderData.subList(100, 200)
        );
        manager.insert(removedRow.removed);

        assertEquals(4, storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100, 101), storage.getPartition(1).getTable());
        assertDataEquals(Collections.emptyList(), storage.getPartition(2).getTable());
        assertDataEquals(data.subList(101, 201), storage.getPartition(3).getTable());
    }

    @Test
    public void addNewInHole2() throws IOException {
        data = generateData(201, SORT_KEY);
        RemovedRow removedRow = new RemovedRow(data, 100);

        createManager(
                removedRow.remainderData.subList(0, 100),
                Collections.emptyList(),
                removedRow.remainderData.subList(100, 200)
        );
        manager.insert(removedRow.removed);

        assertEquals(3, storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100, 101), storage.getPartition(1).getTable());
        assertDataEquals(data.subList(101, 201), storage.getPartition(2).getTable());
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