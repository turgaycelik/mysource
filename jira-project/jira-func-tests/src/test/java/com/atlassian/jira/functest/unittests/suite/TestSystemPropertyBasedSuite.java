package com.atlassian.jira.functest.unittests.suite;

import java.util.EnumSet;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.SystemPropertyBasedSuite;

import org.junit.After;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * Test case for {@link com.atlassian.jira.functest.framework.suite.SystemPropertyBasedSuite}.
 *
 * @since v4.4
 */
public class TestSystemPropertyBasedSuite
{

    @After
    public void cleanUpProperties()
    {
        System.clearProperty("atlassian.test.suite.package");
        System.clearProperty("atlassian.test.suite.includes");
        System.clearProperty("atlassian.test.suite.excludes");
    }

    @Test
    public void shouldReturnValidPackage()
    {
        System.setProperty("atlassian.test.suite.package", "com.atlassian.example.tests");
        assertEquals("com.atlassian.example.tests", new SystemPropertyBasedSuite().webTestPackage());
    }

    @Test(expected = IllegalArgumentException.class)
    public void shouldThrowExceptionGivenNoPackageProperty()
    {
        new SystemPropertyBasedSuite().webTestPackage();
    }

    @Test
    public void shouldReturnValidIncludes()
    {
        System.setProperty("atlassian.test.suite.package", "com.atlassian.example.tests");
        System.setProperty("atlassian.test.suite.includes", "rest, func_test,   ,, COMPonENTS_AND_VERsiONS");
        assertEquals(EnumSet.of(Category.REST ,Category.FUNC_TEST, Category.COMPONENTS_AND_VERSIONS),
                new SystemPropertyBasedSuite().includes());
    }

    @Test
    public void shouldReturnValidExcludes()
    {
        System.setProperty("atlassian.test.suite.package", "com.atlassian.example.tests");
        System.setProperty("atlassian.test.suite.excludes", " REST,selenium_test  ,   ,, CoMPonENTS_AND_VERsiONS");
        assertEquals(EnumSet.of(Category.REST ,Category.SELENIUM_TEST, Category.COMPONENTS_AND_VERSIONS),
                new SystemPropertyBasedSuite().excludes());
    }

    @Test
    public void shouldReturnEmptyIncludesAndExcludesGivenPropertiesNotSet()
    {
        System.setProperty("atlassian.test.suite.package", "com.atlassian.example.tests");
        assertEquals(EnumSet.noneOf(Category.class), new SystemPropertyBasedSuite().excludes());
        assertEquals(EnumSet.noneOf(Category.class), new SystemPropertyBasedSuite().includes());
    }
}
