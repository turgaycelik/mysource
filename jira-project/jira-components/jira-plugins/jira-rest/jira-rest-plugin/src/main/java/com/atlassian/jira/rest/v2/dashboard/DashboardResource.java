package com.atlassian.jira.rest.v2.dashboard;

import com.atlassian.jira.bc.JiraServiceContextImpl;
import com.atlassian.jira.bc.portal.PortalPageService;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.portal.PortalPage;
import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.api.dashboard.DashboardBean;
import com.atlassian.jira.rest.api.dashboard.DashboardsBean;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.context.ContextUriInfo;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.sharing.search.SharedEntitySearchParameters;
import com.atlassian.jira.sharing.search.SharedEntitySearchParametersBuilder;
import com.atlassian.jira.sharing.search.SharedEntitySearchResult;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import com.google.common.collect.Lists;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.PathParam;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.Response;
import javax.ws.rs.core.UriBuilder;
import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;
import static javax.ws.rs.core.MediaType.APPLICATION_JSON;
import static org.apache.commons.lang.StringUtils.isBlank;

/**
 * The <code>/dashboard</code> resource.
 *
 * @since v5.0
 */
@AnonymousAllowed
@Path ("dashboard")
@Consumes ({ APPLICATION_JSON })
@Produces ({ APPLICATION_JSON })
public class DashboardResource
{
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final PortalPageService portalPageService;
    private final I18nHelper.BeanFactory i18nFactory;
    private final JiraBaseUrls jiraBaseUrls;
    private final ContextUriInfo uriInfo;

