package com.atlassian.jira.issue.fields;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayout;
import com.atlassian.jira.issue.fields.layout.field.MockFieldLayoutManager;
import com.atlassian.jira.mock.issue.fields.layout.field.MockFieldConfigurationScheme;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nHelper;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestDefaultProjectFieldLayoutSchemeHelper
{

    private IMocksControl control;
    private JiraAuthenticationContext authenticationContext;
    private MockFieldLayoutManager fieldLayoutManager;
    private MockUser user;
    private ProjectService projectService;

    @Before
    public void setUp() throws Exception
    {
        control = EasyMock.createControl();
        user = new MockUser("mtan");
        authenticationContext = new MockSimpleAuthenticationContext(user, Locale.ENGLISH, new NoopI18nHelper());
        fieldLayoutManager = new MockFieldLayoutManager();
        projectService = control.createMock(ProjectService.class);
    }

    @After
    public void tearDown()
    {
        control = null;
        user = null;
        authenticationContext = null;
        fieldLayoutManager = null;
    }

    @Test
    public void testGetActiveFieldLayoutsWithInvalidServiceResult()
    {
        authenticationContext.setLoggedInUser(user);

        final ServiceOutcome<List<Project>> invalidServiceOutcome = control.createMock(ServiceOutcome.class);
        EasyMock.expect(invalidServiceOutcome.isValid()).andStubReturn(false);
        EasyMock.expect(projectService.getAllProjectsForAction(EasyMock.eq(user), EasyMock.eq(ProjectAction.EDIT_PROJECT_CONFIG)))
                .andStubReturn(invalidServiceOutcome);

        final FieldLayout fieldLayout = new MockFieldLayout();

        control.replay();

        final ProjectFieldLayoutSchemeHelper projectFieldsContextProvider = new DefaultProjectFieldLayoutSchemeHelper(projectService,
                fieldLayoutManager, authenticationContext);

        final Multimap<FieldLayout, Project> activeFieldLayouts = projectFieldsContextProvider.getProjectsForFieldLayouts(Sets.<FieldLayout>newHashSet(fieldLayout));

        assertEquals(0, activeFieldLayouts.keySet().size());

        final List<Project> projectsForFieldLayout = projectFieldsContextProvider.getProjectsForFieldLayout(fieldLayout);

        assertEquals(0, projectsForFieldLayout.size());

        control.verify();

    }

    @Test
    public void testGetActiveFieldLayoutsWithValidServiceResult()
    {
        authenticationContext.setLoggedInUser(user);

        final MockProject sameFieldLayout = new MockProject(888L, "aaa")
                .setIssueTypes("Bug", "Task");
        final MockProject differentFieldLayout = new MockProject(777L, "aaa")
                .setIssueTypes("Bug", "Task");

        final MockFieldLayout fieldLayout = new MockFieldLayout()
                .setDefault(false)
                .setDescription("description")
                .setId(999L)
                .setName("fieldLayout");

        fieldLayoutManager.setFieldLayout(sameFieldLayout, "Bug", fieldLayout);

        final ServiceOutcome<List<Project>> validServiceOutcome = control.createMock(ServiceOutcome.class);
        EasyMock.expect(validServiceOutcome.isValid()).andStubReturn(true);
        EasyMock.expect(validServiceOutcome.getReturnedValue()).andStubReturn(
                Lists.<Project>newArrayList(
                        sameFieldLayout, differentFieldLayout
                )
        );

        EasyMock.expect(projectService.getAllProjectsForAction(EasyMock.eq(user), EasyMock.eq(ProjectAction.EDIT_PROJECT_CONFIG)))
                .andStubReturn(validServiceOutcome);

        control.replay();

        ProjectFieldLayoutSchemeHelper projectFieldLayoutSchemeHelper = new DefaultProjectFieldLayoutSchemeHelper(projectService,
                fieldLayoutManager, authenticationContext);

        Multimap<FieldLayout, Project> activeFieldLayouts = projectFieldLayoutSchemeHelper
                .getProjectsForFieldLayouts(Sets.<FieldLayout>newHashSet(fieldLayout));

        assertEquals(1, activeFieldLayouts.keySet().size());
        assertEquals(Collections.singleton(sameFieldLayout), activeFieldLayouts.get(fieldLayout));

        final List<Project> projectsForFieldLayout = projectFieldLayoutSchemeHelper
                .getProjectsForFieldLayout(fieldLayout);

        assertEquals(1, projectsForFieldLayout.size());
        assertEquals(Collections.singletonList(sameFieldLayout), projectsForFieldLayout);

        control.verify();
    }

    @Test
    public void testGetActiveFieldLayoutsWithValidServiceResultButNoMatchingFieldLayouts()
    {
        authenticationContext.setLoggedInUser(user);

        final MockProject differentFieldLayout = new MockProject(777L, "aaa")
                .setIssueTypes("Bug", "Task");

        final MockFieldLayout fieldLayout = new MockFieldLayout()
                .setDefault(false)
                .setDescription("description")
                .setId(999L)
                .setName("fieldLayout");

        ServiceOutcome<List<Project>> validServiceOutcome = control.createMock(ServiceOutcome.class);
        EasyMock.expect(validServiceOutcome.isValid()).andStubReturn(true);
        EasyMock.expect(validServiceOutcome.getReturnedValue()).andStubReturn(
                Lists.<Project>newArrayList(
                        differentFieldLayout
                )
        );

        EasyMock.expect(projectService.getAllProjectsForAction(EasyMock.eq(user), EasyMock.eq(ProjectAction.EDIT_PROJECT_CONFIG)))
                .andStubReturn(validServiceOutcome);

        control.replay();

        final ProjectFieldLayoutSchemeHelper projectFieldLayoutSchemeHelper = new DefaultProjectFieldLayoutSchemeHelper(projectService,
                fieldLayoutManager, authenticationContext);

        final Multimap<FieldLayout, Project> activeFieldLayouts = projectFieldLayoutSchemeHelper
                .getProjectsForFieldLayouts(Sets.<FieldLayout>newHashSet(fieldLayout));

        assertEquals(0, activeFieldLayouts.keySet().size());

        final List<Project> projectsForFieldLayout = projectFieldLayoutSchemeHelper
                .getProjectsForFieldLayout(fieldLayout);

        assertEquals(0, projectsForFieldLayout.size());

        control.verify();
    }

    @Test
    public void testGetActiveFieldLayoutsWithValidServiceResultAndNoProjects()
    {
        authenticationContext.setLoggedInUser(user);

        final MockFieldLayout fieldLayout = new MockFieldLayout()
                .setDefault(false)
                .setDescription("description")
                .setId(999L)
                .setName("fieldLayout");

        ServiceOutcome<List<Project>> validServiceOutcome = control.createMock(ServiceOutcome.class);
        EasyMock.expect(validServiceOutcome.isValid()).andStubReturn(true);
        EasyMock.expect(validServiceOutcome.getReturnedValue()).andStubReturn(
                Collections.<Project>emptyList()
        );

        EasyMock.expect(projectService.getAllProjectsForAction(EasyMock.eq(user), EasyMock.eq(ProjectAction.EDIT_PROJECT_CONFIG)))
                .andStubReturn(validServiceOutcome);

        control.replay();

        final ProjectFieldLayoutSchemeHelper projectFieldLayoutSchemeHelper = new DefaultProjectFieldLayoutSchemeHelper(projectService,
                fieldLayoutManager, authenticationContext);

        final Multimap<FieldLayout, Project> activeFieldLayouts = projectFieldLayoutSchemeHelper
                .getProjectsForFieldLayouts(Sets.<FieldLayout>newHashSet(fieldLayout));

        assertEquals(0, activeFieldLayouts.keySet().size());

        final List<Project> projectsForFieldLayout = projectFieldLayoutSchemeHelper
                .getProjectsForFieldLayout(fieldLayout);

        assertEquals(0, projectsForFieldLayout.size());

        control.verify();
    }

    @Test
    public void testGetProjectsForScheme()
    {
        final MockProject project1 = new MockProject(101928282L, "ONE");
        final MockProject project2 = new MockProject(35438590L, "TWO");

        final MockFieldConfigurationScheme configScheme = new MockFieldConfigurationScheme().setId(1010101L).setName("SchemeName");
        final MockFieldConfigurationScheme otherConfigScheme = new MockFieldConfigurationScheme().setId(1010102L).setName("OtherSchemeName");

        fieldLayoutManager.setFieldConfigurationScheme(1010101L, configScheme)
            .setFieldConfigurationScheme(project1, configScheme)
            .setFieldConfigurationScheme(project2, otherConfigScheme);


        ServiceOutcome<List<Project>> validServiceOutcome = control.createMock(ServiceOutcome.class);
        EasyMock.expect(validServiceOutcome.isValid()).andStubReturn(true);
        EasyMock.expect(validServiceOutcome.getReturnedValue()).andStubReturn(
                Lists.<Project>newArrayList(project1)
        );

        EasyMock.expect(projectService.getAllProjectsForAction(EasyMock.eq(user), EasyMock.eq(ProjectAction.EDIT_PROJECT_CONFIG)))
                .andStubReturn(validServiceOutcome);

        control.replay();

        final ProjectFieldLayoutSchemeHelper projectFieldLayoutSchemeHelper = new DefaultProjectFieldLayoutSchemeHelper(projectService,
                fieldLayoutManager, authenticationContext);

        List<Project> projects = projectFieldLayoutSchemeHelper.getProjectsForScheme(configScheme.getId());
        assertEquals(Arrays.<Project>asList(project1), projects);

        control.verify();

    }


}
