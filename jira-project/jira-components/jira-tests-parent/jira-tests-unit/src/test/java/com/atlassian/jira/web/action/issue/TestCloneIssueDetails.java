/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.web.action.issue;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationPropertiesImpl;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.TemporaryAttachmentsMonitorLocator;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeImpl;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.i18n.MockI18nHelper;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.plugin.report.impl.MockPermissionManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;

import com.google.common.collect.Lists;
import com.opensymphony.workflow.loader.ActionDescriptor;
import com.opensymphony.workflow.loader.WorkflowDescriptor;

import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.Mockito;
import org.mockito.runners.MockitoJUnitRunner;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import webwork.action.Action;

import static org.mockito.Matchers.anyLong;
import static org.mockito.Matchers.eq;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.spy;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.any;
import static org.mockito.Mockito.anyString;

@RunWith(MockitoJUnitRunner.class)
public class TestCloneIssueDetails
{
    GenericValue originalIssue;
    Map issueParamsMap;
    GenericValue componentGV;
    String[] components;

    GenericValue cloneLink;
    @Mock @AvailableInContainer
    IssueLinkTypeManager mockIssueLinkTypeManager;
    @Mock @AvailableInContainer
    SubTaskManager mockSubTaskManager;
    @Mock @AvailableInContainer
    PermissionManager myPermissionManager;
    @Mock
    IssueManager mockIssueManager;
    @Mock @AvailableInContainer
    ApplicationPropertiesImpl applicationProperties;
    @Mock @AvailableInContainer
    TemporaryAttachmentsMonitorLocator temporaryAttachmentsMonitorLocator;
    @Mock
    IssueService mockIssueService;
    @Mock
    IssueFactory mockIssueFactory;
    @Mock @AvailableInContainer
    CustomFieldManager mockCustomFieldManager;
    @Mock @AvailableInContainer
    WorkflowManager mockWorkflowManager;
    @Mock @AvailableInContainer
    Logger mockLogger;

    @Mock @AvailableInContainer
    private JiraAuthenticationContext authContext;

    @Rule
    public RuleChain ruleChain = MockitoMocksInContainer.forTest(this);

    @Before
    public void setUp() throws Exception
    {

        // Tests fail due to unambiguous definition of IssueIndexManager in VersionManager wihtout a refreshed container
        myPermissionManager = new MyPermissionManager();
        applicationProperties = new MyApplicationProperties("Clones");

        cloneLink = new MockGenericValue("IssueLinkType", EasyMap.build("linkname", "Clones", "inward", "clones", "outward", "is cloned by"));

        issueParamsMap = new HashMap();
        issueParamsMap.put("project", new Long(1));
        issueParamsMap.put("type", "test-type");
        issueParamsMap.put("environment", "test-env");
        issueParamsMap.put("description", "test-desc");
        issueParamsMap.put("reporter", "test-reporter");
        issueParamsMap.put("assignee", "test-assignee");
        issueParamsMap.put("summary", "test-summary");
        issueParamsMap.put("priority", "test-priority");
        issueParamsMap.put("security", new Long(1));
        issueParamsMap.put("key", "TST-1");
        issueParamsMap.put("id", new Long(1));

        originalIssue = new MockGenericValue("Issue", issueParamsMap);

        componentGV = new MockGenericValue("Component", EasyMap.build("id", new Long(1)));
        components = new String[]{componentGV.getString("id")};

        when(authContext.getI18nHelper()).thenReturn(new MockI18nHelper());
    }

    @Test
    public void testDisplayWarningWithCloneName() throws GenericEntityException
    {
        when(mockIssueLinkTypeManager.getIssueLinkTypesByName(eq("Clones"))).thenReturn(Collections.EMPTY_LIST);
        applicationProperties = new MyApplicationProperties("Clones");
        CloneIssueDetails cloneIssueDetails = getCloneIssueDetails(applicationProperties);
        Assert.assertTrue(cloneIssueDetails.isDisplayCloneLinkWarning());
    }

