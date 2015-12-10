package com.atlassian.jira.index;

import java.util.Collection;
import java.util.List;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.TimeoutException;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.junit.rules.InitMockitoMocks;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.util.concurrent.Latch;

import com.google.common.collect.Lists;

import org.hamcrest.CoreMatchers;
import org.hamcrest.Matchers;
import org.hamcrest.collection.IsIterableContainingInAnyOrder;
import org.hamcrest.collection.IsIterableContainingInOrder;
import org.junit.Assert;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;

/**
 * Test for search extractor manager
 */
public class DefaultSearchExtractorRegistrationManagerTest
{
    private final SearchExtractorRegistrationManager extractorManager = new DefaultSearchExtractorRegistrationManager();
    @Rule
    public TestRule init = new InitMockitoMocks(this);
    @Rule
    public ExpectedException exception = ExpectedException.none();
    @Mock
    private IssueSearchExtractor issueSearchExtractor;
    @Mock
    private IssueSearchExtractor issueSearchExtractor2;
    @Mock
    private CommentSearchExtractor commentSearchExtractor;

    @Test
    public void shouldRegisterExtractorAndThenReturnItFromSearchMethod()
    {
        //having
        extractorManager.register(issueSearchExtractor, Issue.class);
        //when
        final Collection<EntitySearchExtractor<Issue>> extractorsForEntity = extractorManager.findExtractorsForEntity(Issue.class);
        //then
        Assert.assertThat(extractorsForEntity, IsIterableContainingInOrder.<EntitySearchExtractor<Issue>>contains(issueSearchExtractor));
    }

    @Test
    public void shouldFindOnlyMatchingProperClassExtractors()
    {
        //having
        extractorManager.register(issueSearchExtractor, Issue.class);
        extractorManager.register(commentSearchExtractor, Comment.class);
        //when
        final Collection<EntitySearchExtractor<Issue>> extractorsForEntity = extractorManager.findExtractorsForEntity(Issue.class);
        //then
        Assert.assertThat(extractorsForEntity, IsIterableContainingInOrder.<EntitySearchExtractor<Issue>>contains(issueSearchExtractor));
    }

    @Test
    public void shouldNotMatchForSubclasses()
    {
        //having
        extractorManager.register(issueSearchExtractor, Issue.class);
        extractorManager.register(commentSearchExtractor, Comment.class);
        //when
        final Collection<EntitySearchExtractor<MockIssue>> extractorsForEntity = extractorManager.findExtractorsForEntity(MockIssue.class);
        //then
        Assert.assertThat(extractorsForEntity, CoreMatchers.is(Matchers.<EntitySearchExtractor<MockIssue>>empty()));

    }

    @Test
    public void shouldValidateClassParameterOnSearch()
    {
        exception.expect(NullPointerException.class);
        extractorManager.findExtractorsForEntity(null);
    }

    @Test
    public void shouldValidateClassParameterRegister()
    {
        exception.expect(NullPointerException.class);
        extractorManager.register(issueSearchExtractor, null);
    }

    @Test
    public void shouldValidateEntityExtractorParameterOnRegister()
    {
        exception.expect(NullPointerException.class);
        extractorManager.register(null, Issue.class);
    }

    @Test
    public void shouldNotBeAbleToRegisterTheSameExtractorTwiceForTheSameClass()
    {
        //having
        extractorManager.register(issueSearchExtractor, Issue.class);
        extractorManager.register(issueSearchExtractor, Issue.class);
        //when
        final Collection<EntitySearchExtractor<Issue>> extractorsForEntity = extractorManager.findExtractorsForEntity(Issue.class);
        //then
        Assert.assertThat(extractorsForEntity, Matchers.<EntitySearchExtractor<Issue>>iterableWithSize(1));
        Assert.assertThat(extractorsForEntity, IsIterableContainingInOrder.<EntitySearchExtractor<Issue>>contains(issueSearchExtractor));
    }

    @Test
    public void shouldUnregisterProperly()
    {
        //having
        extractorManager.register(issueSearchExtractor, Issue.class);
        extractorManager.register(issueSearchExtractor2, Issue.class);

        extractorManager.register(commentSearchExtractor, Comment.class);
        extractorManager.unregister(issueSearchExtractor, Issue.class);
        //when
        final Collection<EntitySearchExtractor<Issue>> extractorsForIssue = extractorManager.findExtractorsForEntity(Issue.class);
        final Collection<EntitySearchExtractor<Comment>> extractorsForComment = extractorManager.findExtractorsForEntity(Comment.class);
        //then
        Assert.assertThat(extractorsForComment, IsIterableContainingInOrder.<EntitySearchExtractor<Comment>>contains(commentSearchExtractor));
        Assert.assertThat(extractorsForIssue, IsIterableContainingInOrder.<EntitySearchExtractor<Issue>>contains(issueSearchExtractor2));


    }

