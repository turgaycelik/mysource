package com.atlassian.jira.util.log;

import com.atlassian.jira.config.util.JiraHome;
import javax.annotation.Nonnull;
import org.apache.log4j.LogManager;

import java.io.File;
import java.util.Collection;
import java.util.Iterator;
import java.util.LinkedHashSet;
import java.util.Set;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Utility class to find the location of JIRA's log file. We try to locate the log file using:
 * <ol>
 * <li> The file configured in the main appender. This is the best way and should work.
 * <li> The log file in the JIRA home directory (log/atlassian-jira.log).
 * <li> The atlassian-jira.log in the
 * </ol>
 * It may be possible to have multiple JIRA log files sitting on the file system.
 *
 * @since v4.1
 */
public class JiraLogLocator
{
    /**
     * The Appender name used for atlassian-jira.log.
     */
    private static final String AJL_APPENDER_NAME = "filelog";

    /**
     * The name of the log file.
     */
    public static final String AJL_FILE_NAME = "atlassian-jira.log";

    private final JiraHome home;

    public JiraLogLocator(final JiraHome home)
    {
        this.home = notNull("home", home);
    }

    /**
     * Return the main JIRA log file.
     *
     * @return the main JIRA log file. Can be null if it was not possible to find the log file.
     */
    public File findJiraLogFile()
    {
        final Collection<File> files = findAllJiraLogFiles();
        if (files.isEmpty())
        {
            return null;
        }
        else
        {
            return files.iterator().next();
        }
    }

    /**
     * Return a collection of all JIRA log files. JIRA may have multiple log files sitting around.
     * All the returned files exist and can be read by JIRA.
     *
     * @return A collection of all JIRA log files. The returned collection is ordered such that the current log file
     * comes first.
     */
    @Nonnull
    public Collection<File> findAllJiraLogFiles()
    {
        return filterFiles(findAllPotentialJiraLogFiles());
    }

    /**
     * Return a collection of all the potential JIRA log files. Some of the log files returned may not actually exist.
     *
     * @return a collection of all the potential JIRA log files. Some of the returned files may not acutally exist.
     */
    private Collection<File> findAllPotentialJiraLogFiles()
    {
        final Set<File> files = new LinkedHashSet<File>();

        File logFile = findLogViaAppender();
        if (logFile != null)
        {
            files.add(logFile);
        }
        logFile = findLogViaHome();
        if (logFile != null)
        {
            files.add(logFile);
        }
        files.add(findLogViaWorking());

        return files;
    }

    /**
     * Find the log file by JIRA home.
     *
     * @return the main log file associated in JIRA home. The returned file may not acutally exist.
     */
    private File findLogViaWorking()
    {
        return new File(AJL_FILE_NAME).getAbsoluteFile();
    }

    /**
     * Find the log file by JIRA home.
     *
     * @return the main log file associated in JIRA home. May be null if JIRA home is not configured.
     * The returned file may not acutally exist.
     */
    private File findLogViaHome()
    {
        try
        {
            return new File(home.getLogDirectory(), AJL_FILE_NAME);
        }
        catch (IllegalStateException e)
        {
            LogManager.getLogger(getClass()).debug("Unable to find log in JIRA home. Returning null.");
            return null;
        }
    }

    /**
     * Find the log file by inspecting the log4j appenders.
     *
     * @return the log file associated with JIRA's default appender. Can return null if the appender does not exist. The
     * returned file may not acutally exist.
     */
    private File findLogViaAppender()
    {
        return Log4jKit.getLogFileName(AJL_APPENDER_NAME);
    }

    /**
     * Filter the passed files to make sure that they actually exist and can be read.
     *
     * @param files the files to filter.
     * @return the filtered list of files.
     */
    private static Collection<File> filterFiles(final Collection<File> files)
    {
        for (final Iterator<File> fileIterator = files.iterator(); fileIterator.hasNext();)
        {
            final File f = fileIterator.next();
            if (!f.exists() || !f.canRead())
            {
                fileIterator.remove();
            }
        }
        return files;
    }
}
