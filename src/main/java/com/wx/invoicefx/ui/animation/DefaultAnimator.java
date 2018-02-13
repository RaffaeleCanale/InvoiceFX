package com.wx.invoicefx.ui.animation;

import com.wx.invoicefx.ui.animation.task.AnimatorTask;
import com.wx.invoicefx.ui.animation.task.ImmediateTask;
import com.wx.invoicefx.ui.animation.task.TimelineTask;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.property.DoubleProperty;
import javafx.scene.Node;
import javafx.scene.control.Labeled;
import javafx.scene.control.ProgressIndicator;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Region;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.Collection;

import static com.wx.invoicefx.ui.animation.task.AnimatorTask.noop;
import static jline.console.internal.ConsoleRunner.property;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.05.17.
 */
public class DefaultAnimator implements AnimatorInterface {

    private static final int EXPAND_DURATION = 300;
    private static final int EXPAND_FADE_DURATION = 100;


    private static final int FADE_IN_OUT_DURATION = 200;


    private static final int INVALID_FLASH_REPETITIONS = 3;
    private static final int INVALID_FLASH_TIME = 100;


    private static final int TRANSLATE_DURATION = 100;
    private static final int TRANSLATE_SLOW_DURATION = 1000;


    @Override
    public AnimatorTask expandAnimation(Region pane, double height) {

        final Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(pane.opacityProperty(), 0)),
                new KeyFrame(Duration.ZERO, new KeyValue(pane.maxHeightProperty(), 0)),
                new KeyFrame(Duration.ZERO, new KeyValue(pane.minHeightProperty(), 0)),

                new KeyFrame(Duration.millis(EXPAND_DURATION - EXPAND_FADE_DURATION), new KeyValue(pane.opacityProperty(), 0)),

                new KeyFrame(Duration.millis(EXPAND_DURATION), new KeyValue(pane.maxHeightProperty(), height)),
                new KeyFrame(Duration.millis(EXPAND_DURATION), new KeyValue(pane.minHeightProperty(), height)),
                new KeyFrame(Duration.millis(EXPAND_DURATION), new KeyValue(pane.opacityProperty(), 1))
        );

        return new TimelineTask(timeline);
    }

    @Override
    public AnimatorTask collapseAnimation(Region pane) {
        double height = pane.getHeight();

        final Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(pane.maxHeightProperty(), height)),
                new KeyFrame(Duration.ZERO, new KeyValue(pane.minHeightProperty(), height)),
                new KeyFrame(Duration.millis(EXPAND_DURATION), new KeyValue(pane.maxHeightProperty(), 0)),
                new KeyFrame(Duration.millis(EXPAND_DURATION), new KeyValue(pane.minHeightProperty(), 0)),

                new KeyFrame(Duration.ZERO, new KeyValue(pane.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(EXPAND_FADE_DURATION), new KeyValue(pane.opacityProperty(), 0))
        );

        return new TimelineTask(timeline);
    }

    @Override
    public AnimatorTask fadeOut(Node component) {
        final Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(component.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(FADE_IN_OUT_DURATION), new KeyValue(component.opacityProperty(), 0.0))
        );

        return new TimelineTask(timeline);
    }

    @Override
    public AnimatorTask fadeIn(Node component) {
        Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(component.opacityProperty(), 0.0)),
                new KeyFrame(Duration.millis(FADE_IN_OUT_DURATION), new KeyValue(component.opacityProperty(), 1.0))
        );

        return new TimelineTask(timeline);
    }

    @Override
    public AnimatorTask setInvalid(Collection<Node> nodes) {
        final Timeline timeline = new Timeline();

        InnerShadow effect = new InnerShadow(0, INVALID_FLASH_COLOR);
//        DropShadow effect = new DropShadow(0, INVALID_FLASH_COLOR);

        timeline.setCycleCount(INVALID_FLASH_REPETITIONS);
        timeline.setAutoReverse(true);
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, new KeyValue(effect.radiusProperty(), 0)),
                new KeyFrame(new Duration(INVALID_FLASH_TIME), new KeyValue(effect.radiusProperty(), INVALID_FLASH_INTENSITY)));

//        form.getNodes().forEach(n -> n.setEffect(effect));
        nodes.forEach(n -> n.setEffect(effect));

        return new TimelineTask(timeline);
    }

    @Override
    public AnimatorTask removeInvalid(Node node) {
        if (node.getEffect() != null) {
            final Timeline timeline = new Timeline();

            InnerShadow effect = new InnerShadow(INVALID_FLASH_INTENSITY, INVALID_FLASH_COLOR);

            timeline.getKeyFrames().addAll(
                    new KeyFrame(Duration.ZERO, new KeyValue(effect.radiusProperty(), INVALID_FLASH_INTENSITY)),
                    new KeyFrame(new Duration(INVALID_FLASH_TIME), new KeyValue(effect.radiusProperty(), 0)));
            timeline.setOnFinished(e -> node.setEffect(null));

            node.setEffect(effect);

            return new TimelineTask(timeline);
        }

        return noop();
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


//    @Override
//    public AnimatorTask translateX(Node node, double newLayoutX) {
//        final Timeline timeline = new Timeline(
//                new KeyFrame(Duration.ZERO, new KeyValue(node.layoutXProperty(), node.getLayoutX())),
//                new KeyFrame(Duration.millis(TRANSLATE_DURATION), new KeyValue(node.layoutXProperty(), newLayoutX))
//        );
//
//        return new TimelineTask(timeline);
//    }

    @Override
    public AnimatorTask translate(DoubleProperty property, double from, double to) {
        final Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(property, from)),
                new KeyFrame(Duration.millis(TRANSLATE_DURATION), new KeyValue(property, to))
        );

        return new TimelineTask(timeline);
    }

    @Override
    public AnimatorTask translateSlow(DoubleProperty property, double from, double newValue) {
        final Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(property, from)),
                new KeyFrame(Duration.millis(TRANSLATE_SLOW_DURATION), new KeyValue(property, newValue))
        );

        return new TimelineTask(timeline);
    }
}
