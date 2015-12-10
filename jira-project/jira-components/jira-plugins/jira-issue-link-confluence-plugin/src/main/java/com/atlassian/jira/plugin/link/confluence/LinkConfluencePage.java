package com.atlassian.jira.plugin.link.confluence;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.plugin.link.applinks.RemoteResponse;
import com.atlassian.jira.plugin.link.confluence.service.ConfluencePageService;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.action.issue.AbstractIssueLinkAction;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.net.ResponseException;
import com.atlassian.velocity.htmlsafe.HtmlSafe;

import org.apache.commons.httpclient.HttpStatus;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.Map;

import static org.apache.commons.lang.StringUtils.isBlank;

public class LinkConfluencePage extends AbstractIssueLinkAction
{
    private final ConfluenceApplicationLinks confluenceAppLinks;
    private final ConfluencePageService confluencePageService;

    private String pageUrl;
    private ApplicationLink appLink;
    private Collection<ApplicationLink> appLinks;

    public LinkConfluencePage(
            @ComponentImport final SubTaskManager subTaskManager,
            @ComponentImport final FieldScreenRendererFactory fieldScreenRendererFactory,
            @ComponentImport final FieldManager fieldManager,
            @ComponentImport final ProjectRoleManager projectRoleManager,
            @ComponentImport final CommentService commentService,
            @ComponentImport final UserUtil userUtil,
            @ComponentImport final RemoteIssueLinkService remoteIssueLinkService,
            @ComponentImport final EventPublisher eventPublisher,
            final ConfluenceApplicationLinks confluenceAppLinks,
            final ConfluencePageService confluencePageService)
    {
        super(subTaskManager, fieldScreenRendererFactory, fieldManager, projectRoleManager, commentService, userUtil, remoteIssueLinkService, eventPublisher);
        this.confluenceAppLinks = confluenceAppLinks;
        this.confluencePageService = confluencePageService;
    }

    @Override
    public boolean isValidToView()
    {
        return super.isValidToView() && !getAppLinks().isEmpty();
    }

    protected void doValidation()
    {
        super.doValidation();

        validateUrl(pageUrl);
        if (hasAnyErrors())
        {
            return;
        }

        pageUrl = ConfluencePageUrl.build(pageUrl, appLink).getUrlRebasedToRpcUrl();
        String pageId = getPageId(pageUrl, appLink);

        if (!hasAnyErrors())
        {
            if (pageId == null)
            {
                addErrorMessage(getText("addconfluencelink.error.pageid.notfound"));
            }
        }

        if (!hasAnyErrors())
        {
            RemoteIssueLink remoteIssueLink = new ConfluenceRemoteIssueLinkBuilder().build(appLink, pageId, getIssue().getLong("id"));
            validationResult = remoteIssueLinkService.validateCreate(getLoggedInUser(), remoteIssueLink);

            if (!validationResult.isValid())
            {
                mapErrors(validationResult.getErrorCollection());
                addErrorCollection(validationResult.getErrorCollection());
            }
        }
    }

    private void mapErrors(final ErrorCollection errorCollection)
    {
        // Convert field errors to error messages so that they will appear on the page
        // Hide the field name (key), as this will mean nothing to users
        for (final Map.Entry<String, String> entry : errorCollection.getErrors().entrySet())
        {
            if ("globalId".equals(entry.getKey()))
            {
                // Give a more meaningful message when a duplicate link exists
                errorCollection.addErrorMessage(getText("addconfluencelink.error.duplicate"));
            }
            else
            {
                errorCollection.addErrorMessage(entry.getValue());
            }
        }
    }

    private void validateUrl(final String pageUrl)
    {
        if (isBlank(pageUrl))
        {
            addError("pageUrl", getText("addconfluencelink.error.url.required"));
            return;
        }

        URI pageUri;
        try
        {
            pageUri = new URI(pageUrl);
        }
        catch (URISyntaxException e)
        {
            addError("pageUrl", getText("addconfluencelink.error.url.invalid"));
            return;
        }

        if (!isThereApplicationLinkFor(pageUri))
        {
            addErrorMessage(getText("addconfluencelink.error.no.matching.app.link", "<a href='#' class='confluence-search-trigger'>", "</a>"));
        }
    }

    private boolean isThereApplicationLinkFor(final URI pageUri)
    {
        appLink = confluenceAppLinks.forPage(pageUri).getOrNull();
        return appLink != null;
    }

    private String getPageId(final String pageUrl, final ApplicationLink appLink)
    {
        // Always use a GET to fetch the pageId (see: JRADEV-8435)
        try
        {
            final RemoteResponse<String> response = confluencePageService.getPageId(appLink, pageUrl);
            if (response.isSuccessful())
            {
                return response.getEntity();
            }

            switch (response.getStatusCode())
            {
                case HttpStatus.SC_FORBIDDEN:
                {
                    addErrorMessage(getText("addconfluencelink.error.page.forbidden"));
                    break;
                }
                case HttpStatus.SC_UNAUTHORIZED:
                {
                    handleCredentialsRequired();
                    break;
                }
                default:
                {
                    addErrorMessage(getText("addconfluencelink.error.pageid.notfound"));
                    log.error("Invalid response from getting the pageId: " + response.getStatusCode() + ": " + response.getStatusText());
                }
            }
        }
        catch (final CredentialsRequiredException e)
        {
            handleCredentialsRequired();
        }
        catch (final ResponseException e)
        {
            addErrorMessage(getText("addconfluencelink.error.pageid.notfound"));
            log.error("Invalid response from getting the pageId: " + e.getMessage());
        }

        return null;
    }

    public String doDefault() throws Exception
    {
        // Set default value
        pageUrl = "http://";

        return super.doDefault();
    }

    @RequiresXsrfCheck
    protected String doExecute()
    {
        final RemoteIssueLinkService.RemoteIssueLinkResult result = createLink();

        if (!result.isValid())
        {
            addErrorCollection(result.getErrorCollection());
            return ERROR;
        }

        createComment();

        return returnComplete(getRedirectUrl());
    }

    @SuppressWarnings("unused")
    public String getPageUrl()
    {
        return pageUrl;
    }

    @SuppressWarnings("unused")
    public void setPageUrl(String pageUrl)
    {
        this.pageUrl = pageUrl;
    }

    @SuppressWarnings("unused")
    public String getAppId()
    {
        if (appLink != null)
        {
            return appLink.getId().get();
        }

        return "";
    }

    @Override
    @HtmlSafe
    public Collection<String> getFlushedErrorMessages()
    {
        return super.getFlushedErrorMessages();
    }

    @SuppressWarnings("unused")
    public Collection<ApplicationLink> getAppLinks()
    {
        if (appLinks == null)
        {
            appLinks = confluenceAppLinks.getAppLinks();
        }

        return appLinks;
    }
}
