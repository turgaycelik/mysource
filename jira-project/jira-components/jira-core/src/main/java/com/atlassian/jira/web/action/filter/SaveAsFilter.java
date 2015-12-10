package com.atlassian.jira.web.action.filter;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.jira.bc.JiraServiceContext;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.fields.layout.column.EditableSearchRequestColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.EditableSearchRequestColumnLayoutImpl;
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
import com.atlassian.jira.user.util.UserSharingPreferencesUtil;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.web.bean.ShareTypeRendererBean;
import com.opensymphony.util.TextUtils;
import org.apache.commons.lang.StringUtils;
import org.ofbiz.core.entity.GenericEntityException;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

public class SaveAsFilter extends AbstractFilterAction implements FilterOperationsAction
{
    private static final String SHARES_FIELD = "shares";
    private static final String FATAL_ERROR = "fatalerror";

    private static final class Key
    {
        private static final String PERSONAL = "1";
        private static final String FILTER = "2";
        private static final String NONE = "3";
    }

    private final ColumnLayoutManager columnLayoutManager;
    private final SearchRequestService searchRequestService;
    private final ShareTypeFactory shareTypeFactory;
    private final JiraAuthenticationContext authCtx;
    private final PermissionManager permissionsManager;
    private final UserSharingPreferencesUtil userSharingPreferencesUtil;

    private String filterDescription = null;
    private String filterName = null;
    private String saveColumnLayout = null;
    private String shareString = null;
    private SharePermissions sharePermissions = SharePermissions.PRIVATE;
    private boolean favourite = true;

    public SaveAsFilter(final IssueSearcherManager issueSearcherManager, final ColumnLayoutManager columnLayoutManager,
            final SearchRequestService searchRequestService, final ShareTypeFactory shareTypeFactory,
            final JiraAuthenticationContext authCtx, final PermissionManager permissionsManager,
            final UserSharingPreferencesUtil userSharingPreferencesUtil, final SearchService searchService,
            final SearchSortUtil searchSortUtil)
    {
        super(issueSearcherManager, searchService, searchSortUtil);

        this.columnLayoutManager = columnLayoutManager;
        this.searchRequestService = searchRequestService;
        this.shareTypeFactory = shareTypeFactory;
        this.authCtx = authCtx;
        this.permissionsManager = permissionsManager;
        this.userSharingPreferencesUtil = userSharingPreferencesUtil;
    }

    @Override
    public String doDefault()
    {
        if (getSearchRequest() == null)
        {
            addErrorMessage(getText("saveasfilter.nocurrent.search"));
            return FATAL_ERROR;
        }
        else if (getLoggedInUser() == null)
        {
            addErrorMessage(getText("admin.errors.filters.no.user"));
            return FATAL_ERROR;
        }

        setPermissions(SharePermissions.PRIVATE);
        if (isEditEnabled())
        {
            setPermissions(userSharingPreferencesUtil.getDefaultSharePermissions(getLoggedInUser()));
        }

        return INPUT;
    }

    @Override
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

        final SearchRequest oldSearchRequest = getSearchRequest();
        if (oldSearchRequest == null)
        {
            addErrorMessage(getText("saveasfilter.nocurrent.search"));
            return;
        }
        else if (getLoggedInUser() == null)
        {
            addErrorMessage(getText("admin.errors.filters.no.user"));
            return;
        }

        if (StringUtils.isBlank(getFilterName()))
        {
            addError("filterName", getText("saveasfilter.specify.name"));
        }

        final SearchRequest newSearchRequest = new SearchRequest(oldSearchRequest);

        newSearchRequest.setName(getFilterName());
        newSearchRequest.setDescription(getFilterDescription());
        newSearchRequest.setPermissions(getPermissions());
        newSearchRequest.setOwner(getLoggedInApplicationUser());

        final JiraServiceContext servceCtx = getJiraServiceContext();
        searchRequestService.validateFilterForCreate(servceCtx, newSearchRequest);
    }

    @Override
    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        /**
         * We need to keep a link around so that we can search for the old search request. This is a hack as the copy constructor of the SearchRequest
         * does not correctly deeply copy the search parameters. TODO: We probably should actually have a way to do a deep copy some how.
         */
        final SearchRequest oldSearchRequest = getSearchRequest();

        oldSearchRequest.setName(getFilterName());
        oldSearchRequest.setDescription(getFilterDescription());
        oldSearchRequest.setOwner(getLoggedInApplicationUser());
        oldSearchRequest.setPermissions(getPermissions());

        final JiraServiceContext ctx = getJiraServiceContext();

        final SearchRequest newSearchRequest = searchRequestService.createFilter(ctx, oldSearchRequest, favourite);

        if (!ctx.getErrorCollection().hasAnyErrors())
        {
            // determine the type of column order to save - default is to save none
            if ((saveColumnLayout == null) || Key.FILTER.equalsIgnoreCase(saveColumnLayout))
            {
                // save the original requests column order if it has one
                if ((oldSearchRequest.getId() != null) && !isDefaultLayout())
                {
                    final EditableSearchRequestColumnLayout currLayout = columnLayoutManager.getEditableSearchRequestColumnLayout(getLoggedInUser(),
                            oldSearchRequest);
                    saveColumnLayout(currLayout, newSearchRequest);
                }
            }
            else if (Key.PERSONAL.equalsIgnoreCase(saveColumnLayout))
            {
                // save the users personal column order
                final EditableSearchRequestColumnLayout currLayout = new EditableSearchRequestColumnLayoutImpl(columnLayoutManager.getColumnLayout(
                        getLoggedInUser()).getColumnLayoutItems(), getLoggedInUser(), oldSearchRequest);
                saveColumnLayout(currLayout, newSearchRequest);
            }
            setSearchRequest(newSearchRequest);

            return getRedirect("IssueNavigator.jspa?mode=hide&requestId=" + newSearchRequest.getId());
        }
        else
        {
            return ERROR;
        }
    }

    public Map getColumnLayoutTypes() throws ColumnLayoutStorageException
    {
        return EasyMap.build(Key.NONE, getText("common.words.none"), Key.FILTER, getText("saveasfilter.columnOrder.filter",
                getSearchRequest().getName()), Key.PERSONAL, getText("saveasfilter.columnOrder.personal"));
    }

    private void saveColumnLayout(final EditableSearchRequestColumnLayout currLayout, final SearchRequest searchRequest)
            throws GenericEntityException, ColumnLayoutStorageException
    {
        currLayout.setSearchRequest(searchRequest);
        columnLayoutManager.storeEditableSearchRequestColumnLayout(currLayout);
    }

    public boolean isDefaultLayout() throws ColumnLayoutStorageException
    {
        return !columnLayoutManager.hasColumnLayout(getSearchRequest());
    }

    public String getFilterDescription()
    {
        return filterDescription;
    }

    public void setFilterDescription(final String filterDescription)
    {
        if (TextUtils.stringSet(filterDescription))
        {
            this.filterDescription = filterDescription;
        }
        else
        {
            this.filterDescription = null;
        }
    }

    public String getFilterName()
    {
        return filterName;
    }

    public void setFilterName(final String filterName)
    {
        this.filterName = filterName;
    }

    public String getsaveColumnLayout()
    {
        return saveColumnLayout;
    }

    public void setsaveColumnLayout(final String saveColumnLayout)
    {
        this.saveColumnLayout = saveColumnLayout;
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
