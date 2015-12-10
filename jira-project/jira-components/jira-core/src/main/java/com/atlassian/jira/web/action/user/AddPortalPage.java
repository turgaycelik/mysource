package com.atlassian.jira.web.action.user;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.issue.comparator.PortalPageNameComparator;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionUtils;
import com.atlassian.jira.sharing.SharedEntity.SharePermissions;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.sharing.type.ShareTypeRenderer.RenderMode;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.UserHistoryItem;
import com.atlassian.jira.user.UserHistoryManager;
import com.atlassian.jira.user.util.UserSharingPreferencesUtil;
import com.atlassian.jira.util.SimpleErrorCollection;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.ShareTypeRendererBean;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * Action to add a new Portal Page to JIRA.
 *
 * @since 3.13
 */
public class AddPortalPage extends JiraWebActionSupport
{
    private static final String SHARES_FIELD = "shares";

    private final PortalPageService portalPageService;
    private final ShareTypeFactory shareTypeFactory;
    private final JiraAuthenticationContext authCtx;
    private final PermissionManager permissionsManager;
    private final UserSharingPreferencesUtil userSharingPreferencesUtil;
    private final UserHistoryManager userHistoryManager;

    private String portalPageDescription = null;
    private String portalPageName = null;
    private String shareString = null;
    private SharePermissions sharePermissions = SharePermissions.PRIVATE;
    private List<ShareTypeRendererBean> types = null;
    private boolean favourite = true;
    private Long clonePageId = null;
    private PortalPage clonePage = null;
    private Collection clonedTargetPages = null;

    public AddPortalPage(final PortalPageService portalPageService, final ShareTypeFactory shareTypeFactory,
            final UserSharingPreferencesUtil userSharingPreferencesUtil, final JiraAuthenticationContext authCtx,
            final PermissionManager permissionsManager, final UserHistoryManager userHistoryManager)
    {
        this.portalPageService = portalPageService;
        this.shareTypeFactory = shareTypeFactory;
        this.authCtx = authCtx;
        this.permissionsManager = permissionsManager;
        this.userSharingPreferencesUtil = userSharingPreferencesUtil;
        this.userHistoryManager = userHistoryManager;
    }

    public String doDefault()
    {
        setPermissions(SharePermissions.PRIVATE);
        if (isEditEnabled())
        {
            setPermissions(userSharingPreferencesUtil.getDefaultSharePermissions(getLoggedInUser()));
        }

        if (clonePageId != null)
        {
            if (getClonePage() == null)
            {
                addErrorMessage(getText("addportalpage.clone.does.not.exist"));
                return ERROR;
            }
        }

        return INPUT;
    }

    protected void doValidation()
    {
        setPermissions(SharePermissions.PRIVATE);

        if (StringUtils.isNotBlank(shareString))
        {
            try
            {
                final SharePermissions permissions = SharePermissionUtils.fromJsonArrayString(shareString);
                setPermissions(permissions);
            }
            catch (final JSONException e)
            {
                log.error("Unable to parse the returned SharePermissions: " + e.getMessage(), e);
                addError(SHARES_FIELD, getText("common.sharing.parse.error"));
                return;
            }
        }

        final JiraServiceContext servceCtx = getJiraServiceContext();

        final PortalPage newPortalPage = createNewPortalPage();
        if (clonePageId != null)
        {
            portalPageService.validateForCreatePortalPageByClone(servceCtx, newPortalPage, clonePageId);
        }
        else
        {
            portalPageService.validateForCreate(servceCtx, newPortalPage);
        }
    }

    @RequiresXsrfCheck
    protected String doExecute()
    {
        final JiraServiceContext servceCtx = getJiraServiceContext();
        final PortalPage newPortalPage;
        if (clonePageId != null)
        {
            newPortalPage = clonePortalPage(servceCtx, clonePageId);
        }
        else
        {
            newPortalPage = createBlankPortalPage(servceCtx);
        }

        if (newPortalPage != null)
        {
            if (favourite)
            {
                //JRA-17301: if we favourited this page, also make it the current tab on the dashboard!
                userHistoryManager.addItemToHistory(UserHistoryItem.DASHBOARD, getLoggedInApplicationUser(), String.valueOf(newPortalPage.getId()));
                return getRedirect("ConfigurePortalPages!default.jspa#view=favourite");
            }
            else
            {
                return getRedirect("ConfigurePortalPages!default.jspa#view=my");
            }
        }
        else
        {
            return ERROR;
        }
    }

    public String getPortalPageDescription()
    {
        return portalPageDescription;
    }

    public void setPortalPageDescription(final String portalPageDescription)
    {
        if (StringUtils.isNotBlank(portalPageDescription))
        {
            this.portalPageDescription = portalPageDescription;
        }
        else
        {
            this.portalPageDescription = null;
        }
    }

