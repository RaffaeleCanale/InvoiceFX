package com.wx.invoicefx.legacy.model;

import java.util.logging.Logger;

/**
 * Interface for any general model that can be considered 'valid' (ie. consistent, not containing errors).
 * <p>
 * It can also provide a debugging method that will log all sources of errors if any.
 * <p>
 * Created on 09/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public interface ValidationModel {

    /**
     * @return {@code true} if this model is considered valid
     */
    boolean isValid();

    /**
     * If this model is not valid, logs all the sources of inconsistencies.
     * <p>
     * Useful for debugging.
     *
     * @param log Logger to output to
     */
    default void diagnosis(Logger log) {
        // Optional method
    }

}
