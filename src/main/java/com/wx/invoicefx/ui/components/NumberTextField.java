package com.wx.invoicefx.ui.components;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TextField;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.Objects;
import java.util.function.Function;
import java.util.function.Predicate;

/**
 * Textfield implementation that accepts formatted number and stores them in a
 * BigDecimal property The user input is formatted when the focus is lost or the
 * user hits RETURN.
 *
 * @author Thomas Bolz
 */
public class NumberTextField extends TextField {

    private NumberFormat nf;
    private DoubleProperty number = new SimpleDoubleProperty();
    private Function<Double, Double> numberToView = Function.identity();
    private Function<Double, Double> viewToNumber = Function.identity();

    private boolean defaultToZero = false;
    private Predicate<Double> numberPredicate = n -> true;


    public NumberTextField() {
        this(0, NumberFormat.getInstance());
    }

    public NumberTextField(double value) {
        this(value, NumberFormat.getInstance());
    }

    public NumberTextField(double value, NumberFormat nf) {
        super();

        this.nf = nf;
        initHandlers();
        setNumber(value);
    }

    public void setNumberPredicate(Predicate<Double> numberPredicate) {
        this.numberPredicate = Objects.requireNonNull(numberPredicate);
    }

    public void setNumberFormat(NumberFormat nf) {
        this.nf = nf;
        updateText(number.get());
    }

    public void setNumberView(Function<Double, Double> viewToNumber, Function<Double, Double> numberToView) {
        this.viewToNumber = viewToNumber;
        this.numberToView = numberToView;

        updateText(number.get());
    }

    public final Number getNumber() {
        return number.get();
    }

    public final void setNumber(double value) {
        if (numberPredicate.test(value)) {
            number.set(value);
        }
    }

    public DoubleProperty numberProperty() {
        return number;
    }

    public void setDefaultToZero(boolean defaultToZero) {
        this.defaultToZero = defaultToZero;
    }

    private void updateText(double value) {
        setText(nf.format(numberToView.apply(value)));
    }

    private void initHandlers() {
        setOnAction(e -> parseAndFormatInput());

        focusedProperty().addListener((observable, oldValue, newValue) -> {
            if (!newValue) {
                parseAndFormatInput();
            }
        });

        // Set text in field if BigDecimal property is changed from outside.
        number.addListener((observable, oldValue, newValue) -> {
            updateText(newValue.doubleValue());
        });
    }

    /**
     * Tries to parse the user input to a number according to the provided
     * NumberFormat
     */
    private void parseAndFormatInput() {
        String input = getText();
        if (input == null || input.length() == 0) {
            if (defaultToZero) {
                setNumber(0.0);
            }
            return;
        }

        try {
            Number parsedNumber = nf.parse(input);
            setNumber(viewToNumber.apply(parsedNumber.doubleValue()));
        } catch (ParseException ex) {
            try {
                double value = Double.parseDouble(input);
                setNumber(viewToNumber.apply(value));

            } catch (NumberFormatException e) {}
        }

        selectAll();
        updateText(number.get());
    }
}