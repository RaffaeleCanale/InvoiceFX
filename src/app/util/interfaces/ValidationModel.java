package app.util.interfaces;

import java.util.logging.Logger;

/**
 * Created on 09/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public interface ValidationModel {

    boolean isValid();

    void diagnosis(Logger log);

}
