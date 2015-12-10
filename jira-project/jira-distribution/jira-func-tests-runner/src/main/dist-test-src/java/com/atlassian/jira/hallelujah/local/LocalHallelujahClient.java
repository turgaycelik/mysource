package com.atlassian.jira.hallelujah.local;

import com.atlassian.buildeng.hallelujah.HallelujahClient;
import com.atlassian.buildeng.hallelujah.api.client.ClientTestCaseProvider;
import com.atlassian.buildeng.hallelujah.api.client.ClientTestCaseResultCollector;
import com.atlassian.buildeng.hallelujah.api.model.TestCaseName;
import com.atlassian.buildeng.hallelujah.api.model.TestCaseResult;
import com.atlassian.buildeng.hallelujah.junit.JUnitClientTestCaseRunner;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.io.FileUtils;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Iterator;

/**
 * Hallelujah Client that takes a collection of test case names as input - able to provide one locally.
 * @since v6.0
 */
public class LocalHallelujahClient extends HallelujahClient
{

    public static final String TEST_LIST_PROPERTY = "hallelujah.local.test.list";

    private static class IterableBasedTestCaseProvider implements ClientTestCaseProvider
    {

        private final Iterator<TestCaseName> iterator;

        private IterableBasedTestCaseProvider(final Iterable<TestCaseName> testCaseNames)
        {
            iterator = testCaseNames.iterator();
        }

        @Override
        public TestCaseName getNextTestName()
        {
            return iterator.hasNext() ? iterator.next() : null;
        }
    }

    private static class MapBasedTestCaseCollector extends HashMap<TestCaseName, TestCaseResult>
            implements ClientTestCaseResultCollector
    {
        @Override
        public boolean addResult(final TestCaseResult testCaseResult)
        {
            put(testCaseResult.testCaseName, testCaseResult);
            return true;
        }

        @Override
        public boolean hasResultsForTest(final TestCaseName testCaseName)
        {
            return containsKey(testCaseName);
        }
    }

    public LocalHallelujahClient(final Iterable<TestCaseName> tests)
    {
        super(new IterableBasedTestCaseProvider(tests),
                new JUnitClientTestCaseRunner(),
                new MapBasedTestCaseCollector());
    }

    public LocalHallelujahClient(final File fileWithTestCaseNames) throws IOException
    {

        this(Iterables.transform(FileUtils.readLines(fileWithTestCaseNames), new Function<String, TestCaseName>()
        {
            @Override
            public TestCaseName apply(final String s)
            {
                final int hashAt = s.lastIndexOf('.');
                return hashAt > 0 ? new TestCaseName(s.substring(0, hashAt), s.substring(hashAt + 1)) : null;
            }
        }));
    }
}
