package app.util.bindings;

import javafx.beans.WeakListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * This class represents a unidirectional binding between a list to the an other list of a any type.
 * <p>
 * Created on 19/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class GenericListContentBinding<E, F> implements ListChangeListener<E>, WeakListener {

    /**
     * Bind an observable list with any list (of any type) using a converter. Every add, remove or set operation on the
     * observed list will propagate to the observer list. The converter is used to "cast" values from the observer to
     * the observed list.
     * <p>
     * When creating the binding, the observer list content will be reset to match the observed list. The binding is not
     * deep, any change applied directly on the objects in the list will not propagate.
     * <p>
     * This binding is unidirectional, any change on the observer will not reflect on the observed.
     * <p>
     * <b>Important:</b> This method cannot "lock" the observer from changes and any changes applied directly on the
     * observer may result in unexpected behaviors.
     *
     * @param observer  List to bind
     * @param observed  List to observe
     * @param converter Converter to cast values from the observed list to the observer list
     * @param <E>       Type of the observed list
     * @param <F>       Type of the observer list
     *
     * @return The created binding
     */
    public static <E, F> GenericListContentBinding<E, F> bind(List<F> observer, ObservableList<E> observed, Function<E, F> converter) {
        GenericListContentBinding<E, F> contentBinding = new GenericListContentBinding<>(observer, converter);
        if (observer instanceof ObservableList) {
            ((ObservableList<F>) observer).setAll(convert(observed, converter));
        } else {
            observer.clear();
            observer.addAll(convert(observed, converter));
        }
        observed.removeListener(contentBinding);
        observed.addListener(contentBinding);

        return contentBinding;
    }

    private static <E, F> List<F> convert(List<? extends E> list, Function<E, F> converter) {
        return list.stream().map(converter).collect(Collectors.toList());
    }

    private final WeakReference<List<F>> listRef;
    private final Function<E, F> converter;

    private GenericListContentBinding(List<F> list, Function<E, F> converter) {
        this.converter = converter;
        this.listRef = new WeakReference<>(list);
    }

    @Override
    public void onChanged(Change<? extends E> change) {
        final List<F> list = listRef.get();
        if (list == null) {
            change.getList().removeListener(this);
        } else {
            while (change.next()) {
                if (change.wasPermutated()) {
                    list.subList(change.getFrom(), change.getTo()).clear();
                    addAll(list, change.getFrom(), change.getList().subList(change.getFrom(), change.getTo()));
                } else {
                    if (change.wasRemoved()) {
                        list.subList(change.getFrom(), change.getFrom() + change.getRemovedSize()).clear();
                    }
                    if (change.wasAdded()) {
                        addAll(list, change.getFrom(), change.getAddedSubList());
                    }
                }
            }
        }
    }

    private void addAll(List<F> list, int from, List<? extends E> subList) {
        list.addAll(from, convert(subList, converter));
    }

    @Override
    public boolean wasGarbageCollected() {
        return listRef.get() == null;
    }

    @Override
    public int hashCode() {
        final List<F> list = listRef.get();
        return (list == null) ? 0 : list.hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (this == obj) {
            return true;
        }

        final List<F> list1 = listRef.get();
        if (list1 == null) {
            return false;
        }

        if (obj instanceof GenericListContentBinding) {
            final GenericListContentBinding<?, ?> other = (GenericListContentBinding<?, ?>) obj;
            final List<?> list2 = other.listRef.get();
            return list1 == list2;
        }
        return false;
    }
}

