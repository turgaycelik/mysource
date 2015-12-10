package com.atlassian.jira.plugin.link.remotejira;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.applinks.api.ApplicationLink;
import com.atlassian.applinks.api.ApplicationLinkService;
import com.atlassian.applinks.api.CredentialsRequiredException;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.applinks.host.spi.InternalHostApplication;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bc.issue.comment.CommentService;
import com.atlassian.jira.bc.issue.link.IssueLinkService;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.CreateValidationResult;
import com.atlassian.jira.bc.issue.link.RemoteIssueLinkService.RemoteIssueLinkResult;
import com.atlassian.jira.config.SubTaskManager;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.comments.Comment;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.screen.FieldScreenRendererFactory;
import com.atlassian.jira.issue.link.IssueLinkDisplayHelper;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkBuilder;
import com.atlassian.jira.plugin.link.applinks.RemoteResponse;
import com.atlassian.jira.plugin.link.remotejira.RemoteJiraRestService.RestVersion;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.UserPropertyManager;
import com.atlassian.jira.user.util.UserUtil;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.web.action.issue.AbstractIssueLinkAction;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;
import com.atlassian.sal.api.net.ResponseException;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.collect.Sets;
import com.opensymphony.util.TextUtils;

import org.apache.commons.httpclient.HttpStatus;
import org.apache.commons.lang.StringUtils;

/**
 * Links an issue to an issue from a remote JIRA server.
 *
 * @since 5.0
 */
public class LinkJiraIssue extends AbstractIssueLinkAction
{
    private static final RestVersion REST_VERSION = RestVersion.VERSION_2;
    private static final String DEFAULT_CREATE_RECIPROCAL_KEY = "jira.issuelink.createreciprocal";

    private Collection<String> linkDescs;
    private Collection<ApplicationLink> jiraAppLinks;

    private String linkDesc = "";
    private String jiraAppId = "";
    private Collection<String> issueKeys;
    private boolean createReciprocal;

    private IssueLinkType issueLinkType;
    private ApplicationLink jiraAppLink;

    private Collection<MutableIssue> validatedIssues;
    private Collection<CreateValidationResult> validationResults;

    private final IssueLinkTypeManager issueLinkTypeManager;
    private final IssueLinkService issueLinkService;
    private final IssueLinkDisplayHelper issueLinkDisplayHelper;
    private final UserHistoryManager userHistoryManager;
    private final UserPropertyManager userPropertyManager;
    private final ApplicationLinkService applicationLinkService;
    private final RemoteJiraRestService remoteJiraRestService;
    private final IssueManager issueManager;
    private final IssueLinkManager issueLinkManager;
    private final InternalHostApplication internalHostApplication;
    private final IssueEventManager issueEventManager;
    private final IssueEventBundleFactory issueEventBundleFactory;

    public LinkJiraIssue(
            @ComponentImport final SubTaskManager subTaskManager,
            @ComponentImport final FieldScreenRendererFactory fieldScreenRendererFactory,
            @ComponentImport final FieldManager fieldManager,
            @ComponentImport final ProjectRoleManager projectRoleManager,
            @ComponentImport final CommentService commentService,
            @ComponentImport final UserHistoryManager userHistoryManager,
            @ComponentImport final UserPropertyManager userPropertyManager,
            @ComponentImport final IssueLinkService issueLinkService,
            @ComponentImport final UserUtil userUtil,
            @ComponentImport final IssueLinkTypeManager issueLinkTypeManager,
            @ComponentImport final RemoteIssueLinkService remoteIssueLinkService,
            @ComponentImport final EventPublisher eventPublisher,
            @ComponentImport final ApplicationLinkService applicationLinkService,
            final RemoteJiraRestService remoteJiraRestService,
            @ComponentImport final IssueManager issueManager,
            @ComponentImport final IssueLinkManager issueLinkManager,
            @ComponentImport final InternalHostApplication internalHostApplication,
            @ComponentImport final IssueEventManager issueEventManager,
            @ComponentImport final IssueEventBundleFactory issueEventBundleFactory)
    {
        super(subTaskManager, fieldScreenRendererFactory, fieldManager, projectRoleManager, commentService, userUtil, remoteIssueLinkService, eventPublisher);
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.issueLinkService = issueLinkService;
        this.userHistoryManager = userHistoryManager;
        this.userPropertyManager = userPropertyManager;
        this.issueManager = issueManager;
        this.issueLinkManager = issueLinkManager;
        this.issueLinkDisplayHelper = new IssueLinkDisplayHelper(userHistoryManager, getLoggedInUser());
        this.applicationLinkService = applicationLinkService;
        this.remoteJiraRestService = remoteJiraRestService;
        this.internalHostApplication = internalHostApplication;
        this.issueEventManager = issueEventManager;
        this.issueEventBundleFactory = issueEventBundleFactory;
    }

