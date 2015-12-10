package com.atlassian.jira.velocity;

import java.io.File;
import java.io.IOException;
import java.util.List;

import com.atlassian.jira.FileChecker;
import com.atlassian.jira.FileFinder;

import org.apache.log4j.Logger;
import org.junit.Test;

import junit.framework.Assert;

/**
 * Verify the integrity of velocity macros in JIRA.
 *
 * @since v4.0
 */
public class TestVelocityMacros
{

    private static final Logger log = Logger.getLogger(TestVelocityMacros.class);

    /**
     * If the project gets refactored and the velocity files are moved, we risk unknowingly missing the checks,
     * as just happened.
     */
    private static final int MIN_FILE_COUNT = 500;

    @Test
    public void testForNameClashes() throws IOException
    {
        testWithChecker(new VelocityMacrosTemplatenamesChecker());
    }

//    @Test
    public void testExplicitHtmlEscapeDirective() throws Exception
    {
        testWithChecker(new HtmlEscapeDirectiveChecker());
    }

    private void testWithChecker(FileChecker checker)
    {
        FileFinder finder = new FileFinder(checker);
        FileFinder.Result result = finder.checkDir(getDir());
        if (!result.success()) {
            List<String> fails = result.getFails();
            for (String fail : fails)
            {
                log.error(fail);
            }
            if (fails.size() == 1) {
                Assert.fail(fails.get(0));
            } else {
                Assert.fail(fails.size() + " failures, including: " + fails.get(0));
            }
        }
        checkEnoughFilesChecked(result.getFilesChecked());
    }

    private void checkEnoughFilesChecked(int count)
    {
        boolean enough = count > MIN_FILE_COUNT;
        Assert.assertTrue("expected to check at least " + MIN_FILE_COUNT + " files, only checked " + count, enough);
    }

    private File getDir()
    {
        final String classFileName = "/" + this.getClass().getName().replace('.', '/') + ".class"; // fully qualified
        final String pathToClassFile = this.getClass().getResource(classFileName).getFile();
        String path = pathToClassFile.substring(0, pathToClassFile.length() - classFileName.length());
        return new File(path).getParentFile().getParentFile().getParentFile().getParentFile(); // project root on a checkout TODO are unit tests run from jar?
    }

}
