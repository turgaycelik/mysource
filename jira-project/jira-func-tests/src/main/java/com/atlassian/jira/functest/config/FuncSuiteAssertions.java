package com.atlassian.jira.functest.config;

import com.atlassian.jira.functest.framework.SuiteListenerWrapper;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.google.common.collect.Sets;
import junit.framework.Test;
import junit.framework.TestCase;
import junit.framework.TestSuite;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;

import static com.atlassian.jira.util.collect.CollectionUtil.toList;
import static junit.framework.Assert.assertEquals;
import static junit.framework.Assert.assertFalse;
import static junit.framework.Assert.assertTrue;

/**
 * Assertions for {@link com.atlassian.jira.functest.framework.FuncTestSuite}.
 *
 * @since v4.3
 */
public final class FuncSuiteAssertions
{
    private static final Logger logger = LoggerFactory.getLogger(FuncSuiteAssertions.class);

    private FuncSuiteAssertions()
    {
        throw new AssertionError("Don't instantiate me");
    }

    public static void assertSuitesEqual(Test suite1, Test suite2)
    {
        Set<Class<?>> caseClasses1 = Sets.newLinkedHashSet(collectCaseClasses(suite1));
        Set<Class<?>> caseClasses2 = Sets.newLinkedHashSet(collectCaseClasses(suite2));
        assertTrue("New tests not found in old suite:\n" + Sets.difference(caseClasses2, caseClasses1),
                caseClasses1.containsAll(caseClasses2));
        assertTrue("Old tests not found in new suite:\n" + Sets.difference(caseClasses1, caseClasses2),
                caseClasses2.containsAll(caseClasses1));
    }

    public static void assertOldTestsInNewSuite(Test oldSuite, Test newSuite)
    {
        Set<Class<?>> caseClasses1 = Sets.newLinkedHashSet(collectCaseClasses(oldSuite));
        Set<Class<?>> caseClasses2 = Sets.newLinkedHashSet(collectCaseClasses(newSuite));
        assertTrue("Old tests not found in new suite:\n" + Sets.difference(caseClasses1, caseClasses2),
                caseClasses2.containsAll(caseClasses1));
    }

    public static void assertHasTests(Test suite, Class<? extends Test>... testClasses)
    {
        List<Class<?>> actualTestClasses = collectCaseClasses(suite);
        for (Class<?> testClass : testClasses)
        {
            assertTrue("Classes in suite " + actualTestClasses + " do not contain expected class " + testClass,
                    actualTestClasses.contains(testClass));
        }
    }

    public static void assertDoesNotHaveTests(Test suite, Class<? extends Test>... testClasses)
    {
        List<Class<?>> actualTestClasses = collectCaseClasses(suite);
        for (Class<?> testClass : testClasses)
        {
            assertFalse("Classes in suite " + actualTestClasses + " contain unexpected class " + testClass,
                    actualTestClasses.contains(testClass));
        }
    }

    public static void assertHasTestNames(Test suite, String... testNames)
    {
        List<String> actualTestNames = collectCaseNames(extractSuite(suite));
        for (String testName : testNames)
        {
            assertTrue("Test names in suite " + actualTestNames + " do not contain expected name " + testName,
                    actualTestNames.contains(testName));
        }
    }

    public static void assertDoesNotHaveTestNames(Test suite, String... testNames)
    {
        List<String> actualTestNames = collectCaseNames(extractSuite(suite));
        for (String testName : testNames)
        {
            assertFalse("Test names in suite " + actualTestNames + " contain unexpected name " + testName,
                    actualTestNames.contains(testName));
        }
    }

    public static void assertNumberOfTestCasesEquals(int expected, Test suite)
    {
        assertEquals(expected, collectCases(extractSuite(suite)).size());
    }

    public static List<Test> collectCases(TestSuite suite)
    {
        List<Test> answer = new ArrayList<Test>();
        for (Test test : toList(suite.tests()))
        {
            if (test instanceof TestSuite)
            {
                answer.addAll(collectCases((TestSuite)test));
            }
            else
            {
                answer.add(test);
            }
        }
        return answer;
    }

    public static List<Class<?>> collectCaseClasses(Test suite)
    {
        return collectCaseClasses(extractSuite(suite));
    }

    public static List<Class<?>> collectCaseClasses(TestSuite suite)
    {
        return filterDuplicates(CollectionUtil.transform(collectCases(suite), new Function<Test,Class<?>>()
        {
            @Override
            public Class<?> get(Test input)
            {
                return input.getClass();
            }
        })
        );
    }

    private static TestSuite extractSuite(Test test)
    {
        if (test instanceof TestSuite)
        {
            return (TestSuite) test;
        }
        else if (test instanceof SuiteListenerWrapper)
        {
            return extractSuite(((SuiteListenerWrapper) test).delegate());
        }
        else
        {
            throw new IllegalArgumentException("Unable to extract TestSuite from " + test.getClass().getName());
        }
    }

    public static List<String> collectCaseNames(TestSuite suite)
    {
        return filterDuplicates(CollectionUtil.transform(collectCases(suite), new Function<Test,String>()
        {
            @Override
            public String get(Test input)
            {
                if (input instanceof TestCase)
                {
                    return ((TestCase)input).getName();
                }
                return "";
            }
        }));
    }

    public static void logSuites(junit.framework.Test oldOne, junit.framework.Test newOne)
    {
        final List<Class<?>> oldList = collectCaseClasses(oldOne);
        final List<Class<?>> newList = collectCaseClasses(newOne);
        final int size = Math.max(oldList.size(), newList.size());
        logger.info("[logSuites] Comparing case classes lists");
        for (int i=0; i<size; i++)
        {
            logger.info(String.format("%-60s%s", safeGetName(oldList, i), safeGetName(newList, i)));
        }
    }

    private static String safeGetName(List<Class<?>> list, int index)
    {
        if (list.size() <= index)
        {
            return "<empty>";
        }
        else
        {
            return list.get(index).getSimpleName();
        }
    }

    private static <T> List<T> filterDuplicates(List<T> testClasses)
    {
        return new ArrayList<T>(new LinkedHashSet<T>(testClasses));
    }

}