    @Override
    public boolean isValidToView()
    {
        return super.isValidToView()
                && !getLinkDescs().isEmpty();
    }

    protected void doValidation()
    {
        // Validate comment and permissions
        super.doValidation();

        if (!getLinkDescs().contains(linkDesc))
        {
            addError("linkDesc", getText("linkjiraissue.error.link.desc.invalid"));
        }
        else if (getIssueLinkType().isSystemLinkType())
        {
            // Check that the chosen link type is not a system link type.
            // This should not happen - the system should not present a user with the description
            // of a system link type to chose
            addError("linkDesc", getText("linkjiraissue.error.systemlink"));
        }

        if (issueKeys == null || issueKeys.isEmpty())
        {
            addError("issueKeys", getText("linkjiraissue.error.issuekey.required"));
        }

        if (StringUtils.isBlank(jiraAppId))
        {
            validateLocalLink();
        }
        else
        {
            validateRemoteLink();
        }
    }

    private void validateLocalLink()
    {
        if (!hasAnyErrors())
        {
            validatedIssues = new ArrayList<MutableIssue>(issueKeys.size());

            for (final String key : issueKeys)
            {
                final MutableIssue issue = issueManager.getIssueObject(key);
                if (issue == null)
                {
                    addError("issueKeys", getText("linkissue.error.notexist", key));
                }
                else if (key.equals(getIssue().getString("key")))
                {
                    addError("issueKeys", getText("linkissue.error.selflink"));
                }
                else
                {
                    validatedIssues.add(issue);
                }
            }
        }
    }

    private void validateRemoteLink()
    {
        if (getJiraAppLink() == null)
        {
            addError("jiraAppId", getText("linkjiraissue.error.jira.app.invalid"));
        }

        if (!hasAnyErrors())
        {
            validationResults = new ArrayList<CreateValidationResult>(issueKeys.size());
            for (final String issueKey : issueKeys)
            {
                final RemoteJiraIssue remoteJiraIssue = queryRemoteIssue(getJiraAppLink(), issueKey);
                if (remoteJiraIssue != null)
                {
                    validationResults.add(validateCreateRemote(remoteJiraIssue));
                }
            }
        }
    }

    @Override
    public String doDefault() throws Exception
    {
        final String result = super.doDefault();

        if (!ERROR.equals(result))
        {
            populateCommentFields();
        }

        return result;
    }

    @RequiresXsrfCheck
    protected String doExecute()
    {
        final boolean createLocalLink = (validatedIssues != null);

        if (createLocalLink)
        {
            createLocalLinks();
        }
        else
        {
            createRemoteLinks();
        }

        if (hasAnyErrors())
        {
            return ERROR;
        }

        // Add link type to user history so it is remembered for next time
        userHistoryManager.addItemToHistory(UserHistoryItem.ISSUELINKTYPE, getLoggedInUser(), String.valueOf(getIssueLinkType().getId()), linkDesc);

        try
        {
            if (!createLocalLink)
            {
                setDefaultCreateReciprocal(createReciprocal);
                if (createReciprocal)
                {
                    final Map<String, ErrorCollection> failedIssueKeys = createReciprocalLinks(issueKeys);
                    if (!failedIssueKeys.isEmpty())
                    {
                        return returnCompleteWithReciprocalError(failedIssueKeys);
                    }
                }
            }
        }
        finally
        {
            // Make sure the comment is created even if the reciprocal link fails
            Comment comment = createComment();
            if (comment != null)
            {
                dispatchCommentEvents(getIssueObject(), comment);
            }
        }

        return returnComplete(getRedirectUrl());
    }

