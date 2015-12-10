package com.atlassian.jira.issue.fields.screen.issuetype;

import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.ServiceOutcome;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.fields.screen.FieldScreenScheme;
import com.atlassian.jira.issue.fields.screen.MockFieldScreenScheme;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.MockPermissionManager;
import com.atlassian.jira.mock.issue.fields.screen.issuetype.MockIssueTypeScreenScheme;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectFactory;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.plugin.event.PluginEventManager;

import com.google.common.collect.Lists;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.easymock.classextension.EasyMock;
import org.easymock.classextension.IMocksControl;
import org.junit.After;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.RuleChain;
import org.mockito.Mock;
import org.ofbiz.core.entity.GenericValue;

import static org.easymock.EasyMock.eq;
import static org.easymock.EasyMock.expect;
import static org.junit.Assert.assertEquals;

/**
 * @since v4.4
 */
public class TestDefaultProjectIssueTypeScreenSchemeHelper
{
    @Rule
    public final RuleChain mockContainer = MockitoMocksInContainer.forTest(this);
    @Mock @AvailableInContainer
    private PluginAccessor pluginAccessor;
    @Mock @AvailableInContainer
    private PluginEventManager pluginEventManager;

    private IMocksControl control;
    private JiraAuthenticationContext authenticationContext;
    private IssueTypeScreenSchemeManager issueTypeScreenSchemeManager;
    private MockUser user;
    private ProjectService projectService;
    private PermissionManager permissionManager;
    private ProjectFactory projectFactory;

    @Before
    public void setUp() throws Exception
    {
        control = EasyMock.createControl();
        user = new MockUser("mtan");
        authenticationContext = new MockSimpleAuthenticationContext(user, Locale.ENGLISH, new NoopI18nHelper());
        issueTypeScreenSchemeManager = control.createMock(IssueTypeScreenSchemeManager.class);
        projectService = control.createMock(ProjectService.class);
        permissionManager = new MockPermissionManager(true);
        projectFactory = control.createMock(ProjectFactory.class);
    }

    @After
    public void tearDown()
    {
        control = null;
        user = null;
        authenticationContext = null;
        issueTypeScreenSchemeManager = null;
        permissionManager = null;
        projectFactory = null;
    }

    @Test
    public void testGetActiveFieldScreenSchemesWithInvalidServiceResult()
    {
        final ServiceOutcome<List<Project>> invalidServiceOutcome = control.createMock(ServiceOutcome.class);
        EasyMock.expect(invalidServiceOutcome.isValid()).andStubReturn(false);
        EasyMock.expect(projectService.getAllProjectsForAction(EasyMock.eq(user), EasyMock.eq(ProjectAction.EDIT_PROJECT_CONFIG)))
                .andStubReturn(invalidServiceOutcome);

        final FieldScreenScheme fieldScreenScheme = control.createMock(FieldScreenScheme.class);

        control.replay();

        final ProjectIssueTypeScreenSchemeHelper projectIssueTypeScreenSchemeHelper = new DefaultProjectIssueTypeScreenSchemeHelper(projectService,
                authenticationContext, issueTypeScreenSchemeManager, null, null);

        final Multimap<FieldScreenScheme, Project> activefieldScreenSchemes = projectIssueTypeScreenSchemeHelper
                .getProjectsForFieldScreenSchemes(Sets.<FieldScreenScheme>newHashSet(fieldScreenScheme));

        assertEquals(0, activefieldScreenSchemes.size());

        final List<Project> projectsForprojectsForFieldScreenScheme = projectIssueTypeScreenSchemeHelper.getProjectsForFieldScreenScheme(fieldScreenScheme);

        assertEquals(0, projectsForprojectsForFieldScreenScheme.size());

        control.verify();

    }

