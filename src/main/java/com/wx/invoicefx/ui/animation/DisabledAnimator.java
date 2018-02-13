package com.wx.invoicefx.ui.animation;

import com.wx.invoicefx.ui.animation.task.AnimatorTask;
import com.wx.invoicefx.ui.animation.task.ImmediateTask;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Region;

import java.util.Collection;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 22.06.17.
 */
public class DisabledAnimator implements AnimatorInterface {

    @Override
    public AnimatorTask expandAnimation(Region pane, double height) {
        return new ImmediateTask(() -> {
            pane.setMinHeight(height);
            pane.setMaxHeight(height);
            pane.setOpacity(1.0);

        });
    }

    @Override
    public AnimatorTask collapseAnimation(Region pane) {
        return new ImmediateTask(() -> {
            pane.setMinHeight(0.0);
            pane.setMaxHeight(0.0);
            pane.setOpacity(0.0);

        });
    }

    @Override
    public AnimatorTask fadeOut(Node component) {
        return new ImmediateTask(() -> {
            component.setOpacity(0.0);

        });
    }

    @Override
    public AnimatorTask fadeIn(Node component) {
        return new ImmediateTask(() -> {
            component.setOpacity(1.0);

        });
    }

    @Override
    public AnimatorTask setInvalid(Collection<Node> nodes) {
        return new ImmediateTask(() -> {
            InnerShadow effect = new InnerShadow(INVALID_FLASH_INTENSITY, INVALID_FLASH_COLOR);
            nodes.forEach(n -> n.setEffect(effect));
        });
    }

    @Override
    public AnimatorTask removeInvalid(Node node) {
        return new ImmediateTask(() -> {
            node.setEffect(null);
        });
    }

    @Override
    public AnimatorTask animateBusyButton(Labeled component, String busyLabel) {
        return new ImmediateTask(() -> {
            ProgressIndicator indicator = new ProgressIndicator();
            indicator.setPrefWidth(component.getHeight() - 5);
            indicator.setPrefHeight(component.getHeight() - 5);

            component.setUserData(component.getText());
            component.setGraphic(indicator);
            component.setText(busyLabel);
            component.setMouseTransparent(true);
            component.getStyleClass().add("button-progress");
        });
    }

    @Override
    public AnimatorTask restoreBusyButton(Labeled component, String label) {
        return new ImmediateTask(() -> {
            component.setGraphic(null);
            component.setText(label);
            component.getStyleClass().remove("button-progress");
            component.setMouseTransparent(false);
        });
    }

    @Override
    public AnimatorTask translateSlow(DoubleProperty property, double from, double newValue) {
        return translate(property, newValue);
    }

    @Override
    public AnimatorTask translate(DoubleProperty property, double from, double newValue) {
        return new ImmediateTask(() -> {
            property.set(newValue);
        });
    }
}
