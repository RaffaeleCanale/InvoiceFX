package app.util.bindings;

import javafx.beans.WeakListener;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;

import java.lang.ref.WeakReference;
import java.util.List;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Created on 19/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class GenericListContentBinding<E, F> implements ListChangeListener<E>, WeakListener {


    public static <E,F> Object bind(List<F> list1, ObservableList<E> list2, Function<E, F> converter) {
        GenericListContentBinding<E, F> contentBinding = new GenericListContentBinding<>(list1, converter);
        if (list1 instanceof ObservableList) {
            ((ObservableList<F>) list1).setAll(convert(list2, converter));
        } else {
            list1.clear();
            list1.addAll(convert(list2, converter));
        }
        list2.removeListener(contentBinding);
        list2.addListener(contentBinding);
        return contentBinding;
    }

    private static <E, F> List<F> convert(List<E> list1, Function<E,F> converter) {
        return list1.stream().map(converter).collect(Collectors.toList());
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
        list.addAll(from, subList.stream().map(converter).collect(Collectors.toList()));

//        for (E e : subList) {
//            list.add(from, converter.apply(e));
//            from++;
//        }
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

