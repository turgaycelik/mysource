package com.atlassian.jira.workflow;

import java.util.Arrays;
import java.util.Collections;
import java.util.List;
import java.util.Locale;

import com.atlassian.jira.bc.ServiceOutcomeImpl;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.issue.issuetype.MockIssueType;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.mock.security.MockSimpleAuthenticationContext;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.user.MockUser;
import com.atlassian.jira.util.NoopI18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.HashMultimap;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.Multimap;
import com.google.common.collect.Sets;

import org.easymock.classextension.IMocksControl;
import org.junit.Before;
import org.junit.Test;

import static java.util.Arrays.asList;
import static org.easymock.classextension.EasyMock.createControl;
import static org.easymock.classextension.EasyMock.expect;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertTrue;

/**
 * @since v4.4
 */
public class TestDefaultProjectWorkflowSchemeHelper
{
    private IMocksControl control;
    private WorkflowSchemeManager workflowSchemeManager;
    private ProjectService projectService;
    private MockUser user;
    private MockSimpleAuthenticationContext context;

    @Before
    public void setUp() throws Exception
    {
        control = createControl();
        user = new MockUser("bbain");
        workflowSchemeManager = control.createMock(WorkflowSchemeManager.class);
        projectService = control.createMock(ProjectService.class);
        context = new MockSimpleAuthenticationContext(user, Locale.ENGLISH, new NoopI18nHelper());
    }

