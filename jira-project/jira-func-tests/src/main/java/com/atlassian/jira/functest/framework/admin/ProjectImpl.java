package com.atlassian.jira.functest.framework.admin;

import com.atlassian.jira.functest.framework.AbstractFuncTestUtil;
import com.atlassian.jira.functest.framework.FunctTestConstants;
import com.atlassian.jira.functest.framework.Navigation;
import com.atlassian.jira.functest.framework.assertions.Assertions;
import com.atlassian.jira.functest.framework.backdoor.Backdoor;
import com.atlassian.jira.functest.framework.util.AsynchronousTasks;
import com.atlassian.jira.functest.framework.util.form.FormParameterUtil;
import com.atlassian.jira.testkit.beans.WorkflowSchemeData;
import com.atlassian.jira.testkit.client.ProjectControl;
import com.atlassian.jira.testkit.client.restclient.*;
import com.atlassian.jira.webtests.util.JIRAEnvironmentData;
import com.sun.jersey.api.client.UniformInterfaceException;
import net.sourceforge.jwebunit.WebTester;

import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.CoreMatchers.nullValue;
import static org.junit.Assert.assertThat;

/**
 * Implementation of Project interface
 *
 * @since v3.13
 */
public class ProjectImpl extends AbstractFuncTestUtil implements Project
{
    private final Backdoor backdoor;
    private final AsynchronousTasks asynchronousTasks;
    private final ProjectControl projectControl;

    public ProjectImpl(final Backdoor backdoor, WebTester tester, JIRAEnvironmentData environmentData, Navigation navigation, Assertions assertions,
            AsynchronousTasks asynchronousTasks)
    {
        super(tester, environmentData, 2);

        this.backdoor = backdoor;
        this.asynchronousTasks = asynchronousTasks;
        this.projectControl = new ProjectControl(environmentData);
    }

    public long addProject(String name, String key, String lead)
    {
        long projectIdFromKey = getProjectIdFromKey(key);
        if (projectIdFromKey != -1L)
        {
            log("Project " + name + " exists ");
            return projectIdFromKey;
        }
        else
        {
            log("Adding project " + name);
            return projectControl.addProject(name, key, lead);
        }
    }

    @Override
    public void editProject(long projectId, String name, String description, String url)
    {
        tester.gotoPage("/secure/project/EditProject!default.jspa?pid=" + projectId);
        tester.assertTextPresent("action=\"EditProject.jspa\"");

        if (name != null)
        {
            tester.setFormElement("name", name);
        }

        if (description != null)
        {
            tester.setFormElement("description", description);
        }

        if (url != null)
        {
            tester.setFormElement("url", url);
        }

        tester.submit();
        String projectKey = getProjectKeyFromId(projectId);
        com.atlassian.jira.testkit.client.restclient.Project project =
                backdoor.project().getProject(projectKey);
        if (name != null)
        {
            assertThat(project.name, equalTo(name));
        }

        if (description != null)
        {
            assertThat(project.description, equalTo(description));
        }

        if (url != null)
        {
            assertThat(project.url, equalTo(url));
        }
    }

    public void deleteProject(long projectId)
    {
        tester.gotoPage("/secure/admin/DeleteProject!default.jspa?pid=" + projectId);
        tester.submit("Delete");
    }

    public void deleteProject(String projectName)
    {
        deleteProject(getProjectIdFromName(projectName));
    }

    public String addComponent(final String projectKey, final String componentName, final String description, final String leadUserName)
    {

        ComponentClient componentClient = new ComponentClient(environmentData);

        final Component component = componentClient.create(new Component().project(projectKey).name(componentName).description(description).leadUserName(leadUserName));

        return "" + component.id;
    }

    public String addVersion(final String projectKey, final String versionName, final String description, final String releaseDate)
    {
        VersionClient versionClient = new VersionClient(environmentData);
        final Version version = new Version();
        version.project(projectKey).name(versionName).description(description).userReleaseDate(releaseDate);
        final Version newVersion = versionClient.create(version);

        return "" + newVersion.id;
    }

    private String getProjectKeyFromId(Long projectId)
    {
        final List<com.atlassian.jira.testkit.client.restclient.Project> projects = backdoor.project().getProjects();

        for (com.atlassian.jira.testkit.client.restclient.Project project : projects)
        {
            if (Long.parseLong(project.id) == projectId)
            {
                return project.key;
            }
        }
        return null;
    }

    private long getProjectIdFromName(String projectName)
    {
        final List<com.atlassian.jira.testkit.client.restclient.Project> projects = backdoor.project().getProjects();

        for (com.atlassian.jira.testkit.client.restclient.Project project : projects)
        {
            if (project.name.equals(projectName))
            {
                return Long.valueOf(project.id);
            }
        }
        throw new IllegalArgumentException("Project '" + projectName + "' does not exist.");
    }


