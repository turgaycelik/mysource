package com.atlassian.jira.issue.index;

import java.util.Collection;
import java.util.Collections;
import java.util.Iterator;
import java.util.List;
import java.util.SortedMap;
import java.util.TreeMap;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.concurrent.BarrierFactory;
import com.atlassian.jira.concurrent.MockBarrierFactory;
import com.atlassian.jira.config.ReindexMessageManager;
import com.atlassian.jira.config.util.IndexPathManager;
import com.atlassian.jira.config.util.IndexingConfiguration;
import com.atlassian.jira.event.ListenerManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.util.IssuesIterable;
import com.atlassian.jira.local.runner.ListeningMockitoRunner;
import com.atlassian.jira.matchers.ReflectionEqualTo;
import com.atlassian.jira.mock.component.MockComponentWorker;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizListIterator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.util.Consumer;

import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Lists;

import org.hamcrest.Matchers;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.invocation.InvocationOnMock;
import org.mockito.stubbing.Answer;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.issue.IssueFieldConstants.PROJECT;
import static com.atlassian.jira.matchers.ReflectionEqualTo.reflectionEqualTo;
import static java.lang.Math.max;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.argThat;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.when;
import static org.ofbiz.core.entity.EntityFindOptions.findOptions;
import static org.ofbiz.core.entity.EntityOperator.EQUALS;
import static org.ofbiz.core.entity.EntityOperator.LESS_THAN_EQUAL_TO;

@RunWith (ListeningMockitoRunner.class)
public class TestDefaultIndexManagerBatching
{
    static final int MAX_RESULTS = 2;

    @Mock OfBizDelegator ofBizDelegator;
    @Mock IndexingConfiguration indexProperties;
    @Mock IssueIndexer issueIndexer;
    @Mock IndexPathManager indexPath;
    @Mock ReindexMessageManager reindexMessageManager;
    @Mock EventPublisher eventPublisher;
    @Mock ListenerManager listenerManager;
    @Mock ProjectManager projectManager;
    @Mock IssueManager issueManager;
    @Mock IssueFactory issueFactory;

    @Mock Project projectA;
    @Mock MutableIssue issueA1;
    @Mock MutableIssue issueA2;

    @Mock Project projectB;
    @Mock MutableIssue issueB1;
    @Mock MutableIssue issueB2;
    @Mock MutableIssue issueB3;

    @Mock Project projectC;
    @Mock MutableIssue issueC1;

    BarrierFactory barrierFactory;

    SortedMap<Long, Issue> testIssues = new TreeMap<Long, Issue>();


    // Set up 5 issues over 3 projects.
    @Before
    public void setUp() throws Exception
    {
        new MockComponentWorker().init();
        barrierFactory = new MockBarrierFactory();

        // create 3 projects
        when(projectA.getId()).thenReturn(10L);
        when(projectB.getId()).thenReturn(20L);
        when(projectC.getId()).thenReturn(30L);
        when(projectManager.getProjectObjects()).thenReturn(ImmutableList.of(projectA, projectB, projectC));

        // create 6 issues across 3 projects in round-robin fashion
        setUpIssue(issueA1, 110L);
        setUpIssue(issueB1, 120L);
        setUpIssue(issueC1, 130L);
        setUpIssue(issueA2, 140L);
        setUpIssue(issueB2, 150L);
        setUpIssue(issueB3, 160L);

    }

    @Test
    public void testPerProjectBatching() throws Exception
    {
        // prepare for querying by project
        prepareDelegatorForSelectByProjectId(10L, issueA1, issueA2);
        prepareDelegatorForSelectByProjectId(20L, issueB1, issueB2, issueB3);
        prepareDelegatorForSelectByProjectId(30L, issueC1);

        // create the batcher and test
        IssuesBatcher batcher = createIndexManager().new ProjectBatcher(ofBizDelegator, issueFactory);
        Iterator<IssuesIterable> batchIt = batcher.iterator();

        List<Issue> issuesInBatch1 = Lists.newArrayList();
        batchIt.next().foreach(new AddToList(issuesInBatch1));
        assertThat(issuesInBatch1, Matchers.<List>equalTo(ImmutableList.of(issueA1, issueA2)));

        List<Issue> issuesInBatch2 = Lists.newArrayList();
        batchIt.next().foreach(new AddToList(issuesInBatch2));
        assertThat(issuesInBatch2, Matchers.<List>equalTo(ImmutableList.of(issueB1, issueB2, issueB3)));

        List<Issue> issuesInBatch3 = Lists.newArrayList();
        batchIt.next().foreach(new AddToList(issuesInBatch3));
        assertThat(issuesInBatch3, Matchers.<List>equalTo(ImmutableList.of(issueC1)));
    }

