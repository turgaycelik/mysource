package com.atlassian.jira.bc.issue.link;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.issue.IssueService;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.GetException;
import com.atlassian.jira.exception.UpdateException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.issue.link.RemoteIssueLinkManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Collections2;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.Iterables;
import com.google.common.collect.Maps;
import org.apache.commons.lang.StringUtils;

import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.issue.link.DefaultRemoteIssueLinkManager.MAX_LONG_VARCHAR_LENGTH;

/**
 * The default implementation of the {@link RemoteIssueLinkService}.
 *
 * @since v5.0
 */
public class DefaultRemoteIssueLinkService implements RemoteIssueLinkService
{
    private final RemoteIssueLinkManager remoteIssueLinkManager;
    private final IssueService issueService;
    private final IssueManager issueManager;
    private final IssueLinkManager issueLinkManager;
    private final I18nHelper.BeanFactory beanFactory;
    private final PermissionManager permissionManager;

    public DefaultRemoteIssueLinkService(final RemoteIssueLinkManager remoteIssueLinkManager, final IssueService issueService, final IssueManager issueManager, final IssueLinkManager issueLinkManager, final I18nHelper.BeanFactory beanFactory, final PermissionManager permissionManager)
    {
        this.remoteIssueLinkManager = remoteIssueLinkManager;
        this.issueService = issueService;
        this.issueManager = issueManager;
        this.issueLinkManager = issueLinkManager;
        this.beanFactory = beanFactory;
        this.permissionManager = permissionManager;
    }

    @Override
    public RemoteIssueLinkResult getRemoteIssueLink(final User user, final Long remoteIssueLinkId)
    {
        return getRemoteIssueLink(ApplicationUsers.from(user), remoteIssueLinkId);
    }

    @Override
    public RemoteIssueLinkResult getRemoteIssueLink(final ApplicationUser user, final Long remoteIssueLinkId)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = getI18n(user);

        validateLinkingEnabled(i18n, errors);

        RemoteIssueLink remoteIssueLink = null;
        if (!errors.hasAnyErrors())
        {
            remoteIssueLink = remoteIssueLinkManager.getRemoteIssueLink(remoteIssueLinkId);
            if (remoteIssueLink == null)
            {
                errors.addErrorMessage(i18n.getText("remotelink.service.does.not.exist"), Reason.NOT_FOUND);
            }
        }

        if (!errors.hasAnyErrors())
        {
            validatePermissionToView(user, remoteIssueLink, errors, i18n);
        }

