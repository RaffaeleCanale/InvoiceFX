package app.util.bindings;

import javafx.collections.ListChangeListener;

import java.util.List;

/**
 * Created on 04/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
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

    public void added(int i, List<? extends E> addedSubList) {
        for (E item : addedSubList) {
            added(item, i);
            i++;
        }
    }




    public abstract void added(E item, int index);

    public abstract void removed(E item, int index);

}
