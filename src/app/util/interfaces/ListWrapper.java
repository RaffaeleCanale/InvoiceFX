package app.util.interfaces;

import java.util.List;

/**
 * Created on 09/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public interface ListWrapper<E> {

    List<E> getElements();

    void setElements(List<E> elements);

}
