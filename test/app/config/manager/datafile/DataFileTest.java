package app.config.manager.datafile;

import org.junit.Test;

import java.io.IOException;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import static app.config.manager.DummyData.*;
import static org.junit.Assert.assertEquals;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 20.06.16.
 */
public abstract class DataFileTest {

    protected abstract DataFile getDataFile();

    @Test
    public void testReadNew() throws IOException {
        List<Object[]> read = getDataFile().read();
        assertEquals(Collections.emptyList(), read);
    }


    @Test
    public void testWriteRead() throws IOException {
        List<Object[]> data = Collections.unmodifiableList(generateData(10));

        getDataFile().write(data);


        List<Object[]> read = getDataFile().read();
        assertDataEquals(data, read);
    }

    @Test
    public void testInsert() throws IOException {
        List<Object[]> data = generateData(10);
        getDataFile().write(data);

        Object[] newRow = generateData(1).get(0);
        List<Object[]> expected = new ArrayList<>(data);
        expected.add(newRow);


        getDataFile().append(data, newRow);
        assertDataEquals(expected, data);
        assertDataEquals(expected, getDataFile().read());
    }

    @Test
    public void testInsertEmpty() throws IOException {
        List<Object[]> data = new ArrayList<>();

        Object[] newRow = generateData(1).get(0);
        List<Object[]> expected = new ArrayList<>(data);
        expected.add(newRow);


        getDataFile().append(data, newRow);
        assertDataEquals(expected, data);
        assertDataEquals(expected, getDataFile().read());
    }
    
    @Test
    public void testDelete() throws IOException {
        List<Object[]> data = generateData(10);
        getDataFile().write(data);

        DataFile dataFile = getDataFile();
        assertDataEquals(data, dataFile.read());
        dataFile.delete();

        assertDataEquals(Collections.emptyList(), dataFile.read());
        assertDataEquals(Collections.emptyList(), getDataFile().read());
    }


}