    public Collection<PortalPage> getCloneTargetDashboardPages()
    {
        if (clonedTargetPages == null)
        {
            final Set<Long> clonePageIds = new HashSet<Long>();

            final PortalPage systemDefaultPortalPage = portalPageService.getSystemDefaultPortalPage();
            clonePageIds.add(systemDefaultPortalPage.getId());

            final Collection<PortalPage> favouritePortalPages = portalPageService.getFavouritePortalPages(getLoggedInUser());
            final List<PortalPage> clonePages = new ArrayList<PortalPage>(favouritePortalPages.size() + 1);
            for (final PortalPage portalPage : favouritePortalPages)
            {
                if (clonePageIds.add(portalPage.getId()))
                {
                    clonePages.add(portalPage);
                }
            }

            final Collection<PortalPage> myPages = portalPageService.getOwnedPortalPages(getLoggedInUser());
            for (final PortalPage portalPage : myPages)
            {
                if (clonePageIds.add(portalPage.getId()))
                {
                    clonePages.add(portalPage);
                }
            }

            if ((clonePageId != null) && !clonePageIds.contains(clonePageId))
            {
                final PortalPage portalPage = getClonePage();
                if (portalPage != null)
                {
                    clonePages.add(portalPage);
                }
            }

            Collections.sort(clonePages, new PortalPageNameComparator());
            clonePages.add(0, systemDefaultPortalPage);

            clonedTargetPages = Collections.unmodifiableCollection(clonePages);
        }
        return clonedTargetPages;
    }

    public String getPortalPageName()
    {
        return portalPageName;
    }

    public void setPortalPageName(final String portalPageName)
    {
        this.portalPageName = portalPageName;
    }

    public void setShareValues(final String values)
    {
        shareString = values;
    }

    private void setPermissions(final SharePermissions sharePermissions)
    {
        this.sharePermissions = sharePermissions;
    }

    private SharePermissions getPermissions()
    {
        return sharePermissions;
    }

    public boolean isFavourite()
    {
        return favourite;
    }

    public void setFavourite(final boolean favourite)
    {
        this.favourite = favourite;
    }

    public Long getClonePageId()
    {
        return clonePageId;
    }

    public void setClonePageId(final Long clonePageId)
    {
        this.clonePageId = clonePageId;
    }

    public Collection /* <ShareTypeRendererBean> */getShareTypes()
    {
        if (types == null)
        {
            final Collection<ShareType> sharesTypes = shareTypeFactory.getAllShareTypes();
            types = new ArrayList<ShareTypeRendererBean>(sharesTypes.size());
            for (final ShareType shareType : sharesTypes)
            {
                types.add(new ShareTypeRendererBean(shareType, authCtx, RenderMode.EDIT, PortalPage.ENTITY_TYPE));
            }
        }

        return types;
    }

    public boolean showShares()
    {
        return (isEditEnabled() || !getPermissions().isEmpty());
    }

    public boolean isEditEnabled()
    {
        return permissionsManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, getLoggedInUser());
    }

    public String getJsonString()
    {
        final List<SharePermission> sortedShares = new ArrayList<SharePermission>(getPermissions().getPermissionSet());
        Collections.sort(sortedShares, shareTypeFactory.getPermissionComparator());
        try
        {
            return SharePermissionUtils.toJsonArray(sortedShares).toString();
        }
        catch (final JSONException e)
        {
            log.error("Unable to create JSON representation of shares: " + e.getMessage(), e);
            return "";
        }
    }

    private PortalPage getClonePage()
    {
        if ((clonePageId != null) && (clonePage == null))
        {
            final JiraServiceContext ctx = new JiraServiceContextImpl(getLoggedInUser(), new SimpleErrorCollection());
            clonePage = portalPageService.getPortalPage(ctx, clonePageId);
        }
        return clonePage;
    }

    private PortalPage createBlankPortalPage(final JiraServiceContext serviceContext)
    {
        final PortalPage newPortalPage = createNewPortalPage();
        if (portalPageService.validateForCreate(serviceContext, newPortalPage))
        {
            return portalPageService.createPortalPage(serviceContext, newPortalPage, favourite);
        }
        return null;
    }

    private PortalPage clonePortalPage(final JiraServiceContext serviceContext, final Long clonePortalPageId)
    {
        final PortalPage newPortalPage = createNewPortalPage();
        if (portalPageService.validateForCreatePortalPageByClone(serviceContext, newPortalPage, clonePortalPageId))
        {
            return portalPageService.createPortalPageByClone(serviceContext, newPortalPage, clonePortalPageId, favourite);
        }
        return null;
    }

    private PortalPage createNewPortalPage()
    {
        final ApplicationUser user = getLoggedInApplicationUser();
        return PortalPage.name(portalPageName).description(portalPageDescription).owner(user).permissions(getPermissions()).build();
    }

}
