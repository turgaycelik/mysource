package com.atlassian.jira.web.action.util;

/**
 * Interface which defines the util that tells us if the calendar has
 * a translation for the provided language.
 */
public interface CalendarLanguageUtil
{
    /**
     * Determines if a translation for a language exists
     *
     * @param language language
     * @return true if exists, false otherwise
     */
    boolean hasTranslationForLanguage(String language);

    /**
     * Returns a filename of the calendar file for a given language.
     *
     * @param language language
     * @return a filename if a translation file exists, null otherwise
     */
    String getCalendarFilenameForLanguage(String language);
}
