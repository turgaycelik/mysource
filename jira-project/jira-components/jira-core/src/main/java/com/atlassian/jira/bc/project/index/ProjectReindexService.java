package com.atlassian.jira.bc.project.index;

import com.atlassian.jira.project.Project;

/**
 *
 * @since v6.1
 */
public interface ProjectReindexService
{
    String reindex(Project project);

    String reindex(Project project, boolean updateReplicatedIndexStore);

    boolean isReindexPossible(Project project);
}
