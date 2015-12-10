package com.atlassian.jira.web.filters;

import com.atlassian.jira.web.filters.steps.ChainedFilterStepRunner;
import com.atlassian.jira.web.filters.steps.FilterStep;
import com.atlassian.jira.web.filters.steps.requestinfo.RequestInfoLastStep;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * This is the last filter that is run during a web request to JIRA.  At this point you know that the request has been
 * wrapped to within an inch of its life and is ready to be passed to a servlet in order to do some work
 * <p/>
 * You are at the inner most entry point for the request in the filter chain.
 * <p/>
 * After extensive market research, this filter has been carefully named to indicate that its the "last" filter and it
 * should remain that way.
 *
 * @since v4.2
 */
public class JiraLastFilter extends ChainedFilterStepRunner
{
    @Override
    protected List<FilterStep> getFilterSteps()
    {
        return Lists.<FilterStep>newArrayList(
                new RequestInfoLastStep()
        );
    }

}
