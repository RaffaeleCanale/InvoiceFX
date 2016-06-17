package app.config.manager.datafile;

import org.jetbrains.annotations.NotNull;

import java.io.IOException;
import java.util.List;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.06.16.
 */
public interface DataFile {

    @NotNull
    List<Object[]> read() throws IOException;

    void write(List<Object[]> values) throws IOException;

    default void append(List<Object[]> currentValues, Object[] newRow) throws IOException {
        currentValues.add(newRow);
        write(currentValues);
    }

}
