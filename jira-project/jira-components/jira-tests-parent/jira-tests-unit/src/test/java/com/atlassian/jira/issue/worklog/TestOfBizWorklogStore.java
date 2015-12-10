package com.atlassian.jira.issue.worklog;

import com.atlassian.core.ofbiz.test.UtilsForTests;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.ExpectedException;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.Map;

import static com.google.common.collect.Iterables.transform;
import static org.hamcrest.Matchers.contains;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

public class TestOfBizWorklogStore
{
    private static final Long ID = 1000L;
    private static final long CREATED_MS = 1000;
    private static final long STARTDATE_MS = 5000L;
    private static final long UPDATED_MS = 10000L;
    private static final String AUTHOR = "testauthor";
    private static final String COMMENT = "testbody";
    private static final String GROUP_LEVEL = "testgrouplevel";
    private static final String UPDATE_AUTHOR = "testupdateauthor";
    private static final Long TIME_SPENT = 60000L;

    @Rule
    public final RuleChain mockito = MockitoMocksInContainer.forTest(this);

    @Rule
    public final ExpectedException expectedException = ExpectedException.none();

    @AvailableInContainer (instantiateMe = true)
    private MockOfBizDelegator ofBizDelegator;

    @Mock
    private IssueManager issueManager;

    private OfBizWorklogStore ofBizWorklogStore;

    @Before
    public void setUp() throws Exception
    {
        ofBizWorklogStore = new OfBizWorklogStore(ofBizDelegator, issueManager);
    }

    @Test
    public void getByIdHappyPath()
    {
        final Long expectedIssueId = 10001L;
        final Long expectedWorklogId = 1000L;
        final Long expectedTimeSpent = 100L;
        UtilsForTests.getTestEntity("Worklog",
                ImmutableMap.of("id", expectedWorklogId, "issue", expectedIssueId, "timeworked", expectedTimeSpent));

        final Worklog worklog = ofBizWorklogStore.getById(expectedWorklogId);

        assertNotNull("Worklog has not been found", worklog);
        assertEquals(expectedWorklogId, worklog.getId());
        assertEquals(expectedTimeSpent, worklog.getTimeSpent());
        verify(issueManager).getIssueObject(expectedIssueId);
    }

    @Test
    public void getByIdReturnsNullWhenNotFound()
    {
        assertNull("Expected null to be returned for null id.", ofBizWorklogStore.getById(null));
        assertNull("Expected null to be returned for nonexistent id.", ofBizWorklogStore.getById(10000L));
    }

    @Test
    public void getByIssueReturnsWorklogsIfIssueExists()
    {
        final long now = System.currentTimeMillis();
        final Long id1 = 100L;
        final Long id2 = 200L;
        final Long id3 = 300L;

        UtilsForTests.getTestEntity("Worklog", ImmutableMap.of("id", id1, "issue", 150L, "timeworked", 1000L, "created", new Timestamp(now)));
        UtilsForTests.getTestEntity("Worklog", ImmutableMap.of("id", id2, "issue", 150L, "timeworked", 2000L, "created", new Timestamp(now + 500)));
        UtilsForTests.getTestEntity("Worklog", ImmutableMap.of("id", id3, "issue", 150L, "timeworked", 0L, "created", new Timestamp(now + 550)));
        UtilsForTests.getTestEntity("Worklog", ImmutableMap.of("id", 400L, "issue", 250L, "timeworked", 3000L, "created", new Timestamp(now)));

        final List<Worklog> worklogs = ofBizWorklogStore.getByIssue(new MockIssue(150L));

        assertThat(transform(worklogs, WORKLOG_2_WORKLOG_IDS), contains(id1, id2, id3));
    }

    @Test
    public void getByIssueShouldThrowExceptionForNullIssue()
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot resolve worklogs for null issue.");

