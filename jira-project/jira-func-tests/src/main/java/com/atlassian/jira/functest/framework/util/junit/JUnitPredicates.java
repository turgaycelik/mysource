package com.atlassian.jira.functest.framework.util.junit;

import com.google.common.base.Predicate;
import org.junit.runner.Description;

/**
 * Common JUnit4 predicates.
 *
 * @since v4.4
 */
public final class JUnitPredicates
{
    private JUnitPredicates()
    {
        throw new AssertionError("Don't instantiate me");
    }

    private static final Predicate<Description> IS_TEST = new Predicate<Description>()
    {
        @Override
        public boolean apply(Description input)
        {
            return input.isTest();
        }
    };

    private static final Predicate<Description> IS_SUITE = new Predicate<Description>()
    {
        @Override
        public boolean apply(Description input)
        {
            return input.isSuite();
        }
    };


    public static Predicate<Description> isTest()
    {
        return IS_TEST;
    }

    public static Predicate<Description> isSuite()
    {
        return IS_SUITE;
    }
}
