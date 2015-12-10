package com.atlassian.jira.issue.changehistory;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MockIssueFactory;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.ofbiz.MockOfBizListIterator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.user.util.MockUserManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ComponentLocator;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import org.hamcrest.collection.IsMapContaining;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.ArgumentCaptor;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.Collection;
import java.util.Collections;
import java.util.Date;
import java.util.List;
import java.util.Map;
import javax.annotation.Nullable;

import static com.google.common.collect.Lists.transform;
import static java.util.Arrays.asList;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertThat;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;
import static org.mockito.Matchers.any;
import static org.mockito.Matchers.anyCollectionOf;
import static org.mockito.Matchers.anyListOf;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestDefaultChangeHistoryManager
{
    @Mock
    private IssueManager issueManager;
    @Mock
    private OfBizDelegator ofBizDelegator;
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private ComponentLocator componentLocator;
    @Mock
    private JsonEntityPropertyManager jsonEntityPropertyManager;


    private UserManager userManager = new MockUserManager();

    private DefaultChangeHistoryManager changeHistoryManager;

    @Before
    public void setUp()
    {
        changeHistoryManager = new DefaultChangeHistoryManager(issueManager, ofBizDelegator, permissionManager,
                componentLocator, userManager, jsonEntityPropertyManager);
    }

    @Test
    public void testGetPreviousIssueKeys()
    {
        final MutableIssue issue = new MockIssue(10023, "HSP-25");
        when(issueManager.getIssueObject("HSP-25")).thenReturn(issue);
        when(issueManager.getIssueObject(10023L)).thenReturn(issue);
        when(issueManager.getAllIssueKeys(10023L)).thenReturn(ImmutableSet.of("MKY-12", "STUFF-23", "BLAH-2", "HSP-25"));

        final Collection<String> previousKeys = changeHistoryManager.getPreviousIssueKeys("HSP-25");

        assertFalse(previousKeys.isEmpty());
        assertEquals(3, previousKeys.size());
        assertTrue(previousKeys.containsAll(ImmutableList.of("MKY-12", "STUFF-23", "BLAH-2")));
    }

    @Test
    public void testFindAllPossibleValuesDoesNotCallGetCompleteList()
    {
        final MockOfBizListIterator mockOfBizListIterator = new MockOfBizListIterator(ImmutableList.of(getMockChangeItemGV(1)))
        {
            @Override
            public List<GenericValue> getCompleteList()
            {
                fail("Method getCompleteList must not be called.");
                return null;
            }
        };

        when(ofBizDelegator.findListIteratorByCondition(
                eq("ChangeItem"), any(EntityCondition.class), isNull(EntityCondition.class),
                eq(ImmutableList.of("oldstring", "oldvalue", "newstring", "newvalue")), eq(ImmutableList.of("asc")),
                isNull(EntityFindOptions.class))).thenReturn(mockOfBizListIterator);

        final Map<String, String> allPossibleValues = changeHistoryManager.findAllPossibleValues("status");

        assertThat(allPossibleValues, IsMapContaining.hasEntry("open", "1"));
        assertThat(allPossibleValues, IsMapContaining.hasEntry("resolved", "5"));
    }

    @Test
    public void testGetChangeItemsForField()
    {
        final MutableIssue issue = MockIssueFactory.createIssue(10000);

        when(ofBizDelegator.findByAnd(
                "ChangeGroupChangeItemView",
                ImmutableMap.of("issue", 10000L, "field", "somefield"),
                ImmutableList.of("created ASC", "changeitemid ASC"))
        ).thenReturn(ImmutableList.of(getMockChangeItemGV(1), getMockChangeItemGV(2), getMockChangeItemGV(3)));

        final List<ChangeItemBean> changeItems = changeHistoryManager.getChangeItemsForField(issue, "somefield");

        assertEquals(3, changeItems.size());

        assertEquals("jira", changeItems.get(0).getFieldType());
        assertEquals("resolution", changeItems.get(0).getField());
        assertEquals("1", changeItems.get(0).getFrom());
        assertEquals("Open", changeItems.get(0).getFromString());
        assertEquals("5", changeItems.get(0).getTo());
        assertEquals("Resolved", changeItems.get(0).getToString());
        assertEquals(new Timestamp(1), changeItems.get(0).getCreated());

        assertEquals("jira", changeItems.get(1).getFieldType());
        assertEquals("resolution", changeItems.get(1).getField());
        assertEquals("1", changeItems.get(1).getFrom());
        assertEquals("Open", changeItems.get(1).getFromString());
        assertEquals("5", changeItems.get(1).getTo());
        assertEquals("Resolved", changeItems.get(1).getToString());
        assertEquals(new Timestamp(2), changeItems.get(1).getCreated());

        assertEquals("jira", changeItems.get(2).getFieldType());
        assertEquals("resolution", changeItems.get(2).getField());
        assertEquals("1", changeItems.get(2).getFrom());
        assertEquals("Open", changeItems.get(2).getFromString());
        assertEquals("5", changeItems.get(2).getTo());
        assertEquals("Resolved", changeItems.get(2).getToString());
        assertEquals(new Timestamp(3), changeItems.get(2).getCreated());
    }

    @Test
    public void testGetChangeItemsForFieldNoItems()
    {
        final MutableIssue issue = MockIssueFactory.createIssue(10000);

        when(ofBizDelegator.findByAnd(
                eq("ChangeGroupChangeItemView"),
                eq(ImmutableMap.of("issue", 10000L, "field", "somefield")),
                anyListOf(String.class)))
                .thenReturn(Collections.<GenericValue>emptyList());

        final List<ChangeItemBean> changeItems = changeHistoryManager.getChangeItemsForField(issue, "somefield");
        assertEquals(0, changeItems.size());
    }

    @Test
    public void testFindUserHistory()
    {
        final User admin = new MockUser("admin");

        when(permissionManager.getProjectObjects(Permissions.BROWSE, admin))
                .thenReturn(ImmutableList.<Project>of(new MockProject(10000L, "MKY"), new MockProject(10001L, "HSP")));

        DefaultChangeHistoryManager changeHistoryManager = new DefaultChangeHistoryManager(issueManager, ofBizDelegator, permissionManager, componentLocator, userManager, jsonEntityPropertyManager)
        {
            @Override
            Collection<Issue> doFindUserHistory(final User remoteUser, final Collection<String> usernames, final Collection<Long> projects, final int maxResults)
            {
                assertEquals(admin, remoteUser);
                assertEquals(ImmutableList.of(10000L, 10001L), projects);
                return Collections.emptyList();
            }
        };
        Collection<Issue> issues = changeHistoryManager.findUserHistory(admin, ImmutableList.of("fred", "john"), 10);
        assertEquals(0, issues.size());
    }

    @Test
    public void testFindUserHistoryWithProjects()
    {
        final User admin = new MockUser("admin");

        final Project monkey = new MockProject(10000L, "MKY");
        final Project homosapien = new MockProject(10001L, "HSP");

        when(permissionManager.hasPermission(Permissions.BROWSE, monkey, admin)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.BROWSE, homosapien, admin)).thenReturn(false);

        final DefaultChangeHistoryManager changeHistoryManager = new DefaultChangeHistoryManager(issueManager, ofBizDelegator, permissionManager, componentLocator, userManager, jsonEntityPropertyManager)
        {
            @Override
            Collection<Issue> doFindUserHistory(final User remoteUser, final Collection<String> usernames, final Collection<Long> projects, final int maxResults)
            {
                assertEquals(admin, remoteUser);
                assertEquals(ImmutableList.of(10000L), projects);
                return Collections.emptyList();
            }
        };
        Collection<Issue> issues = changeHistoryManager.findUserHistory(admin, ImmutableList.of("fred", "john"), ImmutableList.of(monkey, homosapien), 10);
        assertEquals(0, issues.size());
    }

    @Test
    public void testDoFindUserHistoryNoProjects()
    {
        final User admin = new MockUser("admin");

        final DefaultChangeHistoryManager changeHistoryManager = new DefaultChangeHistoryManager(issueManager, ofBizDelegator, permissionManager, componentLocator, userManager, jsonEntityPropertyManager);
        Collection<Issue> issues = changeHistoryManager.doFindUserHistory(admin, ImmutableList.of("fred", "john"), Collections.<Long>emptyList(), 20);
        assertEquals(0, issues.size());
    }

    @Test
    public void testDeleteAllChangeHistoryItems()
    {
        final MutableIssue issue = MockIssueFactory.createIssue(10L);

        when(ofBizDelegator.findByAnd(eq("ChangeGroup"), eq(ImmutableMap.of("issue", 10L)))).thenReturn(groupList(1L, 5L, 10L));

        changeHistoryManager.removeAllChangeItems(issue);

        verify(ofBizDelegator).removeByAnd(eq("ChangeItem"), eq(ImmutableMap.of("group", 1L)));
        verify(ofBizDelegator).removeByAnd(eq("ChangeItem"), eq(ImmutableMap.of("group", 5L)));
        verify(ofBizDelegator).removeByAnd(eq("ChangeItem"), eq(ImmutableMap.of("group", 10L)));
        // groups themselves must be removed
        verify(ofBizDelegator).removeByAnd(eq("ChangeGroup"), eq(ImmutableMap.of("issue", 10L)));

        // entity properties should be removed
        verify(jsonEntityPropertyManager).deleteByEntity(EntityPropertyType.CHANGE_HISTORY_PROPERTY.getDbEntityName(), 1L);
        verify(jsonEntityPropertyManager).deleteByEntity(EntityPropertyType.CHANGE_HISTORY_PROPERTY.getDbEntityName(), 5L);
        verify(jsonEntityPropertyManager).deleteByEntity(EntityPropertyType.CHANGE_HISTORY_PROPERTY.getDbEntityName(), 10L);

    }

    @Test
    public void getChangeHistoriesSincePassesConditionOnDateSince()
    {
        final MutableIssue issue = MockIssueFactory.createIssue(10L);
        final Date sinceDate = new Date();

        changeHistoryManager.getChangeHistoriesSince(issue, sinceDate);

        ArgumentCaptor<EntityConditionList> argumentCaptor = ArgumentCaptor.forClass(EntityConditionList.class);

        verify(ofBizDelegator, times(1)).findByCondition(eq("ChangeGroup"), argumentCaptor.capture(), anyCollectionOf(String.class), anyListOf(String.class));

        final EntityConditionList entityCondition = argumentCaptor.getValue();
        final EntityExpr dateCondition = (EntityExpr) entityCondition.getCondition(1);

        assertThat((String) dateCondition.getLhs(), equalTo("created"));
        assertThat(dateCondition.getOperator(), equalTo(EntityOperator.GREATER_THAN));
        assertThat(((Timestamp) dateCondition.getRhs()).getTime(), equalTo(sinceDate.getTime()));
    }

    private List<GenericValue> groupList(Long... ids)
    {
        return transform(asList(ids), new Function<Long, GenericValue>()
        {
            @Override
            public GenericValue apply(@Nullable Long from)
            {
                return new MockGenericValue("ChangeGroup", from);
            }
        });
    }


    private GenericValue getMockChangeItemGV(int timestamp)
    {
        return new MockGenericValue
                (
                        "ChangeGroupChangeItemView",
                        ImmutableMap.<String, Object>builder()
                                .put("fieldtype", "jira")
                                .put("field", "resolution")
                                .put("oldvalue", "1")
                                .put("oldstring", "Open")
                                .put("newvalue", "5")
                                .put("newstring", "Resolved")
                                .put("created", new Timestamp(timestamp))
                                .build()
                );
    }
}
