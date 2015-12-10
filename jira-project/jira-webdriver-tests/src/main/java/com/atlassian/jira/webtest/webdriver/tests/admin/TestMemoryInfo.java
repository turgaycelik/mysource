package com.atlassian.jira.webtest.webdriver.tests.admin;

import com.atlassian.integrationtesting.runner.restore.Restore;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.pageobjects.BaseJiraWebTest;
import com.atlassian.jira.pageobjects.pages.admin.system.MemoryInfoPage;

import org.junit.Test;

import junit.framework.Assert;

import static org.junit.Assert.assertTrue;

/**
 * Test for the "Memory Info" page
 *
 * @since v7.0
 */
@WebTest ({ Category.WEBDRIVER_TEST, Category.ADMINISTRATION})
@Restore("xml/TestMemoryInfo.xml")
public class TestMemoryInfo extends BaseJiraWebTest
{

    public static final int MEM_PERCENTAGE_MIN = 10;
    public static final int MEM_PERCENTAGE_MAX = 90;

    @Test
    public void testMemoryRanges()
    {
        final boolean permGenJvm;


        final MemoryInfoPage memoryInfoPage = jira.quickLoginAsSysadmin(MemoryInfoPage.class);

        final MemoryInfoPage.MemoryInfo memoryGraph = memoryInfoPage.getJvmInfoForRowTitle("Memory Graph");
        final MemoryInfoPage.MemoryInfo permGen;
        MemoryInfoPage.MemoryInfo nonHeap = memoryInfoPage.getJvmInfoForRowTitle("Non-Heap Memory Graph (includes PermGen)");

        if(nonHeap == null)
        {
            permGenJvm = false;
            permGen = null;
            nonHeap = memoryInfoPage.getJvmInfoForRowTitle("Non-Heap Memory (includes Metaspace)");
        }
        else
        {
            permGenJvm = true;
            permGen = memoryInfoPage.getJvmInfoForRowTitle("PermGen Memory Graph");
        }

        //Here we assume true these values make sense.
        // If this test fails MIN/MAX values can be changes but first make sure that actual values are valid.
        assertMemoryRange("Memory Graph Percentage", memoryGraph.getPercentage(), MEM_PERCENTAGE_MIN, MEM_PERCENTAGE_MAX);

        assertTrue("Total memory can't be zero.", memoryGraph.getTotal() > 0);

        assertTotalGreaterThanUsed("Memory", memoryGraph);

        if(permGenJvm)
        {
            assertTrue("Total non-heap memory can't be zero.", nonHeap.getTotal() > 0);
            assertTrue("Total permgen memory can't be zero.", permGen.getTotal() > 0);

            assertMemoryRange("PermGen Graph Percentage", permGen.getPercentage(), MEM_PERCENTAGE_MIN, MEM_PERCENTAGE_MAX);
            assertMemoryRange("Non-heap Graph Percentage", nonHeap.getPercentage(), MEM_PERCENTAGE_MIN, MEM_PERCENTAGE_MAX);

            assertTotalGreaterThanUsed("PermGen", permGen);
            assertTotalGreaterThanUsed("Non-heap", nonHeap);
        }
        else
        {
            Assert.assertNotNull("Non-heap memory info not found for non-permgen jvm.", nonHeap);
            assertTrue("Used non-heap memory can't be zero.", nonHeap.getUsed() > 0);
        }
    }

    private void assertMemoryRange(final String name, final long actual, final long min, final long max)
    {
        assertTrue(errorMessage(name, actual, min, max), inRange(actual, min, max));
    }

    private boolean inRange(final long actual, final long min, final long max)
    {
        return actual >= min && actual <=max;
    }

    private String errorMessage(final String name, final long actual, final long min, final long max)
    {
        return String.format("%s value does not make sense. Actual %s, min allowed: %s, max allowed: %s",
                            name, actual, min, max);
    }

    private void assertTotalGreaterThanUsed(final String name, final MemoryInfoPage.MemoryInfo memoryInfo)
    {
        assertTrue(String.format("%s used memory is higher than total memory.", name), memoryInfo.getTotal() >= memoryInfo.getUsed());
    }
}
