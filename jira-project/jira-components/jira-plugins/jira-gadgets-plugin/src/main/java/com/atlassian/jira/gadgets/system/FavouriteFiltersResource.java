package com.atlassian.jira.gadgets.system;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.issue.index.SearchUnavailableException;
import com.atlassian.jira.issue.search.SearchException;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.log4j.Logger;

import javax.ws.rs.DefaultValue;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;

import static com.atlassian.jira.rest.v1.util.CacheControl.NO_CACHE;
import static com.atlassian.jira.user.util.Users.isAnonymous;

/**
 * REST endpoint to retrieve the users favourite filters.
 *
 * @since v4.0
 */
@Path ("/favfilters")
@AnonymousAllowed
@Produces ({ MediaType.APPLICATION_JSON })
public class FavouriteFiltersResource extends AbstractResource
{
    private static final Logger log = Logger.getLogger(FavouriteFiltersResource.class);

    private JiraAuthenticationContext authenticationContext;
    private SearchRequestService searchRequestService;
    private SearchProvider searchProvider;
    private PermissionManager permissionManager;
    private VelocityRequestContextFactory velocityRequestContextFactory;

    public FavouriteFiltersResource(final JiraAuthenticationContext authenticationContext, final SearchRequestService searchRequestService,
            final SearchProvider searchProvider, PermissionManager permissionManager, VelocityRequestContextFactory velocityRequestContextFactory)
    {
        this.authenticationContext = authenticationContext;
        this.searchRequestService = searchRequestService;
        this.searchProvider = searchProvider;
        this.permissionManager = permissionManager;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
    }

    @GET
    public Response getFavouriteFilters(@DefaultValue ("false") @QueryParam ("showCounts") boolean includeCount)
    {
        final ApplicationUser user = authenticationContext.getUser();

        if (isAnonymous(user))
        {
            return Response.ok(new NotLoggedIn()).cacheControl(NO_CACHE).build();
        }
        final Collection<SearchRequest> searchRequests = searchRequestService.getFavouriteFilters(user);

        final Collection<Filter> filters = new ArrayList<Filter>(searchRequests.size());

        try
        {
            for (SearchRequest searchRequest : searchRequests)
            {
                if (includeCount)
                {
                    final long count;
                    try
                    {
                        count = searchProvider.searchCount(searchRequest.getQuery(), user);
                        filters.add(new Filter(searchRequest.getName(), searchRequest.getDescription(), searchRequest.getId(), count));
                    }
                    catch (SearchException e)
                    {
                        log.warn("Error while running search for filter count:", e);
                        filters.add(new Filter(searchRequest.getName(), searchRequest.getDescription(), searchRequest.getId()));
                    }
                }
                else
                {
                    filters.add(new Filter(searchRequest.getName(), searchRequest.getDescription(), searchRequest.getId()));
                }
            }

            return Response.ok(new FilterList(filters, includeCount)).cacheControl(NO_CACHE).build();
        }
        catch (SearchUnavailableException e)
        {
            if (!e.isIndexingEnabled())
            {
                return createIndexingUnavailableResponse(createIndexingUnavailableMessage());
            }
            else
            {
                throw e;
            }
        }
    }

    private String createIndexingUnavailableMessage()
    {
        final String msg1 = authenticationContext.getI18nHelper().getText("gadget.common.indexing");
        String msg2;
        if (permissionManager.hasPermission(Permissions.ADMINISTER, authenticationContext.getUser()))
        {
            String baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getBaseUrl();
            msg2 = authenticationContext.getI18nHelper().getText("gadget.common.indexing.configure",
                    "<a href=\"" + baseUrl + "/secure/admin/jira/IndexAdmin.jspa\">", "</a>");
        }
        else
        {
            msg2 = authenticationContext.getI18nHelper().getText("gadget.common.indexing.admin");
        }
        return msg1 + " " + msg2;
    }

    ///CLOVER:OFF
    @XmlRootElement
    public static class NotLoggedIn
    {
        @XmlElement
        private boolean notLoggedIn = false;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private NotLoggedIn()
        {}

        public boolean isNotLoggedIn()
        {
            return notLoggedIn;
        }
    }

    @XmlRootElement
    public static class FilterList
    {
        @XmlElement
        private Collection<Filter> filters;
        @XmlElement
        private boolean includeCount;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private FilterList()
        {}

        FilterList(Collection<Filter> filters, boolean includeCount)
        {
            this.filters = filters;
            this.includeCount = includeCount;
        }

        public Collection<Filter> getFilters()
        {
            return filters;
        }

        public boolean isIncludeCount()
        {
            return includeCount;
        }
    }

    @XmlRootElement
    public static class Filter
    {
        @XmlElement
        private String label;
        @XmlElement
        private String description;
        @XmlElement
        private Long value;
        @XmlElement
        private Long count;

        @SuppressWarnings ({ "UnusedDeclaration", "unused" })
        private Filter()
        {}

        Filter(String label, String description, Long value)
        {
            this.label = label;
            this.description = description;
            this.value = value;
        }

        Filter(String label, String description, Long value, Long count)
        {
            this.label = label;
            this.description = description;
            this.value = value;
            this.count = count;
        }

        public String getLabel()
        {
            return label;
        }

        public String getDescription()
        {
            return description;
        }

        public Long getValue()
        {
            return value;
        }

        public Long getCount()
        {
            return count;
        }
    }
///CLOVER:ON
}
