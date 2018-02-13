package com.wx.invoicefx.ui.components;

import com.wx.invoicefx.ui.animation.Animator;
import com.wx.invoicefx.ui.animation.AnimatorInterface;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.Event;
import javafx.event.EventHandler;
import javafx.scene.Node;
import javafx.scene.layout.AnchorPane;
import javafx.scene.shape.Circle;
import javafx.scene.shape.Rectangle;


public class ToggleSwitch extends AnchorPane {

    private static final double HEAD_RADIUS = 12.0;
    private static final double CONTAINER_WIDTH = 48.0;
    private static final double CONTAINER_HEIGHT = 18.0;

    private static final double LEFT_CENTER = Math.max(HEAD_RADIUS, CONTAINER_HEIGHT/2.0);
    private static final double RIGHT_CENTER = LEFT_CENTER - CONTAINER_HEIGHT + CONTAINER_WIDTH;

    private final BooleanProperty switchedOn = new SimpleBooleanProperty(false);
    private final BooleanProperty sticky = new SimpleBooleanProperty(false);
    private final ObjectProperty<EventHandler<Event>> onStickyPrevent = new SimpleObjectProperty<>(null);

    private final Node toggleNode;
    private final Rectangle containerNode;

    public ToggleSwitch() {
        final double CONTAINER_RADIUS = CONTAINER_HEIGHT/2.0;

        setPrefSize(RIGHT_CENTER + LEFT_CENTER, 2*Math.max(CONTAINER_RADIUS, HEAD_RADIUS));

        containerNode = new Rectangle(CONTAINER_WIDTH, CONTAINER_HEIGHT);
        containerNode.setArcWidth(2*CONTAINER_RADIUS);
        containerNode.setArcHeight(2*CONTAINER_RADIUS);
        containerNode.setLayoutX(LEFT_CENTER - CONTAINER_RADIUS);
        containerNode.setLayoutY(LEFT_CENTER - CONTAINER_RADIUS);
        containerNode.getStyleClass().setAll("toggle-container-off");

        toggleNode = new Circle(HEAD_RADIUS);
        toggleNode.setLayoutX(LEFT_CENTER);
        toggleNode.setLayoutY(LEFT_CENTER);
        toggleNode.getStyleClass().setAll("toggle-head-off");

        getChildren().addAll(containerNode, toggleNode);

        initListeners();

    }

    public EventHandler<Event> getOnStickyPrevent() {
        return onStickyPrevent.get();
    }

    public ObjectProperty<EventHandler<Event>> onStickyPreventProperty() {
        return onStickyPrevent;
    }

    public void setOnStickyPrevent(EventHandler<Event> onStickyPrevent) {
        this.onStickyPrevent.set(onStickyPrevent);
    }

    public boolean getSwitchedOn() {
        return switchedOn.get();
    }

    public BooleanProperty switchedOnProperty() {
        return switchedOn;
    }

    public void setSwitchedOn(boolean switchedOn) {
        this.switchedOn.set(switchedOn);
    }

    public boolean getSticky() {
        return sticky.get();
    }

    public BooleanProperty stickyProperty() {
        return sticky;
    }

    public void setSticky(boolean sticky) {
        this.sticky.set(sticky);
    }

    private void initListeners() {
        setOnMouseClicked(e -> {
            if (getSticky()) {
                final double x1 = getSwitchedOn() ? RIGHT_CENTER : LEFT_CENTER;
                final double x2 = getSwitchedOn() ? LEFT_CENTER*2 : RIGHT_CENTER/2;


                AnimatorInterface animator = Animator.instance();
                animator.translate(toggleNode.layoutXProperty(), x2)
                        .then(() -> animator.translate(toggleNode.layoutXProperty(), x1).run())
                        .run();

                if (onStickyPrevent.get() != null) {
                    onStickyPrevent.get().handle(e);
                }
            } else {
                switchedOn.set(!switchedOn.get());
            }

        });

        switchedOn.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                toggleNode.getStyleClass().setAll("toggle-head-on");
                containerNode.getStyleClass().setAll("toggle-container-on");

                Animator.instance().translate(toggleNode.layoutXProperty(), RIGHT_CENTER).run();
            } else {
                toggleNode.getStyleClass().setAll("toggle-head-off");
                containerNode.getStyleClass().setAll("toggle-container-off");

                Animator.instance().translate(toggleNode.layoutXProperty(), LEFT_CENTER).run();
            }
        });

    }
}