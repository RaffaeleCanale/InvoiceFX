package app.util.helpers;

import app.config.Config;
import app.config.preferences.properties.SharedProperty;
import org.junit.Test;

import static org.junit.Assert.*;

/**
 * Created on 12/03/2016
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public class CommonTest {


    @Test
    public void testReplaceAll() {
        StringBuilder source = new StringBuilder("hello world, hello cat, Othello");

        Common.replaceAll(source, "hello", "foo");
        assertEquals("foo world, foo cat, Otfoo", source.toString());
    }

    @Test
    public void computeEuro() {
        Config.sharedPreferences().setProperty(SharedProperty.EURO_TO_CHF_CURRENCY, 1.09575);

        assertEquals(8.21, Common.computeEuro(9), 0.001);
        assertEquals(20.35, Common.computeEuro(22.3), 0.001);
        assertEquals(1.37, Common.computeEuro(1.5), 0.001);
    }

    @Test
    public void testVatShare() {
        Config.sharedPreferences().setProperty(SharedProperty.VAT_ROUND, false);
        assertEquals(7.41, Common.computeVatShare(8, 100), 0.001);
        assertEquals(2.44, Common.computeVatShare(2.5, 100), 0.001);
        assertEquals(3.02, Common.computeVatShare(2.5, 124), 0.001);


        Config.sharedPreferences().setProperty(SharedProperty.VAT_ROUND, true);
        assertEquals(7.4, Common.computeVatShare(8, 100), 0.001);
        assertEquals(2.45, Common.computeVatShare(2.5, 100), 0.001);
        assertEquals(3, Common.computeVatShare(2.5, 124), 0.001);
    }

}