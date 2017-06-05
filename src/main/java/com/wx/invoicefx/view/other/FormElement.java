package com.wx.invoicefx.view.other;

import javafx.beans.value.ObservableBooleanValue;
import javafx.scene.Node;
import javafx.scene.paint.Color;

import java.util.*;
import java.util.function.Supplier;

/**
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class FormElement {

    /**
     * Build a simple form element from the given validity condition and nodes.
     *
     * @param binding Validity condition (defines whether this form is considered valid or not)
     * @param nodes   Nodes associated to this form element
     *
     * @return The corresponding form element
     */
    public static FormElement simple(ObservableBooleanValue binding, Node... nodes) {
        if (nodes.length == 0) {
            throw new IllegalArgumentException("There must be at least one node");
        }
        Set<Node> set = new HashSet<>(Arrays.asList(nodes));

        return new FormElement(() -> set, binding);
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
    public static FormElement fromSupplier(ObservableBooleanValue binding, Supplier<Collection<? extends Node>> supplier) {
        return new FormElement(supplier, binding);
    }

    public static FormElement fromSingleSupplier(ObservableBooleanValue binding, Supplier<? extends Node> supplier) {
        return new FormElement(() -> Collections.singletonList(supplier.get()), binding);
    }


    private final Supplier<Collection<? extends Node>> nodes;
    private final ObservableBooleanValue isValid;


    private FormElement(Supplier<Collection<? extends Node>> supplier,
                        ObservableBooleanValue isValid) {
        this.nodes = supplier;
        this.isValid = isValid;


        isValid.addListener((observable, oldValue, newValue) -> {
            if (newValue) {
                nodes.get().forEach(n -> n.setEffect(null));
//            } else if (animateImmediately) {
//                animateInvalid();
            }
        });
    }

    public Collection<? extends Node> getNodes() {
        return nodes.get();
    }


    /**
     * @return {@code true} if the validity condition for this form is met
     */
    public boolean isValid() {
        return isValid.get();
    }


}