    @VisibleForTesting
    void dispatchCommentEvents(Issue issue, Comment comment)
    {
        Map<String, Object> params = createIssueEventParameters();
        issueEventManager.dispatchRedundantEvent(EventType.ISSUE_COMMENTED_ID, issue, getLoggedInUser(), comment, null, null, params);

        IssueEventBundle commentAddedBundle = issueEventBundleFactory.createCommentAddedBundle(issue, getLoggedInApplicationUser(), comment, params);
        issueEventManager.dispatchEvent(commentAddedBundle);
    }

    private Map<String, Object> createIssueEventParameters()
    {
        Map<String, Object> params = new HashMap<String, Object>();
        params.put("eventsource", IssueEventSource.ACTION);
        return params;
    }

    private void createLocalLinks()
    {
        final IssueLinkType linkType = getIssueLinkType();

        for (final MutableIssue issue : validatedIssues)
        {
            createLocalLink(linkType, issue);
        }
    }

    private void createLocalLink(final IssueLinkType linkType, final MutableIssue destinationIssue)
    {
        try
        {
            if (linkDesc.equals(linkType.getOutward()))
            {
                issueLinkManager.createIssueLink(getIssue().getLong("id"), destinationIssue.getId(), linkType.getId(), null, getLoggedInUser());
            }
            else
            {
                issueLinkManager.createIssueLink(destinationIssue.getId(), getIssue().getLong("id"), linkType.getId(), null, getLoggedInUser());
            }
        }
        catch (Exception e)
        {
            log.error("Error occurred creating link: " + e, e);
            addErrorMessage(getText("admin.errors.issues.an.error.occured", e));
        }
    }

    private void createRemoteLinks()
    {
        for (final CreateValidationResult validationResult : validationResults)
        {
            final RemoteIssueLinkResult result = createLink(validationResult);
            if (!result.isValid())
            {
                mapErrors(result.getErrorCollection());
                addErrorCollection(result.getErrorCollection());
            }
        }
    }

    private Map<String, ErrorCollection> createReciprocalLinks(final Collection<String> remoteIssueKeys)
    {
        final Map<String, ErrorCollection> failedIssueKeys = new HashMap<String, ErrorCollection>(remoteIssueKeys.size());

        for (final String remoteIssueKey : remoteIssueKeys)
        {
            try
            {
                final RemoteResponse response = remoteJiraRestService.createRemoteIssueLink(getJiraAppLink(), remoteIssueKey, getIssueObject(), getReciprocalLinkDesc(), REST_VERSION);
                if (!response.isSuccessful())
                {
                    failedIssueKeys.put(remoteIssueKey, response.getErrors());
                }
            }
            catch (final CredentialsRequiredException e)
            {
                failedIssueKeys.put(remoteIssueKey, null);
            }
            catch (final ResponseException e)
            {
                failedIssueKeys.put(remoteIssueKey, null);
            }
        }

        return failedIssueKeys;
    }

    private String returnCompleteWithReciprocalError(final Map<String, ErrorCollection> errorsByIssueKey)
    {
        final StringBuilder msg = new StringBuilder();
                
        for (final Map.Entry<String, ErrorCollection> entry : errorsByIssueKey.entrySet())
        {
            final String remoteIssueKey = entry.getKey();
            final ErrorCollection errors = entry.getValue();

            // Failed to create reciprocal link, but we have successfully created the link on this JIRA instance.
            // Close the dialog and show a warning message in the Issue Links section of the View Issue Page.
            if (msg.length() > 0)
            {
                msg.append("<br>");
            }

            final String hyperlink = "<a id='remoteIssue' href='" + getRemoteIssueUrl(TextUtils.htmlEncode(remoteIssueKey)) + "'>" + TextUtils.htmlEncode(remoteIssueKey) + "</a>";
            if (errors != null && errors.getErrorMessages() != null && !errors.getErrorMessages().isEmpty())
            {
                final String remoteErrorMsg = TextUtils.htmlEncode(errors.getErrorMessages().iterator().next());
                msg.append(getText("linkjiraissue.error.reciprocal.desc.with.reason", hyperlink, remoteErrorMsg));
            }
            else
            {
                msg.append(getText("linkjiraissue.error.reciprocal.desc", hyperlink));
            }
        }

        return returnMsgToUser(getRedirectUrl(), msg.toString(), MessageType.WARNING, true, "#linkingmodule .mod-content");
    }

