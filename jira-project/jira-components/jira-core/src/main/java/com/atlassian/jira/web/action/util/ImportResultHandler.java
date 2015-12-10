package com.atlassian.jira.web.action.util;

import com.atlassian.jira.bc.dataimport.DataImportService;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;

import javax.servlet.ServletContext;

/**
 * A generic handler to deal with larger problems during a data import.
 *
 * @since v4.4
 */
public interface ImportResultHandler
{
    /**
     * Given an import result this method will take the appropriate actions to deal with errors such as throwing
     * up JohnsonEvents or populating the passed in errorcollection with the appropriate errors.
     *
     * @param context The servlet context needed to throw up a JohnsonEvent.
     * @param lastResult The import result with validation errors.
     * @param i18n I18n bean needed to i18nize any error messages
     * @param errorCollection The error collection to populate with specific error messages
     * @return true if a JohnsonEvent was created meaning callers should probably redirect to a generic error page to see the JohnsonEvent.
     */
    boolean handleErrorResult(ServletContext context, DataImportService.ImportResult lastResult, I18nHelper i18n, ErrorCollection errorCollection);
}
