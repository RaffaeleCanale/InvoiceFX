package app.config.preferences.properties;

import app.cmd.CommandRunner;
import app.config.preferences.UserPreferences;

import java.util.Map;
import java.util.Objects;

/**
 * Container that describes all the meta information (type, default values,...) related to a property.
 * <p>
 * Created on 15/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public final class PropertyCore {

    /**
     * Describes a string property.
     *
     * @param defaultValue Default value for this property
     * @param defaultArgs  Default arguments used if none are provided
     *
     * @return A core describing this property
     */
    public static PropertyCore stringProperty(String defaultValue, Object... defaultArgs) {
        return new PropertyCore(new Object[]{defaultValue}, false, defaultArgs);
    }

    /**
     * Describes a boolean property.
     *
     * @param defaultValue Default value for this property
     * @param defaultArgs  Default arguments used if none are provided
     *
     * @return A core describing this property
     */
    public static PropertyCore booleanProperty(boolean defaultValue, Object... defaultArgs) {
        return new PropertyCore(new Object[]{defaultValue}, false, defaultArgs);
    }

    /**
     * Describes a double property.
     *
     * @param defaultValue Default value for this property
     * @param defaultArgs  Default arguments used if none are provided
     *
     * @return A core describing this property
     */
    public static PropertyCore doubleProperty(double defaultValue, Object... defaultArgs) {
        return new PropertyCore(new Object[]{defaultValue}, false, defaultArgs);
    }

    /**
     * Describes a double array property.
     *
     * @param defaultValue Default value for this property
     *
     * @return A core describing this property
     */
    public static PropertyCore doubleArrayProperty(double... defaultValue) {
        return new PropertyCore(new Object[]{defaultValue}, false, new Object[0]);
    }

    /**
     * Describes an integer property.
     *
     * @param defaultValue Default value for this property
     * @param defaultArgs  Default arguments used if none are provided
     *
     * @return A core describing this property
     */
    public static PropertyCore intProperty(int defaultValue, Object... defaultArgs) {
        return new PropertyCore(new Object[]{defaultValue}, false, defaultArgs);
    }

    /**
     * Describes a property that has a different value according to the operating system used.
     *
     * @param values A mapping of the default values for each operating system (must be exhaustive)
     * @param defaultArgs  Default values used if none are provided
     *
     * @return A core describing this property
     */
    public static PropertyCore osProperty(Map<CommandRunner.SupportedOs, ?> values, Object... defaultArgs) {
        Object[] defaultValues = new Object[CommandRunner.SupportedOs.values().length];

        values.forEach((os, v) -> defaultValues[os.ordinal()] = v);

        return new PropertyCore(defaultValues, true, defaultArgs);
    }

    final Object[] defaultValues;
    final boolean isOsProperty;
    final Object[] defaultArgs;

    private PropertyCore(Object[] defaultValues, boolean isOsProperty, Object[] defaultArgs) {
        this.defaultValues = defaultValues;
        this.isOsProperty = isOsProperty;
        this.defaultArgs = defaultArgs;
    }

    public Object[] getDefaultArgs() {
        return defaultArgs;
    }

    public String getDefaultValue() {
        Object value = getDefaultValue0();
        if (value instanceof double[]) {
            return UserPreferences.convertDoubleArray((double[]) value);
        }

        return Objects.toString(value);
    }

    public boolean getDefaultAsBoolean() {
        return (boolean) getDefaultValue0();
    }

    public int getDefaultAsInt() {
        return (int) getDefaultValue0();
    }

    public double getDefaultAsDouble() {
        return (double) getDefaultValue0();
    }

    public double[] getDefaultAsDoubleArray() {
        return (double[]) getDefaultValue0();
    }

    private Object getDefaultValue0() {
        if (isOsProperty) {
            return defaultValues[CommandRunner.getOs().ordinal()];
        } else {
            return defaultValues[0];
        }
    }
}