    @Test
    public void testDisplayWarningWithoutCloneName() throws GenericEntityException
    {
        applicationProperties = new MyApplicationProperties("");
        CloneIssueDetails cloneIssueDetails = getCloneIssueDetails(applicationProperties);
        Assert.assertFalse(cloneIssueDetails.isDisplayCloneLinkWarning());
    }

    @Test
    public void testDisplayWarningWithCloneNameThatExists() throws GenericEntityException
    {
        when(mockIssueLinkTypeManager.getIssueLinkTypesByName(eq("Clones"))).thenReturn(Lists.<IssueLinkType>newArrayList(new IssueLinkTypeImpl(cloneLink)));
        applicationProperties = new MyApplicationProperties("Clones");
        CloneIssueDetails cloneIssueDetails = getCloneIssueDetails(applicationProperties);
        Assert.assertFalse(cloneIssueDetails.isDisplayCloneLinkWarning());
    }

    @Test
    public void testShouldStopOperationImmediately_WhenFailingAtCreateIssueTransition() throws Exception
    {
        givenFailureToCreateNewIssueForCloning();
        CloneIssueDetails cloneIssueDetails = spy(getCloneIssueDetails(null));
        cloneIssueDetails.setOriginalIssue(originalIssueWithNeededInformationToBeCloned());

        String actualActionAfterCloning = cloneIssueDetails.doExecute();

        verify(cloneIssueDetails, never()).cloneIssue();
        Assert.assertEquals(Action.ERROR, actualActionAfterCloning);
    }

    private void givenFailureToCreateNewIssueForCloning()
    {
        JiraWorkflow mockJiraWorkflow = mock(JiraWorkflow.class);
        WorkflowDescriptor mockWorkflowDescriptor = mock(WorkflowDescriptor.class);
        List<ActionDescriptor> mockInitialActions = new ArrayList<ActionDescriptor>(1);
        mockInitialActions.add(mock(ActionDescriptor.class));
        when(mockWorkflowDescriptor.getInitialActions()).thenReturn(mockInitialActions);
        when(mockJiraWorkflow.getDescriptor()).thenReturn(mockWorkflowDescriptor);
        when(mockWorkflowManager.getWorkflow(anyLong(), anyString())).thenReturn(mockJiraWorkflow);

        IssueService.IssueResult createIssueForCloningResult = mock(IssueService.IssueResult.class);
        when(createIssueForCloningResult.isValid()).thenReturn(false);
        ErrorCollection mockError = failureToCreateIssueForCloningError();
        when(createIssueForCloningResult.getErrorCollection()).thenReturn(mockError);
        when(mockIssueService.create(any(User.class), any(IssueService.CreateValidationResult.class), anyString())).thenReturn(createIssueForCloningResult);
    }

    private static ErrorCollection failureToCreateIssueForCloningError()
    {
        ErrorCollection failureToCreateIssueForCloningError = mock(ErrorCollection.class);
        when(failureToCreateIssueForCloningError.getErrorMessages()).thenReturn(Arrays.asList("This operation is intentionally forced to be fail"));

        Map<String, String> forcedToBeFailError = new HashMap<String, String>();
        forcedToBeFailError.put("forced.to.be.fail.error", "This operation is intentionally forced to be fail");
        when(failureToCreateIssueForCloningError.getErrors()).thenReturn(forcedToBeFailError);
        when(failureToCreateIssueForCloningError.getReasons()).thenReturn(Collections.EMPTY_SET);
        return failureToCreateIssueForCloningError;
    }

    private Issue originalIssueWithNeededInformationToBeCloned()
    {
        Issue originalIssueToBeCloned = mock(MutableIssue.class);
        when(originalIssueToBeCloned.getOriginalEstimate()).thenReturn(1L);
        when(originalIssueToBeCloned.getFixVersions()).thenReturn(Collections.EMPTY_LIST);
        when(originalIssueToBeCloned.getAffectedVersions()).thenReturn(Collections.EMPTY_LIST);
        when(originalIssueToBeCloned.getId()).thenReturn(1L);

        Project originalProjectObject = mock(Project.class);
        when(originalProjectObject.getId()).thenReturn(1L);
        when(originalIssueToBeCloned.getProjectObject()).thenReturn(originalProjectObject);

        IssueType originalIssueType = mock(IssueType.class);
        when(originalIssueType.getId()).thenReturn("issue1");
        when(originalIssueToBeCloned.getIssueTypeObject()).thenReturn(originalIssueType);
        when(mockIssueFactory.cloneIssue(any(Issue.class))).thenReturn((MutableIssue) originalIssueToBeCloned);
        when(mockIssueFactory.getIssue(any(GenericValue.class))).thenReturn((MutableIssue) originalIssueToBeCloned);
        return originalIssueToBeCloned;
    }

