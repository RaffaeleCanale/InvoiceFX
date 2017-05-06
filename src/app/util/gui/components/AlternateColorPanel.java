package app.util.gui.components;

import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * Created on 08/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class AlternateColorPanel {

    public static void bind(Pane pane) {
        pane.getChildren().addListener((ListChangeListener<Node>) c -> {

            boolean changed = false;
            while (c.next()) {
                changed = c.wasAdded() || c.wasRemoved();
            }

            if (changed) {
                ObservableList<? extends Node> nodes = c.getList();

                for (int i = 0; i < nodes.size(); i++) {
                    if ((i & 1) == 1) {
                        nodes.get(i).setId("alternate_panel");
                    } else {
                        nodes.get(i).setId("");
                    }
                }
            }
        });
    }

}
