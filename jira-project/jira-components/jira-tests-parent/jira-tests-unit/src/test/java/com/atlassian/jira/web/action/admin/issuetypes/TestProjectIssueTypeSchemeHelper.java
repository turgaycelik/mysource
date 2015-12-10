package com.atlassian.jira.web.action.admin.issuetypes;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.fields.config.MockFieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.SimpleErrorCollection;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestProjectIssueTypeSchemeHelper
{
    private IMocksControl control;
    private ProjectService projectService;
    private IssueTypeSchemeManager issueTypeSchemeManager;
    private MockSimpleAuthenticationContext authContext;
    private ProjectFactory projectFactory;
    private PermissionManager permissionManager;
    private MockUser user;

    @Before
    public void setUp() throws Exception
    {
        control = createControl();
        projectService = control.createMock(ProjectService.class);
        issueTypeSchemeManager = control.createMock(IssueTypeSchemeManager.class);
        user = new MockUser("bbain");
        authContext = new MockSimpleAuthenticationContext(user);
        projectFactory = control.createMock(ProjectFactory.class);
        permissionManager = control.createMock(PermissionManager.class);
    }

    @Test
    public void testGetProjectsNonDefault() throws Exception
    {
        final MockProject project1 = new MockProject(101928282L, "ONE");
        final MockProject project2 = new MockProject(35438590L, "TWO");

        final MockFieldConfigScheme configScheme = new MockFieldConfigScheme().setId(1010101L).setName("SchemeName");
        configScheme.addAssociatedProjects(project1.getGenericValue()).addAssociatedProjects(project2.getGenericValue());

        expect(issueTypeSchemeManager.isDefaultIssueTypeScheme(configScheme)).andReturn(false);
        expect(projectFactory.getProjects(Arrays.asList(project1.getGenericValue(), project2.getGenericValue())))
                .andReturn(Lists.<Project>newArrayList(project1, project2));

        ProjectIssueTypeSchemeHelper helper = createHelper(Sets.<Project>newHashSet(project1));

        control.replay();

        List<Project> projects = helper.getProjectsUsingScheme(configScheme);
        assertEquals(Arrays.<Project>asList(project1), projects);

        control.verify();
    }

    @Test
    public void testGetProjectsDefault() throws Exception
    {
        final MockProject project1 = new MockProject(101928282L, "ONE");
        final MockProject project2 = new MockProject(35438590L, "TWO");

        final MockFieldConfigScheme configScheme1 = new MockFieldConfigScheme().setId(1010101L).setName("SchemeName");
        final MockFieldConfigScheme configScheme2 = new MockFieldConfigScheme().setId(1010102L).setName("SchemeName2");

        expect(issueTypeSchemeManager.isDefaultIssueTypeScheme(configScheme1)).andReturn(true);
        expect(projectService.getAllProjectsForAction(user, ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ServiceOutcomeImpl<List<Project>>(new SimpleErrorCollection(), Arrays.<Project>asList(project1, project2)));

        expect(issueTypeSchemeManager.getConfigScheme(project1)).andReturn(configScheme2);
        expect(issueTypeSchemeManager.getConfigScheme(project2)).andReturn(configScheme1);

        ProjectIssueTypeSchemeHelper helper = createHelper(Collections.<Project>emptySet());

        control.replay();

        List<Project> projects = helper.getProjectsUsingScheme(configScheme1);
        assertEquals(Arrays.<Project>asList(project2), projects);

        control.verify();
    }

    @Test
    public void testGetProjectsDefaultWithErrors() throws Exception
    {
        final MockProject project1 = new MockProject(101928282L, "ONE");
        final MockProject project2 = new MockProject(35438590L, "TWO");

        final MockFieldConfigScheme configScheme1 = new MockFieldConfigScheme().setId(1010101L).setName("SchemeName");

        expect(issueTypeSchemeManager.isDefaultIssueTypeScheme(configScheme1)).andReturn(true);
        SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage("errorMessage");
        expect(projectService.getAllProjectsForAction(user, ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ServiceOutcomeImpl<List<Project>>(errorCollection, Arrays.<Project>asList(project1, project2)));

        ProjectIssueTypeSchemeHelper helper = createHelper(Collections.<Project>emptySet());

        control.replay();

        List<Project> projects = helper.getProjectsUsingScheme(configScheme1);
        assertEquals(Collections.<Project>emptyList(), projects);

        control.verify();
    }

    private ProjectIssueTypeSchemeHelper createHelper(final Collection<? extends Project> allowedProjects)
    {
        return new ProjectIssueTypeSchemeHelper(projectService, issueTypeSchemeManager, authContext, projectFactory, permissionManager)
        {
            @Override
            boolean hasEditPermission(User user, Project project)
            {
                return allowedProjects.contains(project);
            }
        };
    }
}
