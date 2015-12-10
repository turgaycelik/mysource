package com.atlassian.jira.web.action.filter;

import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.favourites.FavouritesService;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.managers.IssueSearcherManager;
import com.atlassian.jira.issue.search.util.SearchSortUtil;
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
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.web.bean.ShareTypeRendererBean;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public class EditFilter extends AbstractFilterAction implements FilterOperationsAction
{
    private static final String FATAL_ERROR = "fatalerror";

    private String name = null;
    private String description = null;
    private String shareString = null;
    private final Set<SharePermission> shares = new HashSet<SharePermission>();
    private Boolean isFavourite = null;

    private final FavouritesService favouritesService;
    private final ShareTypeFactory shareTypeFactory;
    private final JiraAuthenticationContext authCtx;
    private final SearchRequestService searchRequestService;
    private final PermissionManager permissionsManager;

    public EditFilter(final IssueSearcherManager issueSearcherManager, final SearchRequestService searchRequestService, final FavouritesService favouritesService, final PermissionManager permissionsManager, final JiraAuthenticationContext authCtx, final ShareTypeFactory shareTypeFactory, final SearchService searchService, final SearchSortUtil searchSortUtil)
    {
        super(issueSearcherManager, searchService, searchSortUtil);
        this.searchRequestService = searchRequestService;
        this.favouritesService = favouritesService;
        this.permissionsManager = permissionsManager;
        this.authCtx = authCtx;
        this.shareTypeFactory = shareTypeFactory;
    }

    @Override
    public String doDefault() throws Exception
    {
        final JiraServiceContext ctx = getJiraServiceContext();
        if (getFilterId() != null)
        {
            final SearchRequest request = getFilter();

            if (ctx.getErrorCollection().hasAnyErrors())
            {
                return FATAL_ERROR;
            }
            else if (request == null)
            {
                addErrorMessage(getText("admin.errors.filters.nonexistent"));
                return FATAL_ERROR;
            }

            setSearchRequest(request);
        }

        final SearchRequest searchRequest = getSearchRequest();

        if (searchRequest == null)
        {
            addErrorMessage(getText("admin.errors.filters.no.search.request"));
            return FATAL_ERROR;
        }
        else if (searchRequest.getOwner() != null)
        {
            if (!searchRequest.getOwner().equals(getLoggedInApplicationUser()))
            {
                addErrorMessage(getText("admin.errors.filters.not.owner"));
                return FATAL_ERROR;
            }
        }
        else if (searchRequest.isModified())
        {
            addErrorMessage(getText("admin.errors.filters.search.request.updated"));
            return ERROR;
        }

        // Make sure that we are working with a saved filter
        if (!validateSearchFilterIsSavedFilter(searchRequest, "editfilter.current.filter.not.saved"))
        {
            return FATAL_ERROR;
        }

        setFilterName(searchRequest.getName());
        setFilterDescription(searchRequest.getDescription());
        setFilterId(searchRequest.getId());
        setSharePermissions(searchRequest.getPermissions());
        setFavourite(favouritesService.isFavourite(getLoggedInApplicationUser(), getSearchRequest()));

        // The share permissions can be invalid now. Give the user an error message .
        searchRequestService.validateFilterForUpdate(getJiraServiceContext(), searchRequest);

        //
        // set up a return URL if it hasn't been specified already. Its use by the Cancel button
        if ((getReturnUrl() == null) || (getReturnUrl().length() == 0))
        {
            final String redirectURL = "IssueNavigator.jspa?mode=hide&requestId=" + getSearchRequest().getId();
            setReturnUrl(redirectURL);
        }
        return INPUT;
    }

    @Override
    protected void doValidation()
    {
        if (getFilterId() == null)
        {
            addErrorMessage(getText("admin.errors.filters.no.search.request"));
            return;
        }
        SearchRequest oldSearchRequest = getSearchRequest();
        if ((oldSearchRequest == null) || !getFilterId().equals(oldSearchRequest.getId()))
        {
            oldSearchRequest = getFilter();
            if (hasAnyErrors())
            {
                setSearchRequest(null);
                return;
            }
            setSearchRequest(oldSearchRequest);
        }

        if (oldSearchRequest != null)
        {
            final ApplicationUser owner = oldSearchRequest.getOwner();
            if (owner != null && !owner.equals(getLoggedInApplicationUser()))
            {
                addErrorMessage(getText("admin.errors.filters.not.owner"));
                return;
            }
            setSharePermissions(oldSearchRequest.getPermissions());
        }

        if (StringUtils.isNotBlank(shareString))
        {
            try
            {
                final SharePermissions permissions = SharePermissionUtils.fromJsonArrayString(shareString);
                setSharePermissions(permissions);
            }
            catch (final JSONException e)
            {
                log.error("Unable to parse the returned SharePermissions: " + e.getMessage(), e);
            }
        }

        if (oldSearchRequest == null)
        {
            addErrorMessage(getText("admin.errors.filters.no.search.request"));
            return;
        }
        else if (getLoggedInUser() == null)
        {
            addErrorMessage(getText("admin.errors.filters.no.user"));
            return;
        }

        if (!TextUtils.stringSet(getFilterName()))
        {
            addError("name", getText("admin.errors.filters.must.specify.name"));
        }

        final SearchRequest newSearchRequest = new SearchRequest(oldSearchRequest);
        newSearchRequest.setOwner(getLoggedInApplicationUser());
        newSearchRequest.setName(getFilterName());
        newSearchRequest.setDescription(getFilterDescription());
        newSearchRequest.setPermissions(new SharePermissions(getSharePermissions()));

        searchRequestService.validateFilterForUpdate(getJiraServiceContext(), newSearchRequest);
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        final SearchRequest searchRequest = getSearchRequest();
        searchRequest.setName(getFilterName());
        searchRequest.setDescription(getFilterDescription());

        if (StringUtils.isNotBlank(shareString))
        {
            searchRequest.setPermissions(getPermissions());
        }

        SearchRequest returnedRequest;

        if (isFavourite == null)
        {
            returnedRequest = searchRequestService.updateFilter(getJiraServiceContext(), searchRequest);
        }
        else
        {
            returnedRequest = searchRequestService.updateFilter(getJiraServiceContext(), searchRequest, isFavourite);
        }

        setSearchRequest(returnedRequest);
        if (isInlineDialogMode())
        {
            return returnComplete(getReturnUrl());
        }
        else
        {
            return getRedirect("IssueNavigator.jspa?mode=hide&requestId=" + getSearchRequest().getId());
        }
    }

    public Set<SharePermission> getSharePermissions()
    {
        return shares;
    }

    private SharePermissions getPermissions()
    {
        return new SharePermissions(new HashSet<SharePermission>(shares));
    }

    private void setSharePermissions(final SharePermissions sharePermissions)
    {
        shares.clear();
        if (sharePermissions != null)
        {
            shares.addAll(sharePermissions.getPermissionSet());
        }
    }

    public void setShareValues(final String values)
    {
        shareString = values;
    }

    public Collection<ShareTypeRendererBean> getShareTypes()
    {
        final Collection<ShareType> sharesTypes = shareTypeFactory.getAllShareTypes();
        final List<ShareTypeRendererBean> types = new ArrayList<ShareTypeRendererBean>(sharesTypes.size());
        for (final ShareType shareType : sharesTypes)
        {
            types.add(new ShareTypeRendererBean(shareType, authCtx, RenderMode.EDIT, SearchRequest.ENTITY_TYPE));
        }
        return types;
    }

    public String getFilterName()
    {
        return name;
    }

    public void setFilterName(final String name)
    {
        this.name = name;
    }

    public String getFilterDescription()
    {
        return description;
    }

    public void setFilterDescription(final String description)
    {
        this.description = description;
    }

    public String getCancelURL()
    {
        String cancelURL = getReturnUrl();
        if ((cancelURL == null) || (cancelURL.length() == 0))
        {
            cancelURL = "IssueNavigator.jspa";
        }
        return cancelURL;
    }

    public boolean isFavourite()
    {
        return isFavourite;
    }

    public void setFavourite(final boolean favourite)
    {
        isFavourite = favourite;
    }

    public String getJsonString()
    {
        final List<SharePermission> sortedShares = new ArrayList<SharePermission>(getSharePermissions());
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

    public boolean showShares()
    {
        return (isEditEnabled() || !getSharePermissions().isEmpty());
    }

    public boolean isEditEnabled()
    {
        return permissionsManager.hasPermission(Permissions.CREATE_SHARED_OBJECTS, getLoggedInUser());
    }

    public boolean isModified()
    {
        return (getSearchRequest() != null) && getSearchRequest().isModified();
    }
}
