package com.wx.invoicefx.config;

import com.wx.io.TextAccessor;
import com.wx.util.Format;
import com.wx.util.log.LogHelper;

import java.io.File;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.Date;
import java.util.logging.Level;
import java.util.logging.Logger;

import static com.wx.invoicefx.config.Places.Dirs.LOGS_DIR;
import static com.wx.invoicefx.config.Places.Files.LOG_FILE;

/**
 * Utility classes that allows to display and log exceptions into log files.
 * <p>
 * These methods require {@link Places} to be initialized (see {@link Places#init()} ).
 * <p>
 * Created on 22/04/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class ExceptionLogger {

    private static final Logger LOG = LogHelper.getLogger(ExceptionLogger.class);

    /**
     * Log an exception into a log file. The exception is also displayed in the console logger.
     *
     * @param e Exception to log
     */
    public static void logException(Throwable e) {
        logException(e, null);
    }

    public static void logException(Throwable e, String message) {
        String content = "[" + e.getClass().getSimpleName() + "] " + e.getMessage();
        if (message != null) {
            content = message + "\nCaused by: " + content;
        }

        LOG.warning(content);

        try {
            File file = Places.getFile(LOG_FILE);

            PrintWriter writer = new PrintWriter(new FileOutputStream(file, true));
            writer.write("\n\n" + Format.formatDate(new Date().getTime()) + "\n" + content + "\n");
            e.printStackTrace(writer);
            writer.close();
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to write logs", ex);
        }
    }

    /**
     * Logs a process output into log files. Should only be used in case of errors from the process.
     *
     * @param output Output of the process
     * @param name   Name that identifies the process
     * @param cmd    Command executed for the process
     * @param code   Exit code of the process
     */
    public static void logProcessOutput(String output, String name, Object cmd, int code) {
        LOG.warning("Command " + cmd + " (" + name + ") exited with code " + code);

        File file = Places.getCustomFile(LOGS_DIR, name + ".log");

        try (TextAccessor accessor = new TextAccessor()
                .setOut(file, false)) {
            accessor.write(cmd.toString());
            accessor.write("Exit code: " + code);
            accessor.write(output);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to write logs", ex);
        }
    }
}
