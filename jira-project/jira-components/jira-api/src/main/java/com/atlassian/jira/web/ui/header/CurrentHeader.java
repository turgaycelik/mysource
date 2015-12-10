package com.atlassian.jira.web.ui.header;

/**
 * Represents the JIRA header that is currently configured for the logged in user.
 *
 * @since v5.2
 * @deprecated Common header is always enabled.
 */
@Deprecated
public interface CurrentHeader
{
    /**
     * Retrieves the current header set for this JIRA instance.
     * @return A Header value that indicates the current header set for this JIRA instance.
     */
    Header get();

    /**
     * Represents the headers that can be displayed by this JIRA instance.
     */
    enum Header
    {
        CLASSIC,
        COMMON
    }
}
