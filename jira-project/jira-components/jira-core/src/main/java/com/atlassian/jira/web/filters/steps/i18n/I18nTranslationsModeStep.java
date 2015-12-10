package com.atlassian.jira.web.filters.steps.i18n;

import com.atlassian.jira.util.i18n.I18nTranslationModeSwitch;
import com.atlassian.jira.web.filters.steps.FilterCallContext;
import com.atlassian.jira.web.filters.steps.FilterStep;

/**
 * @see I18nTranslationModeSwitch
 */
public class I18nTranslationsModeStep implements FilterStep
{
    @Override
    public FilterCallContext beforeDoFilter(FilterCallContext callContext)
    {
        // This will check for the magic parameter and turn on the i18n translations mode if present
        new I18nTranslationModeSwitch().switchTranslationsMode(callContext.getHttpServletRequest(), callContext.getHttpServletResponse());
        return callContext;
    }

    @Override
    public FilterCallContext finallyAfterDoFilter(FilterCallContext callContext)
    {
        return callContext;
    }
}
