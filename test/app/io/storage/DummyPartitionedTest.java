package app.io.storage;

import app.config.manager.DummyData;
import app.io.local.RecordSerializer;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.16.
 */
public class DummyPartitionedTest extends PartitionedStorageTest {

    private final DummyData.DummyPartitionedStorage dummyPartitionedStorage = new DummyData.DummyPartitionedStorage();

    @Override
    protected PartitionedStorage getPartitionedStorage(RecordSerializer serializer) {
        return dummyPartitionedStorage;
    }
}
