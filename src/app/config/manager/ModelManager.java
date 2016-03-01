package app.config.manager;

import javafx.collections.ObservableList;

import java.io.IOException;

/**
 * Represents the interface of a manager able to load and save an abstract model E.
 * <p>
 * The manager should contain the list of models E.
 * <p>
 * Created on 09/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public interface ModelManager<E> {

    /**
     * Load all the models into this manager.
     *
     * @throws IOException
     */
    void load() throws IOException;

    /**
     * Save all the models contained in this manager.
     *
     * @throws IOException
     */
    void save() throws IOException;

    /**
     * Get all the models of this manager.
     *
     * @return Models of this manager
     */
    ObservableList<E> get();

}
