package app.util.bindings;

import com.wx.util.pair.Pair;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import org.junit.Before;
import org.junit.Test;

import java.util.LinkedList;
import java.util.List;

import static org.junit.Assert.assertEquals;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 14.06.16.
 */
public class AddedRemovedListenerTest {

    private static Pair<String, Integer> of(String item, int index) {
        return new Pair<>(item, index);
    }


    private ObservableList<String> list = FXCollections.observableArrayList("hello", "world");
    private final List<Pair<String, Integer>> addedElements = new LinkedList<>();
    private final List<Pair<String, Integer>> removedElements = new LinkedList<>();

    @Before
    public void addListener() {
        list.addListener(new AddedRemovedListener<String>() {
            @Override
            public void added(String item, int index) {
                addedElements.add(of(item, index));
            }

            @Override
            public void removed(String item, int index) {
                removedElements.add(of(item, index));
            }
        });
    }

    @Test
    public void testAdd1() {
        list.add("foo");
        list.add(0, "bar");

        assertEquals(2, addedElements.size());
        assertEquals(0, removedElements.size());

        assertEquals(addedElements.get(0), of("foo", 2));
        assertEquals(addedElements.get(1), of("bar", 0));
    }

    @Test
    public void testAdd2() {
        list.addAll("foo", "bar");

        assertEquals(2, addedElements.size());
        assertEquals(0, removedElements.size());

        assertEquals(addedElements.get(0), of("foo", 2));
        assertEquals(addedElements.get(1), of("bar", 3));
    }

    @Test
    public void removeTest1() {
        list.remove(1);

        assertEquals(0, addedElements.size());
        assertEquals(1, removedElements.size());

        assertEquals(removedElements.get(0), of("world", 1));
    }

    @Test
    public void removeTest2() {
        list.clear();

        assertEquals(0, addedElements.size());
        assertEquals(2, removedElements.size());

        assertEquals(removedElements.get(0), of("hello", 0));
        assertEquals(removedElements.get(1), of("world", 1));
    }

    @Test
    public void setTest() {
        list.set(0, "hey");

        assertEquals(1, addedElements.size());
        assertEquals(1, removedElements.size());

        assertEquals(addedElements.get(0), of("hey", 0));
        assertEquals(removedElements.get(0), of("hello", 0));
    }


}