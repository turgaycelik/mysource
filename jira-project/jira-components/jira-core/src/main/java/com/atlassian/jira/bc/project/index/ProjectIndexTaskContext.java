package com.atlassian.jira.bc.project.index;

import com.atlassian.jira.config.IndexTask;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.util.I18nHelper;

/**
 * Context for project index operations. There can be only one such operation
 * per project at any given time.
 *
 * @since v6.1
 */
public class ProjectIndexTaskContext implements IndexTask
{
    private static final long serialVersionUID = -1875765202444481474L;

    private final Long projectId;
    private final String projectName;

    public ProjectIndexTaskContext(Project project)
    {
        this.projectId = project.getId();
        this.projectName = project.getName();
    }

    @Override
    public String getTaskInProgressMessage(final I18nHelper i18n)
    {
        return i18n.getText("admin.notifications.reindex.in.progress.project", projectName);
    }

    @Override
    public String buildProgressURL(final Long taskId)
    {
        return "/secure/project/IndexProjectProgress.jspa?pid=" + projectId + "&taskId=" + taskId;
    }

    @Override
    public int hashCode()
    {
        return projectId.hashCode();
    }

    @Override
    public boolean equals(final Object obj)
    {
        return (obj != null) && (obj instanceof ProjectIndexTaskContext) && projectId.equals(((ProjectIndexTaskContext)obj).projectId);
    }
}
