package app.config.manager.datafile;

import java.io.IOException;
import java.util.List;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.06.16.
 */
public interface DataFile<Type> {

    List<Type> get() throws IOException;

    void set(List<Type> values) throws IOException;

}
