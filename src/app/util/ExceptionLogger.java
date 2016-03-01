package app.util;

import app.config.Config;
import com.wx.io.TextAccessor;
import com.wx.io.file.FileUtil;
import com.wx.util.Format;
import com.wx.util.log.LogHelper;

import java.io.*;
import java.util.Date;
import java.util.logging.Logger;

/**
 * Created on 22/04/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public class ExceptionLogger {

    private static final Logger LOG = LogHelper.getLogger(ExceptionLogger.class);

    private static final String LOGS_DIR = "Logs";
    private static final String FILE_NAME = "Exceptions.log";


    public static void logException(Exception e) {
        LOG.warning("[" + e.getClass().getSimpleName() + "] " + e.getMessage());
        try {
            File file = getLogFile(FILE_NAME, false);

            PrintWriter writer = new PrintWriter(new FileOutputStream(file, true));
            writer.write("\n\n" + Format.formatDate(new Date().getTime()) + "\n");
            e.printStackTrace(writer);
            writer.close();
        } catch (IOException ex) {
            LOG.severe("Failed to write logs:");
            ex.printStackTrace();
        }
    }

    public static void logProcessOutput(String output, String name, Object cmd, int code) {
        LOG.warning("Command " + cmd + " (" + name + ") exited with code " + code);

        try (TextAccessor accessor = new TextAccessor()
                .setOut(getLogFile(name, true), false)) {
            accessor.write(cmd.toString());
            accessor.write("Exit code: " + code);
            accessor.write(output);
        } catch (IOException e) {
            LOG.severe("Failed to write logs:");
            e.printStackTrace();
        }
    }

    private static File getLogFile(String name, boolean fresh) throws IOException {
        File file = fresh ?
                Config.getFreshConfigFile(LOGS_DIR + File.separator + name, ".txt") :
                Config.getConfigFile(LOGS_DIR + File.separator + name);
        FileUtil.autoCreateDirectory(file.getParentFile());

        return file;
    }
}
