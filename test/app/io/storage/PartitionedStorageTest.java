package app.io.storage;

import app.io.local.RecordSerializer;
import org.junit.Test;

import java.io.IOException;
import java.util.Collections;
import java.util.List;

import static app.config.manager.DummyData.DUMMY_SERIALIZER;
import static app.config.manager.DummyData.assertDataEquals;
import static app.config.manager.DummyData.generateData;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.16.
 */
public abstract class PartitionedStorageTest {

    protected abstract PartitionedStorage getPartitionedStorage(RecordSerializer serializer);

    @Test
    public void test() throws IOException {
        List<Object[]> data = generateData(3);

        PartitionedStorage storage = getPartitionedStorage(DUMMY_SERIALIZER);
        assertEquals(0, storage.getPartitionsCount());

        storage.getPartition(0).write(data.subList(0, 1));
        assertEquals(1, storage.getPartitionsCount());
        storage.getPartition(1).write(data.subList(1, 2));
        assertEquals(2, storage.getPartitionsCount());
        storage.getPartition(2).write(data.subList(2, 3));
        assertEquals(3, storage.getPartitionsCount());

        assertDataEquals(data.subList(0, 1), storage.getPartition(0).read());
        assertDataEquals(data.subList(1, 2), storage.getPartition(1).read());
        assertDataEquals(data.subList(2, 3), storage.getPartition(2).read());

        storage.getPartition(2).delete();
        int partitionsCount = storage.getPartitionsCount();
        assertTrue("Partitions count should be >= 2 but is " + partitionsCount ,partitionsCount >= 2);
        assertDataEquals(Collections.emptyList(), storage.getPartition(2).read());


        partitionsCount = getPartitionedStorage(DUMMY_SERIALIZER).getPartitionsCount();
        assertTrue("Partitions count should be >= 2 but is " + partitionsCount ,partitionsCount >= 2);
        assertDataEquals(data.subList(0, 1), getPartitionedStorage(DUMMY_SERIALIZER).getPartition(0).read());
        assertDataEquals(data.subList(1, 2), getPartitionedStorage(DUMMY_SERIALIZER).getPartition(1).read());
    }

}