    @Test
    public void testGetActiveFieldScreenSchemesWithValidServiceResult()
    {
        final MockGenericValue projectGV = new MockGenericValue("lala");
        final MockProject project = new MockProject(888L, "aaa", "aaa", projectGV)
                .setIssueTypes("Bug", "Task");

        final MockGenericValue unrelatedProjectGV = new MockGenericValue("lalala");
        final MockProject otherProject = new MockProject(777L, "bbb", "bbb", unrelatedProjectGV)
                .setIssueTypes("Bug", "Task");

        final FieldScreenScheme fieldScreenScheme = control.createMock(FieldScreenScheme.class);

        final MockIssueTypeScreenScheme issueTypeScreenScheme = new MockIssueTypeScreenScheme(6868L, "lala", "lala");
        final IssueTypeScreenSchemeEntity mockIssueTypeScreenSchemeEntity = control.createMock(IssueTypeScreenSchemeEntity.class);
        EasyMock.expect(mockIssueTypeScreenSchemeEntity.getFieldScreenScheme()).andStubReturn(fieldScreenScheme);
        issueTypeScreenScheme.setEntities(MapBuilder.<String, IssueTypeScreenSchemeEntity>newBuilder()
                .add(null, mockIssueTypeScreenSchemeEntity)
                .toMap());

        EasyMock.expect(issueTypeScreenSchemeManager.getIssueTypeScreenScheme(EasyMock.eq(project)))
                .andStubReturn(issueTypeScreenScheme);

        final MockIssueTypeScreenScheme unrelatedIssueTypeScreenScheme = new MockIssueTypeScreenScheme(9393L, "haha", "haha");
        final IssueTypeScreenSchemeEntity mockUnrelatedIssueTypeScreenSchemeEntity = control.createMock(IssueTypeScreenSchemeEntity.class);
        final MockFieldScreenScheme otherFieldScreenScheme = new MockFieldScreenScheme();
        EasyMock.expect(mockUnrelatedIssueTypeScreenSchemeEntity.getFieldScreenScheme()).andStubReturn(otherFieldScreenScheme);
        unrelatedIssueTypeScreenScheme.setEntities(MapBuilder.<String, IssueTypeScreenSchemeEntity>newBuilder()
                .add(null, mockUnrelatedIssueTypeScreenSchemeEntity)
                .add("Bug", mockIssueTypeScreenSchemeEntity)
                .toMap());

        EasyMock.expect(issueTypeScreenSchemeManager.getIssueTypeScreenScheme(EasyMock.eq(otherProject)))
                .andStubReturn(unrelatedIssueTypeScreenScheme);

        final ServiceOutcome<List<Project>> validServiceOutcome = control.createMock(ServiceOutcome.class);
        EasyMock.expect(validServiceOutcome.isValid()).andStubReturn(true);
        EasyMock.expect(validServiceOutcome.getReturnedValue()).andStubReturn(
                Lists.<Project>newArrayList(
                        project, otherProject
                )
        );

        EasyMock.expect(projectService.getAllProjectsForAction(EasyMock.eq(user),
                EasyMock.eq(ProjectAction.EDIT_PROJECT_CONFIG))).andStubReturn(validServiceOutcome);

        control.replay();

        final ProjectIssueTypeScreenSchemeHelper projectIssueTypeScreenSchemeHelper =
                new DefaultProjectIssueTypeScreenSchemeHelper(projectService, authenticationContext, issueTypeScreenSchemeManager, null, null);

        final Multimap<FieldScreenScheme, Project> activeFieldScreenSchemes = projectIssueTypeScreenSchemeHelper
                .getProjectsForFieldScreenSchemes(Sets.<FieldScreenScheme>newHashSet(fieldScreenScheme));

        assertEquals(Collections.singleton(fieldScreenScheme), activeFieldScreenSchemes.keySet());
        assertEquals(CollectionBuilder.<Project>newBuilder(project, otherProject).asListOrderedSet(), activeFieldScreenSchemes.get(fieldScreenScheme));

        List<Project> projectsForprojectsForFieldScreenScheme = projectIssueTypeScreenSchemeHelper.getProjectsForFieldScreenScheme(fieldScreenScheme);
        assertEquals(CollectionBuilder.<Project>newBuilder(project, otherProject).asList(), projectsForprojectsForFieldScreenScheme);

        projectsForprojectsForFieldScreenScheme = projectIssueTypeScreenSchemeHelper.getProjectsForFieldScreenScheme(otherFieldScreenScheme);
        assertEquals(Collections.singletonList(otherProject), projectsForprojectsForFieldScreenScheme);

        control.verify();
    }

