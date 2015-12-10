package com.atlassian.jira.web.action.admin.importer.project;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.imports.project.ProjectImportService;
import com.atlassian.jira.config.util.JiraHome;
import com.atlassian.jira.imports.project.ProjectImportTaskContext;
import com.atlassian.jira.imports.project.core.BackupOverview;
import com.atlassian.jira.imports.project.core.BackupProject;
import com.atlassian.jira.imports.project.core.ProjectImportOptionsImpl;
import com.atlassian.jira.issue.AttachmentManager;
import com.atlassian.jira.task.ProvidesTaskProgress;
import com.atlassian.jira.task.TaskDescriptor;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.task.TaskProgressSink;
import com.atlassian.jira.util.BuildUtilsInfo;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.util.HelpUtil;
import com.atlassian.sal.api.websudo.WebSudoRequired;
import org.apache.commons.io.FilenameUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.File;
import java.io.IOException;
import java.util.concurrent.Callable;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Action that controls the first step of the project import wizard.
 *
 * @since v3.13
 */
@WebSudoRequired
public class ProjectImportSelectBackup extends JiraWebActionSupport
{
    // In order to manage this Logger with all the other ProjectImport stuff, we use the generic package hierarchy
    private final static Logger log = Logger.getLogger("com.atlassian.jira.imports.project.web.action.ProjectImportSelectBackup");

    private final ProjectImportService projectImportService;
    private final TaskManager taskManager;
    private final BuildUtilsInfo buildUtilsInfo;
    private final JiraHome jiraHome;
    private final AttachmentManager attachmentManager;

    private String backupXmlPath;

    public ProjectImportSelectBackup(final ProjectImportService projectImportService, final TaskManager taskManager, final BuildUtilsInfo buildUtilsInfo, final JiraHome jiraHome, final AttachmentManager attachmentManager)
    {
        this.projectImportService = projectImportService;
        this.taskManager = taskManager;
        this.buildUtilsInfo = notNull("buildUtilsInfo", buildUtilsInfo);
        this.jiraHome = notNull("jiraHome", jiraHome);
        this.attachmentManager = notNull("attachmentManager", attachmentManager);
    }

    @Override
    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    public String doCancel() throws Exception
    {
        // Just clear out the ProjectImportBean from the session
        ProjectImportBean.clearFromSession();
        log.info("Project Import cancelled by the user.");

        return doDefault();
    }

    @Override
    public void doValidation()
    {
        // This will add any validation errors that may exist
        projectImportService.validateGetBackupOverview(getJiraServiceContext(), new ProjectImportOptionsImpl(getSafeBackupXmlFilename(), getDefaultImportAttachmentsPath()));
    }

    @Override
    protected String doExecute() throws Exception
    {
        // Clear out any session values that may be left over
        ProjectImportBean.clearFromSession();

        // We need an Error Collection for a long-running task that will outlive this request.
        final ErrorCollection errorCollection = new SimpleErrorCollection();
        // Store this in the session object in case we want to retrieve it in a future request.
        final ProjectImportBean beanFromSession = ProjectImportBean.getProjectImportBeanFromSession();
        beanFromSession.getTaskProgressInformation().setErrorCollection(errorCollection);
        beanFromSession.setProjectImportOptions(new ProjectImportOptionsImpl(getSafeBackupXmlFilename(), getDefaultImportAttachmentsPath()));

        final JiraServiceContextImpl serviceContext = new JiraServiceContextImpl(getLoggedInUser(), errorCollection);
        final TaskDescriptor<BackupOverview> descriptor = taskManager.submitTask(new BackupOverviewCallable(serviceContext),
            getText("admin.project.import.progress.task.description"), new ProjectImportTaskContext());
        // put the task descriptor id into the session for the progress action
        beanFromSession.getTaskProgressInformation().setTaskId(descriptor.getTaskId());

        return getRedirect("ProjectImportBackupOverviewProgress.jspa?redirectOnComplete=ProjectImportSelectProject!default.jspa");
    }

