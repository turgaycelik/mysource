package com.atlassian.jira.webtest.webdriver;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.util.TestClassUtils;
import org.apache.commons.lang.ArrayUtils;
import org.junit.Test;

import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;

/**
 * Test the new WebDriver test suite.
 *
 * @since v4.4
 */
public class TestWebDriverSuite
{

    @Test
    public void allTestsShouldBeAnnotatedWithWebTest()
    {
        for (Class<?> testClass : TestClassUtils.getTestClasses("com.atlassian.jira.webtest.webdriver.tests", true))
        {
            WebTest webTest = testClass.getAnnotation(WebTest.class);
            assertNotNull("Test class without @WebTest: " + testClass.getName(), webTest);
            assertTrue("Test class without WEBDRIVER_TEST category: " + testClass.getName(),
                    ArrayUtils.contains(webTest.value(), Category.WEBDRIVER_TEST));
        }
    }
}
