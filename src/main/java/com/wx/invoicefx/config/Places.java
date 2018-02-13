package com.wx.invoicefx.config;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 11.05.17.
 */
public class Places {

    public enum Dirs {
        DATA_ROOT("Data"),
        DATA_DIR(DATA_ROOT, "Local"),
        TEMP_DATA_DIR(DATA_ROOT, "Temp"),
        BACKUP_DIRECTORY(DATA_ROOT, "Backup"),

        TEX_ROOT("Tex"),
        @SuppressWarnings("SpellCheckingInspection")
        TEX_BINARY_DIR(TEX_ROOT, "miktex"),
        TEX_TEMPLATE_ARCHIVES(TEX_ROOT, "Templates"),
        TEX_BUILD_DIR(TEX_ROOT, "Tex"),

        MISC_ROOT("Misc"),
        GOOGLE_CREDENTIALS_FILE(MISC_ROOT, "Credentials"),
        LOGS_DIR(MISC_ROOT, "Logs"),
        CACHE(MISC_ROOT, "Cache"),
        UPDATE(MISC_ROOT, "Update");

        private final String path;


        Dirs(String path) {
            this.path = path;
        }

        Dirs(Dirs dir, String path) {
            if (dir == null) {
                this.path = path;
            } else {
                this.path = dir.path + java.io.File.separator + path;
            }
        }
    }

    public enum Files {
        //        UPDATE_SCRIPT(Dirs.MISC_ROOT, AppResources.localPreferences().getString(UPDATE_SCRIPT_NAME)),
        UPDATER(Dirs.MISC_ROOT, "cpupdater.jar"),
        UPDATER_CONFIG(Dirs.MISC_ROOT, "update.properties"),
        LOG_FILE(Dirs.LOGS_DIR, "Exceptions.log");

        private final String path;

        Files(Dirs dir, String name) {
            if (dir == null) {
                this.path = name;
            } else {
                this.path = dir.path + java.io.File.separator + name;
            }
        }
    }

    private static final String CONFIG_DIR_NAME = "Config";

    private static File configDir;


    public static void init() throws IOException {
        configDir = loadConfigDirectory();
    }

    public static File getDir(Dirs dir) {
        return new File(configDir, dir.path);
    }

    public static File getFile(Files file) {
        return new File(configDir, file.path);
    }

    public static File getCustomFile(Dirs dir, String filename) {
        String path = dir.path + File.separator + filename;

        return new File(configDir, path);
    }

    public static File getConfigDir() {
        return configDir;
    }

    private static File loadConfigDirectory() throws IOException {
        String programDir;
        try {
            programDir = Places.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
        } catch (URISyntaxException e) {
            throw new IOException(e);
        }

        File dir = new File(programDir);
        if (dir.isFile()) {
            dir = dir.getParentFile();
        }


        dir = new File(dir, CONFIG_DIR_NAME);
        if (!dir.exists() && !dir.mkdir()) {
            throw new FileNotFoundException(dir.getAbsolutePath());
        }

        return dir;
    }

    // TODO: 29.06.17 Check if this is still needed for the updater
//    /**
//     * Find the application launcher, that is, the executable Jar file.
//     * <p>
//     * If this application has not been compiled in a .jar file, then a {@code RuntimeException} is thrown.
//     *
//     * @return The executable Jar file
//     */
//    public static java.io.File getAppLauncher() throws IOException {
//        try {
//            java.io.File file = new java.io.File(Places.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
//            if (!file.getName().endsWith(".jar")) {
//                throw new IOException("Not a jar file: " + file.getAbsolutePath());
//            }
//            return file;
//        } catch (URISyntaxException e) {
//            throw new IOException(e);
//        }
//    }

    private Places() {
    }

}

