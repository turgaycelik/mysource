package com.atlassian.jira.web.monitor.dump;

import java.io.File;
import java.io.IOException;
import java.lang.management.ManagementFactory;
import java.lang.management.ThreadMXBean;
import java.text.SimpleDateFormat;
import java.util.Date;

import javax.annotation.Nullable;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import static java.lang.String.format;

/**
 * This class creates thread dump files in a specified directory. If, for some reason, this Dumper is unable to write to
 * the target directory, it will simply print the thread dump to {@link System#err}.
 *
 * @since v4.3
 */
public class Dumper
{
    /**
     * Logger for this Dumper instance.
     */
    private static final Logger LOGGER = LoggerFactory.getLogger(Dumper.class);

    /**
     * The date format used (used in the dump file name).
     */
    private static final String DATE_FORMAT = "yyyyMMdd_HHmmss_SSS";

    /**
     * Obtains a JVM thread dump and writes it to a file, if possible. If writing to a file fails, the thread dump is
     * written to {@link System#err}.
     *
     * @param directory the directory where the thread dump will be saved
     * @return a String containing the absolute path name of the file that was created, or null if the thread dump was
     *         written to System.err
     * @see java.lang.management.ThreadMXBean
     */
    public String dumpThreads(@Nullable String directory)
    {
        String nowDate = new SimpleDateFormat(DATE_FORMAT).format(new Date());
        File file = null;

        // try to create the file
        boolean writeToFile = false;
        if (directory != null)
        {
            try
            {
                file = new File(format("%s%sthreads_%s.txt", directory, File.separator, nowDate));
                writeToFile = file.createNewFile();
            }
            catch (IOException e)
            {
                LOGGER.error(format("Unable to create file '%s', writing thread dump to System.err", file.getAbsolutePath()), e);
            }
        }

        ThreadInfoWriter writer = writeToFile ? new FileWriter(file) : new StreamWriter(System.err);
        try
        {
            // get a reference to Threading
            ThreadMXBean threading = ManagementFactory.getThreadMXBean();
            writer.write(threading.dumpAllThreads(threading.isObjectMonitorUsageSupported(), threading.isSynchronizerUsageSupported()));
        }
        catch (Exception e)
        {
            LOGGER.error("Error encountered while trying to write thread dump", e);
        }

        return writeToFile ? file.getAbsolutePath() : null;
    }
}
