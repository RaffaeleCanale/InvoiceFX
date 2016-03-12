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
    public void testVatShare() {
        Config.sharedPreferences().booleanProperty(SharedProperty.VAT_ROUND).set(false);
        assertEquals(7.41, Common.computeVatShare(8, 100), 0.001);
        assertEquals(2.44, Common.computeVatShare(2.5, 100), 0.001);
        assertEquals(3.02, Common.computeVatShare(2.5, 124), 0.001);


        Config.sharedPreferences().booleanProperty(SharedProperty.VAT_ROUND).set(true);
        assertEquals(7.4, Common.computeVatShare(8, 100), 0.001);
        assertEquals(2.45, Common.computeVatShare(2.5, 100), 0.001);
        assertEquals(3, Common.computeVatShare(2.5, 124), 0.001);
    }

}