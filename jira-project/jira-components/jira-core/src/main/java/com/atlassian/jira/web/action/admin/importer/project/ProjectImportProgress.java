package com.atlassian.jira.web.action.admin.importer.project;

import com.atlassian.jira.imports.project.core.ProjectImportResults;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.web.bean.TaskDescriptorBean;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.concurrent.ExecutionException;

/**
 * Used to show a progress bar for the long running tasks while we are doing the actual import.
 *
 * @since v3.13
 */
@WebSudoRequired
public class ProjectImportProgress extends AbstractProjectImportProgress<ProjectImportResults>
{
    private final TaskManager taskManager;

    public ProjectImportProgress(final TaskManager taskManager, final TaskDescriptorBean.Factory factory)
    {
        super(taskManager, factory);
        this.taskManager = taskManager;
    }

    @Override
    protected boolean taskIsComplete()
    {
        return ProjectImportBean.getProjectImportBeanFromSession().getProjectImportResults() != null;
    }

    @Override
    protected String handleFinishedTask(final TaskDescriptorBean<ProjectImportResults> ourTask) throws ExecutionException, InterruptedException
    {
        final ProjectImportResults projectImportResults = ourTask.getResult();
        final ProjectImportBean projectImportBean = ProjectImportBean.getProjectImportBeanFromSession();
        if (projectImportResults == null)
        {
            // This means we have failed pre-import re-validation, now we need to see at what level we failed, the session bean will let us know
            if (projectImportBean.getProjectImportData() == null)
            {
                // This means that the project is no longer "importable" and we should head back to the project select screen
                return getRedirect("ProjectImportSelectProject!errorFromProgress.jspa");
            }
            else
            {
                // This means the mapping and valiation failed and we should go back to the project import summary screen
                return getRedirect("ProjectImportSummary!default.jspa");
            }
        }
        else
        {
            // Import was started and generated results, clean up after ourselves and head off to the post-import results screen
            taskManager.removeTask(projectImportBean.getTaskProgressInformation().getTaskId());
            projectImportBean.getTaskProgressInformation().setTaskId(null);
            // Don't delete the error collection it will be used on the next screen
            projectImportBean.setProjectImportResults(projectImportResults);
            return getRedirect(getRedirectOnComplete());
        }
    }

    @Override
    protected String getSubmitUrl()
    {
        return "ProjectImportProgress.jspa";
    }
}
