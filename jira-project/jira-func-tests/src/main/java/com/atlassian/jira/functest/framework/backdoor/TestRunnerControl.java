package com.atlassian.jira.functest.framework.backdoor;

import com.atlassian.jira.testkit.client.JIRAEnvironmentData;
import com.sun.jersey.api.client.GenericType;

/**
 * Invokes a REST end point to run some tests on JIRA's side.
 *
 * See TestRunnerBackdoor for how this works.
 *
 * @since v5.0.1
 * @author mtokar
 */
public class TestRunnerControl extends BackdoorControl<TestRunnerControl>
{
    public TestRunnerControl(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
    }

    public TestResult getRunTests(String testClassName)
    {
        return createResource().path("testRunner").path("run").queryParam("testClasses", testClassName).get(new GenericType<TestResult>(){});
    }

    public static class TestResult
    {
        public boolean passed = false;
        public boolean failed = false;
        public String message;
    }
}