    @Test
    public void testGetWorkflowMap() throws Exception
    {
        final MockIssueType type1 = new MockIssueType("type1", "type1");
        final MockIssueType type2 = new MockIssueType("type2", "type2");
        final MockIssueType type3 = new MockIssueType("type3", "type3");
        final MockIssueType type4 = new MockIssueType("type4", "type4");
        final MockIssueType type5 = new MockIssueType("type5", "type5");

        final MockProject project1 = new MockProject(588L, "TST").setIssueTypes(type1, type2, type5);
        final MockProject project2 = new MockProject(589L, "BJB", "BJB").setIssueTypes(type3, type4, type1, type5);
        final MockProject project3 = new MockProject(599L, "ABC", "ABC").setIssueTypes(type4);

        final String workflow1 = "workflow1";
        final String workflow4 = "workflow4";
        final String workflow5 = "workflow5";

        expect(projectService.getAllProjectsForAction(user, ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ServiceOutcomeImpl<List<Project>>(new SimpleErrorCollection(), Arrays.<Project>asList(project1, project2, project3)));

        //Project1 workflow scheme: default ->workflow4
        expect(workflowSchemeManager.getWorkflowMap(project1)).andReturn(MapBuilder.<String, String>build(null, workflow4));

        //Project2 workflow scheme: default -> jira, *type2 -> workflow1, type3 -> workflow5, type4->workflow4
        // * = mapping ignored because the issue type does not exist for that project.
        MapBuilder<String, String> workflowMapBuilder = MapBuilder.newBuilder(null, JiraWorkflow.DEFAULT_WORKFLOW_NAME);
        workflowMapBuilder.add(type2.getId(), workflow1);
        workflowMapBuilder.add(type3.getId(), workflow5);
        workflowMapBuilder.add(type4.getId(), workflow4);

        expect(workflowSchemeManager.getWorkflowMap(project2)).andReturn(workflowMapBuilder.toMap());

        //Project5 workflow scheme: implied default -> jira
        workflowMapBuilder = MapBuilder.newBuilder();
        expect(workflowSchemeManager.getWorkflowMap(project3)).andReturn(workflowMapBuilder.toMap());

        control.replay();

        DefaultProjectWorkflowSchemeHelper schemeHelper = new DefaultProjectWorkflowSchemeHelper(projectService, workflowSchemeManager, context);

        Multimap<String, Project> actualMap = schemeHelper.getProjectsForWorkflow(Sets.newHashSet(workflow1, workflow4, JiraWorkflow.DEFAULT_WORKFLOW_NAME));

        Multimap<String, Project> expectedMap = HashMultimap.create();
        expectedMap.putAll(workflow4, asList(project2, project1));
        expectedMap.putAll(JiraWorkflow.DEFAULT_WORKFLOW_NAME, asList(project3, project2));

        assertEquals(expectedMap, actualMap);

        control.verify();
    }

    @Test
    public void testGetWorkflowMapNoProjects() throws Exception
    {
        SimpleErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessage("Error");
        expect(projectService.getAllProjectsForAction(user, ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ServiceOutcomeImpl<List<Project>>(errors, Collections.<Project>emptyList()));

        control.replay();

        DefaultProjectWorkflowSchemeHelper schemeHelper = new DefaultProjectWorkflowSchemeHelper(projectService, workflowSchemeManager, context);
        Multimap<String, Project> actualMap = schemeHelper.getProjectsForWorkflow(Sets.newHashSet(JiraWorkflow.DEFAULT_WORKFLOW_NAME));

        assertTrue(actualMap.isEmpty());

        control.verify();
    }

    @Test
    public void testGetProjectsForScheme() throws Exception
    {
        final MockProject project1 = new MockProject(588L, "TST");
        final MockProject project2 = new MockProject(589L, "JKL");
        final MockProject project3 = new MockProject(590L, "MKP");

        final MockGenericValue wfs1 = new MockGenericValue("dontCare", ImmutableMap.<String, Object>of());
        final MockGenericValue wfs2 = new MockGenericValue("dontCare", ImmutableMap.of("id", 10L));
        final MockGenericValue wfs3 = new MockGenericValue("dontCare", ImmutableMap.of("id", 11L));

        expect(projectService.getAllProjectsForAction(user, ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ServiceOutcomeImpl<List<Project>>(new SimpleErrorCollection(), Arrays.<Project>asList(project1, project2, project3)));

        expect(workflowSchemeManager.getWorkflowScheme(project1.getGenericValue())).andReturn(wfs1).anyTimes();
        expect(workflowSchemeManager.getWorkflowScheme(project2.getGenericValue())).andReturn(wfs2).anyTimes();
        expect(workflowSchemeManager.getWorkflowScheme(project3.getGenericValue())).andReturn(wfs3).anyTimes();

        control.replay();

        DefaultProjectWorkflowSchemeHelper schemeHelper = new DefaultProjectWorkflowSchemeHelper(projectService, workflowSchemeManager, context);
        List<Project> projects = schemeHelper.getProjectsForScheme(10L);

        assertEquals(projects, Arrays.<Project>asList(project2));

        control.verify();
    }

    @Test
    public void testGetProjectsForSchemeNoProject() throws Exception
    {
        final MockProject project1 = new MockProject(588L, "TST");
        final MockProject project2 = new MockProject(589L, "JKL");
        final MockProject project3 = new MockProject(590L, "MKP");

        final MockGenericValue wfs1 = new MockGenericValue("dontCare", ImmutableMap.<String, Object>of());
        final MockGenericValue wfs2 = new MockGenericValue("dontCare", ImmutableMap.of("id", 10L));
        final MockGenericValue wfs3 = new MockGenericValue("dontCare", ImmutableMap.of("id", 11L));

        expect(projectService.getAllProjectsForAction(user, ProjectAction.EDIT_PROJECT_CONFIG))
                .andReturn(new ServiceOutcomeImpl<List<Project>>(new SimpleErrorCollection(), Arrays.<Project>asList(project1, project2, project3)));

        expect(workflowSchemeManager.getWorkflowScheme(project1.getGenericValue())).andReturn(wfs1).anyTimes();
        expect(workflowSchemeManager.getWorkflowScheme(project2.getGenericValue())).andReturn(wfs2).anyTimes();
        expect(workflowSchemeManager.getWorkflowScheme(project3.getGenericValue())).andReturn(wfs3).anyTimes();

        control.replay();

        DefaultProjectWorkflowSchemeHelper schemeHelper = new DefaultProjectWorkflowSchemeHelper(projectService, workflowSchemeManager, context);
        List<Project> projects = schemeHelper.getProjectsForScheme(null);

        assertEquals(projects, Arrays.<Project>asList(project1));

        control.verify();
    }
}
