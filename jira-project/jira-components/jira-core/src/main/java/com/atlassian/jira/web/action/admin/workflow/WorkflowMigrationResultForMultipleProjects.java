package com.atlassian.jira.web.action.admin.workflow;

import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.collect.Maps;

import java.util.List;
import java.util.Map;

public class WorkflowMigrationResultForMultipleProjects implements WorkflowMigrationResult
{
    private final List<WorkflowMigrationResult> results;

    public WorkflowMigrationResultForMultipleProjects(List<WorkflowMigrationResult> results)
    {
        this.results = results;
    }

    @Override
    public int getResult()
    {
        for (WorkflowMigrationResult result : results)
        {
            if (result.getResult() != SUCCESS)
            {
                return result.getResult();
            }
        }

        return SUCCESS;
    }

    @Override
    public ErrorCollection getErrorCollection()
    {
        ErrorCollection allErrors = new SimpleErrorCollection();

        for (WorkflowMigrationResult result : results)
        {
            ErrorCollection projectErrors = result.getErrorCollection();
            if (projectErrors != null && projectErrors.hasAnyErrors())
            {
                allErrors.addErrorCollection(projectErrors);
            }
        }

        return allErrors;
    }

    @Override
    public int getNumberOfFailedIssues()
    {
        int totalNumber = 0;

        for (WorkflowMigrationResult result : results)
        {
            totalNumber += result.getNumberOfFailedIssues();
        }

        return totalNumber;
    }

    @Override
    public Map<Long, String> getFailedIssues()
    {
        Map<Long, String> allFailedIssues = Maps.newHashMap();

        for (WorkflowMigrationResult result : results)
        {
            Map<Long, String> projectFailedIssues = result.getFailedIssues();
            if (projectFailedIssues != null)
            {
                allFailedIssues.putAll(projectFailedIssues);
            }
        }

        return allFailedIssues;
    }
}