        return new RemoteIssueLinkResult((errors.hasAnyErrors() ? null : remoteIssueLink), errors);
    }

    @Override
    public RemoteIssueLinkListResult getRemoteIssueLinksForIssue(final User user, final Issue issue)
    {
        return getRemoteIssueLinksForIssue(ApplicationUsers.from(user), issue);
    }

    @Override
    public RemoteIssueLinkListResult getRemoteIssueLinksForIssue(final ApplicationUser user, final Issue issue)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = getI18n(user);

        validateLinkingEnabled(i18n, errors);

        if (!errors.hasAnyErrors())
        {
            validatePermissionToView(user, issue, errors, i18n);
        }

        List<RemoteIssueLink> remoteIssueLinks = null;
        if (!errors.hasAnyErrors())
        {
            remoteIssueLinks = remoteIssueLinkManager.getRemoteIssueLinksForIssue(issue);
        }

        return new RemoteIssueLinkListResult((errors.hasAnyErrors() ? null : remoteIssueLinks), errors);
    }

    @Override
    public RemoteIssueLinkResult getRemoteIssueLinkByGlobalId(final User user, final Issue issue, final String globalId)
    {
        return getRemoteIssueLinkByGlobalId(ApplicationUsers.from(user), issue, globalId);
    }

    @Override
    public RemoteIssueLinkResult getRemoteIssueLinkByGlobalId(final ApplicationUser user, final Issue issue, final String globalId)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = getI18n(user);

        validateLinkingEnabled(i18n, errors);

        if (!errors.hasAnyErrors())
        {
            validatePermissionToView(user, issue, errors, i18n);
        }

        RemoteIssueLink remoteIssueLink = null;
        if (!errors.hasAnyErrors())
        {
            remoteIssueLink = remoteIssueLinkManager.getRemoteIssueLinkByGlobalId(issue, globalId);
            if (remoteIssueLink == null)
            {
                errors.addErrorMessage(i18n.getText("remotelink.service.does.not.exist"), Reason.NOT_FOUND);
            }
        }

        return new RemoteIssueLinkResult((errors.hasAnyErrors() ? null : remoteIssueLink), errors);
    }

    @Override
    public RemoteIssueLinkListResult findRemoteIssueLinksByGlobalId(final ApplicationUser user, final Collection<String> globalIds)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = getI18n(user);

        validateLinkingEnabled(i18n, errors);

        if (!errors.hasAnyErrors())
        {
            if (globalIds == null)
            {
                errors.addError("globalIds", i18n.getText("remotelink.service.globalids.null"));
            }
        }

        List<RemoteIssueLink> remoteIssueLinks = null;
        if (!errors.hasAnyErrors())
        {
            try
            {
                remoteIssueLinks = remoteIssueLinkManager.findRemoteIssueLinksByGlobalIds(globalIds);
            }
            catch (GetException e)
            {
                errors.addErrorMessage(e.getMessage());
            }
            if (!errors.hasAnyErrors())
            {
                final Collection<Long> issueIds = Collections2.transform(remoteIssueLinks, new Function<RemoteIssueLink, Long>()
                {
                    @Override
                    public Long apply(final RemoteIssueLink remoteIssueLink)
                    {
                        return remoteIssueLink.getIssueId();
                    }
                });
                final List<Issue> issues = issueManager.getIssueObjects(issueIds);
                // create an id->issue map for easy reference later when checking permission
                final Map<Long, Issue> issueMap = Maps.uniqueIndex(issues, new Function<Issue, Long>()
                {
                    @Override
                    public Long apply(final Issue issue)
                    {
                        return issue.getId();
                    }
                });
                remoteIssueLinks = ImmutableList.copyOf(Iterables.filter(remoteIssueLinks, new Predicate<RemoteIssueLink>()
                {
                    @Override
                    public boolean apply(final RemoteIssueLink input)
                    {
                        final Issue issue = issueMap.get(input.getIssueId());
                        return issue != null && (permissionManager.hasPermission(Permissions.BROWSE, issue, user));
                    }
                }));
            }
        }

        return new RemoteIssueLinkListResult(remoteIssueLinks, errors);
    }

    @Override
    public CreateValidationResult validateCreate(final User user, final RemoteIssueLink remoteIssueLink)
    {
        return validateCreate(ApplicationUsers.from(user), remoteIssueLink);
    }

    @Override
    public CreateValidationResult validateCreate(final ApplicationUser user, final RemoteIssueLink remoteIssueLink)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = getI18n(user);

        validateLinkingEnabled(i18n, errors);

        Issue issue = null;
        if (!errors.hasAnyErrors())
        {
            issue = validateIssueExists(remoteIssueLink, errors, i18n);
        }

        if (!errors.hasAnyErrors())
        {
            validatePermissionToCreate(user, issue, errors, i18n);
        }

        if (!errors.hasAnyErrors())
        {
            validateMandatoryFields(remoteIssueLink, i18n, errors);
            validateFieldLengths(remoteIssueLink, i18n, errors);
            validateUrls(remoteIssueLink, i18n, errors);
            validateGlobalId(remoteIssueLink, issue, errors, i18n);
        }

        return new CreateValidationResult((errors.hasAnyErrors() ? null : remoteIssueLink), errors);
    }

    @Override
    public RemoteIssueLinkResult create(final User user, final CreateValidationResult createValidationResult)
    {
        return create(ApplicationUsers.from(user), createValidationResult);
    }

    @Override
    public RemoteIssueLinkResult create(final ApplicationUser user, final CreateValidationResult createValidationResult)
    {
        if (createValidationResult == null)
        {
            throw new IllegalArgumentException("You cannot create a remote issue link with a null validation result.");
        }

        if (!createValidationResult.isValid())
        {
            throw new IllegalStateException("You cannot create a remote issue link with an invalid validation result.");
        }

        final RemoteIssueLink remoteIssueLink = createValidationResult.getRemoteIssueLink();
        if (remoteIssueLink == null)
        {
            throw new IllegalArgumentException("You cannot create a null remote issue link.");
        }

        final ErrorCollection errors = new SimpleErrorCollection();
        RemoteIssueLink createdRemoteIssueLink = null;
        try
        {
            createdRemoteIssueLink = remoteIssueLinkManager.createRemoteIssueLink(remoteIssueLink, user);
        }
        catch (final CreateException e)
        {
            handleCreateException(getI18n(user), errors, e);
        }

        return new RemoteIssueLinkResult(createdRemoteIssueLink, errors);
    }

    @Override
    public UpdateValidationResult validateUpdate(final User user, final RemoteIssueLink remoteIssueLink)
    {
        return validateUpdate(ApplicationUsers.from(user), remoteIssueLink);
    }

    @Override
    public UpdateValidationResult validateUpdate(final ApplicationUser user, final RemoteIssueLink remoteIssueLink)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = getI18n(user);

        validateLinkingEnabled(i18n, errors);

        Issue issue = null;
        if (!errors.hasAnyErrors())
        {
            issue = validateIssueExists(remoteIssueLink, errors, i18n);
        }

        if (!errors.hasAnyErrors())
        {
            validatePermissionToUpdate(user, issue, errors, i18n);
        }

        RemoteIssueLink found = null;
        if (!errors.hasAnyErrors())
        {
            found = validateExists(remoteIssueLink.getId(), i18n, errors);
        }

        if (!errors.hasAnyErrors())
        {
            validateMandatoryFields(remoteIssueLink, i18n, errors);
            validateFieldLengths(remoteIssueLink, i18n, errors);
            validateUrls(remoteIssueLink, i18n, errors);
            validateGlobalIdForUpdate(issue, found, remoteIssueLink, errors, i18n);
        }

        return new UpdateValidationResult((errors.hasAnyErrors() ? null : remoteIssueLink), errors);
    }

    @Override
    public RemoteIssueLinkResult update(final User user, final UpdateValidationResult updateValidationResult)
    {
        return update(ApplicationUsers.from(user), updateValidationResult);
    }

    @Override
    public RemoteIssueLinkResult update(final ApplicationUser user, final UpdateValidationResult updateValidationResult)
    {
        if (updateValidationResult == null)
        {
            throw new IllegalArgumentException("You cannot update a remote issue link with a null validation result.");
        }

        if (!updateValidationResult.isValid())
        {
            throw new IllegalStateException("You cannot update a remote issue link with an invalid validation result.");
        }

        final RemoteIssueLink remoteIssueLink = updateValidationResult.getRemoteIssueLink();
        if (remoteIssueLink == null)
        {
            throw new IllegalArgumentException("You cannot update a null remote issue link.");
        }

        final ErrorCollection errors = new SimpleErrorCollection();
        RemoteIssueLink updatedRemoteIssueLink = null;
        try
        {
            remoteIssueLinkManager.updateRemoteIssueLink(remoteIssueLink, user);
            updatedRemoteIssueLink = remoteIssueLink;
        }
        catch (final UpdateException e)
        {
            handleUpdateException(getI18n(user), errors, e);
        }

        return new RemoteIssueLinkResult(updatedRemoteIssueLink, errors);
    }

    @Override
    public DeleteValidationResult validateDelete(final User user, final Long remoteIssueLinkId)
    {
        return validateDelete(ApplicationUsers.from(user), remoteIssueLinkId);
    }

    @Override
    public DeleteValidationResult validateDelete(final ApplicationUser user, final Long remoteIssueLinkId)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = getI18n(user);

        validateLinkingEnabled(i18n, errors);

        RemoteIssueLink remoteIssueLink = null;
        if (!errors.hasAnyErrors())
        {
            remoteIssueLink = validateExists(remoteIssueLinkId, i18n, errors);
        }

        if (!errors.hasAnyErrors())
        {
            validatePermissionToDelete(user, remoteIssueLink, errors, i18n);
        }

        return new DeleteValidationResult((errors.hasAnyErrors() ? null : remoteIssueLinkId), errors);
    }

    @Override
    public void delete(final User user, final DeleteValidationResult deleteValidationResult)
    {
        delete(ApplicationUsers.from(user), deleteValidationResult);
    }

    @Override
    public void delete(final ApplicationUser user, final DeleteValidationResult deleteValidationResult)
    {
        if (deleteValidationResult == null)
        {
            throw new IllegalArgumentException("You cannot delete a remote issue link with a null validation result.");
        }

        if (!deleteValidationResult.isValid())
        {
            throw new IllegalStateException("You cannot delete a remote issue link with an invalid validation result.");
        }

        final Long remoteIssueLinkId = deleteValidationResult.getRemoteIssueLinkId();
        if (remoteIssueLinkId == null)
        {
            throw new IllegalArgumentException("You cannot delete with a null remote issue link id.");
        }

        remoteIssueLinkManager.removeRemoteIssueLink(remoteIssueLinkId, user);
    }

    @Override
    public DeleteByGlobalIdValidationResult validateDeleteByGlobalId(User user, Issue issue, String globalId)
    {
        return validateDeleteByGlobalId(ApplicationUsers.from(user), issue, globalId);
    }

    @Override
    public DeleteByGlobalIdValidationResult validateDeleteByGlobalId(final ApplicationUser user, final Issue issue, final String globalId)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        final I18nHelper i18n = getI18n(user);

        validateLinkingEnabled(i18n, errors);

        RemoteIssueLink remoteIssueLink = null;
        if (!errors.hasAnyErrors())
        {
            remoteIssueLink = validateExistsByGlobalId(issue, globalId, i18n, errors);
        }

        if (!errors.hasAnyErrors())
        {
            validatePermissionToDelete(user, remoteIssueLink, errors, i18n);
        }

        return new DeleteByGlobalIdValidationResult(issue, globalId, errors);
    }

    @Override
    public void deleteByGlobalId(User user, DeleteByGlobalIdValidationResult validationResult)
    {
        deleteByGlobalId(ApplicationUsers.from(user), validationResult);
    }

    @Override
    public void deleteByGlobalId(final ApplicationUser user, final DeleteByGlobalIdValidationResult validationResult)
    {
        if (validationResult == null)
        {
            throw new IllegalArgumentException("You cannot delete a remote link with a null validation result.");
        }

        if (!validationResult.isValid())
        {
            throw new IllegalStateException("You cannot delete a remote link with an invalid validation result.");
        }

        final Issue issue = validationResult.getIssue();
        if (issue == null)
        {
            throw new IllegalArgumentException("You cannot delete with a null issue.");
        }

        final String globalId = validationResult.getGlobalId();
        if (globalId == null)
        {
            throw new IllegalArgumentException("You cannot delete with a null remote link global id.");
        }

        remoteIssueLinkManager.removeRemoteIssueLinkByGlobalId(issue, globalId, user);
    }

    private void validateLinkingEnabled(final I18nHelper i18n, final ErrorCollection errors)
    {
        if (!issueLinkManager.isLinkingEnabled())
        {
            errors.addErrorMessage(i18n.getText("admin.issuelinking.status", i18n.getText("admin.common.words.disabled")), Reason.FORBIDDEN);
        }
    }

    private RemoteIssueLink validateExists(final Long remoteIssueLinkId, final I18nHelper i18n, final ErrorCollection errors)
    {
        if (remoteIssueLinkId == null)
        {
            errors.addError("id", i18n.getText("admin.common.words.is.required", "id"), Reason.VALIDATION_FAILED);
            return null;
        }

        final RemoteIssueLink remoteIssueLink = remoteIssueLinkManager.getRemoteIssueLink(remoteIssueLinkId);
        if (remoteIssueLink == null)
        {
            errors.addError("id", i18n.getText("remotelink.service.id.does.not.exist", remoteIssueLinkId.toString()), Reason.NOT_FOUND);
        }

        return remoteIssueLink;
    }

    private RemoteIssueLink validateExistsByGlobalId(final Issue issue, final String globalId, final I18nHelper i18n, final ErrorCollection errors)
    {
        if (issue == null)
        {
            errors.addError("id", i18n.getText("admin.common.words.is.required", "issue"), Reason.VALIDATION_FAILED);
            return null;
        }
        if (globalId == null)
        {
            errors.addError("id", i18n.getText("admin.common.words.is.required", "globalId"), Reason.VALIDATION_FAILED);
            return null;
        }

        final RemoteIssueLink remoteIssueLink = remoteIssueLinkManager.getRemoteIssueLinkByGlobalId(issue, globalId);
        if (remoteIssueLink == null)
        {
            errors.addError("globalId", i18n.getText("remotelink.service.globalid.does.not.exist.in.issue", globalId, issue.getId().toString()), Reason.NOT_FOUND);
        }

        return remoteIssueLink;
    }

    private void validateMandatoryFields(final RemoteIssueLink remoteIssueLink, final I18nHelper i18n, final ErrorCollection errors)
    {
        if (remoteIssueLink.getIssueId() == null)
        {
            errors.addError("issueId", i18n.getText("admin.common.words.is.required", "issueId"), Reason.VALIDATION_FAILED);
        }

        if (StringUtils.isBlank(remoteIssueLink.getTitle()))
        {
            errors.addError("title", i18n.getText("admin.common.words.is.required", i18n.getText("remotelink.service.title")), Reason.VALIDATION_FAILED);
        }

        if (StringUtils.isBlank(remoteIssueLink.getUrl()))
        {
            errors.addError("url", i18n.getText("admin.common.words.is.required", i18n.getText("remotelink.service.url")), Reason.VALIDATION_FAILED);
        }
    }

    private void validateFieldLengths(RemoteIssueLink remoteIssueLink, I18nHelper i18n, ErrorCollection errors)
    {
        if (isLongerThan(remoteIssueLink.getGlobalId(), MAX_LONG_VARCHAR_LENGTH))
        {
            errors.addError("globalId", i18n.getText("remotelink.service.field.too.long", i18n.getText("remotelink.service.globalid"), String.valueOf(MAX_LONG_VARCHAR_LENGTH)), Reason.VALIDATION_FAILED);
        }

        if (isLongerThan(remoteIssueLink.getTitle(), MAX_LONG_VARCHAR_LENGTH))
        {
            errors.addError("title", i18n.getText("remotelink.service.field.too.long", i18n.getText("remotelink.service.title"), String.valueOf(MAX_LONG_VARCHAR_LENGTH)), Reason.VALIDATION_FAILED);
        }

        if (isLongerThan(remoteIssueLink.getRelationship(), MAX_LONG_VARCHAR_LENGTH))
        {
            errors.addError("relationship", i18n.getText("remotelink.service.field.too.long", i18n.getText("remotelink.service.relationship"), String.valueOf(MAX_LONG_VARCHAR_LENGTH)), Reason.VALIDATION_FAILED);
        }

        if (isLongerThan(remoteIssueLink.getApplicationType(), MAX_LONG_VARCHAR_LENGTH))
        {
            errors.addError("applicationType", i18n.getText("remotelink.service.field.too.long", i18n.getText("remotelink.service.applicationtype"), String.valueOf(MAX_LONG_VARCHAR_LENGTH)), Reason.VALIDATION_FAILED);
        }

        if (isLongerThan(remoteIssueLink.getApplicationName(), MAX_LONG_VARCHAR_LENGTH))
        {
            errors.addError("applicationName", i18n.getText("remotelink.service.field.too.long", i18n.getText("remotelink.service.applicationname"), String.valueOf(MAX_LONG_VARCHAR_LENGTH)), Reason.VALIDATION_FAILED);
        }
    }

    private boolean isLongerThan(String value, int length)
    {
        return value != null && value.length() > length;
    }

    private void validateUrls(final RemoteIssueLink remoteIssueLink, final I18nHelper i18n, final ErrorCollection errors)
    {
        if (!isValidUrl(remoteIssueLink.getUrl()))
        {
            errors.addError("url", i18n.getText("remotelink.service.invalid.uri", i18n.getText("remotelink.service.url")), Reason.VALIDATION_FAILED);
        }

        if (!isValidUrl(remoteIssueLink.getIconUrl()))
        {
            errors.addError("iconUrl", i18n.getText("remotelink.service.invalid.uri", "iconUrl"), Reason.VALIDATION_FAILED);
        }

        if (!isValidUrl(remoteIssueLink.getStatusIconUrl()))
        {
            errors.addError("statusIconUrl", i18n.getText("remotelink.service.invalid.uri", "statusIconUrl"), Reason.VALIDATION_FAILED);
        }

        if (!isValidUrl(remoteIssueLink.getStatusIconLink()))
        {
            errors.addError("statusIconLink", i18n.getText("remotelink.service.invalid.uri", "statusIconLink"), Reason.VALIDATION_FAILED);
        }
    }

    private boolean isValidUrl(final String url)
    {
        if (StringUtils.isBlank(url))
        {
            return true;
        }

        try
        {
            final URI uri = new URI(url);

            if (!("http".equalsIgnoreCase(uri.getScheme()) || "https".equalsIgnoreCase(uri.getScheme())))
            {
                return false;
            }
            if (uri.getHost() == null)
            {
                return false;
            }
        }
        catch (final URISyntaxException e)
        {
            return false;
        }

        return true;
    }

    private Issue validateIssueExists(final RemoteIssueLink remoteIssueLink, final ErrorCollection errors, final I18nHelper i18n)
    {
        final Issue issue = issueManager.getIssueObject(remoteIssueLink.getIssueId());
        if (issue == null)
        {
            errors.addError("issueId", i18n.getText("remotelink.service.issue.does.not.exist", String.valueOf(remoteIssueLink.getIssueId())), Reason.VALIDATION_FAILED);
        }

        return issue;
    }

    private void validateGlobalId(final RemoteIssueLink remoteIssueLink, final Issue issue, final ErrorCollection errors, final I18nHelper i18n)
    {
        if (remoteIssueLink.getGlobalId() == null)
        {
            // No need to check for duplicates if it is null
            return;
        }

        // Check for duplicate globalId
        final RemoteIssueLink found = remoteIssueLinkManager.getRemoteIssueLinkByGlobalId(issue, remoteIssueLink.getGlobalId());
        if (found != null)
        {
            errors.addError("globalId", i18n.getText("remotelink.service.globalid.duplicate", remoteIssueLink.getTitle()), Reason.VALIDATION_FAILED);
        }
    }

    private void validateGlobalIdForUpdate(final Issue issue, final RemoteIssueLink found, final RemoteIssueLink remoteIssueLink, final ErrorCollection errors, final I18nHelper i18n)
    {
        // Validate only if it has changed
        if (!isEqual(found.getGlobalId(), remoteIssueLink.getGlobalId()))
        {
            validateGlobalId(remoteIssueLink, issue, errors, i18n);
        }
    }

    private void validatePermissionToView(final ApplicationUser user, final RemoteIssueLink remoteIssueLink, final ErrorCollection errors, final I18nHelper i18n)
    {
        final IssueService.IssueResult result = issueService.getIssue(ApplicationUsers.toDirectoryUser(user), remoteIssueLink.getIssueId());
        if (result.isValid())
        {
            validatePermissionToView(user, result.getIssue(), errors, i18n);
        }
    }
    
    private void validatePermissionToView(final ApplicationUser user, final Issue issue, final ErrorCollection errors, final I18nHelper i18n)
    {
        validatePermissionToViewIssue(user, issue, errors, i18n);
    }

    private void validatePermissionToCreate(final ApplicationUser user, final Issue issue, final ErrorCollection errors, final I18nHelper i18n)
    {
        hasLinkIssuePermission(user, issue, errors, i18n);
    }

    private void validatePermissionToUpdate(final ApplicationUser user, final Issue issue, final ErrorCollection errors, final I18nHelper i18n)
    {
        hasLinkIssuePermission(user, issue, errors, i18n);
    }

    private void validatePermissionToDelete(final ApplicationUser user, final RemoteIssueLink remoteIssueLink, final ErrorCollection errors, final I18nHelper i18n)
    {
        final IssueService.IssueResult result = issueService.getIssue(ApplicationUsers.toDirectoryUser(user), remoteIssueLink.getIssueId());
        if (result.isValid())
        {
            final Issue issue = result.getIssue();
            hasLinkIssuePermission(user, issue, errors, i18n);
        }
    }

    private void validatePermissionToViewIssue(final ApplicationUser user, final Issue issue, final ErrorCollection errors, final I18nHelper i18n)
    {
        if (!permissionManager.hasPermission(Permissions.BROWSE, issue, user))
        {
            errors.addErrorMessage(i18n.getText("admin.errors.issues.no.permission.to.see"), Reason.FORBIDDEN);
        }
    }

    private void hasLinkIssuePermission(final ApplicationUser user, final Issue issue, final ErrorCollection errors, final I18nHelper i18n)
    {
        if (!permissionManager.hasPermission(Permissions.LINK_ISSUE, issue, user))
        {
            errors.addErrorMessage(i18n.getText("remotelink.service.no.link.issue.permission", issue.getKey()), Reason.FORBIDDEN);
        }
    }

    private void handleCreateException(final I18nHelper i18n, final ErrorCollection errors, final CreateException createException)
    {
        errors.addErrorMessage(i18n.getText("remotelink.service.error.creating", createException.getMessage()), Reason.SERVER_ERROR);
    }

    private void handleUpdateException(final I18nHelper i18n, final ErrorCollection errors, final UpdateException updateException)
    {
        errors.addErrorMessage(i18n.getText("remotelink.service.error.updating", updateException.getMessage()), Reason.SERVER_ERROR);
    }

    private I18nHelper getI18n(ApplicationUser user)
    {
        return beanFactory.getInstance(user);
    }

    private boolean isEqual(final String a, final String b)
    {
        return (a == null) ? (b == null) : a.equals(b);
    }
}