    @Test
    public void testIssueIdBatching() throws Exception
    {
        // prepare the maxId db query
        when(ofBizDelegator.findByCondition(eq("IssueMaxId"), any(EntityCondition.class), eq(ImmutableList.of("max")))).thenAnswer(new MaxIdAnswer());

        // prepare for the ranged queries: SELECT * FROM issue WHERE id <= ? ORDER BY ID DESC LIMIT 2
        when(ofBizDelegator.findListIteratorByCondition(
                eq("Issue"),
                any(EntityCondition.class),
                (EntityCondition) isNull(),
                ((Collection) isNull()),
                eq(ImmutableList.of("id DESC")),
                argThat(ReflectionEqualTo.reflectionEqualTo(findOptions().maxResults(MAX_RESULTS)))
        )).thenAnswer(new IdRangeAnswer());

        // create the batcher and test
        IssuesBatcher batcher = new IssueIdBatcher(ofBizDelegator, issueFactory, barrierFactory, 2, null, null);
        Iterator<IssuesIterable> batchIt = batcher.iterator();

        List<Issue> issuesInBatch1 = Lists.newArrayList();
        batchIt.next().foreach(new AddToList(issuesInBatch1));
        assertThat(issuesInBatch1, Matchers.<List>equalTo(ImmutableList.of(issueB3, issueB2)));

        List<Issue> issuesInBatch2 = Lists.newArrayList();
        batchIt.next().foreach(new AddToList(issuesInBatch2));
        assertThat(issuesInBatch2, Matchers.<List>equalTo(ImmutableList.of(issueA2, issueC1)));
        assertThat(issuesInBatch2, Matchers.<List>equalTo(ImmutableList.of(issueA2, issueC1)));

        List<Issue> issuesInBatch3 = Lists.newArrayList();
        batchIt.next().foreach(new AddToList(issuesInBatch3));
        assertThat(issuesInBatch3, Matchers.<List>equalTo(ImmutableList.of(issueB1, issueA1)));
    }

    private DefaultIndexManager createIndexManager()
    {
        return new DefaultIndexManager(indexProperties, issueIndexer, indexPath, reindexMessageManager, eventPublisher,
                listenerManager, projectManager, issueManager, null, null, null);
    }

    private void setUpIssue(MutableIssue issue, long id)
    {
        MockGenericValue genericValue = new MockGenericValue("Issue", ImmutableMap.of(
                "id", id
        ));

        when(issue.getId()).thenReturn(id);
        when(issue.getGenericValue()).thenReturn(genericValue);
        when(issueFactory.getIssue(genericValue)).thenReturn(issue);

        testIssues.put(id, issue);
    }

    private EntityExpr projectIdIs(long projectId)
    {
        return argThat(reflectionEqualTo(new EntityExpr(PROJECT, EQUALS, projectId)));
    }

    private void prepareDelegatorForSelectByProjectId(long projectId, Issue... issues)
    {
        when(ofBizDelegator.findListIteratorByCondition(eq("Issue"), projectIdIs(projectId), (EntityCondition) isNull(), ((Collection) isNull()), ((List) isNull()), ((EntityFindOptions) isNull()))).thenAnswer(new ListIteratorAnswer(issues));
    }

    private static class ListIteratorAnswer implements Answer<OfBizListIterator>
    {
        private final ImmutableList<Issue> issues;

        public ListIteratorAnswer(Issue... issues)
        {
            this.issues = ImmutableList.copyOf(issues);
        }

        public ListIteratorAnswer(Iterable<Issue> issues)
        {
            this.issues = ImmutableList.copyOf(issues);
        }

        @Override
        public OfBizListIterator answer(InvocationOnMock invocation) throws Throwable
        {
            ImmutableList.Builder<GenericValue> issueGVs = ImmutableList.builder();
            for (Issue issue : issues)
            {
                issueGVs.add(issue.getGenericValue());
            }

            return new MockOfBizListIterator(issueGVs.build());
        }
    }

    private static class AddToList implements Consumer<Issue>
    {
        private final List<Issue> issuesInBatch1;

        public AddToList(List<Issue> issuesInBatch1) {this.issuesInBatch1 = issuesInBatch1;}

        @Override
        public void consume(@Nonnull Issue issue)
        {
            issuesInBatch1.add(issue);
        }
    }

    private class MaxIdAnswer implements Answer<List<GenericValue>>
    {
        @Override
        public List<GenericValue> answer(InvocationOnMock invocation) throws Throwable
        {
            SortedMap<Long, Issue> matchingIssues = testIssues;
            EntityExpr expr = (EntityExpr) invocation.getArguments()[1];
            if (expr != null && "id".equals(expr.getLhs()) && expr.getOperator() == LESS_THAN_EQUAL_TO)
            {
                // filter out issues with higher id than specified in the condition
                matchingIssues = testIssues.headMap((Long) expr.getRhs());
            }

            GenericValue maxGV = new MockGenericValue("", ImmutableMap.of("max", matchingIssues.isEmpty() ? null : matchingIssues.lastKey()));
            return Lists.newArrayList(maxGV);
        }
    }

    private class IdRangeAnswer implements Answer<OfBizListIterator>
    {
        @Override
        public OfBizListIterator answer(InvocationOnMock invocation) throws Throwable
        {
            EntityExpr expr = (EntityExpr) invocation.getArguments()[1];
            if (expr != null && "id".equals(expr.getLhs()) && expr.getOperator() == LESS_THAN_EQUAL_TO)
            {
                Long issueId = (Long) expr.getRhs();
                long lessThanId = issueId + 1;
                List<Issue> issuesInRange = Lists.newArrayList(testIssues.headMap(lessThanId).values());

                // the batch has a max size and is sorted in "id DESC" order
                List<Issue> issuesInBatch = Lists.newArrayList(issuesInRange.subList(max(0, issuesInRange.size()-MAX_RESULTS), issuesInRange.size()));
                Collections.reverse(issuesInBatch);

                return new MockOfBizListIterator(Lists.transform(issuesInBatch, new Function<Issue, GenericValue>()
                {
                    @Override
                    public GenericValue apply(@Nullable Issue input)
                    {
                        return input.getGenericValue();
                    }
                }));
            }

            return null;
        }
    }
}