    private RemoteJiraIssue queryRemoteIssue(final ApplicationLink applicationLink, final String issueKey)
    {
        try
        {
            final RemoteResponse<RemoteJiraIssue> response = remoteJiraRestService.getIssue(applicationLink, issueKey, REST_VERSION);
            if (response.isSuccessful())
            {
                return response.getEntity();
            }
            else
            {
                switch (response.getStatusCode())
                {
                    case HttpStatus.SC_NOT_FOUND:
                    {
                        // If we have an error message, the REST resource was found and the issue does not exist
                        if (response.hasErrors())
                        {
                            addErrorMessage(getText("linkjiraissue.error.remoteissue.notfound", TextUtils.htmlEncode(issueKey)));
                        }
                        // Otherwise, the REST resource was not found
                        else
                        {
                            addErrorMessage(getText("linkjiraissue.error.remoteinstance.oldversion", TextUtils.htmlEncode(issueKey)));
                        }
                        break;
                    }
                    case HttpStatus.SC_FORBIDDEN:
                    {
                        addErrorMessage(getText("linkjiraissue.error.remoteissue.forbidden", TextUtils.htmlEncode(issueKey)));
                        break;
                    }
                    case HttpStatus.SC_UNAUTHORIZED:
                    {
                        handleCredentialsRequired();
                        break;
                    }
                    default:
                    {
                        addErrorMessage(getText("linkjiraissue.error.invalid.response", TextUtils.htmlEncode(issueKey)));
                        log.error("Invalid response from remote JIRA server: " + response.getStatusCode() + ": " + response.getStatusText());
                    }
                }
            }
        }
        catch (final CredentialsRequiredException e)
        {
            handleCredentialsRequired();
        }
        catch (final ResponseException e)
        {
            addErrorMessage(getText("linkjiraissue.error.invalid.response", TextUtils.htmlEncode(issueKey)));
            log.error("Invalid response from remote JIRA server: " + e.getMessage());
        }

        return null;
    }

    private CreateValidationResult validateCreateRemote(final RemoteJiraIssue remoteJiraIssue)
    {
        final String globalId = RemoteJiraGlobalIdFactoryImpl.encode(new RemoteJiraGlobalId(getJiraAppLink(), remoteJiraIssue.getId()));

        // Only store the bare minimum information, the rest will be shown using the renderer plugin
        final RemoteIssueLink remoteIssueLink = new RemoteIssueLinkBuilder()
                .url(remoteJiraIssue.getBrowseUrl())
                .title(remoteJiraIssue.getKey())
                .globalId(globalId)
                .issueId(id)
                .relationship(linkDesc)
                .applicationName(getJiraAppLink().getName())
                .applicationType(RemoteIssueLink.APPLICATION_TYPE_JIRA)
                .build();

        final CreateValidationResult validationResult = remoteIssueLinkService.validateCreate(getLoggedInUser(), remoteIssueLink);
        if (!validationResult.isValid())
        {
            mapErrors(validationResult.getErrorCollection());
            addErrorCollection(validationResult.getErrorCollection());
        }

        return validationResult;
    }

    private void mapErrors(final ErrorCollection errorCollection)
    {
        // Convert field errors to error messages so that they will appear on the page
        // Hide the field name (key), as this will mean nothing to users
        for (final Map.Entry<String, String> entry : errorCollection.getErrors().entrySet())
        {
            errorCollection.addErrorMessage(entry.getValue());
        }
    }

    private String getReciprocalLinkDesc()
    {
        final IssueLinkType linkType = getIssueLinkType();
        if (linkType == null)
        {
            return null;
        }

        if (linkDesc.equals(linkType.getOutward()))
        {
            return linkType.getInward();
        }
        return linkType.getOutward();
    }

