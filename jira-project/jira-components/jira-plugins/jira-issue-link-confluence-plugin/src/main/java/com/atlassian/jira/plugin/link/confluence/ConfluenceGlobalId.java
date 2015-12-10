package com.atlassian.jira.plugin.link.confluence;

import com.atlassian.applinks.api.ApplicationLink;
import com.google.common.base.Objects;

/**
 * Represents the globalId for issue links to Confluence pages.
 *
 * @since v5.0
 */
public class ConfluenceGlobalId
{
    private final ApplicationLink applicationLink;
    private final String pageId;

    public ConfluenceGlobalId(final ApplicationLink applicationLink, final String pageId)
    {
        this.applicationLink = applicationLink;
        this.pageId = pageId;
    }

    public ApplicationLink getApplicationLink()
    {
        return applicationLink;
    }

    public String getPageId()
    {
        return pageId;
    }

    @Override
    public int hashCode()
    {
        return Objects.hashCode(applicationLink, pageId);
    }

    @Override
    public boolean equals(final Object obj)
    {
        if (obj instanceof ConfluenceGlobalId)
        {
            final ConfluenceGlobalId other = (ConfluenceGlobalId) obj;
            return Objects.equal(applicationLink, other.applicationLink)
                && Objects.equal(pageId, other.pageId);
        }
        else
        {
            return false;
        }
    }
}