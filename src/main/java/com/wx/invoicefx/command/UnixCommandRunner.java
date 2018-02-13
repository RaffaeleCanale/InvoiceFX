package com.wx.invoicefx.command;

import java.io.File;
import java.util.Arrays;
import java.util.List;

/**
 * A Unix shell command runner
 * <p>
 * Created on 22/04/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
public class UnixCommandRunner extends CommandRunner {

    UnixCommandRunner(File directory, String name, String action) {
        super(directory, name, action);
    }

    @Override
    protected List<String> getCmd(String cmd) {
        return Arrays.asList("bash", "-c", cmd);
    }
}
