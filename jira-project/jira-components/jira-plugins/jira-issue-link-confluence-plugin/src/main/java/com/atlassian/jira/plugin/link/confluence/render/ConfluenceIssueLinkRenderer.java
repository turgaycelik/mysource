package com.atlassian.jira.plugin.link.confluence.render;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.plugin.link.confluence.service.ConfluenceGlobalIdFactory;
import com.atlassian.jira.plugin.link.confluence.service.ConfluenceIssueLinkDecoratingService;
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
 * Renders issue links to Confluence pages.
 *
 * @since v5.0
 */
public class ConfluenceIssueLinkRenderer extends DefaultIssueLinkRenderer
{
    private static final Logger log = LoggerFactory.getLogger(ConfluenceIssueLinkRenderer.class);

    private final ConfluenceIssueLinkDecoratingService confluenceIssueLinkDecoratingService;
    private final ConfluenceGlobalIdFactory confluenceGlobalIdFactory;

    public ConfluenceIssueLinkRenderer(final ConfluenceIssueLinkDecoratingService confluenceIssueLinkDecoratingService,
                                       final ConfluenceGlobalIdFactory confluenceGlobalIdFactory)
    {
        this.confluenceIssueLinkDecoratingService = confluenceIssueLinkDecoratingService;
        this.confluenceGlobalIdFactory = confluenceGlobalIdFactory;
    }

    @Override
    public Map<String, Object> getFinalContext(RemoteIssueLink remoteIssueLink, final Map<String, Object> context)
    {
        Map<String, Object> finalContext = Maps.newHashMap();
        try
        {
            remoteIssueLink = confluenceIssueLinkDecoratingService.decorate(remoteIssueLink);
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
                final ApplicationLink applicationLink = confluenceGlobalIdFactory.create(remoteIssueLink).getApplicationLink();
                final String applicationName = StringUtils.defaultIfEmpty(applicationLink.getName(), i18n.getText("viewissue.links.types.confluencepage"));
                finalContext.put("authenticationRequired", Boolean.TRUE);
                finalContext.put("authenticationUrl", exception.getAuthorisationURI());
                finalContext.put("applicationName", applicationName);
                finalContext.put("appLinkId", applicationLink.getId());
                finalContext.put("applicationUrl", applicationLink.getDisplayUrl());
                remoteIssueLink = new RemoteIssueLinkBuilder(remoteIssueLink).applicationName(applicationName).build();
            }
        }
        catch (final PermissionException e)
        {
            final I18nHelper i18n = (I18nHelper) context.get("i18n");
            final ApplicationLink applicationLink = confluenceGlobalIdFactory.create(remoteIssueLink).getApplicationLink();
            final String applicationName = StringUtils.defaultIfEmpty(applicationLink.getName(), i18n.getText("viewissue.links.types.confluencepage"));
            finalContext.put("permissionDenied", Boolean.TRUE);
            finalContext.put("applicationName", applicationName);
            finalContext.put("applicationUrl", applicationLink.getDisplayUrl());
            remoteIssueLink = new RemoteIssueLinkBuilder(remoteIssueLink).applicationName(applicationName).build();
        }
        catch (final IOException e)
        {
            throw new RuntimeException(e);
        }

        // Use default icon title if needed
        if (StringUtils.isBlank(remoteIssueLink.getIconTitle()))
        {
            remoteIssueLink = new RemoteIssueLinkBuilder(remoteIssueLink)
                    .iconTitle("Page")
                    .build();
        }

        // Use default relationship if needed
        if (StringUtils.isBlank(remoteIssueLink.getRelationship()))
        {
            remoteIssueLink = new RemoteIssueLinkBuilder(remoteIssueLink)
                    .relationship("mentioned in")
                    .build();
        }

        finalContext.putAll(super.getInitialContext(remoteIssueLink, context));

        if (log.isDebugEnabled())
        {
            log.debug("ConfluenceIssueLinkRenderer: finalContext: " + finalContext);
        }

        return finalContext;
    }

    @Override
    public boolean requiresAsyncLoading(final RemoteIssueLink remoteIssueLink)
    {
        return true;
    }
}
