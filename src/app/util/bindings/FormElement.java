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
 * This container links visual components (nodes) with a some boolean binding that indicates whether this component is
 * "valid" or not. The component is considered valid if the value of the input from the user to that component respects
 * all the conditions defined by the boolean binding.
 * <p>
 * This container allows to display an error animation around the corresponding nodes if the validity condition is not
 * met. This animation can be triggered either manually or automatically (every time the condition is equal to false)
 * <p>
 * This class is typically used to wrap form elements.
 * <p>
 * Created on 04/07/2015
 * <p>
 * // TODO: 14.06.16 Move this class to FXLibraries
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class FormElement {

    private static final int ANIMATION_REPETITIONS = 3;
    private static final int ANIMATION_TIME = 100;
    private static final int ANIMATION_INTENSITY = 10;
    private static final Color ANIMATION_COLOR = Color.RED;

    /**
     * Build a simple form element from the given validity condition and nodes.
     *
     * @param binding Validity condition (defines whether this form is considered valid or not)
     * @param nodes   Nodes associated to this form element
     *
     * @return The corresponding form element
     */
    public static FormElement simple(ObservableBooleanValue binding, Node... nodes) {
        return new Builder(binding).nodes(nodes).create();
    }

    /**
     * Build a simple form element from the given validity condition and nodes.
     * <p>
     * Note that the nodes are provided through a supplier that will be only called when the animations are triggered.
     *
     * @param binding  Validity condition (defines whether this form is considered valid or not)
     * @param supplier Supplier of the nodes associated to this form element
     *
     * @return The corresponding form element
     */
    public static FormElement simple(ObservableBooleanValue binding, Supplier<Collection<? extends Node>> supplier) {
        return new Builder(binding).nodesSupplier(supplier).create();
    }

    /**
     * Initialize a form element builder.
     *
     * @param binding Validity condition (defines whether this form is considered valid or not)
     *
     * @return A form element builder
     */
    public static Builder build(ObservableBooleanValue binding) {
        return new Builder(binding);
    }

    private final Supplier<Collection<? extends Node>> nodes;
    private final ObservableBooleanValue isValid;
    private final boolean innerShadow;
    private final Color animationColor;


    private FormElement(Supplier<Collection<? extends Node>> supplier,
                        ObservableBooleanValue isValid,
                        boolean useAlternativeStyle,
                        Color animationColor,
                        boolean animateImmediately) {
        this.innerShadow = useAlternativeStyle;
        this.nodes = supplier;
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

    /**
     * Force to clear any effect present on the form node(s)
     */
    public void clearEffect() {
        applyEffect(null);
    }

    /**
     * @return {@code true} if the validity condition for this form is met
     */
    public boolean isValid() {
        return isValid.get();
    }

    /**
     * Trigger the error animation only if the validity condition for this form is not met.
     *
     * @return {@code true} if this form is valid (hence, no animations where triggered)
     */
    public boolean animateIfInvalid() {
        if (isValid()) {
            return true;
        }

        animateInvalid();
        return false;
    }

    /**
     * Trigger the error animation independently of this form validity state
     */
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
        nodes.get().forEach(n -> n.setEffect(effect));
    }

    /**
     * Builder class to easily construct a {@link FormElement}
     */
    public static class Builder {

        private Supplier<Collection<? extends Node>> supplier = Collections::emptySet;
        private final ObservableBooleanValue isValid;
        private boolean innerShadow = false;
        private Color animationColor = ANIMATION_COLOR;
        private boolean animateImmediately = false;

        /**
         * Initialize a new {@link FormElement} builder.
         *
         * @param isValid Validity condition of this form
         */
        public Builder(ObservableBooleanValue isValid) {
            this.isValid = Objects.requireNonNull(isValid);
        }

        /**
         * Set the main animation color.
         *
         * @param animationColor Animation color to set
         *
         * @return this for chained calls
         */
        public Builder setAnimationColor(Color animationColor) {
            this.animationColor = Objects.requireNonNull(animationColor);

            return this;
        }

        /**
         * Set this form to automatically trigger an animation whenever the validity condition is failed.
         *
         * @return this for chained calls
         */
        public Builder animateImmediately() {
            this.animateImmediately = true;

            return this;
        }

        /**
         * Set the nodes that represent this form and are linked with the validity condition.
         *
         * @param nodes Nodes to attach to this form
         *
         * @return this for chained calls
         */
        public Builder nodes(Node... nodes) {
            if (nodes.length == 0) {
                throw new IllegalArgumentException("There must be at least one node");
            }
            Set<Node> set = new HashSet<>(Arrays.asList(nodes));
            supplier = () -> set;

            return this;
        }

        /**
         * Set the nodes that represent this form and are linked with the validity condition. The forms can be set in
         * form of a supplier.
         * <p>
         * This can be useful when the nodes of the form have not been created yet.
         * <p>
         * Note that the supplier will be called every time the animations triggered.
         *
         * @param supplier Supplier of the nodes to attach to this form
         *
         * @return this for chained calls
         */
        public Builder nodesSupplier(Supplier<Collection<? extends Node>> supplier) {
            this.supplier = Objects.requireNonNull(supplier);

            return this;
        }

        /**
         * Same as {@link #nodesSupplier(Supplier)} but with a supplier that provides a single node.
         *
         * @param supplier Supplier of the single node to attach to this form.
         *
         * @return this for chained calls
         */
        public Builder nodesSingleSupplier(Supplier<? extends Node> supplier) {
            Objects.requireNonNull(supplier);
            this.supplier = () -> Collections.singletonList(supplier.get());

            return this;
        }

        /**
         * Use an alternative visual style.
         *
         * @return this for chained calls
         */
        public Builder useAlternativeStyle() {
            innerShadow = true;

            return this;
        }

        /**
         * @return The created {@link FormElement}
         */
        public FormElement create() {
            return new FormElement(supplier, isValid, innerShadow, animationColor, animateImmediately);
        }
    }
}