package com.atlassian.jira.issue.changehistory;

import com.atlassian.crowd.embedded.api.UserWithAttributes;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchResults;
import com.atlassian.jira.jql.builder.JqlClauseBuilder;
import com.atlassian.jira.jql.builder.JqlQueryBuilder;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.local.testutils.UtilsForTestSetup;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ComponentLocator;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.web.bean.PagerFilter;
import com.atlassian.query.Query;
import com.atlassian.query.order.SortOrder;
import com.google.common.base.Function;
import com.google.common.collect.Lists;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.SQLException;
import java.sql.Timestamp;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import javax.annotation.Nullable;

import static org.junit.Assert.assertEquals;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class TestDefaultChangeHistoryManager_DB
{
    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);

    @AvailableInContainer
    OfBizDelegator ofBizDelegator =  new MockOfBizDelegator();
    @Mock
    @AvailableInContainer
    IssueManager issueManager;

    @AvailableInContainer
    UserManager userManager = new MockUserManager();

    @Mock
    JsonEntityPropertyManager jsonEntityPropertyManager;

    MockUser admin;

    long currentTime = System.currentTimeMillis();

    GenericValue issueGV1, issueGV2, issueGV3;

    @Before
    public void setUp() throws Exception
    {
        UtilsForTestSetup.deleteAllEntities();
        ofBizDelegator = ComponentAccessor.getOfBizDelegator();
        issueManager = ComponentAccessor.getIssueManager();

        admin = new MockUser("admin");

        issueGV1 = createIssue("ABC-1", currentTime - 50000, new ChangeGroup("fred", currentTime), new ChangeGroup("john", currentTime - 10000));
        issueGV2 = createIssue("ABC-2", currentTime - 60000, new ChangeGroup("admin", currentTime - 20000), new Comment("admin", currentTime - 16000));
        issueGV3 = createIssue("ABC-3", currentTime - 70000, new Comment("fred", currentTime - 15000));

        when(issueManager.getIssueObject(issueGV1.getLong("id"))).thenReturn(new MockIssue(issueGV1));
        when(issueManager.getIssueObject(issueGV2.getLong("id"))).thenReturn(new MockIssue(issueGV2));
        when(issueManager.getIssueObject(issueGV3.getLong("id"))).thenReturn(new MockIssue(issueGV3));

    }

    @Test
    public void testDoFindUserHistory() throws Exception
    {
        final ComponentLocator mockComponentLocator = mockSearch(issueManager, admin, issueGV1, issueGV3);
        final DefaultChangeHistoryManager changeHistoryManager = new DefaultChangeHistoryManager(issueManager, ofBizDelegator, null, mockComponentLocator, userManager, jsonEntityPropertyManager);

        final Collection<Issue> issues = changeHistoryManager.doFindUserHistory(admin, CollectionBuilder.newBuilder("fred", "john").asList(),
            Lists.newArrayList(10010L, 10011L), 20);

        assertEquals(2, issues.size());

        final Iterator<Issue> iterator = issues.iterator();
        assertEquals(issueGV1.getLong("id"), iterator.next().getId());
        assertEquals(issueGV3.getLong("id"), iterator.next().getId());
    }

    @Test
    public void testDoFindUserHistoryForAllUsers() throws Exception
    {
        final ComponentLocator mockComponentLocator = mockSearch(issueManager, admin, issueGV1, issueGV3, issueGV2);
        final DefaultChangeHistoryManager changeHistoryManager = new DefaultChangeHistoryManager(issueManager, ofBizDelegator, null, mockComponentLocator, userManager, jsonEntityPropertyManager);

        final Collection<Issue> issues = changeHistoryManager.doFindUserHistory(admin, null, Lists.newArrayList(10010L, 10011L), 20);

        assertEquals(3, issues.size());
        final Iterator<Issue> iterator = issues.iterator();
        assertEquals(issueGV1.getLong("id"), iterator.next().getId());
        assertEquals(issueGV3.getLong("id"), iterator.next().getId());
        assertEquals(issueGV2.getLong("id"), iterator.next().getId());
    }

    private static GenericValue createIssue(final String key, final long createdTime, final Change... changes) throws GenericEntityException, SQLException, SearchException
    {
        long updatedTime = createdTime;

        for (final Change change : changes)
        {
            updatedTime = Math.max(updatedTime, change.time);
        }

        final GenericValue issue = EntityUtils.createValue("Issue", MapBuilder.<String, Object>newBuilder()
            .add("key", key)
            .add("type", 1L)
            .add("created", new Timestamp(createdTime))
            .add("updated", new Timestamp(updatedTime))
            .add("resolution", "resolved")
            .add("project", 10010L).toMap()
            );

        for (final Change change : changes)
        {
            change.create(issue.getLong("id"));
        }

        return issue;
    }

    private static ComponentLocator mockSearch(final IssueManager issueManager, final UserWithAttributes admin, final GenericValue... issueGVs) throws Exception
    {
        final List<Long> issueIds = Lists.transform(Lists.newArrayList(issueGVs), new Function<GenericValue, Long>()
        {
            @Override
            public Long apply(@Nullable final GenericValue issue) {
                return issue == null ? null : issue.getLong("id");
            }
        });

        final List<Issue> issues = Lists.transform(issueIds, new Function<Long, Issue>()
        {
            @Override
            public Issue apply(@Nullable final Long issueId) {
                return issueManager.getIssueObject(issueId);
            }
        });

        final JqlClauseBuilder builder = JqlQueryBuilder.newBuilder().where().defaultAnd();
        builder.issue().in(issueIds.toArray(new Long[issueIds.size()])).endWhere().orderBy().createdDate(SortOrder.DESC);
        final Query query = builder.buildQuery();
        final ComponentLocator mockComponentLocator = mock(ComponentLocator.class);
        final SearchProvider mockSearchProvider = mock(SearchProvider.class);

        when(mockComponentLocator.getComponentInstanceOfType(SearchProvider.class)).thenReturn(mockSearchProvider);

        when(mockSearchProvider.search(eq(query), eq(admin), any(PagerFilter.class))).thenReturn(new SearchResults(issues, PagerFilter.getUnlimitedFilter()));

        return mockComponentLocator;
    }


    static abstract class Change
    {
        final String authorName;
        final long time;

        public Change(final String authorName, final long time)
        {
            this.authorName = authorName;
            this.time = time;
        }

        abstract void create(long issueId);
    }

    static class ChangeGroup extends Change
    {
        ChangeGroup(final String authorName, final long time)
        {
            super(authorName, time);
        }

        @Override
        void create(final long issueId)
        {
            EntityUtils.createValue("ChangeGroupIssueView", MapBuilder.<String, Object>build("project", 10010L,"issueid", issueId, "author", authorName, "created", new Timestamp(time)));
        }
    }

    static class Comment extends Change
    {
        Comment(final String authorName, final long time)
        {
            super(authorName, time);
        }

        @Override
        void create(final long issueId)
        {
            EntityUtils.createValue("ActionIssueView", MapBuilder.<String, Object>build("project", 10010L, "issueid", issueId, "author", authorName, "created", new Timestamp(time)));
        }
    }
}
