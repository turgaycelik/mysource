package com.atlassian.jira.plugin.link.confluence.service;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.confluence.ConfluenceApplicationType;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.plugin.link.confluence.ConfluenceGlobalId;
import com.atlassian.jira.plugin.viewissue.issuelink.GlobalIdFactory;
import com.atlassian.jira.util.UriMatcher;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.List;
import java.util.Map;

/**
 * Factory for encoding and decoding {@link ConfluenceGlobalId}s.
 *
 * @since v5.0
 */
@Component
@ExportAsService
public class ConfluenceGlobalIdFactoryImpl implements ConfluenceGlobalIdFactory
{
    private static final String APP_ID_KEY = "appId";
    private static final String PAGE_ID_KEY = "pageId";
    private static final List<String> KEYS = ImmutableList.of(APP_ID_KEY, PAGE_ID_KEY);

    private final ApplicationLinkService applicationLinkService;

    @Autowired
    public ConfluenceGlobalIdFactoryImpl(@ComponentImport final ApplicationLinkService applicationLinkService)
    {
        this.applicationLinkService = applicationLinkService;
    }

    public static String encode(final ApplicationId applicationId, final String pageId)
    {
        final Map<String, String> values = ImmutableMap.of(APP_ID_KEY, applicationId.get(), PAGE_ID_KEY, pageId);
        return GlobalIdFactory.encode(KEYS, values);
    }

    @Override
    public ConfluenceGlobalId create(final RemoteIssueLink link)
    {
        final Map<String, String> values = GlobalIdFactory.decode(link.getGlobalId(), KEYS);

        // Get application link
        final String appId = values.get(APP_ID_KEY);
        final ApplicationLink appLink = getApplicationLink(appId, link.getUrl());
        if (appLink == null)
        {
            throw new IllegalArgumentException("No Application Link found for the given Application Id: "
                    + appId + " or URL: " + link.getUrl());
        }

        // Get issue id
        final String pageId = values.get(PAGE_ID_KEY);

        return new ConfluenceGlobalId(appLink, pageId);
    }

    private ApplicationLink getApplicationLink(final String appId, final String url)
    {
        ApplicationLink urlLink = null;
        int matchLength = -1;
        URI uri;

        try
        {
            uri = url != null ? new URI(url) : null;
        }
        catch (URISyntaxException e)
        {
            uri = null;
        }

        for (final ApplicationLink appLink : applicationLinkService.getApplicationLinks(ConfluenceApplicationType.class))
        {
            if (appLink.getId().get().equals(appId))
            {
                return appLink;
            }
            else if (uri != null && UriMatcher.isBaseEqual(appLink.getDisplayUrl(), uri))
            {
                int length = appLink.getDisplayUrl().getPath().length();
                if (length > matchLength)
                {
                    urlLink = appLink;
                    matchLength = length;
                }
            }
        }

        return urlLink;
    }
}
