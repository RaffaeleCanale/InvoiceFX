package com.wx.invoicefx.ui.components.settings;

import com.wx.invoicefx.ui.animation.Animator;
import com.wx.invoicefx.ui.animation.DisabledAnimator;
import javafx.beans.NamedArg;
import javafx.beans.property.StringProperty;
import javafx.geometry.Pos;
import javafx.scene.Cursor;
import javafx.scene.Node;
import javafx.scene.control.Label;
import javafx.scene.input.MouseEvent;
import javafx.scene.layout.*;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.17.
 */
public class ExpandPane extends VBox {

    private final VBox headerPane = new VBox(10.0);
    private final Label arrowLabel = new Label();
    private Label subLabel;

    private Region expandableContent;
    private boolean isExpanded = false;

    public ExpandPane(@NamedArg("text") String text) {
        init(text);
    }

    public void setSubLabel(String value) {
        if (subLabel == null) {
            subLabel = new Label(value);
            subLabel.getStyleClass().add("setting-value");

            headerPane.getChildren().add(subLabel);

        } else {
            subLabel.setText(value);
        }

    }

    public void setExpandableContent(Region expandableContent) {
        if (this.expandableContent == null) {
            getChildren().add(expandableContent);
        }  else {
            getChildren().set(1, expandableContent);
        }

        this.expandableContent = expandableContent;
        expandableContent.getStyleClass().add("setting-expandable");

        new DisabledAnimator().collapseAnimation(expandableContent).run();
    }

    protected void onExpand() {}

    protected void onCollapse() {}

    private void doExpand(MouseEvent mouseEvent) {
        if (isExpanded) {
            onCollapse();


            Animator.instance().collapseAnimation(expandableContent).run();

            expandableContent.setMouseTransparent(true);
            arrowLabel.getStyleClass().setAll("down-arrow");
        } else {
            onExpand();

            Animator.instance().expandAnimation(expandableContent, expandableContent.getPrefHeight()).run();

            expandableContent.setMouseTransparent(false);
            arrowLabel.getStyleClass().setAll("up-arrow");
        }

        isExpanded = !isExpanded;
    }

    private void init(String text) {
        BorderPane topPane = new BorderPane();
        Label headerLabel = new Label(text);

        BorderPane.setAlignment(arrowLabel, Pos.CENTER);
        arrowLabel.getStyleClass().setAll("down-arrow");

        headerPane.getChildren().setAll(headerLabel);

        topPane.setLeft(headerPane);
        topPane.setRight(arrowLabel);

        getChildren().add(topPane);

        setSpacing(10.0);
        topPane.setCursor(Cursor.HAND);
        topPane.setOnMouseClicked(this::doExpand);
    }


}
