package app.currency;

import java.io.IOException;

/**
 * Retrieve the current currency rates.
 * <p>
 * Created on 03/11/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public interface CurrentRateRetriever {

    /**
     * Retrieve the current currency rates.
     *
     * @return Current rates
     *
     * @throws IOException
     */
    Rates retrieveRates() throws IOException;

}
