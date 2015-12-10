package com.atlassian.jira.webtests.util;

import java.io.File;

/**
 * This util class was added to be able to create a "temporary" directory.
 * This is basically a copy of the JIRA TempDirectoryUtil class, added here so Func tests can use it.
 *
 * @since v3.13
 */
public class TempDirectoryUtil
{
    private static final Object TMP_FILE_LOCK = new Object();

    /**
     * Creates an empty directory in the default temporary-file directory, using the given prefix and suffix to generate its name.
     *
     * @param  prefix     The prefix string to be used in generating the directory's name.
     *
     * @return  A <code>File</code> object denoting a newly-created empty directory
     * @since 4.0
     */
    public static File createTempDirectory(String prefix)
    {
        synchronized (TMP_FILE_LOCK)
        {
            long counter = System.currentTimeMillis();

            File f;
            String systemTempDir = getSystemTempDir();
            do
            {
                f = new File(systemTempDir, prefix + counter);
                counter++;
            }
            while (!f.mkdir());
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
	    return System.getProperty("java.io.tmpdir");
    }
}
