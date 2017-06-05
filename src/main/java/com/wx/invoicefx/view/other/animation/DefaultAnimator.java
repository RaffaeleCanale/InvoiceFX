package com.wx.invoicefx.view.other.animation;

import com.wx.invoicefx.view.other.FormElement;
import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.event.ActionEvent;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.effect.InnerShadow;
import javafx.scene.layout.Pane;
import javafx.scene.paint.Color;
import javafx.util.Duration;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 15.05.17.
 */
public class DefaultAnimator implements AnimatorInterface {

    private static final int EXPAND_DURATION = 300;
    private static final int EXPAND_FADE_DURATION = 100;


    private static final int FADE_IN_OUT_DURATION = 100;


    private static final int INVALID_FLASH_REPETITIONS = 3;
    private static final int INVALID_FLASH_TIME = 100;
    private static final int INVALID_FLASH_INTENSITY = 10;
    private static final Color INVALID_FLASH_COLOR = Color.RED;



    @Override
    public void expandAnimation(Pane pane, double height) {

        final Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(pane.opacityProperty(), 0)),
                new KeyFrame(Duration.ZERO, new KeyValue(pane.maxHeightProperty(), 0)),
                new KeyFrame(Duration.ZERO, new KeyValue(pane.minHeightProperty(), 0)),

                new KeyFrame(Duration.millis(EXPAND_DURATION - EXPAND_FADE_DURATION), new KeyValue(pane.opacityProperty(), 0)),

                new KeyFrame(Duration.millis(EXPAND_DURATION), new KeyValue(pane.maxHeightProperty(), height)),
                new KeyFrame(Duration.millis(EXPAND_DURATION), new KeyValue(pane.minHeightProperty(), height)),
                new KeyFrame(Duration.millis(EXPAND_DURATION), new KeyValue(pane.opacityProperty(), 1))
        );
        timeline.play();
    }

    @Override
    public void collapseAnimation(Pane pane, EventHandler<ActionEvent> onFinished) {
        double height = pane.getHeight();

        final Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(pane.maxHeightProperty(), height)),
                new KeyFrame(Duration.ZERO, new KeyValue(pane.minHeightProperty(), height)),
                new KeyFrame(Duration.millis(EXPAND_DURATION), new KeyValue(pane.maxHeightProperty(), 0)),
                new KeyFrame(Duration.millis(EXPAND_DURATION), new KeyValue(pane.minHeightProperty(), 0)),

                new KeyFrame(Duration.ZERO, new KeyValue(pane.opacityProperty(), 1)),
                new KeyFrame(Duration.millis(EXPAND_FADE_DURATION), new KeyValue(pane.opacityProperty(), 0))
        );

        timeline.setOnFinished(onFinished);

        timeline.play();
    }

    @Override
    public void fadeInOut(Node component, Runnable process) {
        final Timeline timeline = new Timeline(
                new KeyFrame(Duration.ZERO, new KeyValue(component.opacityProperty(), 1.0)),
                new KeyFrame(Duration.millis(FADE_IN_OUT_DURATION), new KeyValue(component.opacityProperty(), 0.0))
        );

        timeline.setOnFinished(event -> {
            process.run();

            Timeline timeline1 = new Timeline(
                    new KeyFrame(Duration.ZERO, new KeyValue(component.opacityProperty(), 0.0)),
                    new KeyFrame(Duration.millis(FADE_IN_OUT_DURATION), new KeyValue(component.opacityProperty(), 1.0))
            );
            timeline1.play();
        });
        timeline.play();
    }

    @Override
    public void animateInvalid(FormElement form) {
        final Timeline timeline = new Timeline();

        InnerShadow effect = new InnerShadow(0, INVALID_FLASH_COLOR);
//        DropShadow effect = new DropShadow(0, INVALID_FLASH_COLOR);

        timeline.setCycleCount(INVALID_FLASH_REPETITIONS);
        timeline.setAutoReverse(true);
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, new KeyValue(effect.radiusProperty(), 0)),
                new KeyFrame(new Duration(INVALID_FLASH_TIME), new KeyValue(effect.radiusProperty(), INVALID_FLASH_INTENSITY)));

        form.getNodes().forEach(n -> n.setEffect(effect));
        timeline.play();
    }

}
