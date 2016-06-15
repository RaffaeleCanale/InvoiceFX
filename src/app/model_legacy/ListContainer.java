package app.model_legacy;

import java.util.List;

/**
 * Simple interface for a list container.
 * <p>
 * Created on 09/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public interface ListContainer<E> {

    List<E> getElements();

    void setElements(List<E> elements);

}