    @Test
    public void testGetActiveFieldScreenSchemesWithValidServiceResultButNoMatchingFieldScreenSchemes()
    {
        final MockGenericValue projectGV = new MockGenericValue("lala");
        final MockProject project = new MockProject(888L, "aaa", "aaa", projectGV)
                .setIssueTypes("Bug", "Task");

        final FieldScreenScheme fieldScreenScheme = control.createMock(FieldScreenScheme.class);

        final MockIssueTypeScreenScheme issueTypeScreenScheme = new MockIssueTypeScreenScheme(6868L, "lala", "lala");
        final IssueTypeScreenSchemeEntity mockIssueTypeScreenSchemeEntity = control.createMock(IssueTypeScreenSchemeEntity.class);
        EasyMock.expect(mockIssueTypeScreenSchemeEntity.getFieldScreenScheme()).andStubReturn(fieldScreenScheme);
        issueTypeScreenScheme.setEntities(MapBuilder.<String, IssueTypeScreenSchemeEntity>newBuilder()
                .add(null, mockIssueTypeScreenSchemeEntity)
                .toMap());

        EasyMock.expect(issueTypeScreenSchemeManager.getIssueTypeScreenScheme(EasyMock.eq(project)))
                .andStubReturn(issueTypeScreenScheme);

        final MockFieldScreenScheme otherFieldScreenScheme = new MockFieldScreenScheme();

        ServiceOutcome<List<Project>> validServiceOutcome = control.createMock(ServiceOutcome.class);
        EasyMock.expect(validServiceOutcome.isValid()).andStubReturn(true);
        EasyMock.expect(validServiceOutcome.getReturnedValue()).andStubReturn(
                Lists.<Project>newArrayList(
                        project
                )
        );

        EasyMock.expect(projectService.getAllProjectsForAction(EasyMock.eq(user), EasyMock.eq(ProjectAction.EDIT_PROJECT_CONFIG)))
                .andStubReturn(validServiceOutcome);

        control.replay();

        final ProjectIssueTypeScreenSchemeHelper projectIssueTypeScreenSchemeHelper =
                new DefaultProjectIssueTypeScreenSchemeHelper(projectService, authenticationContext, issueTypeScreenSchemeManager, null, null);

        final Multimap<FieldScreenScheme, Project> activeFieldScreenSchemes = projectIssueTypeScreenSchemeHelper
                .getProjectsForFieldScreenSchemes(Sets.<FieldScreenScheme>newHashSet(fieldScreenScheme, otherFieldScreenScheme));

        assertEquals(Collections.singleton(fieldScreenScheme), activeFieldScreenSchemes.keySet());

        // Didn't have any associated projects
        Collection<Project> multimapProjectsForFieldScreenScheme = activeFieldScreenSchemes.get(otherFieldScreenScheme);
        assertEquals(Collections.<Project>emptySet(), multimapProjectsForFieldScreenScheme);

        List<Project> projectsForprojectsForFieldScreenScheme = projectIssueTypeScreenSchemeHelper.getProjectsForFieldScreenScheme(otherFieldScreenScheme);
        assertEquals(Collections.<Project>emptyList(), projectsForprojectsForFieldScreenScheme);

        // Did have associated projects
        multimapProjectsForFieldScreenScheme = activeFieldScreenSchemes.get(fieldScreenScheme);
        assertEquals(Collections.singleton(project), multimapProjectsForFieldScreenScheme);

        projectsForprojectsForFieldScreenScheme = projectIssueTypeScreenSchemeHelper.getProjectsForFieldScreenScheme(fieldScreenScheme);
        assertEquals(Collections.singletonList(project), projectsForprojectsForFieldScreenScheme);

        control.verify();
    }

