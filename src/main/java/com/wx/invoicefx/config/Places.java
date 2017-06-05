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
//            PREFERENCES_DIR("Data"),
            DATA_DIR("Data"),
            GOOGLE_DIR("Google"),
            LOGS_DIR("Logs"),
            LATEX_BUILD_DIR("Tex"),
            TEMPLATE_DIR("Template");

            private final String path;

            Dirs(String path) {
                this.path = path;
            }
        }

        public enum Files {
            LOCAL_INDEX_FILE(null, "Index.properties"),
            GOOGLE_CREDENTIALS_FILE(Dirs.GOOGLE_DIR, "Credentials"),
            PREFERENCES_FILE(Dirs.DATA_DIR, "Preferences.xml"),
            LOG_FILE(Dirs.LOGS_DIR, "Exceptions.log");

            private final String path;

            Files(Dirs dir, String name) {
                if (dir == null) {
                    this.path = name;
                } else {
                    this.path =  dir.path + java.io.File.separator  + name;
                }
            }
        }

//        private static final Logger LOG = LogHelper.getLogger(Config.class);
        private static final String CONFIG_DIR_NAME = "Config";

        private static java.io.File configDir;


        public static void init() throws IOException {
            configDir = loadConfigDirectory();
//            LOG.info("Configuration directory located: " + configDir.getAbsolutePath());

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

        private static java.io.File loadConfigDirectory() throws IOException {
            String programDir;
            try {
                programDir = Places.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }

            java.io.File dir = new java.io.File(programDir);
            if (dir.isFile()) {
                dir = dir.getParentFile();
            }


            dir = new java.io.File(dir, CONFIG_DIR_NAME);
            if (!dir.exists() && !dir.mkdir()) {
                throw new FileNotFoundException(dir.getAbsolutePath());
            }

            return dir;
        }

        /**
         * Find the application launcher, that is, the executable Jar file.
         * <p>
         * If this application has not been compiled in a .jar file, then a {@code RuntimeException} is thrown.
         *
         * @return The executable Jar file
         */
        public static java.io.File getAppLauncher() throws IOException {
            try {
                java.io.File file = new java.io.File(Places.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                if (!file.getName().endsWith(".jar")) {
                    throw new IOException("Not a jar file: " + file.getAbsolutePath());
                }
                return file;
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

        private Places() {
        }

    }

