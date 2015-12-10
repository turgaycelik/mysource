package com.atlassian.jira.gadgets.system.util;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v1.model.errors.ValidationError;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;
import junit.framework.TestCase;
import org.easymock.classextension.EasyMock;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static org.easymock.EasyMock.createNiceMock;
import static org.easymock.EasyMock.expect;

public class TestDefaultProjectsAndCategoriesHelper extends TestCase
{
    private JiraAuthenticationContext ctx;
    private ApplicationUser mockUser;
    private PermissionManager prms;
    private ProjectManager proj;

    @Override
    protected void setUp() throws Exception
    {
        super.setUp();
        mockUser = new MockApplicationUser("admin");
        ctx = EasyMock.createNiceMock(JiraAuthenticationContext.class);
        prms = EasyMock.createNiceMock(PermissionManager.class);
        proj = createNiceMock(ProjectManager.class);
    }

    public void testValidate()
    {
        Project project = createNiceMock(Project.class);
        expect(ctx.getUser()).andReturn(mockUser);
        expect(proj.getProjectObj(666L)).andReturn(project);
        EasyMock.replay(ctx, prms, proj);
        
        ProjectsAndCategoriesHelper helper = new DefaultProjectsAndCategoriesHelper(proj, prms, ctx);

        List<ValidationError> errors = new ArrayList<ValidationError>();
        helper.validate("666", errors, "somefield");
        assertEquals(0, errors.size());
    }

    public void testValidateNullProject()
    {
        Project project = createNiceMock(Project.class);
        expect(ctx.getUser()).andReturn(mockUser);
        expect(proj.getProjectObj(666L)).andReturn(project);
        EasyMock.replay(ctx, prms, proj);

        ProjectsAndCategoriesHelper helper = new DefaultProjectsAndCategoriesHelper(proj, prms, ctx);

        List<ValidationError> errors = new ArrayList<ValidationError>();
        helper.validate(null, errors, "somefield");
        assertErrors(errors, "gadget.common.projects.and.categories.none.selected");
    }

    public void testValidateUnknownProject()
    {
        expect(ctx.getUser()).andReturn(mockUser);
        EasyMock.replay(ctx, prms, proj);

        ProjectsAndCategoriesHelper helper = new DefaultProjectsAndCategoriesHelper(proj, prms, ctx);

        List<ValidationError> errors = new ArrayList<ValidationError>();
        helper.validate("555", errors, "somefield");
        assertErrors(errors, "gadget.common.invalid.project");
    }

    public void testValidateUnknownCategory()
    {
        expect(ctx.getUser()).andReturn(mockUser);
        EasyMock.replay(ctx, prms, proj);

        ProjectsAndCategoriesHelper helper = new DefaultProjectsAndCategoriesHelper(proj, prms, ctx);

        List<ValidationError> errors = new ArrayList<ValidationError>();
        helper.validate("cat555", errors, "somefield");
        assertErrors(errors, "gadget.common.invalid.projectCategory");
    }

    public void testValidateAllProjects()
    {
        expect(ctx.getUser()).andReturn(mockUser);
        EasyMock.replay(ctx, prms, proj);

        ProjectsAndCategoriesHelper helper = new DefaultProjectsAndCategoriesHelper(proj, prms, ctx);

        List<ValidationError> errors = new ArrayList<ValidationError>();
        helper.validate("allprojects", errors, "somefield");
        assertEquals(0, errors.size());
    }

    public void testGetProjectIds()
    {
        expect(ctx.getUser()).andReturn(mockUser);
        Project project = createNiceMock(Project.class);
        expect(project.getId()).andReturn(666L);
        Project project2 = createNiceMock(Project.class);
        expect(project2.getId()).andReturn(323L);
        expect(proj.getProjectObj(666L)).andReturn(project);
        expect(proj.getProjectObj(323L)).andReturn(project2);
        expect(prms.hasPermission(Permissions.BROWSE, project, mockUser)).andReturn(true);
        expect(prms.hasPermission(Permissions.BROWSE, project2, mockUser)).andReturn(true);

        expect(proj.getProjectObjectsFromProjectCategory(555L)).andReturn(Arrays.asList(project2));

        EasyMock.replay(ctx, prms, proj, project, project2);

        ProjectsAndCategoriesHelper helper = new DefaultProjectsAndCategoriesHelper(proj, prms, ctx);

        Set<Long> ids = helper.getProjectIds("666|cat555");
        assertNotNull(ids);
        assertEquals(1, ids.size());
    }

    public void testGetProjectIdsWithAll()
    {
        expect(ctx.getUser()).andReturn(mockUser);
        Project project = createNiceMock(Project.class);
        expect(project.getId()).andReturn(666L);
        expect(prms.getProjects(Permissions.BROWSE, mockUser)).andReturn(Arrays.asList(project));

        EasyMock.replay(ctx, prms, proj, project);

        ProjectsAndCategoriesHelper helper = new DefaultProjectsAndCategoriesHelper(proj, prms, ctx);

        Set<Long> ids = helper.getProjectIds("allprojects");
        assertNotNull(ids);
        assertEquals(1, ids.size());
    }

    public void testGetProjectIdsWithUnknownCategory()
    {
        expect(ctx.getUser()).andReturn(mockUser);
        expect(proj.getProjectObjectsFromProjectCategory(555L)).andReturn(Collections.<Project>emptyList());
        EasyMock.replay(ctx, prms, proj);

        ProjectsAndCategoriesHelper helper = new DefaultProjectsAndCategoriesHelper(proj, prms, ctx);

        Set<Long> ids = helper.getProjectIds("cat555");
        assertEquals(0, ids.size());
    }

    public void testGetProjectIdsWithUnknownProject()
    {
        expect(ctx.getUser()).andReturn(mockUser);
        EasyMock.replay(ctx, prms, proj);

        ProjectsAndCategoriesHelper helper = new DefaultProjectsAndCategoriesHelper(proj, prms, ctx);

        Set<Long> ids = helper.getProjectIds("555");
        assertEquals(0, ids.size());
    }

    private void assertErrors(List<ValidationError> errors, String... errorKeys)
    {
        assertEquals("Should be the same number of links as expected", errorKeys.length, errors.size());
        Set<String> expectErrorKeys = new HashSet<String>(Arrays.asList(errorKeys));
        for (ValidationError err : errors)
        {
            expectErrorKeys.remove(err.getError());
        }
        assertEquals("Expected errors not found: " + expectErrorKeys, 0, expectErrorKeys.size());
    }
}
