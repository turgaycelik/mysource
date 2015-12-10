package com.atlassian.jira.util.devspeed;

import com.atlassian.jira.config.properties.JiraProperties;
import com.atlassian.jira.config.properties.JiraSystemProperties;

import java.io.File;
import java.io.FileWriter;
import java.io.PrintWriter;
import java.util.Date;

/**
 * A really simple class to help us track how long JIRA takes to do stuff over time.
 * <p/>
 * It does NOTHING if jira.dev.mode is not on.
 *
 * @since v4.4
 */
public class JiraDevSpeedTimer
{
    private final String task;
    private final long then;

    /**
     * Runs the provided code and times the result
     *
     * @param task the task to time
     * @param runnable the code to run
     */
    public static void run(final String task, final Runnable runnable)
    {
        final JiraDevSpeedTimer jiraDevSpeedTimer = new JiraDevSpeedTimer(task);
        try
        {
            runnable.run();
        }
        finally
        {
            jiraDevSpeedTimer.end();
        }
    }

    JiraDevSpeedTimer(final String task)
    {
        this.then = System.currentTimeMillis();
        this.task = task;
    }

    void end()
    {
        final long now = System.currentTimeMillis();
        if (JiraSystemProperties.isDevMode())
        {
            appendRecord(now);
        }
    }

    private void appendRecord(final long now)
    {
        final long howLongSec = (now - then) / 1000;
        final Date nowDate = new Date();
        try
        {
            final JiraProperties jiraProperties = JiraSystemProperties.getInstance();
            final String userDotHome = jiraProperties.getProperty("user.home");
            final String userDotName = jiraProperties.getProperty("user.name");
            final File userHome = new File(userDotHome);
            if (userHome.exists())
            {
                final File targetDir = new File(userHome, ".jiradev");
                //noinspection ResultOfMethodCallIgnored
                targetDir.mkdirs();

                final File targetFile = new File(targetDir, "jiratimers.csv");
                final PrintWriter pw = new PrintWriter(new FileWriter(targetFile, true));

                //
                // not this matches the jmake time formats
                pw.printf("%tF %tT,%s,%s,%d\n", nowDate, nowDate, userDotName, task, howLongSec);
                pw.close();
            }
        }
        catch (Exception ignored)
        {
        }
    }
}
