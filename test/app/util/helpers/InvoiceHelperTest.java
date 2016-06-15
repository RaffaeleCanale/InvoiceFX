package app.util.helpers;

import app.config.Config;
import app.config.ModelManagerFactory;
import app.model_legacy.invoice.InvoiceModel;
import org.junit.BeforeClass;
import org.junit.Test;

import java.io.IOException;
import java.util.stream.Stream;

import static org.junit.Assert.*;

/**
 * Created on 15/03/2016
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class InvoiceHelperTest {

    @BeforeClass
    public static void setUp() throws IOException {
        Config.initConfig(ModelManagerFactory.Impl.RUNTIME);
    }

    @Test
    public void createDefaultInvoice() {
        final int n = 10;

        long count = Stream.generate(() -> {
            InvoiceModel invoice = InvoiceHelper.createDefaultInvoice();
            Config.invoicesManager().get().add(invoice);

            return invoice;
        })
                .limit(n)
                .mapToInt(InvoiceModel::getId)
                .distinct()
                .count();

        assertEquals(n, count);
    }

}