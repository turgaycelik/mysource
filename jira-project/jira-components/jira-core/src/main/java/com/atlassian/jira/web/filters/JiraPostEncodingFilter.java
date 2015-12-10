package com.atlassian.jira.web.filters;

import com.atlassian.jira.web.filters.steps.ChainedFilterStepRunner;
import com.atlassian.jira.web.filters.steps.FilterStep;
import com.atlassian.jira.web.filters.steps.i18n.I18nTranslationsModeStep;
import com.atlassian.jira.web.filters.steps.requestcleanup.RequestCleanupStep;
import com.atlassian.jira.web.filters.steps.requestcleanup.WebworkActionCleanupStep;
import com.google.common.collect.Lists;

import java.util.List;

/**
 * A filter than runs immediately after the character encoding in JIRA has been set
 */
public class JiraPostEncodingFilter extends ChainedFilterStepRunner
{
    @Override
    protected List<FilterStep> getFilterSteps()
    {
        return Lists.newArrayList(
                new I18nTranslationsModeStep(),
                new RequestCleanupStep(),
                new WebworkActionCleanupStep()
        );
    }
}
