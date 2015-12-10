package com.atlassian.jira.issue.managers;

import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.changehistory.ChangeHistory;
import com.atlassian.jira.issue.changehistory.ChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.DefaultChangeHistoryManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ComponentLocator;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import java.sql.Timestamp;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

@RunWith (MockitoJUnitRunner.class)
public class TestChangeHistoryManager
{
    private static final String ISSUE_OLD_SUMMARY = "this will be changed";
    private static final String ISSUE_NEW_SUMMARY = "this has been changed";

    @Mock private IssueManager issueManager;
    @Mock private PermissionManager permissionManager;
    @Mock private ComponentLocator componentLocator;
    @Mock private UserManager userManager;
    @Mock private JsonEntityPropertyManager jsonEntityPropertyManager;

    private OfBizDelegator mockOfBizDelegator = new MockOfBizDelegator();
    private ChangeHistoryManager changeHistoryManager;
    private Issue issueObject;
    private GenericValue issue;

    @Rule public RuleChain mockitoMocksInContainer = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        changeHistoryManager = new DefaultChangeHistoryManager( issueManager,  mockOfBizDelegator, permissionManager, componentLocator,  userManager, jsonEntityPropertyManager);

        GenericValue project = mockOfBizDelegator.createValue("Project", FieldMap.build("name", "test project"));

        issue = mockOfBizDelegator.createValue("Issue", FieldMap.build("id", new Long(1), "project", project.getLong("id"), "key", "TST-1", "summary", ISSUE_OLD_SUMMARY));
        issueObject = new MockIssue(issue);
    }

    @Test
    public void testChangeHistory() throws GenericEntityException
    {
        //add a change history
        GenericValue cg = mockOfBizDelegator.createValue("ChangeGroup", FieldMap.build("id", 1L, "issue", issue.getLong("id")));
        mockOfBizDelegator.createValue("ChangeItem", FieldMap.build("group", cg.getLong("id"), "field", "Summary", "oldstring", ISSUE_OLD_SUMMARY, "newstring", ISSUE_NEW_SUMMARY));

        List changeHistories = changeHistoryManager.getChangeHistoriesForUser(issueObject, null);
        assertNotNull(changeHistories);
        assertEquals(1, changeHistories.size());
        assertEquals(new Long(1), ((ChangeHistory) changeHistories.get(0)).getId());
    }

    @Test
    public void testChangeHistoryNone() throws GenericEntityException
    {
        List changeHistories = changeHistoryManager.getChangeHistoriesForUser(issueObject, null);
        assertNotNull(changeHistories);
        assertTrue(changeHistories.isEmpty());
    }

    /**
     * Test that multiple change histories are returned in order
     */
    @Test
    public void testMultipleChangeHistories() throws GenericEntityException
    {
        List changeHistories = changeHistoryManager.getChangeHistoriesForUser(issueObject, null);
        assertNotNull(changeHistories);
        assertTrue(changeHistories.isEmpty());

        Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
        Timestamp timestamp2 = new Timestamp(timestamp1.getTime() + 1);

        //add two change histories
        GenericValue cg = mockOfBizDelegator.createValue("ChangeGroup", FieldMap.build("id", 1L, "created", timestamp1, "issue", issue.getLong("id")));
        mockOfBizDelegator.createValue("ChangeItem", FieldMap.build("group", cg.getLong("id"), "field", "Summary", "oldstring", ISSUE_OLD_SUMMARY, "newstring", ISSUE_NEW_SUMMARY));
        cg = mockOfBizDelegator.createValue("ChangeGroup", FieldMap.build("id", 2L, "created", timestamp2, "issue", issue.getLong("id")));
        mockOfBizDelegator.createValue("ChangeItem", FieldMap.build("group", cg.getLong("id"), "field", "Description", "oldstring", "", "newstring", "New Description"));

        changeHistories = changeHistoryManager.getChangeHistoriesForUser(issueObject, null);
        assertNotNull(changeHistories);
        assertEquals(2, changeHistories.size());
        assertEquals(new Long(1), ((ChangeHistory) changeHistories.get(0)).getId());
        assertEquals(new Long(2), ((ChangeHistory) changeHistories.get(1)).getId());
    }

    @Test
    public void testGetChangeItemsForFieldNullIssue()
    {
        try
        {
            changeHistoryManager.getChangeItemsForField(null, "Link");
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testGetChangeItemsForFieldNullFieldName()
    {
        try
        {
            changeHistoryManager.getChangeItemsForField(new MockIssue(), null);
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }



    @Test
    public void testGetChangeItemsForFieldEmptyFieldName()
    {
        try
        {
            changeHistoryManager.getChangeItemsForField(new MockIssue(), "");
            fail("Should throw IAE");
        }
        catch (IllegalArgumentException e)
        {
            //expected
        }
    }

    @Test
    public void testGetChangeItemsForField()
    {
        List changeHistories = changeHistoryManager.getChangeItemsForField(issueObject, "Link");
        assertNotNull(changeHistories);
        assertTrue(changeHistories.isEmpty());

        Timestamp timestamp1 = new Timestamp(System.currentTimeMillis());
        Timestamp timestamp2 = new Timestamp(timestamp1.getTime() + 1);

        //add two change histories
        GenericValue cg = mockOfBizDelegator.createValue("ChangeGroup", FieldMap.build("id", 1L, "created", timestamp1, "issue", issue.getLong("id")));
        mockOfBizDelegator.createValue("ChangeItem", FieldMap.build("id", 10L, "group", cg.getLong("id"), "field", "Link", "oldstring", ISSUE_OLD_SUMMARY, "newstring", ISSUE_NEW_SUMMARY));
        cg = mockOfBizDelegator.createValue("ChangeGroup", FieldMap.build("id", 2L, "created", timestamp2, "issue", issue.getLong("id")));
        mockOfBizDelegator.createValue("ChangeItem", FieldMap.build("id", 20L, "group", cg.getLong("id"), "field", "Link", "oldstring", "", "newstring", "New Description"));

        //Not ideal but add the view as GVs
        mockOfBizDelegator.createValue("ChangeGroupChangeItemView", FieldMap.build("issue", issue.getLong("id"), "changeitemid", 10L, "created", timestamp1, "changegroupid", 1L, "field", "Link").add("oldstring", ISSUE_OLD_SUMMARY).add("newstring", ISSUE_NEW_SUMMARY));
        mockOfBizDelegator.createValue("ChangeGroupChangeItemView", FieldMap.build("issue", issue.getLong("id"), "changeitemid", 20L, "created", timestamp2, "changegroupid", 2L, "field", "Link").add("oldstring", "").add("newstring", "New Description"));

        changeHistories = changeHistoryManager.getChangeItemsForField(issueObject, "Link");
        assertNotNull(changeHistories);
        assertEquals(2, changeHistories.size());
        assertEquals(ISSUE_NEW_SUMMARY, ((ChangeItemBean) changeHistories.get(0)).getToString());
        assertEquals("New Description", ((ChangeItemBean) changeHistories.get(1)).getToString());
    }
}

