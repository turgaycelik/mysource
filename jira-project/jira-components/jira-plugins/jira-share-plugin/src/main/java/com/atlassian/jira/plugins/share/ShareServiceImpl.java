package com.atlassian.jira.plugins.share;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.plugins.share.issue.ShareIssueService;
import com.atlassian.jira.plugins.share.search.ShareSearchRequestService;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.dbc.Assertions;
import com.atlassian.plugin.spring.scanner.annotation.export.ExportAsService;
import com.atlassian.plugin.spring.scanner.annotation.imports.ComponentImport;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

/**
 * Listens for ShareIssueEvents and sends items to the Mail queue.
 *
 * @since v5.0
 */
@Component
@ExportAsService
public final class ShareServiceImpl implements ShareService
{
    public static final String NO_USERS_OR_EMAILS_PROVIDED = "jira-share-plugin.no.users.or.emails.provided";
    public static final String NO_PERMISSION_TO_BROWSE_USERS = "jira-share-plugin.no.permission.to.browse.users";

    private final I18nHelper i18nHelper;
    private final PermissionManager permissionManager;
    private final ShareIssueService shareIssueService;
    private final ShareSearchRequestService shareSearchRequestService;

    @Autowired
    public ShareServiceImpl(
            @ComponentImport I18nHelper i18nHelper,
            @ComponentImport PermissionManager permissionManager,
            ShareIssueService shareIssueService,
            ShareSearchRequestService shareSearchRequestService)
    {
        this.shareIssueService = shareIssueService;
        this.shareSearchRequestService = shareSearchRequestService;
        this.i18nHelper = Assertions.notNull(i18nHelper);
        this.permissionManager = Assertions.notNull(permissionManager);
    }


    @Override
    public ValidateShareIssueResult validateShareIssue(ApplicationUser remoteUser, ShareBean shareBean, Issue issue)
    {
        final ErrorCollection errors = validateShare(remoteUser, shareBean);
        return new ValidateShareIssueResult(errors, remoteUser, shareBean, issue);
    }

    @Override
    public ValidateShareIssueResult validateShareIssue(final User remoteUser, final ShareBean shareBean, final Issue issue)
    {
        return validateShareIssue(ApplicationUsers.from(remoteUser), shareBean, issue);
    }

    @Override
    public void shareIssue(ValidateShareIssueResult result)
    {
        if (!result.isValid())
        {
            throw new IllegalStateException("Validation result was not valid.");
        }

        shareIssueService.shareIssue(result);
    }

    @Override
    public ValidateShareSearchRequestResult validateShareSearchRequest(ApplicationUser remoteUser, ShareBean shareBean, SearchRequest searchRequest)
    {
        final ErrorCollection errors = validateShare(remoteUser, shareBean);
        return new ValidateShareSearchRequestResult(errors, remoteUser, shareBean, searchRequest);
    }

    @Override
    public ValidateShareSearchRequestResult validateShareSearchRequest(final User remoteUser, final ShareBean shareBean, final SearchRequest searchRequest)
    {
        return validateShareSearchRequest(ApplicationUsers.from(remoteUser), shareBean, searchRequest);
    }

    @Override
    public void shareSearchRequest(ValidateShareSearchRequestResult result)
    {
        if (!result.isValid())
        {
            throw new IllegalStateException("Validation result was not valid.");
        }

        shareSearchRequestService.shareSearchRequest(result);
    }

    private ErrorCollection validateShare(ApplicationUser remoteUser, ShareBean shareBean)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        if (shareBean.getUsernames().isEmpty() && shareBean.getEmails().isEmpty())
        {
            errors.addErrorMessage(i18nHelper.getText(NO_USERS_OR_EMAILS_PROVIDED));
        }

        if (!permissionManager.hasPermission(Permissions.USER_PICKER, remoteUser))
        {
            errors.addErrorMessage(i18nHelper.getText(NO_PERMISSION_TO_BROWSE_USERS));
        }
        return errors;
    }
}
