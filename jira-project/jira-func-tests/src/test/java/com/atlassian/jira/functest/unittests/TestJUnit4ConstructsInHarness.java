package com.atlassian.jira.functest.unittests;

import com.atlassian.jira.functest.config.BlankIgnoresFinder;
import com.atlassian.jira.functest.config.JUnit4Suppressor;
import com.atlassian.jira.webtests.AcceptanceTestHarness;
import org.junit.Test;

/**
 * Finds any Func Tests that are missing from our AcceptanceTestHarness.
 *
 * @since v3.13
 */
public class TestJUnit4ConstructsInHarness
{
    @Test
    public void testNoJUnit4() throws Exception
    {
        new JUnit4Suppressor(AcceptanceTestHarness.SUITE.getAllTests()).killJUnit4();
    }

    @Test
    public void ignoredTestsShouldBeProvidedWithReason()
    {
        new BlankIgnoresFinder("Func Test Suite", AcceptanceTestHarness.SUITE.getAllTests())
                .assertNoIgnoresWithoutReason();
    }

}