    @Test
    public void shouldUnregisterFromSubclass()
    {
        //having
        extractorManager.register(issueSearchExtractor, Issue.class);
        extractorManager.register(issueSearchExtractor2, Issue.class);
        extractorManager.register(issueSearchExtractor, MockIssue.class);

        extractorManager.unregister(issueSearchExtractor, MockIssue.class);
        //when
        final Collection<EntitySearchExtractor<Issue>> extractorsForIssue = extractorManager.findExtractorsForEntity(Issue.class);
        final Collection<EntitySearchExtractor<MockIssue>> extractorsForMockIssue = extractorManager.findExtractorsForEntity(MockIssue.class);
        //then

        Assert.assertThat(extractorsForIssue, IsIterableContainingInAnyOrder.<EntitySearchExtractor<Issue>>containsInAnyOrder(issueSearchExtractor, issueSearchExtractor2));
        Assert.assertThat(extractorsForMockIssue, CoreMatchers.is(Matchers.<EntitySearchExtractor<MockIssue>>empty()));
    }

    @Test
    public void testParallelExecution() throws ExecutionException, InterruptedException, TimeoutException
    {
        //having
        final ExecutorService executor = Executors.newFixedThreadPool(24);
        final Latch writersCountDownLatch = new Latch(16);
        final Latch readersCountDownLatch = new Latch(16);
        final Latch unregisterCountDownLatch = new Latch(16);

        writersCountDownLatch.countDown();
        final List<Future> res = Lists.newArrayList();
        for (int i = 0; i < 8; i++)
        {
            res.add(executor.submit(new WriterRunnable<Issue>(writersCountDownLatch, Issue.class)));
            res.add(executor.submit(new WriterRunnable<MockIssue>(writersCountDownLatch, MockIssue.class)));
        }

        for (final Future re : res)
        {
            re.get(1, TimeUnit.SECONDS);
        }
        res.clear();
        for (int i = 0; i < 8; i++)
        {
            res.add(executor.submit(new WriterRunnable<MockIssue>(readersCountDownLatch, MockIssue.class)));
            res.add(executor.submit(new ReaderRunnable<Issue>(readersCountDownLatch, 8, Issue.class)));

        }

        for (final Future re : res)
        {
            re.get(1, TimeUnit.SECONDS);
        }
        res.clear();
        for (int i = 0; i < 4; i++)
        {
            res.add(executor.submit(new WriterRunnable<Issue>(unregisterCountDownLatch, Issue.class)));
            res.add(executor.submit(new UnregisteringRunnable<Issue>(unregisterCountDownLatch, Issue.class)));
            res.add(executor.submit(new UnregisteringRunnable<Comment>(unregisterCountDownLatch, Comment.class)));
            res.add(executor.submit(new ReaderRunnable<MockIssue>(unregisterCountDownLatch, 16, MockIssue.class)));

        }
        for (final Future re : res)
        {
            re.get(1, TimeUnit.SECONDS);
        }
        res.clear();

    }


    private class WriterRunnable<T extends Issue> implements Runnable
    {
        private final Class<T> entityClass;
        private final Latch latch;

        public WriterRunnable(final Latch latch, final Class<T> entityClass)
        {
            this.latch = latch;

            this.entityClass = entityClass;
        }

        @Override
        public void run()
        {
            latch.countDown();
            latch.await();
            extractorManager.register(Mockito.mock(IssueSearchExtractor.class), entityClass);

        }
    }

    private class UnregisteringRunnable<T> implements Runnable
    {
        private final Class<T> entityClass;
        private final Latch latch;

        public UnregisteringRunnable(final Latch latch, final Class<T> entityClass)
        {
            this.latch = latch;
            this.entityClass = entityClass;
        }

        @Override
        public void run()
        {
            latch.countDown();
            latch.await();

            @SuppressWarnings ("unchecked")
            final EntitySearchExtractor<T> mock = Mockito.mock(EntitySearchExtractor.class);
            extractorManager.register(mock, entityClass);
            final Collection<EntitySearchExtractor<T>> extractorsForEntity = extractorManager.findExtractorsForEntity(entityClass);
            //this iteration is required as it may reveal concurrent modification attempts
            for (final EntitySearchExtractor<T> searchExtractor : extractorsForEntity)
            {
                Assert.assertNotNull(searchExtractor);
            }
            Assert.assertThat(extractorsForEntity, CoreMatchers.not(Matchers.<EntitySearchExtractor<T>>empty()));
            extractorManager.unregister(mock, entityClass);
            Assert.assertThat(extractorManager.findExtractorsForEntity(entityClass),Matchers.not(Matchers.<EntitySearchExtractor<T>>hasItem(mock)));

        }
    }

    private class ReaderRunnable<T extends Issue> implements Runnable
    {
        private final Latch latch;
        private final int expectedSize;
        private final Class<T> entityClass;

        public ReaderRunnable(final Latch latch, final int expectedSize, final Class<T> entityClass)
        {
            this.latch = latch;
            this.expectedSize = expectedSize;
            this.entityClass = entityClass;
        }

        @Override
        public void run()
        {
            latch.countDown();
            latch.await();
            final Collection<EntitySearchExtractor<T>> extractorsForIssue = extractorManager.findExtractorsForEntity(entityClass);
            Assert.assertThat(extractorsForIssue, CoreMatchers.is(Matchers.<EntitySearchExtractor<T>>iterableWithSize(expectedSize)));
        }
    }
}
