package com.atlassian.jira.issue.index;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.ThreadFactory;
import java.util.concurrent.TimeUnit;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.instrumentation.DefaultInstrumentRegistry;
import com.atlassian.instrumentation.InstrumentRegistry;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.concurrent.BarrierFactory;
import com.atlassian.jira.concurrent.MockBarrierFactory;
import com.atlassian.jira.config.FeatureManager;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.MockIndexPathManager;
import com.atlassian.jira.config.util.MockIndexingConfiguration;
import com.atlassian.jira.easymock.EasyMockAnnotations;
import com.atlassian.jira.easymock.Mock;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.util.SimpleMockIssueFactory;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.issue.index.MockIssueIndexer;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.RuntimeInterruptedException;
import com.atlassian.jira.util.collect.CollectionUtil;
import com.atlassian.jira.util.concurrent.Gate;
import com.atlassian.util.concurrent.ThreadFactories;

import com.google.common.collect.Lists;

import org.apache.log4j.Logger;
import org.easymock.EasyMock;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.easymock.EasyMock.replay;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

/**
 * Tests correct behaviour of <tt>DefaultIndexManager.optimizeIfNecessary</tt> under concurrent load.
 *
 * @since v4.3
 */
public class TestDefaultIndexManagerConcurrentIndexing
{
    private static final Logger log = Logger.getLogger(TestDefaultIndexManagerConcurrentIndexing.class);

    private static final int NO_THREADS = 20;

    private ReindexMessageManager reindexMessageManager;
    private EventPublisher eventPublisher;
    private final IndexPathManager indexPath = new MockIndexPathManager();
    private final MockIssueIndexer mockIndexer = new MockIssueIndexer(500, 2000);

    private final ThreadFactory threadFactory = ThreadFactories.namedThreadFactory("TestDefaultIndexManagerConcurrentIndexing");
    private final ExecutorService executor = Executors.newCachedThreadPool(threadFactory);
    @Mock
    private FeatureManager featureManager;

    private BarrierFactory barrierFactory;

    @Before
    public void setUp() throws Exception
    {
        EasyMockAnnotations.initMocks(this);
        EasyMockAnnotations.replayMocks(this);

        barrierFactory = new MockBarrierFactory();
    }

    private static final class IndexRequester implements Runnable
    {
        private final List<Long> issueIds;
        private final IssueIndexManager indexManager;
        private final Gate startGate;
        private final CountDownLatch endGate;
        public volatile Exception error;

        public IndexRequester(IssueIndexManager indexManager, Gate startGate, CountDownLatch endGate, Long... issueIds)
        {
            this.issueIds = asList(issueIds);
            this.indexManager = indexManager;
            this.startGate = startGate;
            this.endGate = endGate;
        }

        @Override
        public void run()
        {
            startGate.ready();
            try
            {
                indexManager.reIndexIssueObjects(issueList());
            }
            catch (IndexException e)
            {
                this.error = e;
            }
            finally
            {
                endGate.countDown();
            }
        }

        List<Issue> issueList()
        {
            return CollectionUtil.transform(issueIds, new Function<Long, Issue>()
            {
                @Override
                public Issue get(Long input)
                {
                    return new MockIssue(input);
                }
            });
        }

        boolean hasError()
        {
            return error != null;
        }
    }

    @Before
    public void setupPICO() throws Exception
    {
        ComponentAccessor.initialiseWorker(
                new MockComponentWorker()
                        .addMock(InstrumentRegistry.class, new DefaultInstrumentRegistry())
                        .addMock(IssueFactory.class, new SimpleMockIssueFactory())
        );
    }

    @Before
    public void createMockMessageManager() throws Exception
    {
        reindexMessageManager = EasyMock.createMock(ReindexMessageManager.class);
        eventPublisher = EasyMock.createNiceMock(EventPublisher.class);
        replay(reindexMessageManager, eventPublisher);
    }

    @After
    public void shutDownExecutor()
    {
        executor.shutdown();
        try
        {
            executor.awaitTermination(1, TimeUnit.MINUTES);
        }
        catch (InterruptedException e)
        {
            throw new RuntimeInterruptedException(e);
        }
        if (!executor.isTerminated())
        {
            log.warn("Thread executor has not shut down properly within one minute");
        }
    }

    @Test
    public void testConcurrentIndexing() throws Exception
    {
        final Gate testGate = new Gate(NO_THREADS);
        final CountDownLatch endGate = new CountDownLatch(NO_THREADS);
        final DefaultIndexManager tested = new DefaultIndexManager(
                new MockIndexingConfiguration().maxReindexes(5).issuesToForceOptimize(5),
                mockIndexer, indexPath, reindexMessageManager, eventPublisher, null, null, null, null, null, null);
        final List<IndexRequester> requesters = new ArrayList<IndexRequester>(NO_THREADS);
        for (long l = 1; l <= NO_THREADS; l++)
        {
            requesters.add(new IndexRequester(tested, testGate, endGate, l));
        }
        for (IndexRequester requester : requesters)
        {
            executor.submit(requester);
        }
        testGate.go();
        endGate.await();
        assertNoErrors(requesters);
        verifyIndexCalls(NO_THREADS);
    }

    @Test
    public void testConcurrentIndexingWithExceededIssuesToForceOptimizeThreshold() throws Exception
    {
        final Gate testGate = new Gate(5);
        final CountDownLatch endGate = new CountDownLatch(5);
        final DefaultIndexManager tested = new DefaultIndexManager(
                new MockIndexingConfiguration().maxReindexes(20).issuesToForceOptimize(5),
                mockIndexer, indexPath, reindexMessageManager, eventPublisher, null, null, null, null, null, null);
        final List<IndexRequester> requesters = new ArrayList<IndexRequester>(NO_THREADS);
        requesters.add(new IndexRequester(tested, testGate, endGate, 1L, 2L, 3L, 4L, 5L));
        requesters.add(new IndexRequester(tested, testGate, endGate, 6L, 7L, 8L));
        requesters.add(new IndexRequester(tested, testGate, endGate, 9L, 10L, 11L, 12L, 13L));
        requesters.add(new IndexRequester(tested, testGate, endGate, 14L, 15L, 16L));
        requesters.add(new IndexRequester(tested, testGate, endGate, 17L, 18L, 19L, 20L, 21L));
        for (IndexRequester requester : requesters)
        {
            executor.submit(requester);
        }
        testGate.go();
        endGate.await();
        assertNoErrors(requesters);
        verifyIndexCalls(21);
    }

    private void assertNoErrors(List<IndexRequester> requesters)
    {
        for (IndexRequester requester : requesters)
        {
            assertFalse(requester.hasError());
        }
    }

    private void verifyIndexCalls(long count)
    {
        List<Issue> indexed = Lists.newArrayList(mockIndexer.reIndexedIssues);
        Collections.sort(indexed, new Comparator<Issue>()
        {
            @Override
            public int compare(Issue o1, Issue o2)
            {
                return o1.getId().compareTo(o2.getId());
            }
        });
        for (int i = 1; i <= count; i++)
        {
            assertEquals(i, indexed.get(i - 1).getId().longValue());
        }
    }

}