    private IssueLinkType getIssueLinkType()
    {
        if (issueLinkType == null)
        {
            for (IssueLinkType linkType : issueLinkTypeManager.getIssueLinkTypes())
            {
                if (linkDesc.equals(linkType.getOutward()) || linkDesc.equals(linkType.getInward()))
                {
                    issueLinkType = linkType;
                    break;
                }
            }
        }

        return issueLinkType;
    }

    @SuppressWarnings("unused")
    public String getLinkDesc()
    {
        return linkDesc;
    }

    @SuppressWarnings("unused")
    public void setLinkDesc(String linkDesc)
    {
        this.linkDesc = linkDesc;
    }

    public Collection<String> getLinkDescs()
    {
        if (linkDescs == null)
        {
            linkDescs = issueLinkDisplayHelper.getSortedIssueLinkTypes(issueLinkService.getIssueLinkTypes());
        }

        return linkDescs;
    }

    @SuppressWarnings("unused")
    public String getLastUsedLinkType()
    {
        return issueLinkDisplayHelper.getLastUsedLinkType();
    }

    @SuppressWarnings("unused")
    public InternalHostApplication getInternalHostApplication()
    {
        return internalHostApplication;
    }

    public ApplicationLink getJiraAppLink()
    {
        if (jiraAppLink == null)
        {
            for (final ApplicationLink appLink : getJiraAppLinks())
            {
                if (appLink.getId().get().equals(jiraAppId))
                {
                    jiraAppLink = appLink;
                }
            }
        }

        return jiraAppLink;
    }

    @SuppressWarnings("unused")
    public String getJiraAppId()
    {
        return jiraAppId;
    }

    @SuppressWarnings("unused")
    public void setJiraAppId(final String jiraAppId)
    {
        this.jiraAppId = jiraAppId;
    }

    public Collection<ApplicationLink> getJiraAppLinks()
    {
        if (jiraAppLinks == null)
        {
            final Iterable<ApplicationLink> iterable = applicationLinkService.getApplicationLinks(JiraApplicationType.class);
            jiraAppLinks = new ArrayList<ApplicationLink>();
            for (final ApplicationLink appLink : iterable)
            {
                jiraAppLinks.add(appLink);
            }
        }

        return jiraAppLinks;
    }

    @SuppressWarnings("unused")
    public String[] getIssueKeys()
    {
        if (issueKeys == null)
        {
            issueKeys = Sets.newHashSet();
        }
        
        return issueKeys.toArray(new String[0]);
    }

    public String getRemoteIssueUrl(final String issueKey)
    {
        return RemoteJiraRestServiceImpl.buildIssueUrl(getJiraAppLink().getDisplayUrl().toASCIIString(), issueKey);
    }

    @SuppressWarnings("unused")
    public void setIssueKeys(final String[] issueKeys)
    {
        this.issueKeys = Sets.newHashSet(issueKeys);
    }

    @SuppressWarnings("unused")
    public boolean isCreateReciprocal()
    {
        return createReciprocal;
    }

    @SuppressWarnings("unused")
    public void setCreateReciprocal(final boolean createReciprocal)
    {
        this.createReciprocal = createReciprocal;
    }
    
    @SuppressWarnings("unused")
    public boolean getDefaultCreateReciprocal()
    {
        final User user = getLoggedInUser();
        if (user == null)
        {
            // Default to false for anonymous users
            return false;
        }

        // Remember the user's last choice. If they don't have any history, default to true.
        final String value = userPropertyManager.getPropertySet(user).getString(DEFAULT_CREATE_RECIPROCAL_KEY);
        if (StringUtils.isBlank(value))
        {
            return true;
        }
        return Boolean.parseBoolean(value);
    }

    private void setDefaultCreateReciprocal(final boolean createReciprocal)
    {
        final User user = getLoggedInUser();
        if (user != null)
        {
            userPropertyManager.getPropertySet(user).setString(DEFAULT_CREATE_RECIPROCAL_KEY, String.valueOf(createReciprocal));
        }
    }
}