    @Test
    public void testGetActiveFieldScreenSchemesWithValidServiceResultAndNoProjects()
    {
        final FieldScreenScheme fieldScreenScheme = control.createMock(FieldScreenScheme.class);

        final MockIssueTypeScreenScheme issueTypeScreenScheme = new MockIssueTypeScreenScheme(6868L, "lala", "lala");
        final IssueTypeScreenSchemeEntity mockIssueTypeScreenSchemeEntity = control.createMock(IssueTypeScreenSchemeEntity.class);
        EasyMock.expect(mockIssueTypeScreenSchemeEntity.getFieldScreenScheme()).andStubReturn(fieldScreenScheme);
        issueTypeScreenScheme.setEntities(MapBuilder.<String, IssueTypeScreenSchemeEntity>newBuilder()
                .add(null, mockIssueTypeScreenSchemeEntity)
                .toMap());

        ServiceOutcome<List<Project>> validServiceOutcome = control.createMock(ServiceOutcome.class);
        EasyMock.expect(validServiceOutcome.isValid()).andStubReturn(true);
        EasyMock.expect(validServiceOutcome.getReturnedValue()).andStubReturn(
                Collections.<Project>emptyList()
        );

        EasyMock.expect(projectService.getAllProjectsForAction(EasyMock.eq(user), EasyMock.eq(ProjectAction.EDIT_PROJECT_CONFIG)))
                .andStubReturn(validServiceOutcome);

        control.replay();

        final ProjectIssueTypeScreenSchemeHelper projectIssueTypeScreenSchemeHelper = new DefaultProjectIssueTypeScreenSchemeHelper(projectService,
                authenticationContext, issueTypeScreenSchemeManager, null, null);

        final Multimap<FieldScreenScheme, Project> activeFieldScreenSchemes = projectIssueTypeScreenSchemeHelper
                .getProjectsForFieldScreenSchemes(Sets.<FieldScreenScheme>newHashSet(fieldScreenScheme));

        assertEquals(0, activeFieldScreenSchemes.keySet().size());

        final List<Project> projectsForFieldScreenScheme = projectIssueTypeScreenSchemeHelper.getProjectsForFieldScreenScheme(fieldScreenScheme);

        assertEquals(0, projectsForFieldScreenScheme.size());

        control.verify();

    }

    @Test
    public void testGetProjectsForScheme()
    {
        final MockProject project1 = new MockProject(101928282L, "ONE");
        final MockProject project2 = new MockProject(35438590L, "TWO");

        final List<GenericValue> genericValues = Arrays.asList(project1.getGenericValue(), project2.getGenericValue());

        final MockIssueTypeScreenScheme issueTypeScreenScheme = new MockIssueTypeScreenScheme(6868L, "lala", "lala")
                .setProjects(genericValues);

        final List<Project> associatedProjects = Lists.<Project>newArrayList(project1, project2);

        expect(projectFactory.getProjects(eq(genericValues))).andStubReturn(associatedProjects);

        control.replay();

        final ProjectIssueTypeScreenSchemeHelper projectIssueTypeScreenSchemeHelper = new DefaultProjectIssueTypeScreenSchemeHelper(projectService,
                authenticationContext, issueTypeScreenSchemeManager, null, projectFactory)
        {
            @Override
            boolean hasEditPermission(User user, Project project)
            {
                return project.equals(project1);
            }
        };

        List<Project> projects = projectIssueTypeScreenSchemeHelper.getProjectsForScheme(issueTypeScreenScheme);
        assertEquals(Arrays.<Project>asList(project1), projects);

        control.verify();

    }

}
