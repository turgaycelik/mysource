package com.atlassian.jira.dev.backdoor;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.bc.issue.search.SearchService;
import com.atlassian.jira.exception.PermissionException;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayout;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutItem;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutManager;
import com.atlassian.jira.issue.fields.layout.column.ColumnLayoutStorageException;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.SharePermission;
import com.atlassian.jira.sharing.SharePermissionImpl;
import com.atlassian.jira.sharing.SharedEntity;
import com.atlassian.jira.sharing.type.GroupShareType;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

@Path ("filter")
@Produces (MediaType.APPLICATION_JSON)
@AnonymousAllowed
public class FilterBackdoor
{
    private final SearchService searchService;
    private final SearchRequestService searchRequestService;
    private final JiraAuthenticationContext authenticationContext;
    private final JqlStringSupport jqlStringSupport;
    private final FavouritesManager<SearchRequest> favouritesManager;
    private final ColumnLayoutManager columnLayoutManager;
    private final UserManager userManager;

    public FilterBackdoor(SearchRequestService searchRequestService, JiraAuthenticationContext authenticationContext,
            SearchService searchService, JqlStringSupport jqlStringSupport, FavouritesManager<SearchRequest> favouritesManager,
            ColumnLayoutManager columnLayoutManager, UserManager userManager)
    {
        this.searchRequestService = searchRequestService;
        this.authenticationContext = authenticationContext;
        this.searchService = searchService;
        this.jqlStringSupport = jqlStringSupport;
        this.favouritesManager = favouritesManager;
        this.columnLayoutManager = columnLayoutManager;
        this.userManager = userManager;
    }

    @GET
    @Path ("create")
    @Produces (MediaType.TEXT_PLAIN)
    public Response createFilter(
            @QueryParam ("jql") String jql,
            @QueryParam ("name") String name,
            @QueryParam ("owner") String owner,
            @QueryParam ("isFavourite") Boolean isFavourite,
            @QueryParam ("groupPermission") String groupPermission)
    {
        ApplicationUser user = null;
        if (owner == null)
        {
            user = authenticationContext.getUser();
        }
        else
        {
            user = userManager.getUserByName(owner);
        }
        JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(user);

        SearchService.ParseResult parseResult = searchService.parseQuery(ApplicationUsers.toDirectoryUser(user), jql);

        //we are all good, full steam ahead
        SearchRequest request = new SearchRequest(parseResult.getQuery());
        request.setName(name);
        request.setOwner(user);
        if (groupPermission == null)
        {
            request.setPermissions(SharedEntity.SharePermissions.PRIVATE);
        }
        else
        {
            //Legacy code sucks
            SharePermission permission = new SharePermissionImpl(GroupShareType.TYPE, groupPermission, null);
            SharedEntity.SharePermissions permissions = new SharedEntity.SharePermissions(Collections.singleton(permission));
            request.setPermissions(permissions);
        }
        if (isFavourite == null)
        {
            isFavourite = false;
        }
        SearchRequest filter = searchRequestService.createFilter(jiraServiceContext, request, isFavourite);
        return Response.ok("" + filter.getId()).build();
    }

    @GET
    @Path ("jql")
    @Produces (MediaType.TEXT_PLAIN)
    public Response createFilter(@QueryParam ("id") String id)
    {
        final User user = authenticationContext.getLoggedInUser();
        JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(user);
        return Response.ok(jqlStringSupport.generateJqlString(searchRequestService.getFilter(jiraServiceContext, Long.parseLong(id)).getQuery())).build();
    }

    @GET
    public Response getFilter(@QueryParam ("id") String id)
    {
        final User user = authenticationContext.getLoggedInUser();
        JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(user);
        SearchRequest filter = searchRequestService.getFilter(jiraServiceContext, Long.parseLong(id));
        String name = filter.getName();
        String description = filter.getDescription();
        Long favouriteCount = filter.getFavouriteCount();
        Set<SharePermission> permissions = filter.getPermissions().getPermissionSet();
        List<SharePermissionInfoBean> permissionBeans = new ArrayList<SharePermissionInfoBean>();
        for (SharePermission permission : permissions)
        {
            SharePermissionInfoBean sharePermissionInfo = new SharePermissionInfoBean(permission.getId(), permission.getType().get(), permission.getParam1(), permission.getParam2());
            permissionBeans.add(sharePermissionInfo);
        }
        Boolean isFavourite = false;
        try
        {
            isFavourite = favouritesManager.isFavourite(user, filter);
        }
        catch (PermissionException e)
        {
            throw new RuntimeException(e);
        }
        FilterInfoBean filterInfo = new FilterInfoBean(Long.parseLong(id), name, description, filter.getOwnerUserName(), isFavourite, favouriteCount, permissionBeans);
        return Response.ok(filterInfo).build();
    }

    @GET
    @Path ("columns")
    @Produces (MediaType.APPLICATION_JSON)
    public Response getFilterCols(@QueryParam ("id") String id)
    {
        final ApplicationUser user = authenticationContext.getUser();
        List<String> cols = new ArrayList<String>();
        JiraServiceContextImpl jiraServiceContext = new JiraServiceContextImpl(user);
        try
        {
            ColumnLayout columnLayout = columnLayoutManager.getColumnLayout(authenticationContext.getLoggedInUser(), searchRequestService.getFilter(jiraServiceContext, Long.parseLong(id)));
            for (ColumnLayoutItem columnLayoutItem : columnLayout.getColumnLayoutItems())
            {
                cols.add(columnLayoutItem.getId());
            }
        }
        catch (ColumnLayoutStorageException e)
        {
            throw new RuntimeException(e);
        }
        return Response.ok(cols).build();
    }
}
