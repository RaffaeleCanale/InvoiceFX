package app.util.bindings;

import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import org.junit.Test;

import java.util.Arrays;
import java.util.LinkedList;
import java.util.List;

import static app.util.bindings.GenericListContentBinding.*;
import static org.junit.Assert.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.06.16.
 */
public class GenericListContentBindingTest {

    private final ObservableList<MutableObject> observed = FXCollections.observableArrayList(new MutableObject("foo"), new MutableObject("bar"));

    @Test
    public void testListInitialization() {
        List<String> observer = bindFromEmpty();
        assertEquals(Arrays.asList("foo", "bar"), observer);
    }

    @Test
    public void testListeners() {
        ObservableList<String> observer = FXCollections.observableArrayList();
        List<ListChangeListener.Change<? extends String>> change = new LinkedList<>();

        observer.addListener((ListChangeListener<String>) change::add);

        bind(observer, observed, MutableObject::getValue);

        assertEquals(1, change.size());
        assertTrue(change.get(0).next());
        assertEquals(2, change.get(0).getAddedSize());
        assertEquals(Arrays.asList("foo", "bar"), change.get(0).getAddedSubList());
    }

    @Test
    public void testDuplicateBinding() {
        List<Object> observer = new LinkedList<>();
        Object binding1 = bind(observer, observed, MutableObject::getValue);
        Object binding2 = bind(observer, observed, MutableObject::getValue);

        assertEquals(binding1, binding2);
    }

    @Test
    public void testAdd() {
        List<String> observer = bindFromEmpty();

        observed.add(1, new MutableObject("hello"));

        assertEquals(Arrays.asList("foo", "hello", "bar"), observer);
    }

    @Test
    public void testRemove() {
        List<String> observer = bindFromEmpty();

        observed.remove(0);

        assertEquals(Arrays.asList("bar"), observer);
    }

    @Test
    public void testSet() {
        List<String> observer = bindFromEmpty();

        observed.set(0, new MutableObject("hello"));

        assertEquals(Arrays.asList("hello", "bar"), observer);
    }

    @Test
    public void weirdTest() {
        List<String> observer = bindFromEmpty();
        observer.clear();

        observed.remove(0);

        System.out.println(observer);

    }

    private List<String> bindFromEmpty() {
        List<String> list = new LinkedList<>();
        bind(list, observed, MutableObject::getValue);

        return list;
    }


    private static class MutableObject {
        private String value;

        public MutableObject(String value) {
            this.value = value;
        }

        public void setValue(String value) {
            this.value = value;
        }

        public String getValue() {
            return value;
        }

        @Override
        public boolean equals(Object o) {
            if (this == o) return true;
            if (o == null || getClass() != o.getClass()) return false;

            MutableObject that = (MutableObject) o;

            return value != null ? value.equals(that.value) : that.value == null;

        }

        @Override
        public int hashCode() {
            return value != null ? value.hashCode() : 0;
        }
    }
}