package com.atlassian.jira.webtests.ztests.remote;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.backdoor.TestRunnerControl;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;

@WebTest ({ Category.FUNC_TEST, Category.INDEXING })
public class TestManagerFactory extends FuncTestCase
{
    
    private static final String BACK_END_TEST_NAME = "com.atlassian.jira.TestManagerFactoryBackEnd";
    
    public void testBackEnd() throws Exception
    {
        final TestRunnerControl.TestResult response = backdoor.testRunner().getRunTests(BACK_END_TEST_NAME);
        if (!response.passed)
        {
            fail(response.message);
        }
    }

}
