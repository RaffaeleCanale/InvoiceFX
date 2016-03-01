package app.cmd;

import app.util.ExceptionLogger;
import app.util.gui.AlertBuilder;
import com.wx.fx.util.callback.SimpleCallback;
import com.wx.io.TextAccessor;
import com.wx.util.log.LogHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * This is an abstract tool allowing to execute a shell command.
 * <p>
 * For Unix, see {@link UnixCommandRunner},
 * <p>
 * For Windows, see {@link WindowsCommandRunner}
 * <p>
 * Created on 22/04/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 0.1
 */
public abstract class CommandRunner {

    private static final Logger LOG = LogHelper.getLogger(CommandRunner.class);

    /**
     * Enumeration of supported operating systems
     */
    public enum SupportedOs {
        UNIX,
        WINDOWS
    }

    private static SupportedOs os;

    /**
     * Get a new instance of a {@code CommandRunner} according to the current platform (see {@link
     * app.cmd.CommandRunner.SupportedOs})
     *
     * @param directory Command working directory
     * @param name      Command display name (in case of error or other message)
     * @param cmd       Command to execute
     *
     * @return A {@code CommandRunner} instance
     */
    public static CommandRunner getInstance(File directory, String name, String cmd) {
        if (os == null) {
            checkOs();
        }

        switch (os) {
            case UNIX:
                return new UnixCommandRunner(directory, name, cmd);
            case WINDOWS:
                return new WindowsCommandRunner(directory, name, cmd);
            default:
                throw new AssertionError();
        }
    }

    /**
     * This method assesses the current operating system. In the case the platform is not supported, an error is shown
     * and the application will exit.
     * <p>
     * Note that this method will be automatically called when needed, alternatively, it can be manually called to force
     * to check the OS at a controlled point (eg. at the application start up).
     */
    public static void checkOs() {
        String osName = System.getProperty("os.name").toLowerCase();

        if (osName.contains("win")) {
            os = SupportedOs.WINDOWS;
        } else if (osName.contains("nix") || osName.contains("nux") || osName.indexOf("aix") > 0) {
            os = SupportedOs.UNIX;
        } else {
            AlertBuilder.showFatalError("Unsupported OS", "Your operating system is not supported");
            System.exit(1);
        }
    }

    /**
     * Get the current operating system.
     *
     * @return The current operating system
     */
    public static SupportedOs getOs() {
        if (os == null) {
            checkOs();
        }

        return os;
    }

    private final File directory;
    private final String name;
    private final List<String> cmd;
    private StringBuilder output;

    CommandRunner(File directory, String name, String action) {
        this.directory = directory;
        this.name = name;
        this.cmd = getCmd(action);
    }

    /**
     * Executes the command. In case of failure, the output is automatically logged.
     *
     * @throws IOException
     */
    public void execute() throws IOException {
        output = new StringBuilder();

        LOG.info("Running: " + cmd);
        Process process = new ProcessBuilder(cmd)
                .redirectErrorStream(true)
                .directory(directory)
                .start();

        new Thread(() -> {
            try (TextAccessor accessor = new TextAccessor().setIn(process.getInputStream())) {
                String line;
                while ((line = accessor.readLine()) != null) {
//                    System.out.println(line);  // Print output?
                    output.append(line).append('\n');
                }
            } catch (IOException e) {
                e.printStackTrace();
                output.append("OUTPUT EXCEPTION: [").append(e.getClass().getSimpleName()).append("] ").append(e.getMessage());
            }
        }).start();

        try {
            int i = process.waitFor();

            if (i != 0) {
                logOutput(i);
                throw new IOException(name + " failed, see logs.");
            }
        } catch (InterruptedException e) {
            // Ignored
        }
    }

    /**
     * Write the command output to logs. This method can only be called after an {@link #execute()}.
     * <p>
     * Note in case of failure of the command execution, this method is automatically called.
     */
    public void logOutput() {
        logOutput(-1);
    }

    /**
     * Write the command output to logs. This method can only be called after an {@link #execute()}.
     * <p>
     * Note in case of failure of the command execution, this method is automatically called.
     *
     * @param code Define the command exit code
     */
    public void logOutput(int code) {
        assert output != null;
        ExceptionLogger.logProcessOutput(output.toString(), name, cmd, code);
    }

    /**
     * Execute in a background thread. See {@link #execute()} for more information.
     * <p>
     * In case of error, the exception is automatically logged and shown with an alert.
     */
    public void executeInBackground() {
        executeInBackground(null);
    }

    /**
     * Execute in a background thread. See {@link #execute()} for more information.
     * <p>
     * In case of error, the exception is automatically logged.
     *
     * @param callback Callback used at the end of the command
     */
    public void executeInBackground(SimpleCallback callback) {
        new Thread(() -> {
            try {
                execute();
                if (callback != null) {
                    callback.success();
                }
            } catch (IOException e) {
                ExceptionLogger.logException(e);
                if (callback != null) {
                    callback.failure(e);
                } else {
                    AlertBuilder.error(e)
                            .key("errors.generic_cmd_failure", name)
                            .show();
                }
            }
        }).start();
    }


    protected abstract List<String> getCmd(String cmd);

}
