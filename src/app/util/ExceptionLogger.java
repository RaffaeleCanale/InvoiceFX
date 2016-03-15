package app.util;

import app.config.Config;
import com.wx.io.AccessorUtil;
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

/**
 * Utility classes that allows to display and log exceptions into log files.
 * <p>
 * These methods require {@link Config} to be initialized (see {@link Config#initConfig(app.config.ModelManagerFactory.Impl)}).
 * <p>
 * Created on 22/04/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class ExceptionLogger {

    private static final Logger LOG = LogHelper.getLogger(ExceptionLogger.class);

    private static final String LOGS_DIR = "Logs";
    private static final String LOG_FILE_NAME = "Exceptions.log";

    /**
     * Log an exception into a log file. The exception is also displayed in the console logger.
     *
     * @param e Exception to log
     */
    public static void logException(Exception e) {
        LOG.warning("[" + e.getClass().getSimpleName() + "] " + e.getMessage());
        try {
            File file = getLogFile(LOG_FILE_NAME, false);

            PrintWriter writer = new PrintWriter(new FileOutputStream(file, true));
            writer.write("\n\n" + Format.formatDate(new Date().getTime()) + "\n");
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

        try (TextAccessor accessor = new TextAccessor()
                .setOut(getLogFile(name, true), false)) {
            accessor.write(cmd.toString());
            accessor.write("Exit code: " + code);
            accessor.write(output);
        } catch (IOException ex) {
            LOG.log(Level.SEVERE, "Failed to write logs", ex);
        }
    }

    private static File getLogFile(String name, boolean fresh) throws IOException {
        File file = fresh ?
                Config.getFreshConfigFile(LOGS_DIR + File.separator + name, ".txt") :
                Config.getConfigFile(LOGS_DIR + File.separator + name);
        AccessorUtil.createParent(file);

        return file;
    }
}
