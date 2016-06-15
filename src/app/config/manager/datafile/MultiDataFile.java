package app.config.manager.datafile;

import java.io.IOException;
import java.util.List;
import java.util.Optional;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.06.16.
 */
public class MultiDataFile<Type> {

    private int numPartitions;
    private List<DataFile<Type>> dataFiles;

    public Optional<Type> getById(int id) throws IOException {
        int fileIndex = idToFileMapping(id);

        DataFile<Type> dataFile = dataFiles.get(fileIndex);

        return dataFile.get()
                .stream()
                .filter(t -> true)
                .findFirst();
    }


    private int idToFileMapping(int id) {
        throw new UnsupportedOperationException();
    }

}
