package com.atlassian.jira.web.action.admin.dashboards;


import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.bc.user.search.UserPickerSearchService;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.portal.PortalPageManager;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.sharing.ShareTypeValidatorUtils;
import com.atlassian.jira.sharing.type.ShareTypeValidator;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.SimpleErrorCollection;
import org.apache.commons.lang.StringUtils;

import java.net.URI;

/**
 * Action for changing the SharedFilter owner
 *
 * @since v4.4
 */
public class ChangeSharedDashboardOwner extends AbstractDashboardAdministration
{

    private ApplicationUser ownerUserObj;
    private String owner;
    private String ownerError;

    private final UserPickerSearchService userPickerSearchService;
    private final AvatarService avatarService;
    private final UserManager userManager;
    private final PermissionManager permissionManager;
    private final PortalPageService portalPageService;
    private final ShareTypeValidatorUtils shareTypeValidatorUtils;

    public ChangeSharedDashboardOwner(PermissionManager permissionManager, UserPickerSearchService userPickerSearchService,
            AvatarService avatarService, UserManager userManager,
            PortalPageService portalPageService, PortalPageManager portalPageManager, ShareTypeValidatorUtils shareTypeValidatorUtils)
    {
        super(permissionManager, portalPageManager);
        this.permissionManager = permissionManager;
        this.userPickerSearchService = userPickerSearchService;
        this.avatarService = avatarService;
        this.userManager = userManager;
        this.portalPageService = portalPageService;
        this.shareTypeValidatorUtils = shareTypeValidatorUtils;
    }


    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        JiraServiceContext ctx = new JiraServiceContextImpl(getOwnerUserObj());
        portalPageService.validateForChangeOwner(ctx, getDashboard() );
        addErrorCollection((ctx.getErrorCollection()));
        if (hasAnyErrors())
        {
            return ERROR;
        }

        portalPageService.updatePortalPageUnconditionally(ctx, getLoggedInApplicationUser(), getDashboard());
        addErrorCollection(convertAllDelegatedUserErrorsToMessages(ctx.getErrorCollection()));
        if (hasAnyErrors())
        {
            return ERROR;
        }
        if (isInlineDialogMode())
        {
            return returnCompleteWithInlineRedirect(buildReturnUri());
        }
        else
        {
            String returnUrl =  buildReturnUri();
            setReturnUrl(null);
            return forceRedirect(returnUrl);
        }
    }

    // The ShareTypeValidator returns validation failures as field errors, the permissions checker
    //  returns them as messages - simply turn them  into messages.  We are also only interested in the delegated user form
    // of the error message
    private ErrorCollection convertAllDelegatedUserErrorsToMessages(ErrorCollection errorCollection)
    {
        final ErrorCollection errors = new SimpleErrorCollection();
        errors.addErrorMessages(errorCollection.getErrorMessages());
        String delegatedUserError = errorCollection.getErrors().get(ShareTypeValidator.DELEGATED_ERROR_KEY);
        if (StringUtils.isNotBlank(delegatedUserError))
        {
            errors.addErrorMessage(delegatedUserError);
        }
        return errors;
    }

    @Override
    protected void doValidation()
    {
        if (StringUtils.isBlank(ownerError))
        {
            setOwnerError(null);
        }
        if (StringUtils.isBlank(owner))
        {
            setOwnerError("");
            addError("owner", getText("shareddashboards.admin.dashboard.owner.empty"));
        }
        else
        {
            validateUserExists(owner);
        }
        if (!hasAnyErrors())
        {

            setDashboard(createUpdatedPortalPageForOwner(getDashboard(), owner));
            final JiraServiceContext ctx = getJiraServiceContext(owner);
            shareTypeValidatorUtils.isValidSharePermission(ctx, getDashboard());
            addErrorCollection(convertAllDelegatedUserErrorsToMessages(ctx.getErrorCollection()));
        }
    }

    public boolean canChangeOwner()
    {
        return !hasAnyErrors();
    }

    public boolean userPickerDisabled()
    {
        return !userPickerSearchService.canPerformAjaxSearch(this.getJiraServiceContext());
    }

    public ApplicationUser getOwnerUserObj()
    {
        if (getOwner() != null && ownerUserObj == null)
        {
            ownerUserObj = userManager.getUserByName(getOwner());
        }
        return ownerUserObj;
    }

    public String getOwner()
    {
        if (owner == null)
        {
            owner = getDashboard().getOwnerUserName();
        }
        return owner;
    }

    public void setOwner(String owner)
    {
        this.owner = owner;
    }

    private void validateUserExists(String owner)
    {
        if (userManager.getUserByName(owner) == null)
        {
            addError("owner", String.format("The user %s does not exist", owner));
            setOwnerError(owner);
        }
    }

    public URI getOwnerUserAvatarUrl()
    {
        return avatarService.getAvatarUrlNoPermCheck(getOwner(), Avatar.Size.SMALL);
    }

    public String getOwnerError()
    {
        return ownerError;
    }

    public void setOwnerError(String ownerError)
    {
        this.ownerError = ownerError;
    }

    private PortalPage createUpdatedPortalPageForOwner(PortalPage dashboard, String ownerName)
    {
        final PortalPage portalPage = getDashboard();
        if (portalPage != null)
        {
            return PortalPage.portalPage(portalPage).
                    owner(getOwnerUserObj()).
                    build();
        }
        return portalPage;
    }

}
