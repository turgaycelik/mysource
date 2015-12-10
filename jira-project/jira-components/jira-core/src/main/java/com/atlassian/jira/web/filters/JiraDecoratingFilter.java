package com.atlassian.jira.web.filters;

import com.atlassian.jira.web.filters.steps.ChainedFilterStepRunner;
import com.atlassian.jira.web.filters.steps.FilterStep;
import com.atlassian.jira.web.filters.steps.pagebuilder.PageBuilderStep;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * A filter that runs decoration. Runs before Sitemesh decoration.
 * @since 6.1
 */
public class JiraDecoratingFilter extends ChainedFilterStepRunner
{
    @Override
    protected List<FilterStep> getFilterSteps()
    {
        return Lists.<FilterStep>newArrayList(
                new PageBuilderStep()
        );
    }
}