    private Version getVersionByName(String projectKey, String versionName)
    {
        final List<Version> versions = backdoor.project().getVersionsForProject(projectKey);

        for (Version version : versions)
        {
            if (version.name.equals(versionName))
            {
                return version;
            }
        }

        return null;

    }

    private Component getComponentByName(String projectKey, String componentName)
    {
        final List<Component> components = backdoor.project().getComponentsForProject(projectKey);
        for (Component component : components)
        {
            if (component.name.equals(componentName))
            {
                return component;
            }
        }

        return null;

    }

    public void archiveVersion(final String projectKey, final String versionName)
    {
        final Version versionByName = getVersionByName(projectKey, versionName);

        VersionClient versionClient = new VersionClient(environmentData);
        versionClient.putResponse(versionByName.archived(true));

    }

    public void releaseVersion(final String projectKey, final String versionName, final String releaseDate)
    {
        final Version versionByName = getVersionByName(projectKey, versionName);

        VersionClient versionClient = new VersionClient(environmentData);
        versionClient.putResponse(versionByName.released(true).userReleaseDate(releaseDate));
    }

    public void unreleaseVersion(final String projectKey, final String versionName, final String releaseDate)
    {
        final Version versionByName = getVersionByName(projectKey, versionName);

        VersionClient versionClient = new VersionClient(environmentData);
        versionClient.putResponse(versionByName.released(false).userReleaseDate(releaseDate));
    }

    public void deleteVersion(final String projectKey, final String versionName)
    {
        final Version versionByName = getVersionByName(projectKey, versionName);

        VersionClient versionClient = new VersionClient(environmentData);
        versionClient.delete("" + versionByName.id);
    }

    public void editVersionDetails(final String projectKey, final String versionName, final String name, final String description, final String releaseDate)
    {
        final Version versionByName = getVersionByName(projectKey, versionName);
        if (name != null)
        {
            versionByName.name(name);
        }
        if (description != null)
        {
            versionByName.description(description);
        }
        if (releaseDate != null)
        {
            versionByName.userReleaseDate(releaseDate);
        }

        VersionClient versionClient = new VersionClient(environmentData);
        versionClient.putResponse(versionByName);
    }

    public void editComponent(final String projectKey, final String componentName, final String name, final String description, final String leadUserName)
    {
        ComponentClient componentClient = new ComponentClient(environmentData);

        final Component component = getComponentByName(projectKey, componentName);

        if (name != null)
        {
            component.name(name);
        }
        if (description != null)
        {
            component.description(description);
        }
        if (leadUserName != null)
        {
            component.leadUserName(leadUserName);
        }
        componentClient.putResponse(component);
    }

    public void associateFieldConfigurationScheme(final String projectName, String newFieldConfigurationSchemeName)
    {
        final Long projectId = getProjectIdFromName(projectName);


        tester.gotoPage("/secure/admin/SelectFieldLayoutScheme!default.jspa?projectId=" + projectId);
        tester.setWorkingForm(FunctTestConstants.JIRA_FORM_NAME);
        if (newFieldConfigurationSchemeName == null)
        {
            newFieldConfigurationSchemeName = "System Default Field Configuration";
        }
        tester.selectOption("schemeId", newFieldConfigurationSchemeName);
        tester.submit("Associate");

        assertThat(backdoor.project().getSchemes(projectId).fieldConfigurationScheme.name, equalTo(newFieldConfigurationSchemeName));
    }

    public void associateIssueLevelSecurityScheme(final String projectName, String newIssueLevelSecuritySchemeName)
    {
        final Long projectId = getProjectIdFromName(projectName);

        tester.gotoPage("/secure/project/SelectProjectIssueSecurityScheme!default.jspa?projectId=" + projectId);
        tester.setWorkingForm(FunctTestConstants.JIRA_FORM_NAME);

        String issueLevelSecuritySchemeNameToSet = newIssueLevelSecuritySchemeName;
        if (issueLevelSecuritySchemeNameToSet == null)
        {
            issueLevelSecuritySchemeNameToSet = "None";
        }
        tester.selectOption("newSchemeId", issueLevelSecuritySchemeNameToSet);
        tester.submit("Next >>");
        tester.submit("Associate");

        if (newIssueLevelSecuritySchemeName == null)
        {
            assertThat(backdoor.project().getSchemes(projectId).issueSecurityScheme, nullValue());
        }
        else
        {
            assertThat(backdoor.project().getSchemes(projectId).issueSecurityScheme.name, equalTo(newIssueLevelSecuritySchemeName));
        }
    }

