package com.atlassian.jira.functest.unittests.suite;

import com.atlassian.jira.functest.framework.suite.RunFirst;
import org.junit.Ignore;
import org.junit.Test;

/**
 * Placeholder for mock JUnit4 test classes used in testing JUnit4 web test stuff.
 *
 * A new dimension of mocking!
 *
 * @since v4.4
 */
public final class MockJUnit4TestClasses
{

    public static class TestOne
    {
        @Test
        public void testOneOne()
        {
        }

    }

    public static class TestTwo
    {
        @Test
        public void testTwoOne()
        {
        }

        @Test
        public void testTwoTwo()
        {
        }
    }

    public static class TestThree
    {
        @Test
        public void testThreeOne()
        {
        }

        @Test
        public void testThreeTwo()
        {
        }

        @Test
        public void testThreeThree()
        {
        }
    }

    public static class TestFour
    {
        @Test
        public void testFourOne()
        {
        }

        @Test
        public void testFourTwo()
        {
        }

        @Test
        public void testFourThree()
        {
        }

        @Test
        public void testFourFour()
        {
        }

    }

    public static class TestFive
    {
        @Test
        public void testFiveOne()
        {
        }

        @Test
        public void testFiveTwo()
        {
        }

        @Test
        public void testFiveThree()
        {
        }

        @Test
        public void testFiveFour()
        {
        }

        @Test
        public void testFiveFive()
        {
        }
    }



    @Ignore
    public static class FullyIgnoredTest
    {
        @Test
        public void test1()
        {
        }

        @Test
        public void test2()
        {
        }

        @Test
        public void test3()
        {
        }
    }


    public static class PartiallyIgnoredTest
    {
        @Test
        @Ignore
        public void ignoredTest1()
        {
        }

        @Test
        @Ignore
        public void ignoredTest2()
        {
        }

        @Test
        public void notIgnoredTest1()
        {
        }

        @Test
        public void notIgnoredTest2()
        {
        }
    }

    @RunFirst
    public static class RunFirstTest
    {
        @Test
        public void testOne()
        {
        }

        @Test
        public void testTwo()
        {
        }

        @Test
        public void testThree()
        {
        }
    }

    public static class PartiallyRunFirstTest
    {
        @Test
        @RunFirst
        public void runFirstTest1()
        {
        }

        @Test
        @RunFirst
        public void runFirstTest2()
        {
        }

        @Test
        public void notRunFirstTest1()
        {
        }

        @Test
        public void notRunFirstTest2()
        {
        }
    }
}
