package com.atlassian.jira.pageobjects.config;

import java.io.File;
import java.text.SimpleDateFormat;
import java.util.Date;

/**
 * Describes JIRA test environment.
 *
 * @since v4.4
 */
public class TestEnvironment
{
    private static final String TARGET_DIR_PROP = "atlassian.test.target.dir";

    private final File targetDir;
    private final File artifactDir;

    public TestEnvironment()
    {
        this.targetDir = new File(targetDirPath());
        this.targetDir.mkdirs();
        this.artifactDir = new File(targetDir, "test-reports");
        this.artifactDir.mkdirs();
    }

    private String targetDirPath()
    {
        String prop = System.getProperty(TARGET_DIR_PROP);
        if (prop != null)
        {
            return prop;
        }
        else
        {
            String yymmdd = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
            return System.getProperty("java.io.tmpdir") + File.separator + "jira-tests" + File.separator + yymmdd;
        }
    }

    /**
     * Target directory of the test.
     *
     * @return target directory
     */
    public File targetDirectory()
    {
        return targetDir;
    }

    /**
     * A directory to store test output in.
     *
     * @return artifact directory of the test
     */
    public File artifactDirectory()
    {
        return artifactDir;
    }
}
