package com.atlassian.jira.plugin.link.remotejira;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.plugin.viewissue.issuelink.DefaultIssueLinkRenderer;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.IOException;
import java.net.URI;
import java.util.Map;

/**
 * Renders remote JIRA issue links.
 *
 * @since v5.0
 */
public class RemoteJiraIssueLinkRenderer extends DefaultIssueLinkRenderer
{
    private static final Logger log = LoggerFactory.getLogger(RemoteJiraIssueLinkRenderer.class);

    private final JiraRemoteIssueLinkDecoratingService jiraRemoteIssueLinkDecoratingService;
    private final RemoteJiraGlobalIdFactory remoteJiraGlobalIdFactory;

    public RemoteJiraIssueLinkRenderer(final JiraRemoteIssueLinkDecoratingService jiraRemoteIssueLinkDecoratingService,
            final RemoteJiraGlobalIdFactory remoteJiraGlobalIdFactory)
    {
        this.jiraRemoteIssueLinkDecoratingService = jiraRemoteIssueLinkDecoratingService;
        this.remoteJiraGlobalIdFactory = remoteJiraGlobalIdFactory;
    }

    @Override
    public Map<String, Object> getFinalContext(RemoteIssueLink remoteIssueLink, Map<String, Object> context)
    {
        Map<String, Object> finalContext = Maps.newHashMap();
        try
        {
            remoteIssueLink = jiraRemoteIssueLinkDecoratingService.decorate(remoteIssueLink);
        }
        catch (final CredentialsRequiredException exception)
        {
            final URI authorisationURI = exception.getAuthorisationURI();
            final I18nHelper i18n = (I18nHelper) context.get("i18n");
            if (authorisationURI == null)
            {
                finalContext.put("noApplinkAuthConfigured", Boolean.TRUE);
            }
            else
            {
                final ApplicationLink applicationLink = remoteJiraGlobalIdFactory.decode(remoteIssueLink.getGlobalId()).getApplicationLink();
                final String applicationName = StringUtils.defaultIfEmpty(applicationLink.getName(), i18n.getText("viewissue.links.types.remoteissue"));
                finalContext.put("authenticationRequired", Boolean.TRUE);
                finalContext.put("authenticationUrl", exception.getAuthorisationURI());
                finalContext.put("applicationName", applicationName);
                finalContext.put("appLinkId", applicationLink.getId());
                finalContext.put("applicationUrl", applicationLink.getDisplayUrl());
                remoteIssueLink = new RemoteIssueLinkBuilder(remoteIssueLink).applicationName(applicationName).build();
            }
        }
        catch (PermissionException e)
        {
            final I18nHelper i18n = (I18nHelper) context.get("i18n");
            final ApplicationLink applicationLink = remoteJiraGlobalIdFactory.decode(remoteIssueLink.getGlobalId()).getApplicationLink();
            final String applicationName = StringUtils.defaultIfEmpty(applicationLink.getName(), i18n.getText("viewissue.links.types.remoteissue"));
            finalContext.put("permissionDenied", Boolean.TRUE);
            finalContext.put("applicationName", applicationName);
            finalContext.put("applicationUrl", applicationLink.getDisplayUrl());
            remoteIssueLink = new RemoteIssueLinkBuilder(remoteIssueLink).applicationName(applicationName).build();
        }
        catch (IOException e)
        {
            throw new RuntimeException(e);
        }
        finalContext.putAll(super.getInitialContext(remoteIssueLink, context));

        if (log.isDebugEnabled())
        {
            log.debug("RemoteJiraIssueLinkRenderer: finalContext: " + finalContext);
        }

        return finalContext;
    }

    @Override
    public boolean requiresAsyncLoading(RemoteIssueLink remoteIssueLink)
    {
        return true;
    }
}