    // ---- Helper Methods & Classes----
    private CloneIssueDetails getCloneIssueDetails(ApplicationPropertiesImpl applicationProperties)
    {
        CloneIssueDetails cloneIssueDetails;
        IssueCreationHelperBean issueCreationHelperBean = getMockIssueCreationHelperBean();
        if (applicationProperties != null)
            cloneIssueDetails = new CloneIssueDetails(applicationProperties, myPermissionManager, null, null, mockIssueLinkTypeManager, mockSubTaskManager, null, null, issueCreationHelperBean, mockIssueFactory, mockIssueService, temporaryAttachmentsMonitorLocator);
        else
            cloneIssueDetails = new CloneIssueDetails(null, myPermissionManager, null, null, mockIssueLinkTypeManager, mockSubTaskManager, null, null, issueCreationHelperBean, mockIssueFactory, mockIssueService, temporaryAttachmentsMonitorLocator);

        return cloneIssueDetails;
    }

    // Mock Application Properties for tests
    private static class MyApplicationProperties extends ApplicationPropertiesImpl
    {
        private final String cloneLinkTypeName;

        public MyApplicationProperties(String cloneLinkTypeName)
        {
            super(null);
            this.cloneLinkTypeName = cloneLinkTypeName;
        }

        public String getDefaultBackedString(String name)
        {
            if (APKeys.JIRA_CLONE_LINKTYPE_NAME.equals(name))
                return cloneLinkTypeName;

            return null;
        }
    }

    private class MyPermissionManager extends MockPermissionManager
    {
        public void addPermission(int permissionsId, GenericValue scheme, String parameter, String securityType) throws CreateException
        {

        }

        public boolean hasPermission(int permissionsId, com.atlassian.crowd.embedded.api.User u)
        {
            return true;
        }

        public boolean hasPermission(int permissionsId, GenericValue entity, com.atlassian.crowd.embedded.api.User u)
        {
            return true;
        }

        public boolean hasPermission(int permissionsId, Issue entity, com.atlassian.crowd.embedded.api.User u)
        {
            return true;
        }

        public boolean hasPermission(int permissionId, GenericValue project, com.atlassian.crowd.embedded.api.User u, boolean issueCreation)
        {
            return true;
        }

        public boolean hasPermission(int permissionsId, Project project, com.atlassian.crowd.embedded.api.User user)
        {
            return true;
        }

        public boolean hasPermission(int permissionsId, Project project, com.atlassian.crowd.embedded.api.User user, boolean issueCreation)
        {
            return true;
        }

        public void removeGroupPermissions(String group) throws RemoveException
        {

        }

        public void removeUserPermissions(String username) throws RemoveException
        {

        }

        public boolean hasProjects(int permissionId, com.atlassian.crowd.embedded.api.User user)
        {
            return false;
        }

        public Collection<Project> getProjectObjects(final int permissionId, final com.atlassian.crowd.embedded.api.User user)
        {
            return null;
        }

        public Collection getProjects(int permissionId, com.atlassian.crowd.embedded.api.User user, GenericValue category)
        {
            return null;
        }

        public Collection<Group> getAllGroups(int permissionId, Project project)
        {
            return null;
        }
    }

    private IssueCreationHelperBean getMockIssueCreationHelperBean()
    {

        UserUtil mockUserUtil = Mockito.mock(UserUtil.class);
        when(mockUserUtil.hasExceededUserLimit()).thenReturn(Boolean.FALSE);
        final JiraLicenseService jiraLicenseService = Mockito.mock(JiraLicenseService.class);
        return new IssueCreationHelperBeanImpl((UserUtil) mockUserUtil, null, null, jiraLicenseService, null);

    }
}
