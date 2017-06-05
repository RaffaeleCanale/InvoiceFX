package com.wx.invoicefx.view.other.animation;

import com.wx.invoicefx.view.other.FormElement;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.Pane;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.05.17.
 */
public interface AnimatorInterface {

    default void expandAnimation(Pane pane) {
        expandAnimation(pane, pane.getPrefHeight());
    }

    void expandAnimation(Pane pane, double height);

    default void collapseAnimation(Pane pane) {
        collapseAnimation(pane, null);
    }

    void collapseAnimation(Pane pane, EventHandler<ActionEvent> onFinished);

    void fadeInOut(Node component, Runnable process);

    void animateInvalid(FormElement form);

}
