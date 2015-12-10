package com.atlassian.jira.web.action.user;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionUtils;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.type.ShareType;
import com.atlassian.jira.sharing.type.ShareTypeFactory;
import com.atlassian.jira.sharing.type.ShareTypeRenderer.RenderMode;
import com.atlassian.jira.util.EmailFormatter;
import com.atlassian.jira.util.GroupPermissionChecker;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.jira.web.bean.ShareTypeRendererBean;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

/**
 * Used to edit the name of a portal page.
 */
public class EditPortalPage extends JiraWebActionSupport
{
    private static final String SHARES_FIELD = "shares";

    private final ShareTypeFactory shareTypeFactory;

    private String shareString = null;
    private SharedEntity.SharePermissions sharePermissions = SharedEntity.SharePermissions.PRIVATE;
    private boolean favourite = true;
    private Long pageId = null;
    private String portalPageName = null;
    private String portalPageDescription = null;

    private final PortalPageService portalPageService;
    private final JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionsManager;
    private final FavouritesService favouritesService;

    public EditPortalPage(final PortalPageService portalPageService, final JiraAuthenticationContext authenticationContext, final ShareTypeFactory shareTypeFactory, final PermissionManager permissionsManager, final FavouritesService favouritesService)
    {
        this.portalPageService = portalPageService;
        this.authenticationContext = authenticationContext;
        this.shareTypeFactory = shareTypeFactory;
        this.permissionsManager = permissionsManager;
        this.favouritesService = favouritesService;
    }

    public Long getPageId()
    {
        return pageId;
    }

    public void setPageId(final Long pageId)
    {
        this.pageId = pageId;
    }

    @Override
    public String doDefault()
    {
        final PortalPage portalPage = getPortalPage();
        if (portalPage != null)
        {
            if (portalPage.isSystemDefaultPortalPage())
            {
                addErrorMessage(getText("admin.errors.user.cannot.edit.default.dashboard"));
                return ERROR;
            }
            else
            {
                portalPageName = portalPage.getName();
                portalPageDescription = portalPage.getDescription();
                setPermissions(portalPage.getPermissions());
                setFavourite(favouritesService.isFavourite(getLoggedInApplicationUser(), portalPage));
            }
        }
        else
        {
            addErrorMessage(getText("admin.errors.user.must.select.page"));
            return ERROR;
        }
        return INPUT;
    }

    @Override
    protected void doValidation()
    {
        setPermissions(SharedEntity.SharePermissions.PRIVATE);

        if (StringUtils.isNotBlank(shareString))
        {
            try
            {
                final SharedEntity.SharePermissions permissions = SharePermissionUtils.fromJsonArrayString(shareString);
                setPermissions(permissions);
            }
            catch (final JSONException e)
            {
                log.error("Unable to parse the returned SharePermissions: " + e.getMessage(), e);
                addError(SHARES_FIELD, getText("common.sharing.parse.error"));
                return;
            }
        }

        final PortalPage portalPage = createUpdatedPortalPage();
        if (portalPage == null)
        {
            addErrorMessage(getText("admin.errors.user.must.select.page"));
        }
        else if (portalPage.isSystemDefaultPortalPage())
        {
            addErrorMessage(getText("admin.errors.user.cannot.edit.default.dashboard"));
        }
        else
        {
            portalPageService.validateForUpdate(getJiraServiceContext(), portalPage);
        }
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute()
    {
        final PortalPage portalPage = createUpdatedPortalPage();
        if (portalPage != null)
        {
            portalPageService.updatePortalPage(getJiraServiceContext(), portalPage, favourite);
        }
        return getRedirect("ConfigurePortalPages!default.jspa");
    }

    private PortalPage createUpdatedPortalPage()
    {
        final PortalPage portalPage = getPortalPage();
        if (portalPage != null)
        {
            return PortalPage.portalPage(portalPage).
                    name(getPortalPageName()).
                    description(getPortalPageDescription()).
                    permissions(getPermissions()).
                    build();
        }
        return portalPage;
    }

    private PortalPage getPortalPage()
    {
        if (pageId != null)
        {
            // prevent multiple errors messages if they bugger up the page id
            final JiraServiceContext ignoredCtx = new JiraServiceContextImpl(getLoggedInUser());
            return portalPageService.getPortalPage(ignoredCtx, pageId);
        }
        return null;
    }

    public String getPortalPageName()
    {
        return portalPageName;
    }

    public void setPortalPageName(final String portalPageName)
    {
        this.portalPageName = portalPageName;
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

    public void setShareValues(final String values)
    {
        shareString = values;
    }

    private void setPermissions(final SharedEntity.SharePermissions sharePermissions)
    {
        this.sharePermissions = sharePermissions;
    }

    private SharedEntity.SharePermissions getPermissions()
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

    public Collection<ShareTypeRendererBean> getShareTypes()
    {
        final Collection<ShareType> sharesTypes = shareTypeFactory.getAllShareTypes();
        final List<ShareTypeRendererBean> types = new ArrayList<ShareTypeRendererBean>(sharesTypes.size());
        for (final ShareType shareType : sharesTypes)
        {
            types.add(new ShareTypeRendererBean(shareType, authenticationContext, RenderMode.EDIT, PortalPage.ENTITY_TYPE));
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

}
