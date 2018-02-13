package com.wx.invoicefx.command;

import com.wx.invoicefx.config.ExceptionLogger;
import com.wx.io.TextAccessor;
import com.wx.util.OsUtils;
import com.wx.util.log.LogHelper;

import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Logger;

/**
 * This is an abstract tool allowing to run a shell command.
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
     * Get a new instance of a {@code CommandRunner} according to the current platform (see {@link
     * OsUtils.OsFamily})
     *
     * @param directory Command working directory
     * @param name      Command display name (in case of error or other message)
     * @param cmd       Command to run
     *
     * @return A {@code CommandRunner} instance
     */
    public static CommandRunner getInstance(File directory, String name, String cmd) {
        switch (OsUtils.getOsFamily()) {
            case UNIX:
                return new UnixCommandRunner(directory, name, cmd);
            case WINDOWS:
                return new WindowsCommandRunner(directory, name, cmd);
            default:
                throw new RuntimeException("OS not supported " + OsUtils.getOsFamily());
        }
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

    public List<String> getCmd() {
        return cmd;
    }

    protected abstract List<String> getCmd(String cmd);

}
