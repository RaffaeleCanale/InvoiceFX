package app.util.bindings;

import javafx.animation.KeyFrame;
import javafx.animation.KeyValue;
import javafx.animation.Timeline;
import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.effect.DropShadow;
import javafx.scene.effect.Effect;
import javafx.scene.effect.InnerShadow;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.util.*;
import java.util.function.Supplier;

/**
 * Created on 04/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class FormElement {

    private static final int ANIMATION_REPETITIONS = 3;
    private static final int ANIMATION_TIME = 100;
    private static final int ANIMATION_INTENSITY = 10;
    private static final Color ANIMATION_COLOR = Color.RED;

    public static FormElement simple(ObservableBooleanValue binding, Node node, Node... nodes) {
        return new Builder(binding).nodes(node, nodes).create();
    }

    public static FormElement simple(ObservableBooleanValue binding, Supplier<Collection<? extends Node>> supplier) {
        return new Builder(binding).nodesSupplier(supplier).create();
    }

    public static Builder build(ObservableBooleanValue binding) {
        return new Builder(binding);
    }

    private final Supplier<Collection<? extends  Node>> node;
    private final ObservableBooleanValue isValid;
    private final boolean innerShadow;
    private final Color animationColor;


    private FormElement(Supplier<Collection<? extends Node>> supplier,
                        ObservableBooleanValue isValid,
                        boolean useAlternativeStyle,
                        Color animationColor,
                        boolean animateImmediately) {
        this.innerShadow = useAlternativeStyle;
        this.node = supplier;
        this.isValid = isValid;
        this.animationColor = animationColor;


        isValid.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                clearEffect();
            } else if (animateImmediately) {
                animateInvalid();
            }
        });
    }

    public void clearEffect() {
        applyEffect(null);
    }

    public boolean isValid() {
        return isValid.get();
    }

    public boolean animateIfInvalid() {
        if (isValid()) {
            return true;
        }

        animateInvalid();
        return false;
    }

    public void animateInvalid() {
        final Timeline timeline = new Timeline();

        Effect effect = innerShadow ?
                addInnerShadowEffect(timeline) :
                addDropShadowEffect(timeline);

        applyEffect(effect);
        timeline.play();
    }

    private Effect addDropShadowEffect(Timeline timeline) {
        DropShadow effect = new DropShadow();
        effect.setColor(animationColor);

        timeline.setCycleCount(ANIMATION_REPETITIONS);
        timeline.setAutoReverse(true);
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, new KeyValue(effect.radiusProperty(), 0)),
                new KeyFrame(new Duration(ANIMATION_TIME), new KeyValue(effect.radiusProperty(), ANIMATION_INTENSITY)));
        return effect;
    }

    private Effect addInnerShadowEffect(Timeline timeline) {
        InnerShadow effect = new InnerShadow();
        effect.setColor(animationColor);

        timeline.setCycleCount(ANIMATION_REPETITIONS);
        timeline.setAutoReverse(true);
        timeline.getKeyFrames().addAll(
                new KeyFrame(Duration.ZERO, new KeyValue(effect.radiusProperty(), 0)),
                new KeyFrame(new Duration(ANIMATION_TIME), new KeyValue(effect.radiusProperty(), ANIMATION_INTENSITY)));
        return effect;
    }

    private void applyEffect(Effect effect) {
        node.get().forEach(n -> n.setEffect(effect));
    }

    public static class Builder {
        private Supplier<Collection<? extends  Node>> supplier = Collections::emptySet;
        private final ObservableBooleanValue isValid;
        private boolean innerShadow = false;
        private Color animationColor = ANIMATION_COLOR;
        private boolean animateImmediately = false;

        public Builder(ObservableBooleanValue isValid) {
            this.isValid = isValid;
        }

        public Builder setAnimationColor(Color animationColor) {
            this.animationColor = animationColor;

            return this;
        }

        public Builder animateImmediately() {
            this.animateImmediately = true;

            return this;
        }

        public Builder nodes(Node node, Node... nodes) {
            Set<Node> set = new HashSet<>();

            set.add(node);
            set.addAll(Arrays.asList(nodes));

            supplier = () -> set;

            return this;
        }

        public Builder nodesSupplier(Supplier<Collection<? extends  Node>> supplier) {
            this.supplier = supplier;

            return this;
        }

        public Builder nodesSingleSupplier(Supplier<? extends Node> supplier) {
            this.supplier = () -> Collections.singletonList(supplier.get());

            return this;
        }

        public Builder useAlternativeStyle() {
            innerShadow = true;

            return this;
        }

        public FormElement create() {
            return new FormElement(supplier, isValid, innerShadow, animationColor, animateImmediately);
        }
    }
}