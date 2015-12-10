package com.atlassian.jira.web.action.admin.importer.project;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.imports.project.ProjectImportService;
import com.atlassian.jira.imports.project.ProjectImportTaskContext;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.BackupSystemInformation;
import com.atlassian.jira.imports.project.core.MappingResult;
import com.atlassian.jira.imports.project.core.MappingResult.ValidationMessage;
import com.atlassian.jira.imports.project.core.ProjectImportData;
import com.atlassian.jira.imports.project.core.ProjectImportOptions;
import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.imports.project.taskprogress.TaskProgressInterval;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.task.ProvidesTaskProgress;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.johnson.JohnsonEventContainer;
import com.atlassian.johnson.event.Event;
import com.atlassian.johnson.event.EventLevel;
import com.atlassian.johnson.event.EventType;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import com.opensymphony.util.TextUtils;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import javax.servlet.ServletContext;
import java.util.List;
import java.util.concurrent.Callable;

/**
 * Action that controls the summary screen before a project import occur
 *
 * @since v3.13
 */
@WebSudoRequired
public class ProjectImportSummary extends JiraWebActionSupport
{
    private static final String PROJECT_IMPORT_JOHNSON_MSG = "JIRA is currently performing a project import. Depending on how large the imported project is, this may take any where from a few minutes to a few hours. Jira will automatically become available as soon as this task is complete.";
    private static final String REPETITIVE_SPACES_REGEXP = "(\\s(?=\\s)|(?<=\\s)\\s)";
    private static final String CONTAINS_REPETITIVE_SPACES_REGEXP = ".*(\\s(?=\\s)|(?<=\\s)\\s).*";

    private final ProjectImportService projectImportService;
    private final TaskManager taskManager;
    private MappingResult mappingResult;
    private ProjectImportData projectImportData;

    public ProjectImportSummary(final ProjectImportService projectImportService, final TaskManager taskManager)
    {
        this.projectImportService = projectImportService;
        this.taskManager = taskManager;
    }

    @Override
    public String doDefault() throws Exception
    {
        // Make sure that we have a MappingResult in the session
        if (getMappingResult() == null)
        {
            addErrorMessage(getText("admin.project.import.summary.no.mapping.result"));
            return ERROR;
        }

        // If there were errors in the waiting pages error collection we need to display them here
        final ProjectImportBean projectImportBean = ProjectImportBean.getProjectImportBeanFromSession();
        if (projectImportBean.getTaskProgressInformation().getErrorCollection() != null)
        {
            addErrorCollection(projectImportBean.getTaskProgressInformation().getErrorCollection());
        }

        return super.doDefault();
    }

    @Override
    public void doValidation()
    {
        // Make sure that we have a MappingResult in the session
        if (getProjectImportData() == null)
        {
            addErrorMessage(getText("admin.project.import.summary.no.project.import.data"));
        }
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final ProjectImportBean beanFromSession = ProjectImportBean.getProjectImportBeanFromSession();
        // Check to see if the Previous button was clicked, if so we should redirect
        if (ActionContext.getParameters().get("prevButton") != null)
        {
            return forceRedirect("ProjectImportSelectProject!default.jspa?projectKey=" + beanFromSession.getSelectedProject().getProject().getKey());
        }
        else if (ActionContext.getParameters().get("refreshValidationButton") != null)
        {
            return forceRedirect("ProjectImportSummary!reMapAndValidate.jspa");
        }

        // We need an Error Collection for a long-running task that will outlive this request.
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        // Store this in the session object in case we want to retrieve it in a future request.
        beanFromSession.getTaskProgressInformation().setErrorCollection(errorCollection);

        final BackupProject project = beanFromSession.getSelectedProject();
        final ProjectImportData projectImportData = beanFromSession.getProjectImportData();

        final JiraServiceContext serviceContext = new JiraServiceContextImpl(getLoggedInUser(), errorCollection);
        final ProjectImportCallable callableTask = new ProjectImportCallable(beanFromSession.getProjectImportOptions(), projectImportData, project,
            beanFromSession.getBackupOverview().getBackupSystemInformation(), serviceContext, beanFromSession);
        final TaskDescriptor<ProjectImportResults> descriptor = taskManager.submitTask(callableTask, getText(
            "admin.project.import.progress.task.description.importing", project.getProject().getName()), new ProjectImportTaskContext());
        // put the task descriptor id into the session for the progress action
        beanFromSession.getTaskProgressInformation().setTaskId(descriptor.getTaskId());

        return getRedirect("/secure/admin/ProjectImportProgress.jspa?redirectOnComplete=ProjectImportResults.jspa");
    }

