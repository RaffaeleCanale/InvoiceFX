package com.wx.invoicefx.ui.components;

import javafx.beans.property.IntegerProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.Node;
import javafx.scene.layout.StackPane;

import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 17.06.17.
 */
public class PagePane extends StackPane {

    private final IntegerProperty selectedPage = new SimpleIntegerProperty();

    public void init() {
        Iterator<Node> it = getChildren().iterator();
        it.next().setVisible(true);

        while (it.hasNext()) {
            it.next().setVisible(false);
        }

        selectedPage.addListener((observable, oldValue, newValue) -> {
            PagePane.this.getChildren().get(oldValue.intValue()).setVisible(false);
            PagePane.this.getChildren().get(newValue.intValue()).setVisible(true);
        });
    }

    public int getSelectedPage() {
        return selectedPage.get();
    }

    public IntegerProperty selectedPageProperty() {
        return selectedPage;
    }

    public void setSelectedPage(int selectedPage) {
        this.selectedPage.set(selectedPage);
    }
}
