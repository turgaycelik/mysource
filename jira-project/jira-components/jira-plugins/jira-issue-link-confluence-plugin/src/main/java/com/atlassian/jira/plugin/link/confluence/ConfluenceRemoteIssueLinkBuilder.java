package com.atlassian.jira.plugin.link.confluence;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.plugin.link.confluence.service.ConfluenceGlobalIdFactoryImpl;
import com.atlassian.jira.util.UrlBuilder;

/**
 * Builder class for {@link RemoteIssueLink} that points to a Confluence page.
 * @see RemoteIssueLinkBuilder
 */
public class ConfluenceRemoteIssueLinkBuilder
{
    private static final String TITLE = "Wiki Page";
    private static final String RELATIONSHIP = "Wiki Page";

    /**
     * Builds a RemoteIssueLink pointing to a page in Confluence.
     * @param appLink The application link to the Confluence instance.
     * @param pageId The identifier of the page.
     * @param issueId The JIRA issue that is being linked to the Confluence page.
     * @return A RemoteIssueLink pointing the JIRA issue to the Confluence page.
     */
    public RemoteIssueLink build(final ApplicationLink appLink, final String pageId, final Long issueId)
    {
        final String globalId = ConfluenceGlobalIdFactoryImpl.encode(appLink.getId(), pageId);

        return new RemoteIssueLinkBuilder()
                .issueId(issueId)
                .url(buildPageUrl(appLink, pageId))
                .title(TITLE)
                .globalId(globalId)
                .relationship(RELATIONSHIP)
                .applicationType(RemoteIssueLink.APPLICATION_TYPE_CONFLUENCE)
                .applicationName(appLink.getName())
                .build();
    }

    private static String buildPageUrl(final ApplicationLink appLink, final String pageId)
    {
        return new UrlBuilder(appLink.getRpcUrl().toASCIIString())
                .addPathUnsafe("pages/viewpage.action")
                .addParameter("pageId", pageId)
                .asUrlString();
    }
}
