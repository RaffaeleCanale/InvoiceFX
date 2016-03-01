package app.util.gui.components;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.collections.FXCollections;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

import java.util.*;

/**
 * Created on 02/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class PagePane<Key> {

    private final Pane rootPane;
    private final Map<Key, ObservableList<Node>> pagesElements = new HashMap<>();
    private final ObjectProperty<Key> currentPage = new SimpleObjectProperty<>();
    private final boolean autoCreate;

    public PagePane(Pane rootPane, boolean autoCreate) {
        this.rootPane = rootPane;
        this.autoCreate = autoCreate;
        currentPage.setValue(null);

        currentPage.addListener((observable, oldValue, newValue) -> {
            setPage(newValue);
        });
    }

    public ObjectProperty<Key> currentPageProperty() {
        return currentPage;
    }

    public void createNewPage(Key key) {
        ObservableList<Node> nodes = FXCollections.observableArrayList();
        pagesElements.put(key, nodes);

        nodes.addListener((ListChangeListener<Node>) c -> {
            if (key.equals(currentPage.get())) {
                while (c.next()) {
                    rootPane.getChildren().removeAll(c.getRemoved());
                    rootPane.getChildren().addAll(c.getAddedSubList());
                }
            }
        });
    }

    public ObservableList<Node> page(Key key) {
        if (!pagesElements.containsKey(key)) {
            if (autoCreate) {
                createNewPage(key);
            } else {
                throw new NoSuchElementException();
            }
        }

        return pagesElements.get(key);
    }

    public void removePage(Key key) {
        pagesElements.remove(key);

        if (key.equals(currentPage.get())) {
            rootPane.getChildren().removeAll();
            currentPage.setValue(null);
        }
    }

    private void setPage(Key key) {
        if (key == null) {
            rootPane.getChildren().clear();
            return;
        }

        ObservableList<Node> nodes = page(key);

        rootPane.getChildren().clear();
        rootPane.getChildren().addAll(nodes);
    }
}