    public String doReMapAndValidate()
    {
        final ProjectImportBean beanFromSession = ProjectImportBean.getProjectImportBeanFromSession();

        // First lets clear out the mapping results
        beanFromSession.setMappingResult(null);
        // We need an Error Collection for a long-running task that will outlive this request.
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        // Store this in the session object in case we want to retrieve it in a future request.
        beanFromSession.getTaskProgressInformation().setErrorCollection(errorCollection);

        final BackupProject project = beanFromSession.getBackupOverview().getProject(
            beanFromSession.getProjectImportOptions().getSelectedProjectKey());
        final ProjectImportData projectImportData = beanFromSession.getProjectImportData();

        // Clear any mapped values that may have been added to the ProjectImportMapper
        projectImportData.getProjectImportMapper().clearMappedValues();

        final JiraServiceContext serviceContext = new JiraServiceContextImpl(getLoggedInUser(), errorCollection);
        final ReMappingResultCallable mappingResultCallable = new ReMappingResultCallable(beanFromSession.getProjectImportOptions(),
            projectImportData, project, beanFromSession.getBackupOverview().getBackupSystemInformation(), serviceContext);
        final TaskDescriptor<MappingResult> descriptor = taskManager.submitTask(mappingResultCallable, getText(
            "admin.project.import.progress.task.description.map.and.validate", project.getProject().getName()), new ProjectImportTaskContext());
        // put the task descriptor id into the session for the progress action
        beanFromSession.getTaskProgressInformation().setTaskId(descriptor.getTaskId());

        return getRedirect("ProjectImportMappingProgress.jspa?redirectOnComplete=ProjectImportSummary!default.jspa");
    }

    public List<ValidationMessage> getSystemFieldsValidateMessages()
    {
        return getMappingResult().getSystemFieldsMessageList();
    }

    public List<ValidationMessage> getCustomFieldsValidateMessages()
    {
        return getMappingResult().getCustomFieldsMessageList();
    }

    public boolean isCanImport()
    {
        return getMappingResult().canImport();
    }

    public String getProjectName()
    {
        final ProjectImportBean beanFromSession = ProjectImportBean.getProjectImportBeanFromSession();
        final BackupProject project = beanFromSession.getSelectedProject();
        if (project == null)
        {
            return "";
        }
        return project.getProject().getName();
    }

    public String escapeValuePreserveSpaces(final String value)
    {
        final String escapedValue = TextUtils.htmlEncode(value);
        // Replace all occurrences of more than one space with a character that will be obvious
        if (escapedValue.matches(CONTAINS_REPETITIVE_SPACES_REGEXP))
        {
            return escapedValue.replaceAll(REPETITIVE_SPACES_REGEXP, "-") + " " + getText("admin.project.import.summary.repetitive.spaces");
        }
        return escapedValue;
    }

    public MappingResult getMappingResult()
    {
        if (mappingResult == null)
        {
            mappingResult = ProjectImportBean.getProjectImportBeanFromSession().getMappingResult();
        }
        return mappingResult;
    }

    private ProjectImportData getProjectImportData()
    {
        if (projectImportData == null)
        {
            projectImportData = ProjectImportBean.getProjectImportBeanFromSession().getProjectImportData();
        }
        return projectImportData;
    }

    /**
     * Used to execute the getBackupOverview method on the ProjectImportService with a taskProgressSink. This
     * callable is passed to the taskManager for execution.
     */
    private class ReMappingResultCallable implements Callable<MappingResult>, ProvidesTaskProgress
    {
        private TaskProgressSink taskProgressSink;
        private final ProjectImportOptions projectImportOptions;
        private final ProjectImportData projectImportData;
        private final BackupProject selectedProject;
        private final BackupSystemInformation backupSystemInformation;
        private final JiraServiceContext serviceContext;

        public ReMappingResultCallable(final ProjectImportOptions projectImportOptions, final ProjectImportData projectImportData, final BackupProject selectedProject, final BackupSystemInformation backupSystemInformation, final JiraServiceContext serviceContext)
        {
            this.projectImportOptions = projectImportOptions;
            this.projectImportData = projectImportData;
            this.selectedProject = selectedProject;
            this.backupSystemInformation = backupSystemInformation;
            this.serviceContext = serviceContext;
        }

        public MappingResult call() throws Exception
        {
            // Build a TaskProgressInterval just for the Mapping.
            final TaskProgressInterval taskProgressInterval = new TaskProgressInterval(taskProgressSink, 0, 100);
            return projectImportService.doMapping(serviceContext, projectImportOptions, projectImportData, selectedProject, backupSystemInformation,
                taskProgressInterval);
        }

        public void setTaskProgressSink(final TaskProgressSink taskProgressSink)
        {
            this.taskProgressSink = taskProgressSink;
        }
    }

    /**
     * Used to execute the actual import with a taskProgressSink. This
     * callable is passed to the taskManager for execution.
     */
    private class ProjectImportCallable implements Callable<ProjectImportResults>, ProvidesTaskProgress
    {
        private TaskProgressSink taskProgressSink;
        private final ProjectImportOptions projectImportOptions;
        private final ProjectImportData projectImportData;
        private final BackupProject selectedProject;
        private final BackupSystemInformation backupSystemInformation;
        private final Event johnsonEvent;
        private final JiraServiceContext serviceContext;
        private final ProjectImportBean projectImportBean;

