package com.atlassian.jira.bc.issue.link;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.link.Direction;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.link.LinkCollection;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;

import java.util.Collection;

import static com.atlassian.jira.util.ErrorCollection.Reason;
import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * An implementation of Issue Linking Service
 *
 * @since 5.0
 */
public class DefaultIssueLinkService implements IssueLinkService
{
    private final IssueLinkTypeManager issueLinkTypeManager;
    private final IssueLinkManager issueLinkManager;
    private final PermissionManager permissionManager;
    private final IssueManager issueManager;
    private final I18nHelper.BeanFactory beanFactory;
    private final UserHistoryManager userHistoryManager;


    public DefaultIssueLinkService(final IssueLinkTypeManager issueLinkTypeManager, final IssueManager issueManager, final PermissionManager permissionManager, final I18nHelper.BeanFactory beanFactory, final IssueLinkManager issueLinkManager, UserHistoryManager userHistoryManager)
    {
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.issueManager = issueManager;
        this.permissionManager = permissionManager;
        this.beanFactory = beanFactory;
        this.issueLinkManager = issueLinkManager;
        this.userHistoryManager = userHistoryManager;
    }

    @Override
    public Collection<IssueLinkType> getIssueLinkTypes()
    {
        return issueLinkTypeManager.getIssueLinkTypes();

    }

    @Override
    public IssueLink getIssueLink(Long sourceId, Long destinationId, Long issueLinkTypeId)
    {
        return issueLinkManager.getIssueLink(sourceId, destinationId, issueLinkTypeId);
    }

    @Override
    public SingleIssueLinkResult getIssueLink(Long issueLinkId, User user)
    {
        Assertions.notNull("issueLinkId", issueLinkId);
        IssueLink issueLink = issueLinkManager.getIssueLink(issueLinkId);
        if (issueLink == null)
        {
            SimpleErrorCollection simpleErrorCollection = new SimpleErrorCollection();
            simpleErrorCollection.addErrorMessage(beanFactory.getInstance(user).getText("rest.issue.link.not.found", issueLinkId.toString()));
            return new SingleIssueLinkResult(simpleErrorCollection, null);
        }
        MutableIssue sourceIssue = issueManager.getIssueObject(issueLink.getSourceId());
        ErrorCollection sourceIssueErrors = validateIssuePermission(user, sourceIssue, Permissions.BROWSE);

        MutableIssue destinationIssue = issueManager.getIssueObject(issueLink.getDestinationId());
        ErrorCollection destinationIssueErrors = validateIssuePermission(user, destinationIssue, Permissions.BROWSE);

        if (sourceIssueErrors.hasAnyErrors() || destinationIssueErrors.hasAnyErrors())
        {
            sourceIssueErrors.getErrors().putAll(destinationIssueErrors.getErrors());
            sourceIssueErrors.getErrorMessages().addAll(destinationIssueErrors.getErrorMessages());
            return new SingleIssueLinkResult(sourceIssueErrors, null);
        }
        return new SingleIssueLinkResult(new SimpleErrorCollection(), issueLink);
    }

    @Override
    public IssueLinkResult getIssueLinks(User user, Issue issue)
    {
        return this.getIssueLinks(user, issue, true);
    }

    @Override
    public IssueLinkResult getIssueLinks(User user, Issue issue, boolean excludeSystemLinks)
    {
        ErrorCollection errorCollection = validateIssuePermission(user, issue, Permissions.BROWSE);
        if (!errorCollection.hasAnyErrors())
        {

            final LinkCollection linkCollection = issueLinkManager.getLinkCollection(issue, user, excludeSystemLinks);
            return new IssueLinkResult(errorCollection, linkCollection);
        }
        else
        {
            return new IssueLinkResult(errorCollection, null);
        }
    }

