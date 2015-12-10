package com.atlassian.jira.functest.unittests;

import com.atlassian.jira.functest.config.FuncSuiteAssertions;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.webtests.AcceptanceTestHarness;
import com.atlassian.jira.webtests.CategorisingTestSuite;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.atlassian.jira.webtests.util.TestClassUtils;
import com.google.common.collect.ImmutableList;
import org.apache.commons.lang.StringUtils;
import org.junit.After;
import org.junit.Test;

import static com.atlassian.jira.functest.config.FuncSuiteAssertions.logSuites;
import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertTrue;

/**
 * Test compatibility of the new {@link com.atlassian.jira.webtests.CategorisingTestSuite} with
 * {@link com.atlassian.jira.functest.framework.FuncTestSuite}.
 *
 * @since v4.4
 */
public class TestCategorisingTestSuiteCompatibility
{
    private static final String FUNC_TEST_PACKAGE = "com.atlassian.jira.webtests.ztests";


    @After
    public void clearSystemProperties()
    {
        System.clearProperty("jira.edition");
        System.clearProperty("atlassian.test.suite.package");
        System.clearProperty("atlassian.test.suite.includes");
        System.clearProperty("atlassian.test.suite.excludes");
    }

    @Test
    public void shouldContainAllFuncTests()
    {
        System.setProperty("jira.edition", "all");
        System.setProperty("atlassian.test.suite.package", FUNC_TEST_PACKAGE);
        System.setProperty("atlassian.test.suite.includes", Category.FUNC_TEST.toString());
        System.setProperty("atlassian.test.suite.excludes", excludedList());
        junit.framework.Test oldOne = AcceptanceTestHarness.SUITE.createTest(allTestsEnvData());
        junit.framework.Test newOne = CategorisingTestSuite.suite();
        logSuites(oldOne, newOne);
        // new suite moves forward, old suite does not - we only need to make sure all old tests are in the new suite
        FuncSuiteAssertions.assertOldTestsInNewSuite(oldOne, newOne);
    }

    private String excludedList() {
        return StringUtils.join(ImmutableList.of(
                Category.TPM.toString()), ",");
    }

    @Test
    public void allFuncTestsShouldBeAnnotatedWithWebTest()
    {
        for (Class<?> testClass : TestClassUtils.getTestClasses(FUNC_TEST_PACKAGE, true))
        {
            assertTrue("Test class without @WebTest: " + testClass.getName(), testClass.isAnnotationPresent(WebTest.class));
        }
    }

    private JIRAEnvironmentData allTestsEnvData()
    {
        JIRAEnvironmentData mockData = createNiceMock(JIRAEnvironmentData.class);
        expect(mockData.isAllTests()).andReturn(true).anyTimes();
        replay(mockData);
        return mockData;
    }

}
