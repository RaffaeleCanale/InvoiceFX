package com.wx.invoicefx.config;

import com.wx.io.AccessorUtil;
import com.wx.io.file.FileUtil;

import java.io.File;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.net.URISyntaxException;

/**
 * @author Raffaele Canale (<a href="mailto:raffaelecanale@gmail.com?subject=InvoiceFX">raffaelecanale@gmail.com</a>)
 * @version 0.1 - created on 11.05.17.
 */
public class ConfigDirectory {

        public enum Files {
            EXPORTED_PREFERENCES_FILE("preferences.xml"),
            GOOGLE_CREDENTIALS_FILE("google_credentials"),
            LOGS_DIR("Logs"),
            LATEX_BUILD_DIR("Tex"),
            TEMPLATE_DIR("Template");

            private final String path;

            Files(String path) {
                this.path = path;
            }
        }

//        private static final Logger LOG = LogHelper.getLogger(Config.class);
        private static final String CONFIG_DIR_NAME = "Config";

        private static File configDir;


        public static void init() throws IOException {
            configDir = loadConfigDirectory();
//            LOG.info("Configuration directory located: " + configDir.getAbsolutePath());

        }

        public static File getConfigFile(Files configFile, String... subFile) {
            File resultFile;

            if (subFile != null && subFile.length > 1) {
                String path = configFile.path + File.separator + String.join(File.separator, subFile);
                resultFile = new File(configDir, path);
            } else {
                resultFile = new File(configDir, configFile.path);
            }

            AccessorUtil.createParent(resultFile);
            return resultFile;
        }

        /**
         * Get a file contained in the config directory, ensuring that this file does not exist.
         *
         * @param name      Name of the file
         * @param extension Extension of the file
         *
         * @return File in the config directory
         */
        public static File getFreshConfigFile(Files configFile, String name, String extension) {
            String path = configFile.path + File.separator + name;
            return FileUtil.getFreshFile(configDir, path, extension);
        }



//        /**
//         * Return the config directory.
//         *
//         * @return The config directory
//         */
//        public static File getConfigDirectory2() {
//            return configDir;
//        }


        private static File loadConfigDirectory() throws IOException {
            String programDir;
            try {
                programDir = ConfigDirectory.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath();
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

        /**
         * Find the application launcher, that is, the executable Jar file.
         * <p>
         * If this application has not been compiled in a .jar file, then a {@code RuntimeException} is thrown.
         *
         * @return The executable Jar file
         */
        public static File getAppLauncher() throws IOException {
            try {
                File file = new File(ConfigDirectory.class.getProtectionDomain().getCodeSource().getLocation().toURI().getPath());
                if (!file.getName().endsWith(".jar")) {
                    throw new IOException("Not a jar file: " + file.getAbsolutePath());
                }
                return file;
            } catch (URISyntaxException e) {
                throw new IOException(e);
            }
        }

        private ConfigDirectory() {
        }

    }

