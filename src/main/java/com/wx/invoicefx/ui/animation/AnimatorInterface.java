package com.wx.invoicefx.ui.animation;

import com.wx.invoicefx.ui.animation.task.AnimatorTask;
import com.wx.invoicefx.ui.animation.task.TimelineTask;
import javafx.beans.property.DoubleProperty;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;

import java.util.Collection;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.05.17.
 */
public interface AnimatorInterface {

    Color INVALID_FLASH_COLOR = Color.RED;
    int INVALID_FLASH_INTENSITY = 10;
//    default AnimatorTask expandAnimation(Region pane) {
//        return expandAnimation(pane, pane.getPrefHeight());
//    }
    AnimatorTask expandAnimation(Region pane, double height);
    AnimatorTask collapseAnimation(Region pane);


    AnimatorTask fadeOut(Node component);
    AnimatorTask fadeIn(Node component);


    AnimatorTask setInvalid(Collection<Node> nodes);
    AnimatorTask removeInvalid(Node node);


    AnimatorTask animateBusyButton(Labeled component, String busyLabel);
    AnimatorTask restoreBusyButton(Labeled component, String label);


    AnimatorTask translateSlow(DoubleProperty property, double from, double to);
    AnimatorTask translate(DoubleProperty property, double from, double to);

    default AnimatorTask translate(DoubleProperty property, double to) {
        return translate(property, property.get(), to);
    }

    default AnimatorTask translateSlow(DoubleProperty property, double to) {
        return translateSlow(property, property.get(), to);
    }

//    default AnimatorTask translateX(Node node, double newLayoutX) {
//        return translate(node.layoutXProperty(), newLayoutX);
//    }

}