        ofBizWorklogStore.getByIssue(null);
    }

    @Test
    public void convertToWorklogHappyPath()
    {
        final Map<String, Object> attributes = getTestParamMap();
        final GenericValue worklogGV = new MockGenericValue("Worklog", attributes);

        final Issue issue = mock(Issue.class);
        final Worklog worklog = ofBizWorklogStore.convertToWorklog(issue, worklogGV);

        assertEquals(issue, worklog.getIssue());
        assertEquals(attributes.get("id"), worklog.getId());
        assertEquals(attributes.get("timeworked"), worklog.getTimeSpent());
        assertEquals(((Timestamp) attributes.get("created")).getTime(), worklog.getCreated().getTime());
        assertEquals(((Timestamp) attributes.get("startdate")).getTime(), worklog.getStartDate().getTime());
        assertEquals(((Timestamp) attributes.get("updated")).getTime(), worklog.getUpdated().getTime());
        assertEquals(attributes.get("author"), worklog.getAuthor());
        assertEquals(attributes.get("updateauthor"), worklog.getUpdateAuthor());
        assertEquals(attributes.get("body"), worklog.getComment());
        assertEquals(attributes.get("grouplevel"), worklog.getGroupLevel());
        assertNull("Expected converted RoleLevelId to be null.", worklog.getRoleLevelId());
    }

    @Test
    public void createPAramMapShouldContainCopiedPropertiesOgWorklogObject()
    {
        final Long issueId = 1L;
        final Issue mockIssue = new MockIssue(issueId);

        final long now = System.currentTimeMillis();
        final Date created = new Date(now - 10000);
        final Date performed = new Date(now);
        final Date updated = new Date(now + 10000);

        final Worklog worklog = new WorklogImpl(null, mockIssue, null, "tim", "a comment", performed,
                "group level", 12783L, 1000L, "dylan", created, updated);

        final Map<String, Object> params = ofBizWorklogStore.createParamMap(worklog);

        assertEquals(issueId, params.get("issue"));
        assertEquals("tim", params.get("author"));
        assertEquals("a comment", params.get("body"));
        assertEquals("dylan", params.get("updateauthor"));
        assertEquals("group level", params.get("grouplevel"));
        assertEquals(12783L, params.get("rolelevel"));
        assertEquals(1000L, params.get("timeworked"));
        assertEquals(created, params.get("created"));
        assertEquals(performed, params.get("startdate"));
        assertEquals(updated, params.get("updated"));
    }

    @Test
    public void createHappyPath() throws Exception
    {
        final MockIssue issue = new MockIssue(101, "TST-20");
        when(issueManager.getIssueObject(101L)).thenReturn(issue);

        final long now = System.currentTimeMillis();
        final Date created = new Date(now - 1000);
        final Date performed = new Date(now);
        final Date updated = new Date(now + 1000);

        final Worklog worklog = new WorklogImpl(null, issue, null, "tim", "a comment", performed,
                "group level", 12783L, 1000L, "dylan", created, updated);

        final Worklog createdWorklog = ofBizWorklogStore.create(worklog);

        //check attributes returned by the create method
        assertWorklogValues(issue, createdWorklog, created, performed, updated);

        //retrieve worklog from data store and assert attributes again
        assertWorklogValues(issue, ofBizWorklogStore.getById(createdWorklog.getId()), created, performed, updated);
    }

    @Test
    public void updateHappyPath() throws Exception
    {
        final MockIssue issue = new MockIssue(101, "TST-20");
        when(issueManager.getIssueObject(101L)).thenReturn(issue);

        final Map<String, Object> attributes = ImmutableMap.<String, Object>builder()
                .putAll(getTestParamMap())
                .put("issue", issue.getId())
                .build();
        EntityUtils.createValue("Worklog", attributes);

        final Long updatedTimeSpent = 100000L;
        final Worklog worklog = new WorklogImpl(null, issue, ID, AUTHOR, COMMENT, new Date(STARTDATE_MS),
                GROUP_LEVEL, null, updatedTimeSpent, UPDATE_AUTHOR, new Date(CREATED_MS), new Date(UPDATED_MS));

        final Worklog editedWorklog = ofBizWorklogStore.update(worklog);

        assertEquals("Worklog should have updated value", updatedTimeSpent, editedWorklog.getTimeSpent());
        assertEquals("worklog retrieved from data store should have updated value", updatedTimeSpent, ofBizWorklogStore.getById(ID).getTimeSpent());
    }

    @Test
    public void removeShouldDeleteWorklogIfExists() throws Exception
    {
        final Map<String, Object> params = getTestParamMap();

        final GenericValue value = EntityUtils.createValue("Worklog", params);
        final Long worklogId = value.getLong("id");

        assertNotNull("Expected to find prepared worklog in store.", ofBizWorklogStore.getById(worklogId));
        assertTrue("Expected delete operation to be performed.", ofBizWorklogStore.delete(worklogId));

        assertNull("Did not expect to find deleted worklog in store.", ofBizWorklogStore.getById(worklogId));
    }

    @Test
    public void removeWithNullIdShouldThrowException() throws Exception
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("Cannot remove a worklog with id null.");

        ofBizWorklogStore.delete(null);
    }

    @Test
    public void removeWithNonExistantIdShouldReturnFalse() throws Exception
    {
        assertFalse("Should return false when delete fails", ofBizWorklogStore.delete(new Long(123)));
    }

    @Test
    public void getCountForWorklogsRestrictedByGroupNullGroupShouldThrowException()
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("You must provide a non null group name.");

        ofBizWorklogStore.getCountForWorklogsRestrictedByGroup(null);
    }

    @Test
    public void swapWorklogGroupRestrictionShouldThrowExceptionOnNullGroup()
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("You must provide a non null group name.");

        ofBizWorklogStore.swapWorklogGroupRestriction(null, "SwapGroup");
    }

    @Test
    public void swapWorklogGroupRestrictionShouldThrowExceptionOnNullSwap()
    {
        expectedException.expect(IllegalArgumentException.class);
        expectedException.expectMessage("You must provide a non null swap group name.");

        ofBizWorklogStore.swapWorklogGroupRestriction("GroupName", null);
    }

    @Test
    public void swapWorklogGroupRestrictionHappyPath() throws Exception
    {
        final long now = System.currentTimeMillis();
        final Long id1 = 100L;
        final Long id2 = 200L;
        final Long id3 = 300L;

        final Long expectedIssueId = 150L;
        UtilsForTests.getTestEntity("Worklog", ImmutableMap.of("id", id1, "issue", expectedIssueId, "timeworked", 1000L, "created", new Timestamp(now), "grouplevel", "Test Group"));
        UtilsForTests.getTestEntity("Worklog", ImmutableMap.of("id", id2, "issue", expectedIssueId, "timeworked", 2000L, "created", new Timestamp(now + 500), "grouplevel", "Test Group"));
        UtilsForTests.getTestEntity("Worklog", ImmutableMap.of("id", id3, "issue", expectedIssueId, "timeworked", 0L, "created", new Timestamp(now + 550), "grouplevel", "Test Group"));
        UtilsForTests.getTestEntity("Worklog", ImmutableMap.of("id", 400L, "issue", 250L, "timeworked", 3000L, "created", new Timestamp(now)));

        final Issue issue = new MockIssue(expectedIssueId);

        assertEquals(3, ofBizWorklogStore.swapWorklogGroupRestriction("Test Group", "SwapGroup"));

        final List<Worklog> worklogs = ofBizWorklogStore.getByIssue(issue);

        assertThat("Worklogs should be ordered by creation date",
                transform(worklogs, WORKLOG_2_WORKLOG_IDS), contains(id1, id2, id3));
        assertThat("All worklogs should have altered groups.",
                ImmutableSet.copyOf(transform(worklogs, WORKLOG_2_GROUP_LEVEL)), contains("SwapGroup"));
    }

    private void assertWorklogValues(final Issue issue, final Worklog worklog, final Date created, final Date performed, final Date updated)
    {
        assertEquals(issue, worklog.getIssue());
        assertEquals("tim", worklog.getAuthor());
        assertEquals("a comment", worklog.getComment());
        assertEquals("dylan", worklog.getUpdateAuthor());
        assertEquals("group level", worklog.getGroupLevel());
        assertEquals(Long.valueOf(12783), worklog.getRoleLevelId());
        assertEquals(Long.valueOf(1000), worklog.getTimeSpent());
        assertEquals(created, worklog.getCreated());
        assertEquals(performed, worklog.getStartDate());
        assertEquals(updated, worklog.getUpdated());
    }

    private Map<String, Object> getTestParamMap()
    {
        return ImmutableMap.<String, Object>builder()
                .put("id", ID)
                .put("timeworked", TIME_SPENT)
                .put("created", new Timestamp(CREATED_MS))
                .put("startdate", new Timestamp(STARTDATE_MS))
                .put("updated", new Timestamp(System.currentTimeMillis()))
                .put("author", AUTHOR)
                .put("body", COMMENT)
                .put("grouplevel", GROUP_LEVEL)
                .put("updateauthor", UPDATE_AUTHOR)
                .build();
    }

    private static final Function<? super Worklog, Long> WORKLOG_2_WORKLOG_IDS = new Function<Worklog, Long>()
    {
        @Override
        public Long apply(final Worklog input)
        {
            return input.getId();
        }
    };

    private static final Function<? super Worklog, String> WORKLOG_2_GROUP_LEVEL = new Function<Worklog, String>()
    {
        @Override
        public String apply(final Worklog input)
        {
            return input.getGroupLevel();
        }
    };
}
