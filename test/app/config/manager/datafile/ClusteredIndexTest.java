package app.config.manager.datafile;

import app.config.manager.TestsHelper.*;
import com.wx.properties.property.SimpleProperty;
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

    private List<Object[]> readFromManager() {
        return manager.stream().collect(Collectors.toList());
    }

//    private List<Object[]> read(DummyPartitionedStorage storage) {
//        return read(createManager(storage));
//    }

    int getSortKey() {
        return 2;
    }

    void createManager(int... partitions) {
        data = new ArrayList<>();
        storage = dummyStorage(2, data, partitions);
        manager = new ClusteredIndex(storage, DEF_PARTITION_SIZE, getSortKey(), new SimpleProperty<>(storage.getMaxId()));
    }

    void createManager(List<Object[]>... partitions) {
        storage = dummyStorage(partitions);
        manager = new ClusteredIndex(storage, DEF_PARTITION_SIZE, getSortKey(), new SimpleProperty<>(storage.getMaxId()));
    }

    private List<Object[]> data;
    private DummyPartitionedStorage storage;
    private ClusteredIndex manager;

    @Test
    public void readTestSimple() {
        createManager(DEF_PARTITION_SIZE);

        assertDataEquals(reverse(data), readFromManager());
    }

    @Test
    public void multiPartitionRead() {
        createManager(40, 40, 20);

        assertDataEquals(reverse(data), readFromManager());
    }

    @Test
    public void emptyPartitionRead() {
        createManager(0);
        assertDataEquals(Collections.emptyList(), readFromManager());
    }

    @Test
    public void holePartitionRead() {
        createManager(50, 0, 50);
        assertDataEquals(reverse(data), readFromManager());
    }

    @Test
    public void lazyReadTest() {
        createManager(25, 25, 25, 25);

        for (int i = 0; i < 4; i++) {
            storage.assertReadWriteCount(i, 0, 0);
        }

        List<Object[]> read30 = manager.stream().limit(30).collect(Collectors.toList());
        assertDataEquals(reverse(data).subList(0, 30), read30);
        read30 = manager.stream().limit(30).collect(Collectors.toList());
        assertDataEquals(reverse(data).subList(0, 30), read30);

        storage.assertReadWriteCount(0, 0, 0);
        storage.assertReadWriteCount(1, 0, 0);
        storage.assertReadWriteCount(2, 1, 0);
        storage.assertReadWriteCount(3, 1, 0);
    }

    @Test
    public void repartition() throws IOException {
        data = generateData(220, getSortKey());

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
    public void getById() throws IOException {
        createManager(100, 100, 0, 11);

        Object[] someRow = data.get(22);
        Optional<Object[]> query = manager.getById((Long) someRow[0]);


        assertTrue(query.isPresent());
        assertArrayEquals(query.get(), someRow);

        assertFalse(manager.getById(100000L).isPresent());
        assertFalse(manager.getById(-1L).isPresent());
    }

    @Test(expected = IllegalArgumentException.class)
    public void duplicateId() throws IOException {
        createManager(100);

        manager.insert(data.get(0));
    }

    @Test(expected = IllegalArgumentException.class)
    public void nullSortKey() throws IOException {
        createManager(0);
        data = generateData(1, getSortKey());
        data.get(0)[2] = null;

        manager.insert(this.data.get(0));
    }

    @Test
    public void addEmpty() throws IOException {
        createManager(new int[0]);

        data = generateData(1, getSortKey());
        manager.insert(data.get(0));

        assertDataEquals(data, readFromManager());
        assertEquals(data.get(0)[0], manager.getLargestId());
    }

    @Test
    public void addNewSimple() throws IOException {
        data = generateData(10, getSortKey());
        RemovedRow removedRow = new RemovedRow(data, 9);


        createManager(removedRow.remainderData);
        removedRow.insertAsNewIn(manager);

        assertDataEquals(reverse(data), readFromManager());
        assertEquals(9L, manager.getLargestId());
    }

    @Test
    public void addNewFull1() throws IOException {
        data = generateData(101, getSortKey());
        RemovedRow removedRow = new RemovedRow(data, 100);

        createManager(removedRow.remainderData);
        removedRow.insertAsNewIn(manager);

        assertEquals(2, storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100,101), storage.getPartition(1).getTable());
    }

    @Test
    public void addNewFull2() throws IOException {
        data = generateData(151, getSortKey());
        RemovedRow removedRow = new RemovedRow(data, 100);

        createManager(
                removedRow.remainderData.subList(0, 100),
                removedRow.remainderData.subList(100, 150)
        );
        removedRow.insertAsNewIn(manager);

        assertEquals(2, storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100,151), storage.getPartition(1).getTable());
    }

    @Test
    public void addNewFull3() throws IOException {
        data = generateData(151, getSortKey());
        RemovedRow removedRow = new RemovedRow(data, 10);

        createManager(
                removedRow.remainderData.subList(0, 100),
                removedRow.remainderData.subList(100, 150)
        );
        removedRow.insertAsNewIn(manager);

        assertEquals(2, storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100,151), storage.getPartition(1).getTable());
    }

    @Test
    public void addNewFull4() throws IOException {
        data = generateData(301, getSortKey());
        RemovedRow removedRow = new RemovedRow(data, 0);

        createManager(
                removedRow.remainderData.subList(0, 100),
                removedRow.remainderData.subList(100, 200),
                removedRow.remainderData.subList(200, 300)
        );
        removedRow.insertAsNewIn(manager);

        assertEquals(4, storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100, 200), storage.getPartition(1).getTable());
        assertDataEquals(data.subList(200, 300), storage.getPartition(2).getTable());
        assertDataEquals(data.subList(300, 301), storage.getPartition(3).getTable());
    }

    @Test
    public void addNewInHole1() throws IOException {
        data = generateData(201, getSortKey());
        RemovedRow removedRow = new RemovedRow(data, 50);

        createManager(
                removedRow.remainderData.subList(0, 100),
                Collections.emptyList(),
                Collections.emptyList(),
                removedRow.remainderData.subList(100, 200)
        );
        removedRow.insertAsNewIn(manager);

        assertEquals(4, storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100, 101), storage.getPartition(1).getTable());
        assertDataEquals(Collections.emptyList(), storage.getPartition(2).getTable());
        assertDataEquals(data.subList(101, 201), storage.getPartition(3).getTable());
    }

    @Test
    public void addNewInHole2() throws IOException {
        data = generateData(201, getSortKey());
        RemovedRow removedRow = new RemovedRow(data, 100);

        createManager(
                removedRow.remainderData.subList(0, 100),
                Collections.emptyList(),
                removedRow.remainderData.subList(100, 200)
        );
        removedRow.insertAsNewIn(manager);

        assertEquals(3, storage.getPartitionsCount());
        assertDataEquals(data.subList(0, 100), storage.getPartition(0).getTable());
        assertDataEquals(data.subList(100, 101), storage.getPartition(1).getTable());
        assertDataEquals(data.subList(101, 201), storage.getPartition(2).getTable());
    }

    private class RemovedRow {
        private final List<Object[]> remainderData;
        private final Object[] removed;
//        private final long removedId;

        RemovedRow(List<Object[]> fullData, int indexToRemove) {
            remainderData = new ArrayList<>(fullData);
            removed = remainderData.remove(indexToRemove);
//            removedId = (long) removed[0];
        }

        void insertAsNewIn(ClusteredIndex manager) throws IOException {
//            removed[0] = null;
            manager.insert(removed);

            assertEquals("Last ID do not match", storage.getMaxId(), manager.getLargestId());
//            assertEquals("Insert ID is wrong", removedId, manager.insert(removed));
        }
    }
}