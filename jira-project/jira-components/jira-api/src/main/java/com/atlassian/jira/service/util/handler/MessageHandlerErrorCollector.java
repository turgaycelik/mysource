package com.atlassian.jira.service.util.handler;

import com.atlassian.annotations.PublicApi;

import javax.annotation.Nullable;

/**
 * Message handlers should report here what they are doing
 * Depending on the context in which handlers are run (either normal production run as from a
 * scheduled service or in a test mode - a dry run - from UI) this information can be transparently
 * routed to appropriate place.
 * Consider passing here translated strings (i18n), do not pass i18n keys.
 *
 * @since v5.0
 */
@PublicApi
public interface MessageHandlerErrorCollector
{
    /**
     * Report an error.
     * @param error a serious problem which usually makes further hanling of given message pointless
     * @param e associated throwable or <code>null</code>
     */
    void error(String error, @Nullable Throwable e);

    /**
     * Report an error.
     * @param error a serious problem which usually makes further hanling of given message pointless
     */
    void error(String error);

    /**
     * Report a warning
     * @param warning a warning, but nothing critical for the handler
     */
    void warning(String warning);

    /**
     * Report a warning
     * @param warning a warning, but nothing critical for the handler
     * @param e associated throwable or <code>null</code>
     */
    void warning(String warning, @Nullable Throwable e);

    /**
     * Report valuable information for JIRA admin
     * @param info information message (something expected, OK, normal)
     */
    void info(String info);

    /**
     * Report valuable information for JIRA admin
     * @param info information message (something expected, OK, normal)
     * @param e associated throwable or <code>null</code>
     */
    void info(String info, @Nullable Throwable e);

}
