package com.atlassian.jira.web.action.admin.importer.project;

import com.atlassian.jira.imports.project.core.MappingResult;
import com.atlassian.jira.task.TaskManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.bean.TaskDescriptorBean;
import com.atlassian.sal.api.websudo.WebSudoRequired;

import java.util.concurrent.ExecutionException;

/**
 * Progress action for the 2nd phase of the import which creates the project mappings.
 *
 * @since v3.13
 */
@WebSudoRequired
public class ProjectImportMappingProgress extends AbstractProjectImportProgress<MappingResult>
{
    private final TaskManager taskManager;

    public ProjectImportMappingProgress(final TaskManager taskManager, final TaskDescriptorBean.Factory factory)
    {
        super(taskManager, factory);
        this.taskManager = taskManager;
    }

    @Override
    protected boolean taskIsComplete()
    {
        return ProjectImportBean.getProjectImportBeanFromSession().getMappingResult() != null;
    }

    protected String handleFinishedTask(final TaskDescriptorBean<MappingResult> ourTask) throws ExecutionException, InterruptedException
    {
        final MappingResult mappingResult = ourTask.getResult();
        if (mappingResult == null)
        {
            return getRedirect("ProjectImportSelectProject!errorFromProgress.jspa");
        }
        else
        {
            // Clean up after ourselves
            final ProjectImportBean beanFromSession = ProjectImportBean.getProjectImportBeanFromSession();
            taskManager.removeTask(beanFromSession.getTaskProgressInformation().getTaskId());
            beanFromSession.getTaskProgressInformation().setTaskId(null);
            beanFromSession.setMappingResult(mappingResult);
            final ErrorCollection errorCollection = beanFromSession.getTaskProgressInformation().getErrorCollection();
            if ((errorCollection != null) && !errorCollection.hasAnyErrors())
            {
                beanFromSession.getTaskProgressInformation().setErrorCollection(null);
            }
            return getRedirect(getRedirectOnComplete());
        }
    }

    @Override
    protected String getSubmitUrl()
    {
        return "ProjectImportMappingProgress.jspa";
    }
}
