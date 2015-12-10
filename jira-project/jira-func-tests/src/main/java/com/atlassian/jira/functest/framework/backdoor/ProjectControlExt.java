package com.atlassian.jira.functest.framework.backdoor;

import java.util.List;

import com.atlassian.jira.testkit.beans.WorkflowSchemeData;
import com.atlassian.jira.testkit.client.ProjectControl;
import com.atlassian.jira.testkit.client.restclient.Component;
import com.atlassian.jira.testkit.client.restclient.Project;
import com.atlassian.jira.testkit.client.restclient.ProjectClient;
import com.atlassian.jira.testkit.client.restclient.Version;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;

import com.sun.jersey.api.client.GenericType;
import com.sun.jersey.api.client.WebResource;

import static com.atlassian.jira.functest.framework.FunctTestConstants.FUNC_TEST_PLUGIN_REST_PATH;

/**
 * Extended ProjectControl.
 *
 * @since v5.2
 */
public class ProjectControlExt extends ProjectControl
{
    private final ProjectClient projectClient;
    private final IndexingControl indexingControl;

    public ProjectControlExt(JIRAEnvironmentData environmentData)
    {
        super(environmentData);
        projectClient = new ProjectClient(environmentData);
        indexingControl = new IndexingControl(environmentData);
    }

    public WorkflowSchemeData setWorkflowScheme(String projectKey, long id)
    {
        return createWorkflowSchemeResource(projectKey).post(WorkflowSchemeData.class, id);
    }

    public void setDefaultWorkflowScheme(String projectKey)
    {
        createWorkflowSchemeResource(projectKey).delete();
    }

    private WebResource createWorkflowSchemeResource(String projectKey)
    {
        // for workflow scheme-related backdoor, use the func-test-plugin
        return createResourceForPath(FUNC_TEST_PLUGIN_REST_PATH).path("project").path(projectKey).path("workflowscheme");
    }

    public void addProjectKey(Long projectId, String previousProjectKey)
    {
        // for workflow scheme-related backdoor, use the func-test-plugin
        createProjectKeysResource(projectId).put(previousProjectKey);
    }

    public void editProjectKey(Long projectId, String newProjectKey)
    {
        editProjectKeyNoWaitForReindex(projectId, newProjectKey);
        indexingControl.getProjectIndexingProgress(projectId).waitForCompletion();
    }

    public void editProjectKeyNoWaitForReindex(Long projectId, String newProjectKey)
    {
        createProjectKeyResource(projectId).put(newProjectKey);
    }

    public Project getProject(String projectKey)
    {
        return projectClient.get(projectKey);
    }

    public List<Project> getProjects()
    {
        return projectClient.getProjects();
    }

    public List<Version> getVersionsForProject(String projectKey)
    {
        return projectClient.getVersions(projectKey);
    }

    public List<Component> getComponentsForProject(String projectKey)
    {
        return projectClient.getComponents(projectKey);
    }

    public Long getProjectId(String projectKey)
    {
        return createProjectResource(projectKey).path("id").get(Long.class);
    }

    public String getProjectCategoryName(String projectKey)
    {
        return createProjectResource(projectKey).path("category").path("name").get(String.class);
    }

    public List<String> getProjectKeys(Long projectId)
    {
        return createProjectKeysResource(projectId).get(new GenericType<List<String>>() {
        });
    }

    public String getProjectName(Long projectId)
    {
        return createProjectResourceWithId(projectId).path("name").get(String.class);
    }

    private WebResource createProjectKeysResource(Long projectId)
    {
        // for workflow scheme-related backdoor, use the func-test-plugin
        return createProjectResourceWithId(projectId).path("keys");
    }

    private WebResource createProjectKeyResource(Long projectId)
    {
        return createProjectResourceWithId(projectId).path("key");
    }

    private WebResource createProjectResourceWithId(final Long projectId)
    {
        return createProjectResource(Long.toString(projectId));
    }

    private WebResource createProjectResource(final String projectIdOrKey)
    {
        return createResourceForPath(FUNC_TEST_PLUGIN_REST_PATH).path("project").path(projectIdOrKey);
    }
}
