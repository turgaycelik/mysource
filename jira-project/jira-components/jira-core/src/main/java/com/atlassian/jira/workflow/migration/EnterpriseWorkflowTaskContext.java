package com.atlassian.jira.workflow.migration;

import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.project.Project;
import com.atlassian.jira.task.TaskContext;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.jira.web.action.admin.workflow.scheme.SelectProjectWorkflowScheme;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import org.apache.commons.lang.StringUtils;

import static org.apache.commons.collections.CollectionUtils.intersection;
import static org.apache.commons.collections.CollectionUtils.isNotEmpty;

/**
 * The context used by enterprise workflow migration.  Only unique within project via .equals()
 *
 * @since v3.13
 */
public class EnterpriseWorkflowTaskContext implements TaskContext
{
    private static final long serialVersionUID = -8380874131082626949L;

    private final Long triggerProjectId;
    private final Long schemeId;
    private final boolean draftMigration;
    private final List<Long> projectIds;
    private boolean safeToDelete;

    public EnterpriseWorkflowTaskContext(Project triggerProject)
    {
        this(triggerProject, null, true);
    }

    public EnterpriseWorkflowTaskContext(final Project triggerProject, final Long schemeId, boolean draftMigration)
    {
        this(triggerProject, Arrays.asList(triggerProject), schemeId, draftMigration);
        Assertions.notNull("triggerProject", triggerProject);
    }

    public EnterpriseWorkflowTaskContext(final Project triggerProject, final List<Project> projects, final Long schemeId, boolean draftMigration)
    {
        Assertions.notEmpty("projects", projects);
        this.schemeId = schemeId;
        this.triggerProjectId = triggerProject != null ? triggerProject.getId() : null;
        this.projectIds = getProjectIds(projects);
        this.draftMigration = draftMigration;
    }

    private ImmutableList<Long> getProjectIds(final List<Project> projects)
    {
        return ImmutableList.copyOf(Lists.transform(projects, new Function<Project, Long>()
        {
            @Override
            public Long apply(Project project)
            {
                return project.getId();
            }
        }));
    }

    public String buildProgressURL(final Long taskId)
    {
        String url = "/secure/project/SelectProjectWorkflowSchemeStep3.jspa?taskId=" + taskId + "&draftMigration=" + isDraftMigration() + "&projectIdsParameter=" + getProjectIdString();
        if (triggerProjectId != null)
        {
            url = url + "&projectId=" + triggerProjectId;
        }
        if (getSchemeId() != null)
        {
            url = url + "&schemeId=" + getSchemeId();
        }
        return url;
    }

    public Long getSchemeId()
    {
        return schemeId;
    }

    public boolean isDraftMigration()
    {
        return draftMigration;
    }

    private String getProjectIdString()
    {
        return StringUtils.join(projectIds, ",");
    }

    public boolean isSafeToDelete()
    {
        return safeToDelete;
    }

    public void markSafeToDelete()
    {
        this.safeToDelete = true;
    }

    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final EnterpriseWorkflowTaskContext that = (EnterpriseWorkflowTaskContext) o;

        return isNotEmpty(intersection(projectIds, that.projectIds));
    }

    public int hashCode()
    {
        // The equals method checks for intersection of the 2 project collections.
        // Don't see a better alternative than a constant for hashCode.
        return 0;
    }

    @VisibleForTesting
    public Long getTriggerProjectId()
    {
        return triggerProjectId;
    }
}
