package com.wx.invoicefx.command;

import java.io.File;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

/**
 * A Windows shell command runner.
 * <p>
 * Created on 23/04/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class WindowsCommandRunner extends CommandRunner {

    WindowsCommandRunner(File directory, String name, String action) {
        super(directory, name, action);
    }

    @Override
    protected List<String> getCmd(String cmd) {
        if (cmd.startsWith("\"") || cmd.startsWith("C:\\")) {
            return Collections.singletonList(cmd);
        }
        return Arrays.asList("cmd", "/C", cmd);
    }
}
