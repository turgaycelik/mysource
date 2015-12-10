package com.atlassian.jira.issue.security;

import java.util.Arrays;
import java.util.Collection;
import java.util.List;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;

import com.google.common.collect.Lists;
import com.google.common.collect.Sets;

import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.easymock.classextension.EasyMock.createControl;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestDefaultProjectIssueSecuritySchemeHelper
{
    private IMocksControl control;
    private MockSimpleAuthenticationContext authContext;
    private PermissionManager permissionManager;
    private MockUser user;
    private IssueSecuritySchemeManager issueSecuritySchemeManager;

    @Before
    public void setUp() throws Exception
    {
        control = createControl();
        user = new MockUser("mtan");
        authContext = new MockSimpleAuthenticationContext(user);
        permissionManager = control.createMock(PermissionManager.class);
        issueSecuritySchemeManager = control.createMock(IssueSecuritySchemeManager.class);
    }

    @Test
    public void testGetProjectsNonDefault() throws Exception
    {
        final MockProject project1 = new MockProject(101928282L, "ONE");
        final MockProject project2 = new MockProject(35438590L, "TWO");

        final Scheme issueSecurityScheme = new Scheme("101010L", "lalala");

        expect(issueSecuritySchemeManager.getProjects(eq(issueSecurityScheme)))
                .andReturn(Lists.<Project>newArrayList(project1, project2));

        ProjectIssueSecuritySchemeHelper helper = createHelper(Sets.<Project>newHashSet(project1));

        control.replay();

        List<Project> projects = helper.getSharedProjects(issueSecurityScheme);
        assertEquals(Arrays.<Project>asList(project1), projects);

        control.verify();
    }

    private ProjectIssueSecuritySchemeHelper createHelper(final Collection<? extends Project> allowedProjects)
    {
        return new DefaultProjectIssueSecuritySchemeHelper(issueSecuritySchemeManager, authContext, permissionManager)
        {
            boolean hasEditPermission(final User user, final Project project)
            {
                return allowedProjects.contains(project);
            }
        };
    }

}
