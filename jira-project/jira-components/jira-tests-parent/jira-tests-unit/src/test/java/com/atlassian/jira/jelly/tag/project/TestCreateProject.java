package com.atlassian.jira.jelly.tag.project;

import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.jelly.tag.JellyTagConstants;
import com.atlassian.jira.junit.rules.AvailableInContainer;
import com.atlassian.jira.junit.rules.MockitoMocksInContainer;
import com.atlassian.jira.mock.ofbiz.MockGenericValue;
import com.atlassian.jira.project.MockProject;
import com.atlassian.jira.project.ProjectAssigneeTypes;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.MockApplicationUser;

import com.google.common.collect.ImmutableMap;

import org.apache.commons.jelly.JellyContext;
import org.apache.commons.jelly.Script;
import org.apache.commons.jelly.XMLOutput;
import org.junit.Before;
import org.junit.Rule;
import org.junit.Test;
import org.junit.rules.TestRule;
import org.mockito.Mock;
import org.mockito.Mockito;

import static org.mockito.Mockito.when;

public class TestCreateProject
{
    @Rule
    public TestRule rule = MockitoMocksInContainer.forTest(this);
    @Mock
    private XMLOutput xmlOutput;

    private ApplicationUser applicationUser = new MockApplicationUser("bob");

    @Mock
    @AvailableInContainer
    private JiraAuthenticationContext authenticationContext;

    @Mock
    @AvailableInContainer
    private ApplicationProperties applicationProperties;

    @Mock
    @AvailableInContainer
    private ProjectService projectService;

    @Mock
    @AvailableInContainer
    private ProjectManager projectManager;

    @Mock
    private Script body;
    @Mock
    private ProjectService.CreateProjectValidationResult validationResult;

    @Mock
    private JellyContext jellyContext;
    private CreateProject createProject;
    private String projectKey;
    private String projectName;
    private String lead;

    @Before
    public void setUp() throws Exception
    {
        when(authenticationContext.getUser()).thenReturn(applicationUser);
        createProject = new CreateProject();
        projectKey = "Project key";
        projectName = "Project name";
        lead = "Project lead";
        createProject.setAttribute("key", projectKey);
        createProject.setAttribute("name", projectName);
        createProject.setAttribute("lead", lead);
        createProject.setContext(jellyContext);


        final MockProject value = new MockProject(12l, projectKey);
        when(projectService.createProject(validationResult)).thenReturn(value);
        createProject.setBody(body);
    }

    @Test
    public void testCreateProjectWithMinimalSetOfAttributesWhenUnassignedEnabled() throws Exception
    {
        //having
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED)).thenReturn(true);
        when(projectService.validateCreateProject(applicationUser.getDirectoryUser(), projectName, projectKey, "", lead, "", ProjectAssigneeTypes.UNASSIGNED)).thenReturn(validationResult);
        when(validationResult.isValid()).thenReturn(true);
        //noinspection deprecation
        when(projectManager.getProjectByKey(projectKey)).thenReturn(new MockGenericValue("project", ImmutableMap.of("id", 123l, "key", projectKey)));
        //when
        createProject.doTag(xmlOutput);

        //then
        Mockito.verify(jellyContext).setVariable(JellyTagConstants.PROJECT_ID, 123l);
        Mockito.verify(jellyContext).setVariable(JellyTagConstants.PROJECT_KEY, projectKey);
        Mockito.verify(projectService).createProject(validationResult);

    }

    @Test
    public void testCreateProjectWithMinimalSetOfAttributesWhenUnassignedDisabled() throws Exception
    {
        //having

        when(applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED)).thenReturn(false);
        when(projectService.validateCreateProject(applicationUser.getDirectoryUser(), projectName, projectKey, "", lead, "", ProjectAssigneeTypes.PROJECT_LEAD)).thenReturn(validationResult);
        when(validationResult.isValid()).thenReturn(true);
        //noinspection deprecation
        when(projectManager.getProjectByKey(projectKey)).thenReturn(new MockGenericValue("project", ImmutableMap.of("id", 123l, "key", projectKey)));

        //when
        createProject.doTag(xmlOutput);
        //then
        Mockito.verify(jellyContext).setVariable(JellyTagConstants.PROJECT_ID, 123l);
        Mockito.verify(jellyContext).setVariable(JellyTagConstants.PROJECT_KEY, projectKey);
        Mockito.verify(projectService).createProject(validationResult);

    }

    @Test
    public void testCreateProjectWithAvatar() throws Exception
    {
        //having
        final Long avatarId = 8l;
        createProject.setAttribute("avatarId", avatarId.toString());
        when(applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWUNASSIGNED)).thenReturn(false);
        when(projectService.validateCreateProject(applicationUser.getDirectoryUser(), projectName, projectKey, "", lead, "", ProjectAssigneeTypes.PROJECT_LEAD, avatarId)).thenReturn(validationResult);
        when(validationResult.isValid()).thenReturn(true);
        //noinspection deprecation
        when(projectManager.getProjectByKey(projectKey)).thenReturn(new MockGenericValue("project", ImmutableMap.of("id", 123l, "key", projectKey)));

        //when
        createProject.doTag(xmlOutput);
        //then
        Mockito.verify(jellyContext).setVariable(JellyTagConstants.PROJECT_ID, 123l);
        Mockito.verify(jellyContext).setVariable(JellyTagConstants.PROJECT_KEY, projectKey);
        Mockito.verify(projectService).createProject(validationResult);

    }

}
