/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
package com.atlassian.jira.web.action.issue;

import java.util.HashMap;
import java.util.Map;

import javax.servlet.http.HttpServletRequest;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.issue.MockIssue;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.servlet.MockHttpServletRequest;
import com.atlassian.jira.mock.servlet.MockHttpSession;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.UserIssueHistoryManager;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.issue.enterprise.MoveIssueUpdateWorkflow;
import com.atlassian.jira.web.bean.MockI18nBean;
import com.atlassian.jira.web.bean.MoveIssueBean;
import com.atlassian.jira.web.util.AuthorizationSupport;
import com.atlassian.jira.workflow.WorkflowManager;

import org.junit.After;
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

import webwork.action.ActionContext;

import static org.mockito.Mockito.when;

@RunWith(MockitoJUnitRunner.class)
public class TestMoveIssueUpdateWorkflow
{
    MoveIssueUpdateWorkflow miuw;

    @Mock @AvailableInContainer
    private SubTaskManager subtaskManager;
    @Mock @AvailableInContainer
    private PermissionManager permissionManager;
    @Mock @AvailableInContainer
    private ConstantsManager constantsManager;
    @Mock @AvailableInContainer
    private WorkflowManager workflowManager;
    @Mock @AvailableInContainer
    private FieldLayoutManager fieldLayoutManager;
    @Mock @AvailableInContainer
    private ProjectManager projectManager;
    @Mock @AvailableInContainer
    private IssueManager issueManager;
    @Mock @AvailableInContainer
    private UserIssueHistoryManager userIssueHistoryManager;
    @Mock @AvailableInContainer
    private AuthorizationSupport authorizationSupport;

    private GenericValue issue1;
    private User testUser;
    private GenericValue project1;

    @AvailableInContainer
    @Mock
    private JiraAuthenticationContext authContext;
    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {
        when(authContext.getI18nHelper()).thenReturn(new MockI18nBean());
        HttpServletRequest request = new MockHttpServletRequest(new MockHttpSession());
        ActionContext.setRequest(request);

        // Cant mock out IssueManager
        issue1 = new MockGenericValue("Issue", FieldMap.build("summary", "test source summary", "key", "TST-1", "id", new Long(10001), "project", new Long(1)));
        final MockIssue mockIssue = new MockIssue(issue1);
        when(issueManager.getIssueObject(new Long(10001))).thenReturn(mockIssue);

        miuw = new MoveIssueUpdateWorkflow(subtaskManager, constantsManager, workflowManager, null, fieldLayoutManager, null, null, null, null);

        MockApplicationUser testApplicationUser = new MockApplicationUser("Test User");
        testUser =  testApplicationUser.getDirectoryUser();
        when(authContext.getLoggedInUser()).thenReturn(testUser);
        when(authContext.getUser()).thenReturn(testApplicationUser);
        when(permissionManager.hasPermission(Permissions.BROWSE, mockIssue, testApplicationUser)).thenReturn(true);
        when(authorizationSupport.hasIssuePermission(Permissions.MOVE_ISSUE, mockIssue)).thenReturn(true);

        project1 = new MockGenericValue("Project", FieldMap.build("id", new Long(1)));
    }

    private MoveIssueBean setupMoveIssueBean()
    {
        MoveIssueBean moveIssueBean = new MoveIssueBean(constantsManager, projectManager);
        ActionContext.getSession().put(SessionKeys.MOVEISSUEBEAN, moveIssueBean);
        return moveIssueBean;
    }

    @Test
    public void testDoValidationNoStatusId() throws GenericEntityException, CreateException
    {
        Map sessionMap = new HashMap();
        ActionContext.setSession(sessionMap);
        setupMoveIssueBean();

        miuw.setId(issue1.getLong("id"));
        miuw.doValidation();

        Assert.assertTrue(miuw.getHasErrorMessages());
        Assert.assertEquals(1, miuw.getErrorMessages().size());
    }

    @Test
    public void testDoValidation() throws GenericEntityException, CreateException
    {
        Map sessionMap = new HashMap();
        ActionContext.setSession(sessionMap);
        MoveIssueBean moveIssueBean = setupMoveIssueBean();

        miuw.setId(issue1.getLong("id"));
        moveIssueBean.setTargetStatusId("1");
        miuw.doValidation();

        Assert.assertFalse(miuw.getHasErrorMessages());
    }

    @After
    public void tearDown() throws Exception
    {
        HttpServletRequest request = new MockHttpServletRequest(new MockHttpSession());
        ActionContext.setRequest(null);
    }

}
