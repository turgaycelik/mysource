package com.atlassian.jira.web.action.admin.importer.project;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.imports.project.ProjectImportService;
import com.atlassian.jira.imports.project.ProjectImportTaskContext;
import com.atlassian.jira.imports.project.core.BackupOverview;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.MappingResult;
import com.atlassian.jira.imports.project.core.ProjectImportData;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.taskprogress.TaskProgressInterval;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.task.ProvidesTaskProgress;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.json.JSONEscaper;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;

import java.util.Collection;
import java.util.Iterator;
import java.util.concurrent.Callable;

/**
 * Action that is used to display the project information contained in the backup XML selected in the
 * ProjectImportSelectBackup action and to submit the selected project to be processed by the importer.
 *
 * @since v3.13
 */
@WebSudoRequired
public class ProjectImportSelectProject extends JiraWebActionSupport
{
    private final ProjectImportService projectImportService;
    private final TaskManager taskManager;
    private final ProjectManager projectManager;

    private BackupOverview backupOverview = null;
    private String projectKey;
    private boolean overwrite;
    private boolean noBackupOverview = false;

    public ProjectImportSelectProject(final ProjectImportService projectImportService, final TaskManager taskManager, final ProjectManager projectManager)
    {
        this.projectImportService = projectImportService;
        this.taskManager = taskManager;
        this.projectManager = projectManager;
    }

    @Override
    public String doDefault() throws Exception
    {
        // Make sure that we have a backupOverview in the session
        if (getBackupOverview() == null)
        {
            addErrorMessage(getText("admin.project.import.select.project.no.projects"));
            noBackupOverview = true;
            return ERROR;
        }
        return super.doDefault();
    }

    @Override
    public void doValidation()
    {
        // Make sure that we have a backupOverview in the session
        if (getBackupOverview() == null)
        {
            addErrorMessage(getText("admin.project.import.select.project.no.projects"));
            noBackupOverview = true;
            return;
        }

        final BackupProject selectedProject = getBackupOverview().getProject(projectKey);

        final ProjectImportBean beanFromSession = ProjectImportBean.getProjectImportBeanFromSession();

        if (projectManager.getProjectObjByKey(projectKey) == null)
        {
            // Always set overwrite to true if we are creating the project for the user
            beanFromSession.getProjectImportOptions().setOverwriteProjectDetails(true);
        }
        else
        {
            // Set the over-write flag as provided
            beanFromSession.getProjectImportOptions().setOverwriteProjectDetails(overwrite);
        }

        projectImportService.validateBackupProjectImportableSystemLevel(getJiraServiceContext(), selectedProject,
            getBackupOverview().getBackupSystemInformation());
        projectImportService.validateDoMapping(getJiraServiceContext(), beanFromSession.getProjectImportOptions(), selectedProject,
            beanFromSession.getBackupOverview().getBackupSystemInformation());
    }

    @Override
    protected String doExecute() throws Exception
    {
        final ProjectImportBean beanFromSession = ProjectImportBean.getProjectImportBeanFromSession();
        beanFromSession.getProjectImportOptions().setSelectedProjectKey(projectKey);

        // We need an Error Collection for a long-running task that will outlive this request.
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        // Store this in the session object in case we want to retrieve it in a future request.
        beanFromSession.getTaskProgressInformation().setErrorCollection(errorCollection);

        final BackupProject project = beanFromSession.getBackupOverview().getProject(
            beanFromSession.getProjectImportOptions().getSelectedProjectKey());
        final JiraServiceContext serviceContext = new JiraServiceContextImpl(getLoggedInUser(), errorCollection);
        final TaskDescriptor<MappingResult> descriptor = taskManager.submitTask(new MappingResultCallable(beanFromSession.getProjectImportOptions(),
            project, beanFromSession.getBackupOverview().getBackupSystemInformation(), serviceContext, beanFromSession), getText(
            "admin.project.import.progress.task.description.map.and.validate", project.getProject().getName()), new ProjectImportTaskContext());
        // put the task descriptor id into the session for the progress action
        beanFromSession.getTaskProgressInformation().setTaskId(descriptor.getTaskId());

        return getRedirect("ProjectImportMappingProgress.jspa?redirectOnComplete=ProjectImportSummary!default.jspa");
    }

    /**
     * Method is used to populate the page with errors that may have been collected from the long running task
     * progress page.
     * @return  ERROR
     */
    public String doErrorFromProgress()
    {
        // Get error collection from session
        final ProjectImportBean projectImportBean = ProjectImportBean.getProjectImportBeanFromSession();
        final ErrorCollection errorCollection = projectImportBean.getTaskProgressInformation().getErrorCollection();

        if ((errorCollection != null) && errorCollection.hasAnyErrors())
        {
            addErrorCollection(errorCollection);
            // Clear it out now that we have shown the messages
            projectImportBean.getTaskProgressInformation().setErrorCollection(null);
        }

        projectKey = projectImportBean.getProjectImportOptions().getSelectedProjectKey();
        return ERROR;
    }

    public BackupOverview getBackupOverview()
    {
        if (backupOverview == null)
        {
            backupOverview = ProjectImportBean.getProjectImportBeanFromSession().getBackupOverview();
        }
        return backupOverview;
    }

    public boolean isOverwrite()
    {
        return overwrite;
    }

    public void setOverwrite(final boolean overwrite)
    {
        this.overwrite = overwrite;
    }

    public String getProjectKey()
    {
        return projectKey;
    }

