package com.atlassian.jira.gadgets.system;

import java.util.Arrays;
import java.util.List;

import javax.ws.rs.core.Response;

import com.atlassian.jira.plugin.projectpanel.ProjectTabPanel;
import com.atlassian.jira.plugin.projectpanel.ProjectTabPanelModuleDescriptor;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.project.browse.BrowseContext;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.plugin.PluginAccessor;

import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.mockito.Mock;
import org.mockito.runners.MockitoJUnitRunner;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;
import static org.mockito.Matchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@RunWith (MockitoJUnitRunner.class)
public class TestProjectGadgetResource
{
    @Mock
    private JiraAuthenticationContext authenticationContext;
    @Mock
    private PermissionManager permissionManager;
    @Mock
    private ProjectManager projectManager;
    @Mock
    private PluginAccessor pluginAccessor;

    private ProjectGadgetResource resource;

    @Before
    public void setUp()
    {
        resource = new ProjectGadgetResource(
                authenticationContext,
                permissionManager,
                projectManager,
                pluginAccessor,
                null,
                null,
                null,
                null,
                null
        );
    }

    @Test
    public void reportsUrlsAreCorrectlyFormed()
    {
        String projectId = "1";

        mockProjectWithTabs(projectId, Arrays.asList(
                mockProjectTabPanel("tab1"),
                mockProjectTabPanel("tab2")
        ));

        Response response = resource.getReports(projectId);

        List<ProjectGadgetResource.OptionData> reports = ((ProjectGadgetResource.OptionDataList) response.getEntity()).getOptions();

        assertReportUrlIsCorrect(reports.get(0), "/browse/1?selectedTab=tab1");
        assertReportUrlIsCorrect(reports.get(1), "/browse/1?selectedTab=tab2");
    }

    private void mockProjectWithTabs(final String id, final List<ProjectTabPanelModuleDescriptor> projectTabPanels)
    {
        Project project = mock(Project.class);
        when(project.getKey()).thenReturn(id);
        ApplicationUser user = mock(ApplicationUser.class);

        when(authenticationContext.getUser()).thenReturn(user);
        when(projectManager.getProjectObj(Long.parseLong(id))).thenReturn(project);
        when(permissionManager.hasPermission(Permissions.BROWSE, project, user)).thenReturn(true);

        when(pluginAccessor.getEnabledModuleDescriptorsByClass(ProjectTabPanelModuleDescriptor.class)).thenReturn(projectTabPanels);
    }

    private ProjectTabPanelModuleDescriptor mockProjectTabPanel(final String key)
    {
        ProjectTabPanel module = mock(ProjectTabPanel.class);
        when(module.showPanel(any(BrowseContext.class))).thenReturn(true);

        ProjectTabPanelModuleDescriptor tabPanelDescriptor = mock(ProjectTabPanelModuleDescriptor.class);
        when(tabPanelDescriptor.getModule()).thenReturn(module);
        when(tabPanelDescriptor.getCompleteKey()).thenReturn(key);

        return tabPanelDescriptor;
    }

    private void assertReportUrlIsCorrect(ProjectGadgetResource.OptionData report, String expectedUrl)
    {
        assertThat(report.getKey(), equalTo(expectedUrl));
    }
}
