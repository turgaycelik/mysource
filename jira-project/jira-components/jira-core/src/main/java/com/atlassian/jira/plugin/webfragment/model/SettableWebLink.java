package com.atlassian.jira.plugin.webfragment.model;

import com.atlassian.plugin.web.model.WebLink;

/**
 * Defines a setter for a weblink
 *
 * @since v3.12
 */
public interface SettableWebLink
{
    /**
     * Provides access to the passed {@link com.atlassian.plugin.web.model.WebLink}.
     *
     * @param link
     */
    public void setLink(WebLink link);
}
