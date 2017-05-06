package app.config.preferences.properties;

import app.cmd.CommandRunner;

/**
 * Interface of a property identifier used in {@link app.config.preferences.UserPreferences}
 * <p>
 * Created on 15/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public interface ConfigProperty {

    /**
     * Get the core (containing the property meta-information).
     *
     * @return The core of this property
     */
    PropertyCore core();

    /**
     * Display name of this property. Must be unique.
     *
     * @return Display name of this property
     */
    String name();

    /**
     * Encode this property into a unique key according to its name and meta-information.
     *
     * @return A unique key identifier for this property
     */
    default String key() {
        return core().isOsProperty ?
                name().toLowerCase() + "_" + CommandRunner.getOs().name().toLowerCase() :
                name().toLowerCase();
    }
}
