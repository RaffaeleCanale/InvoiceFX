package app.util.bindings;

import javafx.collections.ListChangeListener;

import java.util.List;

/**
 * Simple list listener that is triggered for every single add/remove operation on the list.
 * <p>
 * Not that the set operation is capture by this listener as a remove and add.
 * <p>
 * Created on 04/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public abstract class AddedRemovedListener<E> implements ListChangeListener<E> {

    @Override
    public void onChanged(Change<? extends E> c) {
        while (c.next()) {
            int i = c.getFrom();
            if (c.wasAdded()) {
                added(i, c.getAddedSubList());
            }
            if (c.wasRemoved()) {
                removed(i, c.getRemoved());
            }
        }
    }

    private void removed(int i, List<? extends E> removed) {
        for (E item : removed) {
            removed(item, i);
            i++;
        }
    }

    private void added(int i, List<? extends E> addedSubList) {
        for (E item : addedSubList) {
            added(item, i);
            i++;
        }
    }

    /**
     * This method is triggered for every single element added to the observed list.
     *
     * @param item  Item added to the list
     * @param index Index where the item has been added
     */
    protected abstract void added(E item, int index);

    /**
     * This method is triggered for every single element removed from the observed list.
     *
     * @param item  Item removed from the list
     * @param index Index where the item has been removed
     */
    protected abstract void removed(E item, int index);

}
