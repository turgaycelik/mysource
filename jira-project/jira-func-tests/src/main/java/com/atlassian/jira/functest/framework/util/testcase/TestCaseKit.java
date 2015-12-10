package com.atlassian.jira.functest.framework.util.testcase;

import junit.framework.Test;
import junit.framework.TestCase;

/**
 * A help class for TestCase operations
 *
 * @since v3.13
 */
public class TestCaseKit
{
    private TestCaseKit()
    {
    }

    /**
     * Returns the fully qualified name of a testCase.  eg ClassName.testName
     * @param testCase
     * @return
     */
    public static String getFullName(TestCase testCase)
    {
        String className = testCase.getClass().getName();
        return className + "." + testCase.getName();
    }

    public static String getFullName(Test test)
    {
        if (test instanceof TestCase)
        {
            return TestCaseKit.getFullName((TestCase) test);
        }
        else
        {
            return test.toString();
        }
    }
}