    public DashboardResource(JiraAuthenticationContext jiraAuthenticationContext, PortalPageService portalPageService, I18nHelper.BeanFactory i18nFactory, JiraBaseUrls jiraBaseUrls, ContextUriInfo uriInfo)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.portalPageService = portalPageService;
        this.i18nFactory = i18nFactory;
        this.jiraBaseUrls = jiraBaseUrls;
        this.uriInfo = uriInfo;
    }

    /**
     * Returns a list of all dashboards, optionally filtering them.
     *
     * @param filter an optional filter that is applied to the list of dashboards. Valid values include
     * <code>"favourite"</code> for returning only favourite dashboards, and <code>"my"</code> for returning
     * dashboards that are owned by the calling user.
     * @param startAtParam the index of the first dashboard to return (0-based). must be 0 or a multiple of
     * <code>maxResults</code>
     * @param maxResultsParam a hint as to the the maximum number of dashboards to return in each call. Note that the
     * JIRA server reserves the right to impose a <code>maxResults</code> limit that is lower than the value that a
     * client provides, dues to lack or resources or any other condition. When this happens, your results will be
     * truncated. Callers should always check the returned <code>maxResults</code> to determine the value that is
     * effectively being used.
     * @return a list of Dashboards
     *
     * @response.representation.200.doc
     *      Returns a list of dashboards.
     *
     * @response.representation.200.example
     *      {@link DashboardResourceExamples#LIST_EXAMPLE}
     */
    @GET
    public Response list(@QueryParam ("filter") String filter, @QueryParam ("startAt") Integer startAtParam, @QueryParam ("maxResults") Integer maxResultsParam)
    {
        int startAt = startAtParam != null ? Math.max(startAtParam, 0) : 0;
        if (maxResultsParam != null && maxResultsParam != 0 && (startAt % maxResultsParam != 0))
        {
            throw new BadRequestWebException(ErrorCollection.of(i18n().getText("rest.dashboard.invalid.startAt", String.valueOf(maxResultsParam), String.valueOf(startAt))));
        }

        int maxResults = (maxResultsParam == null) ? (startAt != 0 ? startAt : 20) : maxResultsParam < 0 ? 20 : Math.min(maxResultsParam, 1000);

        DashboardsBean dashboards = null;
        if ("favourite".equals(filter))
        {
            dashboards = search(searchParams().setFavourite(true).toSearchParameters(), startAt, maxResults);
        }
        else if ("my".equals(filter))
        {
            ApplicationUser user = jiraAuthenticationContext.getUser();
            if (user == null)
            {
                dashboards = new DashboardsBean().startAt(startAt).maxResults(maxResults).total(0).dashboards(Collections.<DashboardBean>emptyList());
            }
            else
            {
                dashboards = search(searchParams().setUserName(user.getUsername()).toSearchParameters(), startAt, maxResults);
            }
        }
        else if (isBlank(filter))
        {
            dashboards = search(searchParams().toSearchParameters(), startAt, maxResults);
        }

        if (dashboards != null)
        {
            dashboards = addPreviousNextLinks(filter, startAt, maxResults, dashboards);

            return Response.ok().entity(dashboards).cacheControl(never()).build();
        }

        throw new BadRequestWebException(ErrorCollection.of(i18n().getText("rest.dashboard.filter.bad.param", filter, "filter", "'favourite', 'my'")));
    }

    /**
     * Returns a single dashboard.
     *
     * @param id the dashboard id
     * @return a dashboard
     *
     * @response.representation.200.doc
     *      Returns a single dashboard.
     *
     * @response.representation.200.example
     *      {@link DashboardResourceExamples#SINGLE_EXAMPLE}
     *
     * @response.representation.404.doc
     *      Returned if there is no dashboard with the specified id, or if the user does not have permission to see it.
     */
    @GET
    @Path ("{id}")
    public Response getDashboard(@PathParam ("id") String id)
    {
        PortalPage portalPage = null;
        try
        {
            portalPage = portalPageService.getPortalPage(makeContext(), Long.valueOf(id));
        }
        catch (NumberFormatException e)
        {
            // ignore
        }

        if (portalPage != null)
        {
            return Response.ok().entity(buildDashboardBean(portalPage)).cacheControl(never()).build();
        }

        throw new NotFoundWebException(ErrorCollection.of(i18n().getText("rest.dashboard.not.found", id)));
    }

    private DashboardsBean search(SharedEntitySearchParameters searchParams, Integer startAt, Integer maxResults)
    {
        int total;
        List<PortalPage> results;
        if (searchParams.getFavourite() != null && searchParams.getFavourite())
        {
            // PortalPageService.search() doesn't really support searching for portal pages that are favourited by a
            // certain user only, so we have to use getFavouritePortalPages and fake pagination ourselves.
            Collection<PortalPage> allFavourites = portalPageService.getFavouritePortalPages(jiraAuthenticationContext.getLoggedInUser());

            total = allFavourites.size();
            results = startAt > total ? Collections.<PortalPage>emptyList() : Lists.newArrayList(allFavourites).subList(startAt, Math.min(total, startAt + maxResults));
        }
        else
        {
            // PortalPageService.search() doesn't support passing pageWidth=0 unfortunately, so we need to hack around
            // that by requesting pageWidth=1 and then discarding the returned dashboard.
            SharedEntitySearchResult<PortalPage> searchResults = portalPageService.search(makeContext(), searchParams, toPageNumber(startAt, maxResults), maxResults == 0 ? 1 : maxResults);

            total = searchResults.getTotalResultCount();
            results = maxResults == 0 ? Collections.<PortalPage>emptyList() : searchResults.getResults();
        }

        return makeResponse(startAt, maxResults, total, results);
    }

    private DashboardsBean addPreviousNextLinks(String filter, int startAt, int maxResults, DashboardsBean dashboards)
    {
        UriBuilder builder = uriInfo.getBaseUriBuilder().path(DashboardResource.class).replaceQueryParam("maxResults", maxResults);
        if (filter != null)
        {
            builder = builder.queryParam("filter", filter);
        }

        if (startAt > 0)
        {
            dashboards = dashboards.prev(builder.replaceQueryParam("startAt", startAt - maxResults).build().toString());
        }

        if (maxResults != 0 && (startAt + dashboards.dashboards().size()) < dashboards.total())
        {
            dashboards = dashboards.next(builder.replaceQueryParam("startAt", startAt + maxResults).build().toString());
        }

        return dashboards;
    }

    private DashboardsBean makeResponse(int startAt, int maxResults, int total, Iterable<PortalPage> portalPages)
    {
        List<DashboardBean> dashboards = Lists.newArrayList();
        for (PortalPage page : portalPages)
        {
            dashboards.add(buildDashboardBean(page));
        }

        return new DashboardsBean().startAt(startAt)
                .maxResults(maxResults)
                .total(total)
                .dashboards(dashboards);
    }

    private DashboardBean buildDashboardBean(PortalPage portalPage)
    {
        return new DashboardBean(String.valueOf(portalPage.getId()), portalPage.getName(), selfLinkFor(portalPage), viewLinkFor(portalPage));
    }

    private String selfLinkFor(PortalPage page)
    {
        // e.g. http://localhost:8090/jira/rest/api/2/dashboard/10019
        return String.format("%s%s/%s", jiraBaseUrls.restApi2BaseUrl(), "dashboard", page.getId());
    }

    private String viewLinkFor(PortalPage page)
    {
        // e.g. http://localhost:8090/jira/secure/Dashboard.jspa?selectPageId=10019
        return String.format("%s/secure/Dashboard.jspa?selectPageId=%d", jiraBaseUrls.baseUrl(), page.getId());
    }

    private SharedEntitySearchParametersBuilder searchParams()
    {
        return new SharedEntitySearchParametersBuilder();
    }

    private JiraServiceContextImpl makeContext()
    {
        return new JiraServiceContextImpl(jiraAuthenticationContext.getLoggedInUser());
    }

    private int toPageNumber(int startAt, int maxResults)
    {
        return maxResults != 0 ? startAt / maxResults : 1;
    }

    private I18nHelper i18n()
    {
        return i18nFactory.getInstance(jiraAuthenticationContext.getLoggedInUser());
    }
}
