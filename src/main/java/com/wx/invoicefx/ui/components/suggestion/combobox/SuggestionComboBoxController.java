package com.wx.invoicefx.ui.components.suggestion.combobox;

import com.wx.invoicefx.ui.components.suggestion.textfield.SuggestionTextField;
import com.wx.invoicefx.util.view.FormattedListFactory;
import javafx.fxml.FXML;
import javafx.geometry.Point2D;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.control.ListCell;
import javafx.scene.control.ListView;
import javafx.scene.input.KeyCode;
import javafx.stage.Popup;
import javafx.util.Callback;

import java.util.function.Function;


/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 13.05.17.
 */
public class SuggestionComboBoxController<T> {


    @FXML
    private SuggestionTextField<T> textField;
    @FXML
    private Label dropButton;


    private final ListView<T> listView = new ListView<>();
    private final Popup popup = new Popup();

    public SuggestionTextField<T> getTextField() {
        return textField;
    }

    public void setToStringFn(Function<T, String> toStringFn) {
        textField.setToStringFn(toStringFn);
        ((CustomFormattedListFactory<T>) listView.getCellFactory()).setConverter(toStringFn);
    }

    public void setCellFactory(Function<T, Node> factory) {
        listView.setCellFactory(new Callback<ListView<T>, ListCell<T>>() {
            @Override
            public ListCell<T> call(ListView<T> param) {
                return new ListCell<T>() {
                    @Override
                    protected void updateItem(T item, boolean empty) {
                        super.updateItem(item, empty);

                        if (!empty && item != null) {
                            setGraphic(factory.apply(item));
                        }
                    }
                };
            }
        });
    }

    public void initialize() {
        listView.getStyleClass().setAll("custom-list-view-2");
        listView.setCellFactory(new CustomFormattedListFactory<>());
        listView.prefWidthProperty().bind(getTextField().widthProperty());


        popup.getContent().setAll(listView);
        popup.setAutoHide(true);

        listView.setOnMouseClicked(event -> {
            T item = listView.getSelectionModel().getSelectedItem();
            if (item != null) {
                textField.getItemConsumer().accept(item);
                textField.hidePopup();
            }


            popup.hide();
        });

        listView.setOnKeyPressed(event -> {
            if (event.getCode().equals(KeyCode.ENTER)) {
                T item = listView.getSelectionModel().getSelectedItem();
                if (item != null) {
                    textField.getItemConsumer().accept(item);
                    textField.hidePopup();
                }

                popup.hide();

            } else if (event.getCode().equals(KeyCode.ESCAPE)) {
                popup.hide();
            }
        });

        dropButton.setOnMouseClicked(e -> {
            listView.getItems().setAll(textField.getEntries().values());

            Point2D p = textField.localToScene(0.0, 0.0);
            popup.show(textField.getScene().getWindow(),
                    p.getX() + textField.getScene().getX() + textField.getScene().getWindow().getX(),
                    p.getY() + textField.getScene().getY() + textField.getScene().getWindow().getY() + textField.getHeight());
        });
    }


    private static class CustomFormattedListFactory<T> extends FormattedListFactory<T> {
        private static final int HOR_MARGIN = 30;

//        @Override
//        public ListCell<T> call(ListView<T> param) {
//            return new ListCell<T>() {
//                @Override
//                protected void updateItem(Object item, boolean empty) {
//                    super.updateItem(item, empty);
//
//                    if (empty || item == null) {
//                        setText(null);
//                        setGraphic(null);
//                    } else {
//                        setText(converter.apply(item));
//                    }
//                }
//            };
//        }

        //        @Override
//        protected void notifyCellWidth(ListCell<T> cell, double width) {
//            super.notifyCellWidth(cell, width);
//
//            ListView<T> listView = cell.getListView();
//
//            if (width > listView.getWidth() + HOR_MARGIN) {
//                listView.setPrefWidth(width + HOR_MARGIN);
//            }
//        }
//
//        @Override
//        protected void onHover(ListCell<T> cell, Boolean hover) {
//            super.onHover(cell, hover);
//
//            ListView<T> listView = cell.getListView();
//
//            if (hover) listView.getSelectionModel().select(cell.getIndex());
//            else listView.getSelectionModel().clearSelection();
//        }
    }
}