    @Override
    public AddIssueLinkValidationResult validateAddIssueLinks(User user, Issue issue, String linkName, Collection<String> linkKeys)
    {
        IssueLinkType linkType = matchToIssueLinkType(linkName);
        if (null == linkType)
        {
            ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage(beanFactory.getInstance(user).getText("issuelinking.service.error.invalid.link.name", linkName));
            return new AddIssueLinkValidationResult(user, errors, null, null, null, null);
        }
        Direction direction = linkName.equals(linkType.getInward()) ? Direction.IN : Direction.OUT;
        return validateAddIssueLinks(user, issue, linkType, direction, linkKeys, true);
    }

    @Override
    public AddIssueLinkValidationResult validateAddIssueLinks(User user, Issue issue, Long issueLinkTypeId, Direction direction, Collection<String> linkKeys, boolean excludeSystemLinks)
    {
        IssueLinkType linkType = issueLinkTypeManager.getIssueLinkType(issueLinkTypeId);
        if (null == linkType)
        {
            ErrorCollection errors = new SimpleErrorCollection();
            errors.addErrorMessage(beanFactory.getInstance(user).getText("issuelinking.service.error.invalid.link.id", issueLinkTypeId));
            return new AddIssueLinkValidationResult(user, errors, null, null, null, null);
        }
        return validateAddIssueLinks(user, issue, linkType, direction, linkKeys, excludeSystemLinks);
    }

    private AddIssueLinkValidationResult validateAddIssueLinks(User user, Issue issue, IssueLinkType linkType, Direction direction, Collection<String> linkKeys, boolean excludeSystemLinks)
    {
        Assertions.notNull("issue", issue);
        Assertions.notNull("linkKeys", linkKeys);

        ErrorCollection errorCollection = validateIssuePermission(user, issue, Permissions.LINK_ISSUE);
        if (!errorCollection.hasAnyErrors())
        {
            validateLinkInput(user, errorCollection, issue, linkType, linkKeys, excludeSystemLinks);
        }
        return new AddIssueLinkValidationResult(user, errorCollection, issue, linkType,  direction, linkKeys);
    }


    @Override
    public void addIssueLinks(User user, AddIssueLinkValidationResult result)
    {
        notNull("result", result);

        if (!result.isValid())
        {
            throw new IllegalStateException("Cannot add issue links with invalid validation result!");
        }

        IssueLinkType linkType = result.getLinkType();
        if (linkType == null)
        {
            throw new IllegalArgumentException("Cant find issue link type '" + result.getLinkType().getName() + "'");
        }

        final Issue sourceIssue = result.getIssue();

        for (String targetIssueKey : result.getLinkKeys())
        {
            MutableIssue destinationIssue = issueManager.getIssueObject(targetIssueKey);
            if (destinationIssue == null)
            {
                throw new IllegalArgumentException("Issue with key '" + targetIssueKey + "' no longer exists!");
            }

            try
            {
                // This reflects what the old code used to do.  It matches the chosen direction and then depending on whether
                // it's outwards our inwards, it flips the issue value around to match.
                if (result.getDirection() == Direction.OUT)
                {
                    issueLinkManager.createIssueLink(sourceIssue.getId(), destinationIssue.getId(), linkType.getId(), null, result.getUser());
                }
                else
                {
                    issueLinkManager.createIssueLink(destinationIssue.getId(), sourceIssue.getId(), linkType.getId(), null, result.getUser());
                }
                userHistoryManager.addItemToHistory(UserHistoryItem.ISSUELINKTYPE, result.getUser(), String.valueOf(linkType.getId()), result.getLinkName());
            }
            catch (CreateException createE)
            {
                throw new RuntimeException(createE);
            }

        }
    }

    @Override
    public DeleteIssueLinkValidationResult validateDelete(User user, Issue issue, IssueLink issueLink)
    {
        final ErrorCollection errors = validateIssuePermission(user, issue, Permissions.LINK_ISSUE, "admin.errors.issues.no.permission.to.delete.links");

        validateLinkingEnabled(user, errors);

        if (issueLink == null)
        {
            errors.addErrorMessage(beanFactory.getInstance(user).getText("admin.errors.issues.cannot.find.link"), Reason.NOT_FOUND);
        }

        return new DeleteIssueLinkValidationResult(errors, errors.hasAnyErrors() ? null : issueLink, user);
    }

