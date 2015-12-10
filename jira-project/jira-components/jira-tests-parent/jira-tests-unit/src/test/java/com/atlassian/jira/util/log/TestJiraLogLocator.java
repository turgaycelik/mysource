package com.atlassian.jira.util.log;

import java.io.File;
import java.io.IOException;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;

import com.atlassian.jira.config.util.MockJiraHome;
import com.atlassian.jira.util.TempDirectoryUtil;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

/**
 * Test for {@link com.atlassian.jira.util.log.JiraLogLocator}.
 *
 * @since v4.1
 */
public class TestJiraLogLocator
{
    /*
     * Make sure that we find the file in the home directory. This test may be a little flaky.
     */
    @Test
    public void testHomeDirectory()
    {
        File homeDirectory = TempDirectoryUtil.createTempDirectory("testHomeDirectory").getAbsoluteFile();
        try
        {
            File log = new File(homeDirectory, "log");
            if (!log.mkdirs())
            {
                return;
            }
            File logFile = new File(log, "atlassian-jira.log");

            try
            {
                if (!logFile.createNewFile() && !logFile.exists())
                {
                    return;
                }
            }
            catch (IOException e)
            {
                return;
            }

            final MockJiraHome home = new MockJiraHome(homeDirectory.getAbsolutePath());
            final JiraLogLocator locator = new JiraLogLocator(home);
            assertEquals(logFile, locator.findJiraLogFile());
            assertTrue(locator.findAllJiraLogFiles().contains(logFile));
        }
        finally
        {
            try
            {
                FileUtils.deleteDirectory(homeDirectory);
            }
            catch (IOException ignored)
            {
            }
        }
    }

    /**
     * Make sure that it picks up a log file in the working directory.
     */
    @Test
    public void testWorkingDirectory()
    {
        final File log = new File("atlassian-jira.log").getAbsoluteFile();
        boolean created;
        try
        {
            created = log.createNewFile();
        }
        catch (IOException e)
        {
            return;
        }

        try
        {
            final MockJiraHome home = new MockJiraHome();
            final JiraLogLocator locator = new JiraLogLocator(home);
            assertEquals(log, locator.findJiraLogFile());
            assertTrue(locator.findAllJiraLogFiles().contains(log));
        }
        finally
        {
            if (created)
            {
                if (!log.delete())
                {
                    log.deleteOnExit();
                }
            }
        }
    }

    @Test
    public void testWorkingAndHomeDirectory() throws Exception
    {
        File homeDirectory = TempDirectoryUtil.createTempDirectory("testWorkingAndHomeDirectory").getAbsoluteFile();
        File workingLog = null;
        boolean workingCreated = false;
        try
        {
            File log = new File(homeDirectory, "log");
            if (!log.mkdirs())
            {
                return;
            }
            File homeLog = new File(log, "atlassian-jira.log");

            try
            {
                if (!homeLog.createNewFile() && !homeLog.exists())
                {
                    return;
                }
            }
            catch (IOException e)
            {
                return;
            }


            workingLog = new File("atlassian-jira.log").getAbsoluteFile();
            try
            {
                workingCreated = workingLog.createNewFile();
            }
            catch (IOException e)
            {
                return;
            }


            final MockJiraHome home = new MockJiraHome(homeDirectory.getAbsolutePath());
            final JiraLogLocator locator = new JiraLogLocator(home);
            assertEquals(homeLog, locator.findJiraLogFile());
            assertContainsAndOrder(Arrays.asList(homeLog, workingLog), locator.findAllJiraLogFiles());            
        }
        finally
        {
            try
            {
                FileUtils.deleteDirectory(homeDirectory);
            }
            catch (IOException ignored)
            {
            }

            if (workingCreated)
            {
                if (!workingLog.delete())
                {
                    workingLog.deleteOnExit();
                }
            }
        }
    }

    /*
     * Test to make sure that all elements from order are in actual and are in the same relative order as given
     * by order.
     */
    private static <T> void assertContainsAndOrder(Collection<T> order, Collection<T> actual)
    {
        if (actual.isEmpty())
        {
            assertTrue(order.isEmpty());
        }

        if (order.isEmpty())
        {
            return;
        }

        final Iterator<T> orderIter = order.iterator();
        T orderItem = orderIter.next();

        for (Iterator<T> actualIter = actual.iterator(); actualIter.hasNext();)
        {
            T actualItem = actualIter.next();
            if (actualItem.equals(orderItem))
            {
                if (orderIter.hasNext())
                {
                    orderItem = orderIter.next();
                    if (!actualIter.hasNext())
                    {
                        //We have got to the end of the list without finding orderItem.
                        if (actual.contains(orderItem))
                        {
                            fail("Could not find element '" + orderItem + "' in the right position.");
                        }
                        else
                        {
                            fail("Could not find element '" + orderItem + "' in the list.");
                        }
                    }

                }
                else
                {
                    return;
                }
            }
        }

        //We got here but stil did not find the last orderItem.
        if (actual.contains(orderItem))
        {
            fail("Could not find element '" + orderItem + "' in the right position.");
        }
        else
        {
            fail("Could not find element '" + orderItem + "' in the list.");
        }
    }
}
