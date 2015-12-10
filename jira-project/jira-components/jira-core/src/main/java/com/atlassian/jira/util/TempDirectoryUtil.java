package com.atlassian.jira.util;

import com.atlassian.jira.cluster.ClusterSafe;
import com.atlassian.jira.config.properties.JiraSystemProperties;
import com.atlassian.jira.util.dbc.Assertions;

import java.io.File;

/**
 * This util class was added to be able to create a "temporary" directory.
 *
 * @since v3.13
 */
public class TempDirectoryUtil
{
    private static final Object TMP_FILE_LOCK = new Object();

    /**
     * Creates an empty directory in the default temporary-file directory, using the given prefix and suffix to generate its name.
     * <p>
     * Note: these directories will get deleted when the JVM exits.
     *
     * @param  prefix     The prefix string to be used in generating the directory's name.
     * @return  A <code>File</code> object denoting a newly-created empty directory
     */
    @ClusterSafe("Local")
    public static File createTempDirectory(final String prefix)
    {
        Assertions.notNull("prefix", prefix);
        synchronized (TMP_FILE_LOCK)
        {
            long counter = System.currentTimeMillis();

            File f;
            final String systemTempDir = getSystemTempDir();
            do
            {
                f = new File(systemTempDir, prefix + counter);
                counter++;
            }
            while (!f.mkdir());
            f.deleteOnExit();
            return f;
        }
    }

    /**
     * Returns the system's temp directory.
     * <p>
     * Be aware that some operating systems place a trailing slash and others don't.
     *
     * @return the system's temp directory.
     */
    public static String getSystemTempDir()
    {
        return JiraSystemProperties.getInstance().getProperty("java.io.tmpdir");
    }
}
