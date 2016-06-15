package app.util.gui.components;

import com.sun.javafx.binding.BidirectionalBinding;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.control.TextField;

import java.text.NumberFormat;
import java.text.ParseException;
import java.util.function.Function;

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

    private void updateText(double value) {
        setText(nf.format(numberToView.apply(value)));
    }

    public void setNumberFormat(NumberFormat nf) {
        this.nf = nf;
        updateText(number.get());
    }

    public void multiplyInView(double value) {
        setNumberToView(d -> d*value);
        setViewToNumber(d -> d/value);
    }

    public void setViewToNumber(Function<Double, Double> viewToNumber) {
        this.viewToNumber = viewToNumber;
    }

    public void setNumberToView(Function<Double, Double> numberToView) {
        this.numberToView = numberToView;
    }

    public final Number getNumber() {
        return number.get();
    }

    public final void setNumber(double value) {
        number.set(value);
    }

    public DoubleProperty numberProperty() {
        return number;
    }

    private void initHandlers() {

        // try to parse when focus is lost or RETURN is hit
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
//        if (input == null || input.length() == 0) {
//            return;
//        }

        try {
            Number parsedNumber = nf.parse(input);
//            BigDecimal newValue = new BigDecimal(parsedNumber.toString());
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