package com.atlassian.jira.util.i18n;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * This allows a plugin or JIRA to set a special translations mode flag on for the current thread.  The I18n text
 * generated after that will be is a special meta data mode.
 * <p/>
 * JIRA will check the HttpServletRequest for sensibleness however and hence it may not be set.
 *
 * @since 4.3
 */
public interface I18nTranslationMode
{
    /**
     * @return true if the I18n translations mode is on for this thread
     */
    boolean isTranslationMode();

    /**
     * Attempts to turn on the i18n translations mode.  JIRA may decide that its not sensible to turn it on
     *
     * @param httpServletRequest the request in play
     * @param httpServletResponse the response in play
     */
    void setTranslationsModeOn(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse);

    /**
     * Turns off the i18n translations mode for this thread.
     */
    void setTranslationsModeOff();
}
