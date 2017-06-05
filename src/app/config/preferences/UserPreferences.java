package app.config.preferences;

import app.config.Config;
import app.config.preferences.properties.ConfigProperty;
import app.util.ExceptionLogger;
import app.util.helpers.KeyWordHelper;
import com.sun.javafx.binding.BidirectionalBinding;
import com.wx.util.representables.DelimiterEncoder;
import javafx.beans.property.*;
import javafx.util.StringConverter;
import javafx.util.converter.BooleanStringConverter;
import javafx.util.converter.DoubleStringConverter;

import java.io.*;
import java.util.HashMap;
import java.util.Map;
import java.util.Objects;
import java.util.prefs.*;
import java.util.stream.DoubleStream;


/**
 * Represents a set of preferences. The storage of preferences is performed by {@link Preferences}.
 * <p>
 * This class adds more features, such as, representing a preference using {@link Property}, using default values,
 * etc...
 * <p>
 * <p>
 * Created on 14/07/2015
 * // TODO: 14.06.16 Render Preferences completely invisible inside this class! Change the constructor, offer methods to get/store, remove resyncProperties. Make the assumption that no-one modifies the underlying preferences (better yet! Add a change listener to the preferences(!!))
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class UserPreferences<E extends ConfigProperty> {

    private static final String DOUBLE_SEPARATOR = " / ";

    /**
     * Encode an array of double into a string.
     *
     * @param array Array to encode
     *
     * @return Encoded array
     */
    public static String convertDoubleArray(double[] array) {
        return DelimiterEncoder.encode(DOUBLE_SEPARATOR, DoubleStream.of(array).mapToObj(String::valueOf));
    }

    /**
     * Decode a string containing an array of double.
     *
     * @param value Value to decode
     *
     * @return Array of double encoded in the string
     */
    public static double[] convertDoubleArray(String value) {
        return DelimiterEncoder.decode(DOUBLE_SEPARATOR, value).stream().mapToDouble(Double::parseDouble).toArray();
    }

    private final Preferences preferences;

    private boolean propertiesChanged = false;
    private final Map<E, StringProperty> properties = new HashMap<>();

    /**
     * Build a set of preferences based on the given {@link Preferences}.
     *
     * @param preferences Preferences where properties will be loaded/stored
     */
    public UserPreferences(Preferences preferences) {
        this.preferences = preferences;
    }

    /**
     * Export the properties into a file (that can be imported using {@link Preferences#importPreferences(InputStream)}.
     *
     * @param file File where the properties will be saved
     *
     * @throws IOException
     */
    public void saveToFile(File file) throws IOException {
        try (OutputStream out = new BufferedOutputStream(new FileOutputStream(file, false))) {
            preferences.exportNode(out);
        } catch (BackingStoreException e) {
            throw new IOException(e);
        }
    }

    /**
     * If the underlying {@link Preferences} have been changed without the use of this object, then, use this method to
     * notify all properties of the change.
     */
    public void resyncProperties() {
        properties.forEach((key, prop) -> prop.setValue(getProperty(key)));
    }

    /**
     * Remove all properties
     */
    public void clearPreferences() {
        try {
            preferences.clear();
        } catch (BackingStoreException e) {
            ExceptionLogger.logException(e);
        }
    }

    /**
     * Get a string property. If this property is not present, its default value is returned.
     *
     * @param prop Property to get
     * @param args All occurrences of '{@code ${i}}' (in the property value) will be replaced by the argument {@code
     *             args[i]}
     *
     * @return The value of the property {@code prop}
     */
    public String getProperty(E prop, Object... args) {
        String value = preferences.get(prop.key(), prop.core().getDefaultValue());

        if (args.length == 0) {
            args = prop.core().getDefaultArgs();
        }

        if (value != null) {
            for (int i = 0; i < args.length; i++) {
                value = KeyWordHelper.replace(value, String.valueOf(i), args[i].toString());
            }
        }

        return value;
    }

    /**
     * Get a integer property. If this property is not present, its default value is returned.
     *
     * @param prop Property to get
     *
     * @return The value of the property {@code prop}
     */
    public int getIntProperty(E prop) {
        return preferences.getInt(prop.key(), prop.core().getDefaultAsInt());
    }

    /**
     * Get a double property. If this property is not present, its default value is returned.
     *
     * @param prop Property to get
     *
     * @return The value of the property {@code prop}
     */
    public double getDoubleProperty(E prop) {
        return preferences.getDouble(prop.key(), prop.core().getDefaultAsDouble());
    }

    /**
     * Get a double array property. If this property is not present, its default value is returned.
     *
     * @param prop Property to get
     *
     * @return The value of the property {@code prop}
     */
    public double[] getDoubleArrayProperty(E prop) {
        String value = preferences.get(prop.key(), "");
        if (value.isEmpty()) {
            return prop.core().getDefaultAsDoubleArray();
        }

        return convertDoubleArray(value);
    }

    /**
     * Get a boolean property. If this property is not present, its default value is returned.
     *
     * @param prop Property to get
     *
     * @return The value of the property {@code prop}
     */
    public boolean getBooleanProperty(E prop) {
        return preferences.getBoolean(prop.key(), prop.core().getDefaultAsBoolean());
    }

    /**
     * Get a path property. If this property is not present, its default value is returned.
     * <p>
     * The keyword '${config}' can be used in the property value to specify the config directory.
     *
     * @param prop Property to get
     * @param args All occurrences of '{@code ${i}}' (in the property value) will be replaced by the argument {@code
     *             args[i]}
     *
     * @return The value of the property {@code prop}
     */
    public File getPathProperty(E prop, Object... args) {
        String path = getProperty(prop, args);
        if (path != null) {
            path = path.replace("${config}", KeyWordHelper.getDirectoryPath(Config.getConfigDirectory()));
        }

        return path == null ? null : new File(path);
    }

    /**
     * Remove a property.
     *
     * @param prop Property to remove
     */
    public void remove(E prop) {
        preferences.remove(prop.key());
//        stringProperty(prop).set(prop.core().getDefaultValue());
    }

    /**
     * Tests if a property is present in the underlying {@link Preferences}.
     *
     * @param prop Property to test existence
     *
     * @return {@code true} if the property is present in the underlying {@link Preferences}
     */
    public boolean isSet(E prop) {
        return !preferences.get(prop.key(), "").isEmpty();
    }

    /**
     * Get a string {@link Property}. Modifying this property will automatically affect the underlying {@link
     * Preferences} and any other {@link Property} of the same key.
     *
     * @param key Property to get
     *
     * @return A {@link Property} corresponding to the given key, mapped to the underlying {@link Preferences}
     */
    public StringProperty stringProperty(E key) {
        StringProperty prop = properties.get(key);
        if (prop == null) {
            prop = new SimpleStringProperty(getProperty(key));
            prop.addListener((observable, oldValue, newValue) -> {
                if (Objects.equals(key.core().getDefaultValue(), newValue)) {
                    preferences.remove(key.key());
                } else {
                    preferences.put(key.key(), newValue);
                }
                propertiesChanged = true;
            });

            properties.put(key, prop);
        }

        return prop;
    }

    /**
     * Get a double {@link Property}. Modifying this property will automatically affect the underlying {@link
     * Preferences} and any other {@link Property} of the same key.
     *
     * @param key Property to get
     *
     * @return A {@link Property} corresponding to the given key, mapped to the underlying {@link Preferences}
     */
    public DoubleProperty doubleProperty(E key) {
        Property<Double> prop = new SimpleObjectProperty<>(getDoubleProperty(key));
        BidirectionalBinding.bind(stringProperty(key), prop, new DoubleStringConverter());

        return DoubleProperty.doubleProperty(prop);
    }

    /**
     * Get a double array {@link Property}. Modifying this property will automatically affect the underlying {@link
     * Preferences} and any other {@link Property} of the same key.
     *
     * @param key Property to get
     *
     * @return A {@link Property} corresponding to the given key, mapped to the underlying {@link Preferences}
     */
    public ObjectProperty<double[]> doubleArrayProperty(E key) {
        ObjectProperty<double[]> prop = new SimpleObjectProperty<>(getDoubleArrayProperty(key));
        BidirectionalBinding.bind(stringProperty(key), prop, new StringConverter<double[]>() {
            @Override
            public String toString(double[] object) {
                return convertDoubleArray(object);
            }

            @Override
            public double[] fromString(String string) {
                return convertDoubleArray(string);
            }
        });

        return prop;
    }

    /**
     * Get a boolean {@link Property}. Modifying this property will automatically affect the underlying {@link
     * Preferences} and any other {@link Property} of the same key.
     *
     * @param key Property to get
     *
     * @return A {@link Property} corresponding to the given key, mapped to the underlying {@link Preferences}
     */
    public BooleanProperty booleanProperty(E key) {
        BooleanProperty prop = new SimpleBooleanProperty(getBooleanProperty(key));
        BidirectionalBinding.bind(stringProperty(key), prop, new BooleanStringConverter());

        return prop;
    }

    /**
     * Set a string property value.
     *
     * @param key   Property to set
     * @param value Value to set
     */
    public void setProperty(E key, String value) {
        stringProperty(key).setValue(value);
    }

    /**
     * Set a double property value.
     *
     * @param key   Property to set
     * @param value Value to set
     */
    public void setProperty(E key, double value) {
        stringProperty(key).setValue(String.valueOf(value));
    }

    /**
     * Set a double array property value.
     *
     * @param key   Property to set
     * @param value Value to set
     */
    public void setProperty(E key, double[] value) {
        stringProperty(key).setValue(convertDoubleArray(value));
    }

    /**
     * Set a int property value.
     *
     * @param key   Property to set
     * @param value Value to set
     */
    public void setProperty(E key, int value) {
        stringProperty(key).setValue(String.valueOf(value));
    }

    /**
     * Set a boolean property value.
     *
     * @param key   Property to set
     * @param value Value to set
     */
    public void setProperty(E key, boolean value) {
        stringProperty(key).setValue(String.valueOf(value));
    }

    /**
     * Set a path property value.
     *
     * @param key   Property to set
     * @param value Value to set
     */
    public void setProperty(E key, File value) {
        setProperty(key, value == null ? null : value.getAbsolutePath());
    }

    /**
     * Tests if the preferences have been changed or not. This method may return false positive, but no false negative.
     *
     * @return {@code true} if the properties have been changed
     */
    public boolean propertiesChanged() {
        return propertiesChanged;
    }

    @Override
    public String toString() {
        StringBuilder result = new StringBuilder();

        try {
            for (String key : preferences.keys()) {
                result.append(key).append(" = ").append(preferences.get(key, null)).append("\n");
            }
        } catch (BackingStoreException e) {
            result.append(e.getMessage());
        }

        return result.toString();
    }


}
