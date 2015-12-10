package com.atlassian.jira.rest.v2.admin;

import com.atlassian.jira.bc.workflow.WorkflowService;
import com.atlassian.jira.datetime.DateTimeFormatter;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.rest.mock.MockJiraWorkflow;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.workflow.JiraWorkflow;
import com.atlassian.jira.workflow.WorkflowManager;
import com.google.common.base.Function;
import com.google.common.collect.Maps;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import java.util.Arrays;
import java.util.Date;
import java.util.Map;
import javax.ws.rs.core.Response;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.mockito.Mockito.when;

/**
 * @since v5.2
 */
@RunWith(MockitoJUnitRunner.class)
public class WorkflowsResourceTest
{
    private static final ApplicationUser USER_ADMIN = new MockApplicationUser("admin", "Administrator", "admin@admin.com");
    private static final ApplicationUser USER_USER = new MockApplicationUser("user", "User", "user@user.com");
    private static final JiraWorkflow WORKFLOW_JIRA = new MockJiraWorkflow(JiraWorkflow.DEFAULT_WORKFLOW_NAME, "The default JIRA workflow.");
    private static final JiraWorkflow WORKFLOW_TEST = new MockJiraWorkflow("Test Workflow", "").setUpdateAuthor(USER_ADMIN).setUpdatedDate(new Date());

    @Mock
    private WorkflowManager workflowManager;

    @Mock
    private DateTimeFormatter dateTimeFormatter;

    @Mock
    private UserManager userManager;

    @Mock
    private PermissionManager permissionManager;

    @Mock
    private JiraAuthenticationContext jiraAuthenticationContext;

    @Mock
    private WorkflowService workflowService;

    @Mock
    private WorkflowTransitionResource.Factory factory;

    @Test
    public void testGetAllWorkflows()
    {
        @SuppressWarnings ("unchecked")
        Iterable<WorkflowBean> entity = (Iterable<WorkflowBean>)getResponseForUser(USER_ADMIN).getEntity();

        Map<String, WorkflowBean> beans = Maps.uniqueIndex(entity, new Function<WorkflowBean, String>()
        {
            @Override
            public String apply(WorkflowBean input)
            {
                return input.getName();
            }
        });

        assertTrue(beans.containsKey(WORKFLOW_JIRA.getName()));
        WorkflowBean jiraWorkflow = beans.get(WORKFLOW_JIRA.getName());
        assertEquals(WORKFLOW_JIRA.getName(), jiraWorkflow.getName());
        assertEquals(WORKFLOW_JIRA.getDescription(), jiraWorkflow.getDescription());
        assertEquals(WORKFLOW_JIRA.getDescriptor().getSteps().size(), jiraWorkflow.getSteps().intValue());
        assertNull(jiraWorkflow.getLastModifiedDate());
        assertNull(jiraWorkflow.getLastModifiedUser());

        assertTrue(beans.containsKey(WORKFLOW_TEST.getName()));
        WorkflowBean testWorkflow = beans.get(WORKFLOW_TEST.getName());
        assertEquals(WORKFLOW_TEST.getName(), testWorkflow.getName());
        assertEquals(WORKFLOW_TEST.getDescription(), testWorkflow.getDescription());
        assertEquals(WORKFLOW_TEST.getDescriptor().getSteps().size(), testWorkflow.getSteps().intValue());
        assertEquals(WORKFLOW_TEST.getUpdatedDate().toString(), testWorkflow.getLastModifiedDate());
        assertEquals(USER_ADMIN.getDisplayName(), testWorkflow.getLastModifiedUser());
    }

    @Test(expected = NotAuthorisedWebException.class)
    public void testNotLoggedIn()
    {
        getResponseForUser(null);
    }

    @Test(expected = NotAuthorisedWebException.class)
    public void testNotAdmin()
    {
        getResponseForUser(USER_USER);
    }

    @Before
    public void setUp()
    {
        when(workflowManager.getWorkflows()).thenReturn(Arrays.<JiraWorkflow>asList(WORKFLOW_JIRA, WORKFLOW_TEST));
        when(dateTimeFormatter.forLoggedInUser()).thenReturn(dateTimeFormatter);
        when(dateTimeFormatter.format(WORKFLOW_TEST.getUpdatedDate())).thenReturn(WORKFLOW_TEST.getUpdatedDate().toString());
        when(userManager.getUserByKey(USER_ADMIN.getKey())).thenReturn(USER_ADMIN);
        when(userManager.isUserExisting(USER_ADMIN)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, USER_ADMIN)).thenReturn(true);
        when(permissionManager.hasPermission(Permissions.ADMINISTER, USER_USER)).thenReturn(false);
    }

    private Response getResponseForUser(ApplicationUser user)
    {
        when(jiraAuthenticationContext.getUser()).thenReturn(user);
        WorkflowsResource workflowsResource = new WorkflowsResource(workflowManager, dateTimeFormatter, userManager,
                jiraAuthenticationContext, permissionManager, factory);
        return workflowsResource.getAllWorkflows(null);
    }
}
