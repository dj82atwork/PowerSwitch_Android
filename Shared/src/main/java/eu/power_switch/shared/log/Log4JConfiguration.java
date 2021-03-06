/*
 *     PowerSwitch by Max Rosin & Markus Ressel
 *     Copyright (C) 2015  Markus Ressel
 *
 *     This program is free software: you can redistribute it and/or modify
 *     it under the terms of the GNU General Public License as published by
 *     the Free Software Foundation, either version 3 of the License, or
 *     (at your option) any later version.
 *
 *     This program is distributed in the hope that it will be useful,
 *     but WITHOUT ANY WARRANTY; without even the implied warranty of
 *     MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 *     GNU General Public License for more details.
 *
 *     You should have received a copy of the GNU General Public License
 *     along with this program.  If not, see <http://www.gnu.org/licenses/>.
 */

package eu.power_switch.shared.log;

import android.os.Environment;

import org.apache.log4j.Level;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

import de.mindpipe.android.logging.log4j.LogConfigurator;

/**
 * Created by Markus on 11.08.2015.
 */
public class Log4JConfiguration {

    private static LogConfigurator logConfigurator;

    /**
     * Private Constructor
     *
     * @throws UnsupportedOperationException because this class cannot be instantiated.
     */
    private Log4JConfiguration() {
        throw new UnsupportedOperationException("This class is non-instantiable. Use static one time init via configure() method instead");
    }

    public static void configure() {
        if (logConfigurator != null) {
            return;
        }

        logConfigurator = new LogConfigurator();

        if (LogHandler.createLogDirectory() && LogHandler.isExternalStorageReadable() && LogHandler
                .isExternalStorageWritable()) {
            logConfigurator.setFileName(Environment.getExternalStorageDirectory() + File.separator +
                    LogHandler.LOG_FOLDER + File.separator + "PowerSwitch__" + getHumanReadableDate() + ".log");
            String filePattern = "%d{dd-MM-yyyy HH:mm:ss,SSS} [%-5p] %m%n";
            logConfigurator.setFilePattern(filePattern);
            String logCatPattern = "[%-5p] %m%n";
            logConfigurator.setLogCatPattern(logCatPattern);
            logConfigurator.setRootLevel(Level.ALL);
            logConfigurator.setImmediateFlush(true);
            logConfigurator.setUseLogCatAppender(true);
            logConfigurator.setUseFileAppender(true);
            logConfigurator.setMaxFileSize(10 * 1024 * 1024); // 10 MB
            try {
                logConfigurator.configure();
            } catch (Exception e) {
                e.printStackTrace();
            }

            try {
                LogHandler.removeOldLogs();
            } catch (Exception e) {
                e.printStackTrace();
                try {
                    Log.e(e);
                } catch (Exception e1) {
                    e.printStackTrace();
                }
            }
        } else {
            return;
        }
    }

    private static String getHumanReadableDate() {
        SimpleDateFormat simpleDateFormat = new SimpleDateFormat("yyyy-MM-dd_HH-mm-ss");
        return simpleDateFormat.format(new Date());
    }


}
