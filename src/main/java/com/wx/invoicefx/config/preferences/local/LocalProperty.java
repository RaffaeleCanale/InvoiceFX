package com.wx.invoicefx.config.preferences.local;


import com.google.common.collect.ImmutableMap;
import com.wx.fx.preferences.properties.PropertyCore;
import com.wx.fx.preferences.properties.UserProperty;
import com.wx.invoicefx.util.DesktopUtils;
import com.wx.util.OsUtils;

import java.io.File;
import java.util.Locale;

import static com.wx.fx.preferences.properties.PropertyCore.*;
import static com.wx.invoicefx.util.string.KeyWordHelper.validatePath;

/**
 * List of all properties that are NOT synchronized.
 * <p>
 * Created on 07/07/2015
 *
 * @author Raffaele Canale (raffaelecanale@gmail.com)
 * @version 1.0
 */
@SuppressWarnings("SpellCheckingInspection")
public enum LocalProperty implements UserProperty {
    ENABLE_ANIMATIONS(booleanProperty(true)),
    ENABLE_SYNC(booleanProperty(false)),
    COMPUTER_NAME(stringProperty(DesktopUtils.getHostName())),

    INVOICE_DIRECTORY(osProperty(ImmutableMap.of(
            OsUtils.OsFamily.UNIX, "invoice",
            OsUtils.OsFamily.WINDOWS, "${0}Documents" + File.separator + "Factures"
    ), validatePath(System.getProperty("user.home")))),
    TEX_COMMAND(osProperty(ImmutableMap.of(
            OsUtils.OsFamily.UNIX, "\"pdflatex\" -synctex=1 -interaction=nonstopmode \"${0}\"",
            OsUtils.OsFamily.WINDOWS, "\"${1}\\miktex\\bin\\pdflatex\" -synctex=1 -interaction=nonstopmode \"${0}\""
    ))),
    LANGUAGE(stringProperty(Locale.getDefault().toLanguageTag())),
    DEFAULT_APP_OPEN(osProperty(ImmutableMap.of(
            OsUtils.OsFamily.UNIX, "xdg-open \"${0}\"",
            OsUtils.OsFamily.WINDOWS, "start \"\" \"${0}\""
    ))),
//    UPDATE_SCRIPT_NAME(osProperty(ImmutableMap.of(
//            OsUtils.OsFamily.UNIX, "update.sh",
//            OsUtils.OsFamily.WINDOWS, "update.bat"
//    ))),
    DRIVE_CURRENT_USER(stringProperty("")),
    BACKUP_LENGTH(intProperty(20)),
    LAST_KNOWW_VERSION(doubleProperty(0.0));


    private final PropertyCore core;

    LocalProperty(PropertyCore core) {
        this.core = core;
    }

    @Override
    public PropertyCore core() {
        return core;
    }
}
