package com.atlassian.jira.web.action.admin.importer.project;

import com.atlassian.jira.imports.project.core.BackupOverview;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.web.bean.TaskDescriptorBean;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.concurrent.ExecutionException;

/**
 * Used to show a progress bar for the long running tasks after the Select Backup action.
 *
 * @since v3.13
 */
@WebSudoRequired
public class ProjectImportBackupOverviewProgress extends AbstractProjectImportProgress<BackupOverview>
{
    private final TaskManager taskManager;

    public ProjectImportBackupOverviewProgress(final TaskManager taskManager, final TaskDescriptorBean.Factory factory)
    {
        super(taskManager, factory);
        this.taskManager = taskManager;
    }

    @Override
    protected boolean taskIsComplete()
    {
        return ProjectImportBean.getProjectImportBeanFromSession().getBackupOverview() != null;
    }

    @Override
    protected String handleFinishedTask(final TaskDescriptorBean<BackupOverview> ourTask) throws ExecutionException, InterruptedException
    {
        final BackupOverview backupOverview = ourTask.getResult();
        if (backupOverview == null)
        {
            return getRedirect("ProjectImportSelectBackup!errorFromProgress.jspa");
        }
        else
        {
            // Clean up after ourselves
            final ProjectImportBean projectImportBean = ProjectImportBean.getProjectImportBeanFromSession();
            taskManager.removeTask(projectImportBean.getTaskProgressInformation().getTaskId());
            projectImportBean.getTaskProgressInformation().setTaskId(null);
            projectImportBean.getTaskProgressInformation().setErrorCollection(null);
            projectImportBean.setBackupOverview(backupOverview);
            return getRedirect(getRedirectOnComplete());
        }
    }

    @Override
    protected String getSubmitUrl()
    {
        return "ProjectImportBackupOverviewProgress.jspa";
    }
}
