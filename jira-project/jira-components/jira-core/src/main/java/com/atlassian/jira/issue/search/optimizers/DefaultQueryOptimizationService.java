package com.atlassian.jira.issue.search.optimizers;

import javax.annotation.Nullable;

import com.atlassian.query.Query;

public class DefaultQueryOptimizationService implements QueryOptimizationService
{
    @Override
    public Query optimizeQuery(@Nullable final Query query)
    {
        if(query == null)
        {
            return null;
        }

        final Query unreleasedVersionsOptimized = optimizeUnreleasedVersionsFunction(query);
        return optimizeReleasedVersionsFunction(unreleasedVersionsOptimized);
    }

    private Query optimizeUnreleasedVersionsFunction(final Query query)
    {
        final UnreleasedVersionsFunctionOptimizer optimizer = new UnreleasedVersionsFunctionOptimizer();

        return optimizer.createOptimizedQuery(query);
    }

    private Query optimizeReleasedVersionsFunction(final Query query)
    {
        final ReleasedVersionsFunctionOptimizer optimizer = new ReleasedVersionsFunctionOptimizer();

        return optimizer.createOptimizedQuery(query);
    }
}
