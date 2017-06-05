package com.wx.invoicefx.config.preferences;

import com.google.common.collect.ImmutableMap;
import com.wx.fx.preferences.properties.PropertyCore;
import com.wx.fx.preferences.properties.UserProperty;
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
public enum LocalProperty implements UserProperty {
    INVOICE_DIRECTORY(osProperty(ImmutableMap.of(
            OsUtils.OsFamily.UNIX, "invoice",
            OsUtils.OsFamily.WINDOWS, "${0}Documents" + File.separator + "Factures"
    ), validatePath(System.getProperty("user.home")))),
//    TEX_COMMAND(osProperty(ImmutableMap.of(
//            OsUtils.OsFamily.UNIX, "\"pdflatex\" -synctex=1 -interaction=nonstopmode \"${0}\"",
//            OsUtils.OsFamily.WINDOWS, "\"${1}\\miktex\\miktex\\bin\\pdflatex\" -synctex=1 -interaction=nonstopmode \"${0}\""
//    ))),
    LANGUAGE(stringProperty(Locale.getDefault().toLanguageTag())),
    DEFAULT_APP_OPEN(osProperty(ImmutableMap.of(
            OsUtils.OsFamily.UNIX, "xdg-open \"${0}\"",
            OsUtils.OsFamily.WINDOWS, "start \"\" \"${0}\""
    ))),
    UPDATE_SCRIPT_NAME(osProperty(ImmutableMap.of(
            OsUtils.OsFamily.UNIX, "update.sh",
            OsUtils.OsFamily.WINDOWS, "update.bat"
    ))),
    DRIVE_CURRENT_USER(stringProperty("")),
    ARCHIVES_VIEW_SPLITTER(doubleProperty(0.5)),
    ARCHIVES_VIEW_ENABLED(booleanProperty(false)),
    BACKUP_INTERVAL_DAYS(intProperty(1)),
    BACKUP_LENGTH(intProperty(50)),
    FAST_BACKUP_LENGTH(intProperty(50));


    private final PropertyCore core;

    LocalProperty(PropertyCore core) {
        this.core = core;
    }

    @Override
    public PropertyCore core() {
        return core;
    }
}
