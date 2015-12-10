package com.atlassian.jira.webtests.util;

import junit.framework.TestCase;

import java.lang.reflect.Field;
import java.lang.reflect.Method;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Use to make an 'educated' guess about a method name of a given test case.
 *
 * @since v4.3
 */
public final class TestCaseMethodNameDetector
{
    private static final String WE_ALL_LOVE_HUNGARIAN_NOTATION = "fName";

    private final TestCase testCase;
    private final Class<? extends TestCase> testCaseClass;


    public TestCaseMethodNameDetector(TestCase testCase)
    {
        this.testCase = notNull("testCase", testCase);
        this.testCaseClass = testCase.getClass();
    }

    public Method resolve()
    {
        // there's no way to get the original test method for a JUnit warning. don't blow up when this happens.
        if ("warning".equals(testCase.getName()))
        {
            return null;
        }

        Method result = resolveByName();
        if (result != null)
        {
            return result;
        }
        result = resolveByModifiedName();
        if (result != null)
        {
            return result;
        }
        else
        {
            result = resolveByHacking();
            if (result == null)
            {
                throw new IllegalStateException("Unable to resolve test method of <" + testCase + ">");
            }
            return result;
        }
    }

    private Method resolveByName()
    {
        return safeGetMethod(testCase.getName());
    }

    private Method resolveByModifiedName()
    {
        String methodName = extractMethodName(testCase.getName());
        return safeGetMethod(methodName);
    }

    private Method resolveByHacking()
    {
        return safeGetMethod(extractRealNameByHacking());
    }

    private Method safeGetMethod(String methodName)
    {
        try
        {
            return testCaseClass.getMethod(methodName);
        }
        catch (NoSuchMethodException e)
        {
            return null;
        }
    }

    private String extractMethodName(String name)
    {
        return name.substring(name.lastIndexOf(".") + 1);
    }

    private String extractRealNameByHacking()
    {
        try
        {
            Field nameField = TestCase.class.getDeclaredField(WE_ALL_LOVE_HUNGARIAN_NOTATION);
            nameField.setAccessible(true);
            return (String) nameField.get(testCase);
        }
        catch (NoSuchFieldException e)
        {
            throw new RuntimeException(e);
        }
        catch (IllegalAccessException e)
        {
            throw new RuntimeException(e);
        }
    }

}
