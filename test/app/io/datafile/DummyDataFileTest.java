package app.io.datafile;

import app.config.manager.DummyData;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.16.
 */
public class DummyDataFileTest extends DataFileTest {

    private final DataFile dataFile = new DummyData.DummyDataFile();

    @Override
    protected DataFile getDataFile() {
        return dataFile;
    }
}
