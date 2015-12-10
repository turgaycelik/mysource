package com.atlassian.jira.config;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.crowd.exception.InvalidCredentialException;
import com.atlassian.crowd.exception.InvalidUserException;
import com.atlassian.crowd.exception.OperationNotPermittedException;
import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.exception.StoreException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MockIssueFactory;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkImpl;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeImpl;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockIssueManager;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.CollectionReorderer;

import com.google.common.collect.Lists;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static org.mockito.Matchers.any;
import static org.mockito.Matchers.eq;
import static org.mockito.Matchers.isNull;
import static org.mockito.Mockito.doThrow;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestDefaultSubTaskManager
{
    @Mock @AvailableInContainer
    private DefaultSubTaskManager defaultSubTaskManager;
    @Mock @AvailableInContainer
    private ConstantsManager mockConstantsManager;
    @Mock @AvailableInContainer
    private IssueLinkManager mockIssueLinkManager;
    @Mock @AvailableInContainer
    private IssueLinkTypeManager mockIssueLinkTypeManager;
    @Mock @AvailableInContainer
    private PermissionManager mockPermissionManager;
    @Mock @AvailableInContainer
    private IssueFactory issueFactory;
    @Mock @AvailableInContainer
    private ApplicationProperties applicationProperties;
    private GenericValue issue;
    private MockGenericValue issueLinkTypeGV;

    @Mock @AvailableInContainer
    private IssueTypeSchemeManager mockIssueTypeSchemeManager;
    @Mock @AvailableInContainer
    private IssueManager mockIssueManager;

    @Mock
    private JiraAuthenticationContext authContext;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);
    private long generatedId = 0;

    @Before
    public void setUp() throws Exception
    {
        issue = new MockGenericValue("Issue", FieldMap.build("summary", "test summary"));
        defaultSubTaskManager = getDefaultSubTaskManager();
    }

    private DefaultSubTaskManager getDefaultSubTaskManager()
    {
        return getDefaultSubTaskManager(applicationProperties);
    }

    private DefaultSubTaskManager getDefaultSubTaskManager(final ApplicationProperties applicationProperties)
    {
        return new DefaultSubTaskManager(mockConstantsManager, mockIssueLinkTypeManager, mockIssueLinkManager, mockPermissionManager, applicationProperties, new CollectionReorderer(),
                mockIssueTypeSchemeManager, mockIssueManager);
    }

    @Test
    public void testCreateSubTaskIssueType() throws CreateException
    {
        String name = "sub-task issue type name";
        Long sequence = new Long(1);
        String description = "sub-task issue type description";
        String iconurl = "sub-task issue type icon url";

        when(mockConstantsManager.constantExists(eq("IssueType"), eq(name))).thenReturn(Boolean.FALSE);
        final MockGenericValue mockGv = new MockGenericValue("IssueType", FieldMap.build("id", "1", "name", name));
        when(mockConstantsManager.createIssueType(eq(name), eq(sequence), eq(SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE), eq(description), eq(iconurl))).thenReturn(mockGv);

        mockIssueTypeSchemeManager.addOptionToDefault("1");

        defaultSubTaskManager.createSubTaskIssueType(name, sequence, description, iconurl);
        verify(mockConstantsManager, times(1)).createIssueType(eq(name), eq(sequence), eq(SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE), eq(description), eq(iconurl));
    }

    @Test
    public void testUpdateSubTaskIssueType() throws StoreException
    {
        String id = "1";
        String name = "sub-task issue type name";
        Long sequence = new Long(1);
        String description = "sub-task issue type description";
        String iconurl = "sub-task issue type icon url";

        defaultSubTaskManager.updateSubTaskIssueType(id, name, sequence, description, iconurl);
        verify(mockConstantsManager).updateIssueType(eq(id), eq(name), eq(sequence), eq(SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE), eq(description), eq(iconurl));
    }

    @Test
    public void testCreateSubTaskIssueTypeAlreadyExists()
    {
        String name = "sub-task issue type name";
        Long sequence = new Long(1);
        String description = "sub-task issue type description";
        String iconurl = "sub-task issue type icon url";

        when(mockConstantsManager.constantExists(eq("IssueType"), eq(name))).thenReturn(Boolean.TRUE);

        try
        {
            defaultSubTaskManager.createSubTaskIssueType(name, sequence, description, iconurl);
            Assert.fail("Create exceoption should have been thrown.");
        }
        catch (CreateException e)
        {
            Assert.assertEquals("Issue Type with name '" + name + "' already exists.", e.getMessage());
        }
    }

    @Test
    public void testRemoveSubTaskIssueTypeDoesNotExist()
    {
        String name = "sub-task issue type name";

        when(mockConstantsManager.getIssueConstantByName(eq("IssueType"), eq(name))).thenReturn(null);

        try
        {
            defaultSubTaskManager.removeSubTaskIssueType(name);
            Assert.fail("Remove exception should have been thrown.");
        }
        catch (RemoveException e)
        {
            Assert.assertEquals("Issue Type with name '" + name + "' does not exist.", e.getMessage());
        }
    }

    @Test
    public void testRemoveSubTaskIssueTypeNotASubTask()
    {
        String name = "Bug";

        final IssueConstant issueType = new MockIssueType("Bug", name, false);

        when(mockConstantsManager.getIssueConstantByName(eq("IssueType"), eq(name))).thenReturn(issueType);

        try
        {
            defaultSubTaskManager.removeSubTaskIssueType(name);
            Assert.fail("Remove exception should have been thrown.");
        }
        catch (RemoveException e)
        {
            Assert.assertEquals("Issue Type with name '" + name + "' is not a sub-task issue type.", e.getMessage());
        }

    }

    @Test
    public void testRemoveSubTaskIssueType() throws RemoveException
    {
        String name = "sub-task issue type name";
        String id = "test-id";

        final IssueConstant issueType = new MockIssueType(id, name, true);

        when(mockConstantsManager.getIssueConstantByName(eq("IssueType"), eq(name))).thenReturn(issueType);

        defaultSubTaskManager.removeSubTaskIssueType(name);
        verify(mockConstantsManager, times(1)).removeIssueType(eq(id));
    }

    @Test
    public void testEnableSubTasks() throws CreateException, GenericEntityException, StoreException
    {
        when(mockConstantsManager.getSubTaskIssueTypeObjects()).thenReturn(Collections.EMPTY_LIST);
        when(mockConstantsManager.constantExists(eq("IssueType"), eq("Sub-task"))).thenReturn(Boolean.FALSE);
        when(mockConstantsManager.createIssueType(eq("Sub-task"), eq(new Long(0)), eq(SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE), eq("The sub-task of the issue"), eq("/images/icons/issuetypes/subtask_alternate.png"))).thenReturn(new MockGenericValue("IssueType", FieldMap.build("id", "1")));
        when(mockIssueLinkTypeManager.getIssueLinkTypesByStyle(eq("jira_subtask"))).thenReturn(Collections.EMPTY_LIST);

        defaultSubTaskManager = getDefaultSubTaskManager(applicationProperties);

        defaultSubTaskManager.enableSubTasks();
        verify(mockConstantsManager).createIssueType(eq("Sub-task"), eq(new Long(0)), eq(SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE), eq("The sub-task of the issue"), eq("/images/icons/issuetypes/subtask_alternate.png"));
        verify(mockIssueLinkTypeManager).createIssueLinkType(eq("jira_subtask_link"), eq("jira_subtask_outward"), eq("jira_subtask_inward"), eq("jira_subtask"));
        verify(applicationProperties).setOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS, true);
    }

    @Test
    public void testEnableSubTasksSubTaskIssueTypeExists()
            throws CreateException, GenericEntityException, StoreException
    {
        final IssueType issueType = mock(IssueType.class);
        final IssueLinkType issueLinkType = mock(IssueLinkType.class);
        when(mockConstantsManager.getSubTaskIssueTypeObjects()).thenReturn(Lists.<IssueType>newArrayList(issueType));
        when(mockIssueLinkTypeManager.getIssueLinkTypesByStyle(eq("jira_subtask"))).thenReturn(Lists.<IssueLinkType>newArrayList(issueLinkType));

        defaultSubTaskManager = getDefaultSubTaskManager(applicationProperties);
        defaultSubTaskManager.enableSubTasks();
        verify(mockConstantsManager, times(0)).createIssueType(eq("Sub-task"), eq(new Long(0)), eq(SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE), eq("The sub-task of the issue"), eq("/images/icons/issuetypes/subtask_alternate.png"));
        verify(mockIssueLinkTypeManager, times(0)).createIssueLinkType(eq("jira_subtask_link"), eq("jira_subtask_outward"), eq("jira_subtask_inward"), eq("jira_subtask"));
        verify(applicationProperties).setOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS, true);
    }

    @Test
    public void testDisableSubTasks()
    {
        defaultSubTaskManager.disableSubTasks();
        verify(applicationProperties).setOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS, false);
    }

    @Test
    public void testIssueTypeExistsByIdNullId()
    {
        try
        {
            defaultSubTaskManager.issueTypeExistsById(null);
            Assert.fail("IllegalArgumentException should have been thrown");
        }
        catch (IllegalArgumentException e)
        {
            Assert.assertEquals("Id cannot be null.", e.getMessage());
        }
    }

    @Test
    public void testIssueTypeExistsById()
    {
        final String id = "1";
        final GenericValue issueType = new MockGenericValue("IssueType", FieldMap.build("id", id, "name", "some other name", "sequence", new Long(1), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));

        when(mockConstantsManager.getIssueType(eq(id))).thenReturn(issueType);
        Assert.assertTrue(defaultSubTaskManager.issueTypeExistsById(id));

        when(mockConstantsManager.getIssueType(eq(id))).thenReturn(null);
        defaultSubTaskManager = getDefaultSubTaskManager();
        Assert.assertFalse(defaultSubTaskManager.issueTypeExistsById(id));
    }

    @Test
    public void testIssueTypeExistsByName()
    {
        String name = "test name";
        when(mockConstantsManager.constantExists(eq("IssueType"), eq(name))).thenReturn(Boolean.FALSE);
        Assert.assertFalse(defaultSubTaskManager.issueTypeExistsByName(name));

        when(mockConstantsManager.constantExists(eq("IssueType"), eq(name))).thenReturn(Boolean.TRUE);
        defaultSubTaskManager = getDefaultSubTaskManager();
        Assert.assertTrue(defaultSubTaskManager.issueTypeExistsByName(name));
    }

    @Test
    public void testMoveSubTaskIssueTypeUpNullId() throws StoreException
    {
        try
        {
            defaultSubTaskManager.moveSubTaskIssueTypeUp(null);
            Assert.fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            Assert.assertEquals("Id cannot be null.", e.getMessage());
        }
    }

    @Test
    public void testMoveSubTaskIssueTypeUp() throws StoreException
    {
        int i = 0;
        final GenericValue issueType = new MockGenericValue("IssueType", FieldMap.build("id", "1", "name", "some name", "sequence", new Long(i++), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));
        final int sequence2 = i++;
        final GenericValue issueType2 = new MockGenericValue("IssueType", FieldMap.build("id", "2", "name", "some other name", "sequence", new Long(sequence2), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));
        final int sequence3 = i++;
        final String id = "3";
        final GenericValue issueType3 = new MockGenericValue("IssueType", FieldMap.build("id", id, "name", "yet another name", "sequence", new Long(sequence3), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));

        when(mockConstantsManager.getEditableSubTaskIssueTypes()).thenReturn(EasyList.build(issueType, issueType2, issueType3));
        when(mockConstantsManager.getIssueType(eq(id))).thenReturn(issueType3);

        defaultSubTaskManager.moveSubTaskIssueTypeUp(id);

        verify(mockConstantsManager).storeIssueTypes(eq(EasyList.build(issueType, issueType3, issueType2)));
        Assert.assertEquals(new Long(sequence2 + 1), issueType2.getLong("sequence"));
        Assert.assertEquals(new Long(sequence3 - 1), issueType3.getLong("sequence"));
    }

    @Test
    public void testMoveSubTaskIssueTypeUpOneIssueType() throws StoreException
    {
        final String id = "1";
        int i = 0;
        final GenericValue issueType = new MockGenericValue("IssueType", FieldMap.build("id", id, "name", "some name", "sequence", new Long(i), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));

        when(mockConstantsManager.getEditableSubTaskIssueTypes()).thenReturn(EasyList.build(issueType));
        when(mockConstantsManager.getIssueType(eq(id))).thenReturn(issueType);

        defaultSubTaskManager.moveSubTaskIssueTypeUp(id);

        verify(mockConstantsManager).storeIssueTypes(eq(EasyList.build(issueType)));
        Assert.assertEquals(new Long(i), issueType.getLong("sequence"));
    }

    @Test
    public void testMoveSubTaskIssueTypeDownNullId() throws StoreException
    {
        try
        {
            defaultSubTaskManager.moveSubTaskIssueTypeDown(null);
            Assert.fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            Assert.assertEquals("Id cannot be null.", e.getMessage());
        }
    }

    @Test
    public void testMoveSubTaskIssueTypeDown() throws StoreException
    {
        int i = 0;
        final GenericValue issueType = new MockGenericValue("IssueType", FieldMap.build("id", "1", "name", "some name", "sequence", new Long(i++)));
        final int sequence2 = i++;
        String id = "2";
        final GenericValue issueType2 = new MockGenericValue("IssueType", FieldMap.build("id", id, "name", "some other name", "sequence", new Long(sequence2)));
        final int sequence3 = i++;
        final GenericValue issueType3 = new MockGenericValue("IssueType", FieldMap.build("id", "3", "name", "yet another name", "sequence", new Long(sequence3)));

        when(mockConstantsManager.getEditableSubTaskIssueTypes()).thenReturn(EasyList.build(issueType, issueType2, issueType3));
        when(mockConstantsManager.getIssueType(eq(id))).thenReturn(issueType2);

        defaultSubTaskManager.moveSubTaskIssueTypeDown(id);

        verify(mockConstantsManager).storeIssueTypes(eq(EasyList.build(issueType, issueType3, issueType2)));
        Assert.assertEquals(new Long(sequence2 + 1), issueType2.getLong("sequence"));
        Assert.assertEquals(new Long(sequence3 - 1), issueType3.getLong("sequence"));
    }

    @Test
    public void testMoveSubTaskIssueTypeDownOneIssueType() throws StoreException
    {
        final String id = "1";
        int i = 0;
        final GenericValue issueType = new MockGenericValue("IssueType", FieldMap.build("id", id, "name", "some name", "sequence", new Long(i), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));

        when(mockConstantsManager.getEditableSubTaskIssueTypes()).thenReturn(EasyList.build(issueType));
        when(mockConstantsManager.getIssueType(eq(id))).thenReturn(issueType);

        defaultSubTaskManager.moveSubTaskIssueTypeDown(id);

        verify(mockConstantsManager).storeIssueTypes(eq(EasyList.build(issueType)));
        Assert.assertEquals(new Long(i), issueType.getLong("sequence"));
    }

    @Test
    public void testGetSubTaskIssueTypeByIdNullId()
    {
        try
        {
            defaultSubTaskManager.getSubTaskIssueTypeById(null);
            Assert.fail("IllegalArgumentException should have bben thrown.");
        }
        catch (IllegalArgumentException e)
        {
            Assert.assertEquals("Id cannot be null.", e.getMessage());
        }
    }

    @Test
    public void testGetSubTaskIssueTypeByIdIssueTypeDoesNotExist()
    {
        final String id = "1";
        when(mockConstantsManager.getIssueType(eq(id))).thenReturn(null);
        final GenericValue subTaskIssueType = defaultSubTaskManager.getSubTaskIssueTypeById(id);
        Assert.assertNull(subTaskIssueType);
    }

    @Test
    public void testGetSubTaskIssueTypeByIdIssueTypeIsNotSubTaskIssueType()
    {
        // Test that the manager detects that the issue type is not an sub-task issue type
        final String id = "1";
        final GenericValue issueTypeGV = new MockGenericValue("IssueType", FieldMap.build("id", id, "name", "some name"));
        try
        {
            when(mockConstantsManager.getIssueType(eq(id))).thenReturn(issueTypeGV);
            defaultSubTaskManager.getSubTaskIssueTypeById(id);
            Assert.fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            Assert.assertEquals("The issue type with id '" + id + "' is not a sub-task issue type.", e.getMessage());
        }
    }

    @Test
    public void testGetSubTaskIssueTypeById()
    {
        // Test that the manager detects that the issue type is not an sub-task issue type
        final String id = "1";
        final String name = "some name";
        final GenericValue issueTypeGV = new MockGenericValue("IssueType", FieldMap.build("id", id, "name", name, "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE, "sequence", new Long(0)));

        when(mockConstantsManager.getIssueType(eq(id))).thenReturn(issueTypeGV);
        final GenericValue subTaskIssueTypeGV = defaultSubTaskManager.getSubTaskIssueTypeById(id);
        Assert.assertNotNull(subTaskIssueTypeGV);
        Assert.assertEquals(id, subTaskIssueTypeGV.getString("id"));
        Assert.assertEquals(name, subTaskIssueTypeGV.getString("name"));
        Assert.assertEquals(SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE, subTaskIssueTypeGV.getString("style"));
    }

    @Test
    public void testGetSubTaskIssueTypeObjects()
    {
        final List expected = EasyList.build("test 1", "test 2");
        when(mockConstantsManager.getSubTaskIssueTypeObjects()).thenReturn(expected);
        Assert.assertEquals(expected, defaultSubTaskManager.getSubTaskIssueTypeObjects());
    }

    @Test
    public void testIsSubtask()
    {
        // Test false
        when(mockIssueLinkManager.getInwardLinks(eq(issue.getLong("id")))).thenReturn(Collections.EMPTY_LIST);
        Assert.assertFalse(defaultSubTaskManager.isSubTask(issue));

        // Test True
        MockGenericValue issueLinkTypeGV = new MockGenericValue("IssueLinkType", FieldMap.build("id", new Long(1), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));
        when(mockIssueLinkTypeManager.getIssueLinkType(eq(issueLinkTypeGV.getLong("id")))).thenReturn(new IssueLinkTypeImpl(issueLinkTypeGV));
        MockGenericValue issueLinkGV = new MockGenericValue("IssueLink", FieldMap.build("linktype", issueLinkTypeGV.getLong("id"), "source", new Long(10000)));
        when(mockIssueLinkManager.getInwardLinks(eq(issue.getLong("id")))).thenReturn(Lists.<IssueLink>newArrayList(new IssueLinkImpl(issueLinkGV, mockIssueLinkTypeManager, null)));
        defaultSubTaskManager = getDefaultSubTaskManager();
        Assert.assertTrue(defaultSubTaskManager.isSubTask(issue));
    }

    @Test
    public void testIsSubTaskIssueType()
    {
        Assert.assertFalse(defaultSubTaskManager.isSubTaskIssueType(new MockGenericValue("IssueType")));
        Assert.assertFalse(defaultSubTaskManager.isSubTaskIssueType(new MockGenericValue("IssueType", FieldMap.build("description", "no style"))));
        Assert.assertFalse(defaultSubTaskManager.isSubTaskIssueType(new MockGenericValue("IssueType", FieldMap.build("style", null))));
        Assert.assertFalse(defaultSubTaskManager.isSubTaskIssueType(new MockGenericValue("IssueType", FieldMap.build("style", ""))));
        Assert.assertFalse(defaultSubTaskManager.isSubTaskIssueType(new MockGenericValue("IssueType", FieldMap.build("style", "invalid"))));

        Assert.assertTrue(defaultSubTaskManager.isSubTaskIssueType(new MockGenericValue("IssueType", FieldMap.build("style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE))));
    }

    @Test
    public void testGetParentIssueIdNoParent()
    {
        when(mockIssueLinkManager.getInwardLinks(eq(issue.getLong("id")))).thenReturn(Collections.EMPTY_LIST);
        Assert.assertNull(defaultSubTaskManager.getParentIssueId(issue));
    }

    @Test
    public void testGetParentIssue()
    {
        MockGenericValue issueLinkTypeGV = new MockGenericValue("IssueLinkType", FieldMap.build("id", new Long(1), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));
        when(mockIssueLinkTypeManager.getIssueLinkType(eq(issueLinkTypeGV.getLong("id")))).thenReturn(new IssueLinkTypeImpl(issueLinkTypeGV));
        final Long parentIssueId = new Long(10000);
        MockGenericValue issueLinkGV = new MockGenericValue("IssueLink", FieldMap.build("linktype", issueLinkTypeGV.getLong("id"), "source", parentIssueId));
        when(mockIssueLinkManager.getInwardLinks(eq(issue.getLong("id")))).thenReturn(Lists.<IssueLink>newArrayList(new IssueLinkImpl(issueLinkGV, mockIssueLinkTypeManager, null)));
        Assert.assertEquals(parentIssueId, defaultSubTaskManager.getParentIssueId(issue));
    }

    @Test
    public void testGetSubTaskBean()
    {
        setupSubTasks(issue);

        when(mockPermissionManager.hasPermission(eq(new Integer(Permissions.BROWSE)), any(Issue.class), isNull(User.class))).thenReturn(Boolean.TRUE);
        final SubTaskBean subTaskBean = defaultSubTaskManager.getSubTaskBean(issue, null);
        final Collection subTasks = subTaskBean.getSubTasks(SubTaskBean.SUB_TASK_VIEW_ALL);
        Assert.assertEquals(2, subTasks.size());
    }

    private List setupSubTasks(final GenericValue issue)
    {
        issueLinkTypeGV = new MockGenericValue("IssueLinkType", FieldMap.build("id", new Long(1), "style", SubTaskManager.SUB_TASK_ISSUE_TYPE_STYLE));
        when(mockIssueLinkTypeManager.getIssueLinkType(eq(issueLinkTypeGV.getLong("id")))).thenReturn(new IssueLinkTypeImpl(issueLinkTypeGV));

        final GenericValue subTaskIssue1 = new MockGenericValue("Issue", FieldMap.build("id", Long.valueOf(generatedId++),"summary", "test summary"));
        final GenericValue subTaskIssue2 = new MockGenericValue("Issue", FieldMap.build("id", Long.valueOf(generatedId++),"summary", "test summary"));
        when(issueFactory.getIssue(eq(subTaskIssue1))).thenReturn(new MockIssue(subTaskIssue1));
        when(issueFactory.getIssue(eq(subTaskIssue2))).thenReturn(new MockIssue(subTaskIssue2));


        MockGenericValue issueLinkGV1 = new MockGenericValue("IssueLink", FieldMap.build("linktype", issueLinkTypeGV.getLong("id"), "destination", subTaskIssue1.getLong("id")));
        MockGenericValue issueLinkGV2 = new MockGenericValue("IssueLink", FieldMap.build("linktype", issueLinkTypeGV.getLong("id"), "destination", subTaskIssue2.getLong("id")));

        MockIssueManager mockIssueManager = new MockIssueManager();
        mockIssueManager.addIssue(subTaskIssue1);
        mockIssueManager.addIssue(subTaskIssue2);

        final List issueLinks = EasyList.build(new IssueLinkImpl(issueLinkGV1, mockIssueLinkTypeManager, mockIssueManager), new IssueLinkImpl(issueLinkGV2, mockIssueLinkTypeManager, mockIssueManager));
        when(mockIssueLinkManager.getOutwardLinks(eq(issue.getLong("id")))).thenReturn(issueLinks);
        return issueLinks;
    }

    @Test
    public void testMoveSubTask()
    {
        final List expectedIssueLinks = setupSubTasks(issue);

        Long currentSequence = new Long(0);
        Long sequence = new Long(1);

        defaultSubTaskManager.moveSubTask(issue, currentSequence, sequence);
        verify(mockIssueLinkManager).moveIssueLink(eq(expectedIssueLinks), eq(currentSequence), eq(sequence));
    }

    @Test
    public void testResetSequences()
    {
        final List subTaskIssueLinks = setupSubTasks(issue);

        defaultSubTaskManager.resetSequences(MockIssueFactory.createIssue(issue.getLong("id")));
        verify(mockIssueLinkManager).resetSequences(eq(subTaskIssueLinks));
    }

    @Test
    public void testCreateSubTaskIssueLinkWithNulls() throws CreateException
    {
        try
        {
            defaultSubTaskManager.createSubTaskIssueLink((GenericValue) null, null, null);
            Assert.fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            Assert.assertEquals("Parent Issue cannot be null.", e.getMessage());
        }

        try
        {
            defaultSubTaskManager.createSubTaskIssueLink(issue, null, null);
            Assert.fail("IllegalArgumentException should have been thrown.");
        }
        catch (IllegalArgumentException e)
        {
            Assert.assertEquals("Sub-Task Issue cannot be null.", e.getMessage());
        }
    }

    @Test
    public void testCreateSubTaskIssueLinkException()
            throws OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final GenericValue subTaskIssue = new MockGenericValue("Issue", FieldMap.build("summary", "sub task test issue"));

        final Long subTaskIssueLinkTypeId = new Long(9879);
        issueLinkTypeGV = new MockGenericValue("IssueLinkType", FieldMap.build("id", subTaskIssueLinkTypeId));
        when(mockIssueLinkTypeManager.getIssueLinkTypesByStyle(eq(SubTaskManager.SUB_TASK_LINK_TYPE_STYLE))).thenReturn(Lists.<IssueLinkType>newArrayList(new IssueLinkTypeImpl(issueLinkTypeGV)));
        when(mockIssueLinkManager.getOutwardLinks(eq(issue.getLong("id")))).thenReturn(Collections.EMPTY_LIST);
        User testUser = new MockUser("testUser");
        final String expectedMessage = "test exception";
        try
        {
            doThrow(new CreateException(expectedMessage)).when(mockIssueLinkManager).createIssueLink(eq(issue.getLong("id")), eq(subTaskIssue.getLong("id")), eq(subTaskIssueLinkTypeId), eq(new Long(0)), eq(testUser));
            defaultSubTaskManager.createSubTaskIssueLink(issue, subTaskIssue, testUser);
            Assert.fail("CreateException should have been thrown.");
        }
        catch (CreateException e)
        {
            Assert.assertEquals(expectedMessage, e.getMessage());
        }
    }

    @Test
    public void testCreateSubTaskIssueLinkFirstSubTaskLink()
            throws CreateException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final GenericValue subTaskIssue = new MockGenericValue("Issue", FieldMap.build("summary", "sub task test issue"));

        final Long subTaskIssueLinkTypeId = new Long(9879);
        issueLinkTypeGV = new MockGenericValue("IssueLinkType", FieldMap.build("id", subTaskIssueLinkTypeId));
        when(mockIssueLinkTypeManager.getIssueLinkTypesByStyle(eq(SubTaskManager.SUB_TASK_LINK_TYPE_STYLE))).thenReturn(Lists.<IssueLinkType>newArrayList(new IssueLinkTypeImpl(issueLinkTypeGV)));
        when(mockIssueLinkManager.getOutwardLinks(eq(issue.getLong("id")))).thenReturn(Collections.EMPTY_LIST);
        User testUser = new MockUser("testUser");

        defaultSubTaskManager.createSubTaskIssueLink(issue, subTaskIssue, testUser);
        verify(mockIssueLinkManager).createIssueLink(eq(issue.getLong("id")), eq(subTaskIssue.getLong("id")), eq(subTaskIssueLinkTypeId), eq(new Long(0)), eq(testUser));
    }

    @Test
    public void testCreateSubTaskIssueLink()
            throws CreateException, OperationNotPermittedException, InvalidUserException, InvalidCredentialException
    {
        final GenericValue subTaskIssue = new MockGenericValue("Issue", FieldMap.build("summary", "sub task test issue"));

        final List subTaskIssueLinks = setupSubTasks(issue);

        User testUser = new MockUser("testUser");
        when(mockIssueLinkTypeManager.getIssueLinkTypesByStyle(eq(SubTaskManager.SUB_TASK_LINK_TYPE_STYLE))).thenReturn(Lists.<IssueLinkType>newArrayList(new IssueLinkTypeImpl(issueLinkTypeGV)));
        when(mockIssueLinkManager.getOutwardLinks(eq(issue.getLong("id")))).thenReturn(subTaskIssueLinks);

        defaultSubTaskManager.createSubTaskIssueLink(issue, subTaskIssue, testUser);
        verify(mockIssueLinkManager).createIssueLink(eq(issue.getLong("id")), eq(subTaskIssue.getLong("id")), eq(issueLinkTypeGV.getLong("id")), eq(new Long(subTaskIssueLinks.size())), eq(testUser));
    }

    @Test
    public void testGetAllSubTaskIssueIds()
    {
        final List issueLinks = setupSubTasks(issue);
        final GenericValue issue2 = new MockGenericValue("Issue", FieldMap.build("summary", "another issue"));
        issueLinks.addAll(setupSubTasks(issue2));

        final IssueLinkType issueLinkType = new IssueLinkTypeImpl(issueLinkTypeGV);
        when(mockIssueLinkTypeManager.getIssueLinkTypesByStyle(eq(SubTaskManager.SUB_TASK_LINK_TYPE_STYLE))).thenReturn(EasyList.build(issueLinkType));
        when(mockIssueLinkManager.getIssueLinks(eq(issueLinkType.getId()))).thenReturn(issueLinks);
        final Collection result = defaultSubTaskManager.getAllSubTaskIssueIds();
        Assert.assertEquals(4, result.size());
        // As the order does not matter check for presence of ids
        for (Iterator iterator = issueLinks.iterator(); iterator.hasNext();)
        {
            IssueLink issueLink = (IssueLink) iterator.next();
            Assert.assertTrue(result.contains(issueLink.getDestinationId()));
        }
    }

    @Test
    /**
     * This tests JRA-10546
     *
     * @throws CreateException
     * @throws RemoveException
     */
    public void testChangeParentUpdatesSecurityLevel() throws CreateException, RemoveException
    {
        JiraMockGenericValue subtaskIssueGv = new JiraMockGenericValue("Issue", FieldMap.build("key", "HSP-12", "security", null));
        Long securityLevelId = new Long(10000);
        final JiraMockGenericValue parentIssueGv = new JiraMockGenericValue("Issue", FieldMap.build("security", securityLevelId, "key", "HSP-11"));

        when(mockIssueLinkManager.getInwardLinks(any(Long.class))).thenReturn(Collections.EMPTY_LIST);

        DefaultSubTaskManager ditm = new DefaultSubTaskManager(mockConstantsManager, mockIssueLinkTypeManager, mockIssueLinkManager,
                mockPermissionManager, applicationProperties, new CollectionReorderer(), mockIssueTypeSchemeManager, mockIssueManager)
        {
            @Override
            public GenericValue getParentIssue(GenericValue subtask)
            {
                return parentIssueGv;
            }

            @Override
            public void createSubTaskIssueLink(GenericValue parentIssue, GenericValue subTaskIssue, User remoteUser)
                    throws CreateException
            {
                //do nothing
            }

            @Override
            public List<IssueLink> getSubTaskIssueLinks(final Long issueId)
            {
                return null;
            }

        };
        IssueUpdateBean issueUpdateBean = ditm.changeParent(subtaskIssueGv, parentIssueGv, null);
        Assert.assertEquals(securityLevelId, issueUpdateBean.getChangedIssue().get("security"));
        verify(mockIssueLinkManager, times(2)).resetSequences(any(List.class));
    }

    @Test
    /**
     * This tests JRA-10546
     *
     * @throws CreateException
     * @throws RemoveException
     */
    public void testChangeParentSetsSecurityLevelToNull() throws CreateException, RemoveException
    {
        Long securityLevelId = new Long(10000);
        JiraMockGenericValue subtaskIssueGv = new JiraMockGenericValue("Issue", FieldMap.build("key", "HSP-12", "security", securityLevelId));
        final JiraMockGenericValue parentIssueGv = new JiraMockGenericValue("Issue", FieldMap.build("key", "HSP-11"));

        when(mockIssueLinkManager.getInwardLinks(any(Long.class))).thenReturn(Collections.EMPTY_LIST);

        DefaultSubTaskManager ditm = new DefaultSubTaskManager(mockConstantsManager, mockIssueLinkTypeManager,  mockIssueLinkManager,
                mockPermissionManager, applicationProperties, new CollectionReorderer(), mockIssueTypeSchemeManager, mockIssueManager)
        {
            public GenericValue getParentIssue(GenericValue subtask)
            {
                return parentIssueGv;
            }

            public void createSubTaskIssueLink(GenericValue parentIssue, GenericValue subTaskIssue, User remoteUser)
                    throws CreateException
            {
                //do nothing
            }

            @Override
            public List<IssueLink> getSubTaskIssueLinks(final Long issueId)
            {
                return null;
            }

        };

        IssueUpdateBean issueUpdateBean = ditm.changeParent(subtaskIssueGv, parentIssueGv, null);
        Assert.assertNull(issueUpdateBean.getChangedIssue().get("security"));
        verify(mockIssueLinkManager, times(2)).resetSequences(any(List.class));
    }

    /**
     * This should really live in atlassian-ofbiz.
     */
    public class JiraMockGenericValue extends MockGenericValue
    {

        public JiraMockGenericValue(String entityName, Map fields)
        {
            super(entityName, fields);
        }

        public Set entrySet()
        {
            return getAllFields().entrySet();
        }

        public Set keySet()
        {
            return getAllFields().keySet();
        }

        public int size()
        {
            return getAllFields().size();
        }

        public boolean isEmpty()
        {
            return getAllFields().isEmpty();
        }

        public Collection values()
        {
            return getAllFields().values();
        }

        public Object clone()
        {
            return new JiraMockGenericValue(entityName, new HashMap(getAllFields()));
        }
    }
}
