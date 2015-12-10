package com.atlassian.jira.web.action.setup;

/**
 * Defines an IndexLanguageToLocaleMapper.
 */
public interface IndexLanguageToLocaleMapper
{
    String getLanguageForLocale(String locale);
}
