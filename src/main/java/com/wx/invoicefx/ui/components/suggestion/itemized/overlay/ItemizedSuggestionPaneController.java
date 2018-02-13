package com.wx.invoicefx.ui.components.suggestion.itemized.overlay;

import com.wx.fx.Lang;
import com.wx.invoicefx.ui.components.suggestion.textfield.SuggestionTextField;
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
import java.util.SortedMap;
import java.util.function.Function;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 13.05.17.
 */
public class ItemizedSuggestionPaneController<T> {


    @FXML
    private SuggestionTextField<T> textField;
    @FXML
    private HBox clientOverBox;

    private final ObservableList<T> selectedItems = FXCollections.observableArrayList();
    private final List<Double> selectedItemsWidths = new LinkedList<>();

    public void initialize() {
        clientOverBox.setMaxWidth(0);

        textField.setItemConsumer(item -> {
            textField.setText("");
            addItem(item);
        });

        textField.setOnKeyReleased(event -> {
            if (event.getCode() == KeyCode.BACK_SPACE && textField.getText().isEmpty() && !selectedItems.isEmpty()) {
                removeItem(selectedItems.size() - 1);
            }
        });
    }

    public ObservableList<T> getSelectedItems() {
        return selectedItems;
    }

    public void setToStringFn(Function<T, String> toStringFn) {
        textField.setToStringFn(toStringFn);
    }

    public void setEntries(Collection<T> entries) {
        textField.setEntries(entries);

//        test(entries.iterator().next());
    }

    public void setEntries(SortedMap<String, T> entries) {
        textField.setEntries(entries);
    }

    public void addItem(T item) {
        try {
            ItemOverlayController itemPaneController = createItemPane(item);
            Pane itemPane = itemPaneController.getContentPane();

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
        textField.setStyle("-fx-padding: 5px 5px 5px " + (prefixWidth+5) + "px;");
    }

    private double getItemsWidth() {
        return Math.max(0, selectedItemsWidths.stream().mapToDouble(Double::new).sum() + 5*(selectedItemsWidths.size()-1));
    }

    private ItemOverlayController createItemPane(T item) throws IOException {
        FXMLLoader loader = new FXMLLoader(
                ItemizedSuggestionPaneController.class.getResource("/com/wx/invoicefx/ui/components/suggestion/itemized/overlay/ItemOverlay.fxml"),
                Lang.getBundle());

        loader.load();

        ItemOverlayController controller = loader.getController();
        controller.setItemName(textField.getToStringFn().apply(item));

        return controller;
    }
}