    public void associateWorkflowScheme(String projectName, String workflowSchemeName, Map<String, String> statusMapping, boolean wait)
    {
        tester.gotoPage("/secure/project/SelectProjectWorkflowScheme!default.jspa?projectId=" + getProjectIdFromName(projectName));

        // We want to select the default workflow scheme for a project but it is already selected
        // and there are no workflow schemes available.
        if (workflowSchemeName.equals("Default") && tester.getDialog().getElement("schemeId_select") == null)
        {
            return;
        }

        tester.setWorkingForm(FunctTestConstants.JIRA_FORM_NAME);
        tester.selectOption("schemeId", workflowSchemeName);
        tester.submit("Associate");

        associateWorkflowScheme(statusMapping, wait);
    }

    @Override
    public void createWorkflowSchemeDraft(String projectKey)
    {
        WorkflowSchemeData workflowSchemeData = backdoor.workflowSchemes().getWorkflowSchemeByProjectKey(projectKey);
        // If it is the default workflow scheme, then copy it and set it on the project (cannot create draft of
        // default workflow).
        if (workflowSchemeData.getId() == null)
        {
            workflowSchemeData = backdoor.workflowSchemes().createScheme(workflowSchemeData);
            backdoor.project().setWorkflowScheme(projectKey, workflowSchemeData.getId());
        }
        backdoor.workflowSchemes().createDraft(workflowSchemeData);
    }

    @Override
    public void assignToDraftScheme(String projectKey, String workflowName, String... issueTypeIds)
    {
        final WorkflowSchemeData workflowSchemeData = backdoor.workflowSchemes().getWorkflowSchemeByProjectKey(projectKey);
        workflowSchemeData.setDefaultWorkflow(workflowName);
        for (String issueTypeId : issueTypeIds)
        {
            workflowSchemeData.setMapping(issueTypeId, workflowName);
        }
        backdoor.workflowSchemes().updateDraftScheme(workflowSchemeData.getId(), workflowSchemeData);
    }

    @Override
    public void associateWorkflowScheme(String projectName, String workflowSchemeName)
    {
        associateWorkflowScheme(projectName, workflowSchemeName, null, true);
    }

    public void publishWorkflowSchemeDraft(String projectName, Long schemeId, Map<String, String> statusMapping, boolean wait)
    {
        tester.gotoPage("/secure/project/SelectProjectWorkflowSchemeStep2!default.jspa?projectId=" + getProjectIdFromName(projectName)
                + "&schemeId=" + schemeId + "&draftMigration=true");

        associateWorkflowScheme(statusMapping, wait);
    }

    private void associateWorkflowScheme(Map<String, String> statusMapping, boolean wait)
    {
        boolean thereAreIssuesToMigrate = !locators.id("workflow-associate-noissues").exists();

        if (statusMapping != null && !statusMapping.isEmpty())
        {
            // Select status mappings
            for (Map.Entry<String, String> entry : statusMapping.entrySet())
            {
                tester.selectOption(entry.getKey(), entry.getValue());
            }
        }
        // We may get back to the workflow configuration page if there was no need
        // for confirmation
        if (!tester.getDialog().hasSubmitButton("Associate"))
        {
            return;
        }
        tester.submit("Associate");

        if (thereAreIssuesToMigrate && wait)
        {
            waitForWorkflowMigration(1000, 100);
        }
    }

    @Override
    public void associateNotificationScheme(String projectKey, String notificationSchemeName)
    {
        final Long projectId = getProjectIdFromKey(projectKey);
        tester.gotoPage("secure/project/SelectProjectScheme!default.jspa?projectId=" + projectId);
        tester.setWorkingForm(FunctTestConstants.JIRA_FORM_NAME);
        tester.selectOption("schemeIds", notificationSchemeName);
        tester.submit("Associate");
    }

    @Override
    public void waitForWorkflowMigration(long sleepTime, int retryCount)
    {
        asynchronousTasks.waitForSuccessfulCompletion(sleepTime, retryCount, "Workflow Migration");
    }

    public void setProjectLead(final String projectName, final String userName)
    {
        final Long projectId = getProjectIdFromName(projectName);
        tester.gotoPage("secure/project/EditProjectLeadAndDefaultAssignee!default.jspa?pid=" + projectId);

        FormParameterUtil formParameterUtil = new FormParameterUtil(tester, "project-edit-lead-and-default-assignee", "Update");
        formParameterUtil.addOptionToHtmlSelect("lead", new String[] { userName });
        formParameterUtil.setFormElement("lead", userName);
        formParameterUtil.submitForm();
    }

    public boolean projectWithKeyExists(final String projectKey)
    {
        log("Checking if project with key '" + projectKey + "' exists");
        return getProjectIdFromKey(projectKey) != -1L;
    }

    private long getProjectIdFromKey(String projectKey)
    {
        try
        {
            return backdoor.project().getProjectId(projectKey);
        }
        catch (UniformInterfaceException e)
        {
            return -1L;
        }
    }
}
