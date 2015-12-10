package com.atlassian.jira.web.filters.steps.i18n;

import com.atlassian.jira.util.i18n.I18nTranslationModeImpl;
import com.atlassian.jira.web.filters.steps.FilterCallContext;
import com.atlassian.jira.web.filters.steps.FilterStep;

/**
 * This ensures that the i18n mode thread local starts clear and finishes off
 *
 * @since v4.3
 */
public class I18nTranslationsModeThreadlocaleStep implements FilterStep
{
    private final I18nTranslationModeImpl i18nTranslationMode;

    public I18nTranslationsModeThreadlocaleStep()
    {
        i18nTranslationMode = new I18nTranslationModeImpl();
    }

    @Override
    public FilterCallContext beforeDoFilter(FilterCallContext callContext)
    {
        // we start with thread local off and then reply on it being switch on as appropriate
        i18nTranslationMode.setTranslationsModeOff();
        return callContext;
    }

    @Override
    public FilterCallContext finallyAfterDoFilter(FilterCallContext callContext)
    {
        // we end with thread local off
        i18nTranslationMode.setTranslationsModeOff();
        return callContext;
    }
}