    public void setProjectKey(final String projectKey)
    {
        this.projectKey = projectKey;
    }

    public boolean isNoBackupOverview()
    {
        return noBackupOverview;
    }

    public String getJsonProject(final BackupProject project)
    {
        final JiraServiceContext serviceContext = new JiraServiceContextImpl(getLoggedInUser(), new SimpleErrorCollection());
        final MessageSet messageSet = projectImportService.validateBackupProjectImportableSystemLevel(serviceContext, project,
            getBackupOverview().getBackupSystemInformation());
        final StringBuilder sb = new StringBuilder("{");
        sb.append("\"prj_name\": \"").append(jsonHtmlEscape(project.getProject().getName())).append('"');
        sb.append(", ");
        sb.append("\"prj_key\": \"").append(jsonHtmlEscape(project.getProject().getKey())).append('"');
        sb.append(", ");
        sb.append("\"prj_desc\": \"").append(jsonHtmlEscape(project.getProject().getDescription())).append('"');
        sb.append(", ");
        sb.append("\"prj_lead\": \"").append(jsonHtmlEscape(project.getProject().getLead())).append('"');
        sb.append(", ");
        sb.append("\"prj_url\": \"").append(jsonHtmlEscape(project.getProject().getUrl())).append('"');
        sb.append(", ");
        sb.append("\"prj_send\": \"").append(jsonHtmlEscape(project.getProject().getEmailSender())).append('"');
        sb.append(", ");
        sb.append("\"prj_iss\": \"").append(project.getIssueIds().size()).append('"');
        sb.append(", ");
        sb.append("\"prj_ass\": \"").append(jsonHtmlEscape(getAssigneeTypeString(project.getProject().getAssigneeType()))).append('"');
        sb.append(", ");
        sb.append("\"prj_comp\": \"").append(project.getProjectComponents().size()).append('"');
        sb.append(", ");
        sb.append("\"prj_ver\": \"").append(project.getProjectVersions().size()).append('"');
        sb.append(", ");
        sb.append("\"prj_imp\": ").append(!messageSet.hasAnyErrors());
        sb.append(", ");
        sb.append("\"errors\": ").append("[").append(makeJsArray(messageSet.getErrorMessages())).append("]");
        sb.append(", ");
        sb.append("\"warnings\": ").append("[").append(makeJsArray(messageSet.getWarningMessages())).append("]");

        sb.append("}");

        return sb.toString();
    }

    /**
     * Used to execute the getBackupOverview method on the ProjectImportService with a taskProgressSink. This
     * callable is passed to the taskManager for execution.
     */
    private class MappingResultCallable implements Callable<MappingResult>, ProvidesTaskProgress
    {
        private TaskProgressSink taskProgressSink;
        private final ProjectImportOptions projectImportOptions;
        private final BackupProject selectedProject;
        private final BackupSystemInformation backupSystemInformation;
        private final JiraServiceContext serviceContext;
        private final ProjectImportBean projectImportBean;

        public MappingResultCallable(final ProjectImportOptions projectImportOptions, final BackupProject selectedProject, final BackupSystemInformation backupSystemInformation, final JiraServiceContext serviceContext, final ProjectImportBean projectImportBean)
        {
            this.projectImportOptions = projectImportOptions;
            this.selectedProject = selectedProject;
            this.backupSystemInformation = backupSystemInformation;
            this.serviceContext = serviceContext;
            this.projectImportBean = projectImportBean;
        }

        public MappingResult call() throws Exception
        {
            // Task Progress interval for getProjectImportData. 0% - 50%
            TaskProgressInterval taskProgressInterval = new TaskProgressInterval(taskProgressSink, 0, 50);
            final ProjectImportData projectImportData = projectImportService.getProjectImportData(serviceContext, projectImportOptions,
                selectedProject, backupSystemInformation, taskProgressInterval);
            // Save this result in the session
            projectImportBean.setProjectImportData(projectImportData);
            if (projectImportData == null)
            {
                // There was an error getting the ProjectImportData. This will have been logged in the Error Collection.
                return null;
            }
            // Task Progress interval for getProjectImportData. 50% - 100%
            taskProgressInterval = new TaskProgressInterval(taskProgressSink, 50, 100);
            return projectImportService.doMapping(serviceContext, projectImportOptions, projectImportData, selectedProject, backupSystemInformation,
                taskProgressInterval);
        }

        public void setTaskProgressSink(final TaskProgressSink taskProgressSink)
        {
            this.taskProgressSink = taskProgressSink;
        }
    }

    private String makeJsArray(final Collection<String> elements)
    {
        final StringBuilder array = new StringBuilder();
        for (final Iterator<String> iterator = elements.iterator(); iterator.hasNext();)
        {
            final String msg = iterator.next();
            array.append('"').append(jsonHtmlEscape(msg)).append('"');
            if (iterator.hasNext())
            {
                array.append(",");
            }
        }
        return array.toString();
    }

    private String getAssigneeTypeString(final String assigneeType)
    {
        if (String.valueOf(AssigneeTypes.UNASSIGNED).equals(assigneeType))
        {
            return getText(AssigneeTypes.PRETTY_UNASSIGNED);
        }
        else if (String.valueOf(AssigneeTypes.PROJECT_LEAD).equals(assigneeType))
        {
            return getText(AssigneeTypes.PRETTY_PROJECT_LEAD);
        }
        else
        {
            return "";
        }
    }

    private String jsonHtmlEscape(final String str)
    {
        return JSONEscaper.escape(TextUtils.htmlEncode(str));
    }
}