    /**
     * Method is used to populate the page with errors that may have been collected from the long running task
     * progress page.
     * @return  ERROR
     */
    public String doErrorFromProgress()
    {
        // Get error collection from session
        final ErrorCollection errorCollection = ProjectImportBean.getProjectImportBeanFromSession().getTaskProgressInformation().getErrorCollection();

        if ((errorCollection != null) && errorCollection.hasAnyErrors())
        {
            addErrorCollection(errorCollection);
        }

        final ProjectImportBean beanFromSession = ProjectImportBean.getProjectImportBeanFromSession();
        backupXmlPath = new File(beanFromSession.getProjectImportOptions().getPathToBackupXml()).getName();
        return ERROR;
    }

    public String getBackupXmlPath()
    {
        return backupXmlPath;
    }

    public void setBackupXmlPath(final String backupXmlPath)
    {
        this.backupXmlPath = backupXmlPath;
    }

    /**
     * Returns the absolution path for the Default Import directory ([jira-home/import]).
     *
     * @return the absolute path for the Default Import directory ([jira-home/import])
     */
    public String getDefaultImportPath()
    {
        return jiraHome.getImportDirectory().getAbsolutePath();
    }

    /**
     * Returns the absolution path for the Default Import Attachments directory ([jira-home/import/attachments]) if
     * attachments are enabled
     *
     * @return the absolute path for the Default Import Attachments directory ([jira-home/import/attachments]) or null
     * if attachments are not enabled
     */
    public String getDefaultImportAttachmentsPath()
    {
        if(attachmentManager.attachmentsEnabled())
        {
            return jiraHome.getImportAttachmentsDirectory().getAbsolutePath();
        }
        else
        {
            return null;
        }
    }

    private String getSafeBackupXmlFilename()
    {
        if(StringUtils.isEmpty(getBackupXmlPath())) {
            return null;
        }
        else
        {
            File xmlPath = new File(getBackupXmlPath());
            final File importXmlPath = new File(getDefaultImportPath(), xmlPath.getName());
            try
            {
                return importXmlPath.getCanonicalPath();
            }
            catch (IOException e)
            {
                return FilenameUtils.normalize(importXmlPath.getAbsolutePath());
            }
        }
    }

    /**
     * Returns true if the user has an active Project Import that has not completed.
     * This means we show a link on this page to let the user resume that import.
     *
     * @return true if the user has an active Project Import that has not completed.
     */
    public boolean isShowResumeLinkStep2()
    {
        final ProjectImportBean projectImportBean = ProjectImportBean.getProjectImportBeanFromSession();
        return (projectImportBean.getBackupOverview() != null) && !isShowResumeLinkStep3();
    }

    public boolean isShowResumeLinkStep3()
    {
        final ProjectImportBean projectImportBean = ProjectImportBean.getProjectImportBeanFromSession();
        return projectImportBean.getProjectImportData() != null;
    }

    public String getSelectedProjectName()
    {

        final ProjectImportBean projectImportBean = ProjectImportBean.getProjectImportBeanFromSession();
        final BackupProject backupProject = projectImportBean.getSelectedProject();
        if (backupProject != null)
        {
            return backupProject.getProject().getName();
        }
        return null;
    }

    public String getDocsLink()
    {
        final HelpUtil.HelpPath helpPath = HelpUtil.getInstance().getHelpPath("restore_project");
        if (helpPath != null)
        {
            return helpPath.getUrl();
        }
        return null;
    }

    public String getVersion()
    {
        return buildUtilsInfo.getVersion();
    }

    /**
     * Used to execute the getBackupOverview method on the ProjectImportService with a taskProgressSink. This
     * callable is passed to the taskManager for execution.
     */
    private class BackupOverviewCallable implements Callable<BackupOverview>, ProvidesTaskProgress
    {
        private TaskProgressSink taskProgressSink;
        private final JiraServiceContext serviceContext;

        public BackupOverviewCallable(final JiraServiceContext serviceContext)
        {
            this.serviceContext = serviceContext;
        }

        public BackupOverview call() throws Exception
        {
            return projectImportService.getBackupOverview(serviceContext, new ProjectImportOptionsImpl(getSafeBackupXmlFilename(), getDefaultImportAttachmentsPath()),
                taskProgressSink);
        }

        public void setTaskProgressSink(final TaskProgressSink taskProgressSink)
        {
            this.taskProgressSink = taskProgressSink;
        }
    }
}
