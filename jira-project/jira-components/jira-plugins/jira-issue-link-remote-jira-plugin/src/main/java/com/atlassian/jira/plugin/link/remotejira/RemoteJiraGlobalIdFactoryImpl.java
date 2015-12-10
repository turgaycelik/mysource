package com.atlassian.jira.plugin.link.remotejira;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.jira.plugin.viewissue.issuelink.GlobalIdFactory;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.google.common.collect.ImmutableList;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import java.util.Arrays;
import java.util.List;
import java.util.Map;

/**
 * Factory for encoding and decoding {@link RemoteJiraGlobalId}s.
 *
 * @since v5.0
 */
@Component
public class RemoteJiraGlobalIdFactoryImpl implements RemoteJiraGlobalIdFactory
{
    private static final String APP_ID_KEY = "appId";
    private static final String ISSUE_ID_KEY = "issueId";
    private static final List<String> KEYS = ImmutableList.of(APP_ID_KEY, ISSUE_ID_KEY);

    private final ApplicationLinkService applicationLinkService;

    @Autowired
    public RemoteJiraGlobalIdFactoryImpl(@ComponentImport final ApplicationLinkService applicationLinkService)
    {
        this.applicationLinkService = applicationLinkService;
    }

    public static String encode(final RemoteJiraGlobalId globalId)
    {
        return encode(globalId.getApplicationLink().getId(), globalId.getRemoteIssueId());
    }

    public static String encode(final ApplicationId applicationId, final Long issueId)
    {
        final Map<String, String> values = MapBuilder.<String, String>newBuilder()
                .add(APP_ID_KEY, applicationId.get())
                .add(ISSUE_ID_KEY, issueId.toString())
                .toMap();
        return GlobalIdFactory.encode(KEYS, values);
    }

    @Override
    public RemoteJiraGlobalId decode(final String globalId)
    {
        final Map<String, String> values = GlobalIdFactory.decode(globalId, KEYS);

        // Get application link
        final String appId = values.get(APP_ID_KEY);
        final ApplicationLink appLink = getApplicationLink(appId);
        if (appLink == null)
        {
            throw new IllegalArgumentException("No Application Link found for the given Application Id: " + appId);
        }

        // Get issue id
        final String issueIdString = values.get(ISSUE_ID_KEY);
        final Long issueId;
        try
        {
            issueId = Long.parseLong(issueIdString);
        }
        catch (final NumberFormatException e)
        {
            throw new IllegalArgumentException("Invalid Issue Id. Expecting intege, found: " + issueIdString, e);
        }

        return new RemoteJiraGlobalId(appLink, issueId);
    }

    private ApplicationLink getApplicationLink(final String appId)
    {
        for (final ApplicationLink appLink : applicationLinkService.getApplicationLinks(JiraApplicationType.class))
        {
            if (appLink.getId().get().equals(appId))
            {
                return appLink;
            }
        }

        return null;
    }
}