    @Override
    public void delete(DeleteIssueLinkValidationResult validationResult)
    {
        notNull("validationResult", validationResult);

        if (!validationResult.isValid())
        {
            throw new IllegalStateException("You cannot delete an issue link with an invalid validation result.");
        }

        final IssueLink issueLink = validationResult.getIssueLink();
        final Long sourceId = issueLink.getSourceObject().getId();
        final Long destinationId = issueLink.getDestinationObject().getId();
        final Long issueLinkTypeId = issueLink.getIssueLinkType().getId();

        try
        {
            issueLinkManager.removeIssueLink(issueLinkManager.getIssueLink(sourceId, destinationId, issueLinkTypeId),
                                             validationResult.getUser());
        }
        catch (RemoveException e)
        {
            throw new RuntimeException("Issue link deletion failed" , e);
        }
    }

    private ErrorCollection validateIssuePermission(final User user, final Issue issue, final int permissionsId)
    {
        return validateIssuePermission(user, issue, permissionsId, "issuelinking.service.error.issue.no.permission");
    }

    private ErrorCollection validateIssuePermission(final User user, final Issue issue, final int permissionsId, final String errorMsgKey)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        if (issue == null)
        {
            errors.addErrorMessage(beanFactory.getInstance(user).getText("issuelinking.service.error.issue.doesnt.exist"));
            return errors;
        }
        if (!permissionManager.hasPermission(permissionsId, issue, user))
        {
            errors.addErrorMessage(beanFactory.getInstance(user).getText(errorMsgKey, issue.getKey()));
        }
        return errors;
    }

    private void validateLinkingEnabled(final User user, final ErrorCollection errors)
    {
        final I18nHelper i18n = beanFactory.getInstance(user);
        if (!issueLinkManager.isLinkingEnabled())
        {
            errors.addErrorMessage(i18n.getText("admin.issuelinking.status", i18n.getText("admin.common.words.disabled")), Reason.FORBIDDEN);
        }
    }
    
    private void validateLinkInput(final User user, ErrorCollection errors, Issue issue, IssueLinkType linkType, Collection<String> linkKeys, boolean excludeSystemLinks)
    {
        if (excludeSystemLinks && linkType.isSystemLinkType())
        {
            errors.addErrorMessage(beanFactory.getInstance(user).getText("issuelinking.service.error.invalid.link.type", linkType.getName()));
        }
        if (linkKeys == null || linkKeys.isEmpty())
        {
            errors.addErrorMessage(beanFactory.getInstance(user).getText("issuelinking.service.error.must.provide.issue.links"));
        }
        else
        {
            for (String linkKey : linkKeys)
            {
                if (linkKey.equalsIgnoreCase(issue.getKey()))
                {
                    errors.addErrorMessage(beanFactory.getInstance(user).getText("issuelinking.service.error.self.reference", linkKey));
                }
                MutableIssue linkedIssue = issueManager.getIssueObject(linkKey);
                if (linkedIssue == null)
                {
                    errors.addErrorMessage(beanFactory.getInstance(user).getText("issuelinking.service.error.linked.issue.doesnt.exist", linkKey));
                }
                // NOTE we don't validate that the link type requested does not already link the two issues because the
                // IssueLinkManager ignores this on creation if it already exists.
            }
        }
    }

    private IssueLinkType matchToIssueLinkType(String linkName)
    {
        Collection<IssueLinkType> linkTypes = issueLinkTypeManager.getIssueLinkTypes();
        for (IssueLinkType linkType : linkTypes)
        {
            if (linkName.equals(linkType.getOutward()) || linkName.equals(linkType.getInward()))
            {
                return linkType;
            }
        }
        return null;
    }
}
