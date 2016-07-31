package app.config;

import app.config.manager.ManagerInterface;

import java.util.Objects;

/**
 * Created on 15/03/2016
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 */
public class ModelManager {

    private static ManagerInterface instance;


    public static ManagerInterface instance() {
        checkInit();
        return instance;
    }

    public static void setInstance(ManagerInterface instance) {
        ModelManager.instance = Objects.requireNonNull(instance);
    }

    private static void checkInit() {
        if (instance == null) {
            throw new IllegalStateException("No instance set");
        }
    }

}