        public ProjectImportCallable(final ProjectImportOptions projectImportOptions, final ProjectImportData projectImportData, final BackupProject selectedProject, final BackupSystemInformation backupSystemInformation, final JiraServiceContext serviceContext, final ProjectImportBean projectImportBean)
        {
            this.projectImportOptions = projectImportOptions;
            this.projectImportData = projectImportData;
            this.selectedProject = selectedProject;
            this.backupSystemInformation = backupSystemInformation;
            this.serviceContext = serviceContext;
            this.projectImportBean = projectImportBean;

            johnsonEvent = new Event(EventType.get("project-import"), PROJECT_IMPORT_JOHNSON_MSG, EventLevel.get(EventLevel.WARNING));
        }

        public ProjectImportResults call() throws Exception
        {
            // We want to wrap this entire operation in a Johnson event so that the rest of JIRA will become inaccessable
            JohnsonEventContainer eventCont = null;
            final ServletContext ctx = ServletActionContext.getServletContext();
            if (ctx != null)
            {
                // Don't really know why we would not have a ServletContext but all the other Johnson callers seem to try
                // to be this safe
                eventCont = JohnsonEventContainer.get(ctx);
                eventCont.addEvent(johnsonEvent);
            }
            try
            {
                // Since we have thrown up a Johnson event we NOW assume that no one else can change the state of JIRA
                // so we want to run through all the validation once again.
                projectImportService.validateBackupProjectImportableSystemLevel(serviceContext, selectedProject, backupSystemInformation);
                projectImportService.validateDoMapping(serviceContext, projectImportOptions, selectedProject, backupSystemInformation);
                final ErrorCollection errorCollection = serviceContext.getErrorCollection();
                if (errorCollection.hasAnyErrors())
                {
                    // Redirect to the project select page + add an error saying why the import stopped
                    errorCollection.addErrorMessage(getText("admin.project.import.summary.error.project.validation"));
                    projectImportBean.setProjectImportData(null);
                    projectImportBean.setMappingResult(null);
                    return null;
                }
                // Lets re-do all the mappings and validation to make sure they are still valid
                projectImportData.getProjectImportMapper().clearMappedValues();
                // Build a TaskProgressInterval for doMapping. 0% - 5%
                final TaskProgressInterval taskProgressInterval = new TaskProgressInterval(taskProgressSink, 0, 5);
                final MappingResult mappingResult = projectImportService.doMapping(serviceContext, projectImportOptions, projectImportData,
                    selectedProject, backupSystemInformation, taskProgressInterval);
                if (errorCollection.hasAnyErrors())
                {
                    // Redirect to the project import summary page and display the errors + add an error saying why the import stopped
                    errorCollection.addErrorMessage(getText("admin.project.import.summary.error.mapping.validation"));
                    projectImportBean.setMappingResult(mappingResult);
                    return null;
                }

                // Finally, now that all the validation passes lets get to doing this thing!!
                // Lets give the main portion of the import 95% of the progress bar
                final TaskProgressInterval importSubInterval = new TaskProgressInterval(taskProgressSink, 5, 100);
                // Note that the ProjectImportResultsAction cleans up the Session Objects.
                return projectImportService.doImport(serviceContext, projectImportOptions, selectedProject, backupSystemInformation,
                    projectImportData, importSubInterval);
            }
            finally
            {
                // Be sure that we always remove the event we throw up
                if (eventCont != null)
                {
                    eventCont.removeEvent(johnsonEvent);
                }
            }
        }

        public void setTaskProgressSink(final TaskProgressSink taskProgressSink)
        {
            this.taskProgressSink = new JohnsonUpdatingTaskProgressSink(johnsonEvent, taskProgressSink);
        }
    }

    /**
     * Used to propagate the tasks progress information from the task progress sink, which the client code is reporting
     * progress to, to the Johnson event which is keeping the rest of JIRA from being accessed.
     */
    private static class JohnsonUpdatingTaskProgressSink implements TaskProgressSink
    {
        private final Event johnsonEvent;
        private final TaskProgressSink delegate;

        public JohnsonUpdatingTaskProgressSink(final Event johnsonEvent, final TaskProgressSink taskProgressSink)
        {
            this.johnsonEvent = johnsonEvent;
            delegate = taskProgressSink;
        }

        public void makeProgress(final long taskProgress, final String currentSubTask, final String message)
        {
            // Delegate the make Progress to our task progress sink
            delegate.makeProgress(taskProgress, currentSubTask, message);
            // Set the progress on the Johnson event too.
            johnsonEvent.setProgress((int) taskProgress);
        }
    }
}
