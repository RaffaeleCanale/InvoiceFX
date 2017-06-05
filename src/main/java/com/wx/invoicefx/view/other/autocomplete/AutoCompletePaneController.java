package com.wx.invoicefx.view.other.autocomplete;

import com.wx.fx.Lang;
import com.wx.invoicefx.view.archives.debug.DebugViewController;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.fxml.FXMLLoader;
import javafx.scene.input.KeyCode;
import javafx.scene.layout.HBox;
import javafx.scene.layout.Pane;

import java.io.IOException;
import java.util.Collection;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Function;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 13.05.17.
 */
public class AutoCompletePaneController<T> {


    @FXML
    private AutoCompleteTextField<T> searchField;
    @FXML
    private HBox clientOverBox;

    private final ObservableList<T> selectedItems = FXCollections.observableArrayList();
    private final List<Double> selectedItemsWidths = new LinkedList<>();

    public void initialize() {
        clientOverBox.setMaxWidth(0);

        searchField.setItemConsumer(item -> {
            addItem(item);
        });

        searchField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE && searchField.getText().isEmpty() && !selectedItems.isEmpty()) {
                removeItem(selectedItems.size() - 1);
            }
        });
    }

    public ObservableList<T> getSelectedItems() {
        return selectedItems;
    }

    public void setToStringFn(Function<T, String> toStringFn) {
        searchField.setToStringFn(toStringFn);
    }

    public void addEntries(Collection<T> entries) {
        searchField.addEntries(entries);

//        test(entries.iterator().next());
    }

    public void addItem(T item) {
        try {
            AutoCompleteItemController itemPaneController = createItemPane(item);
            Pane itemPane = itemPaneController.contentPane;

            double itemWidth = itemPane.getPrefWidth();

            itemPaneController.onRemove(() -> {
                int index = selectedItems.indexOf(item);

                removeItem(index);
            });

            selectedItems.add(item);
            selectedItemsWidths.add(itemWidth);
            clientOverBox.getChildren().add(itemPane);

            updatePrefixWidth();



        } catch (IOException e) {
            e.printStackTrace();
        }

    }

    private void removeItem(int index) {
        selectedItems.remove(index);
        selectedItemsWidths.remove(index);
        clientOverBox.getChildren().remove(index);
        updatePrefixWidth();
    }

    private void updatePrefixWidth() {
        double prefixWidth = getItemsWidth();
        clientOverBox.setMaxWidth(prefixWidth);
        searchField.setStyle("-fx-padding: 5px 5px 5px " + (prefixWidth+5) + "px;");
    }

    private double getItemsWidth() {
        return Math.max(0, selectedItemsWidths.stream().mapToDouble(Double::new).sum() + 5*(selectedItemsWidths.size()-1));
    }

    private AutoCompleteItemController createItemPane(T item) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                DebugViewController.class.getResource("/com/wx/invoicefx/view/other/autocomplete/AutoCompleteItem.fxml"),
                Lang.getBundle());

        loader.load();

        AutoCompleteItemController controller = loader.getController();
        controller.setItemName(searchField.toStringFn.apply(item));

        return controller;
    }
}
