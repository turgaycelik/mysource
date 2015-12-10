package com.atlassian.jira.workflow.migration;

import com.atlassian.analytics.api.annotations.Analytics;
import com.atlassian.jira.workflow.WorkflowScheme;

/**
 * Event indicating that workflow scheme migration has completed successfully.
 *
 * @since v5.2
 */
@Analytics("administration.projectconfig.workflowscheme.migrationcompleted")
public class WorkflowSchemeMigrationCompletedEvent
{
    private final WorkflowScheme scheme;
    private final boolean draftMigration;

    public WorkflowSchemeMigrationCompletedEvent(WorkflowScheme scheme)
    {
        this.scheme = scheme;
        draftMigration = scheme.isDraft();
    }

    public WorkflowScheme getScheme()
    {
        return scheme;
    }

    public boolean isDraftMigration()
    {
        return draftMigration;
    }

    @Override
    public boolean equals(Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        WorkflowSchemeMigrationCompletedEvent that = (WorkflowSchemeMigrationCompletedEvent) o;

        if (draftMigration != that.draftMigration) { return false; }
        if (scheme != null ? !scheme.equals(that.scheme) : that.scheme != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = scheme != null ? scheme.hashCode() : 0;
        result = 31 * result + (draftMigration ? 1 : 0);
        return result;
    }
}
