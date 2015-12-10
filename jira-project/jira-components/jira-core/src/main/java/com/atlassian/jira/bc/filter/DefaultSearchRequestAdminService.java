package com.atlassian.jira.bc.filter;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.favourites.FavouritesManager;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.issue.search.SearchRequestAdminManager;
import com.atlassian.jira.util.Resolver;
import com.atlassian.jira.util.collect.EnclosedIterable;
import com.atlassian.jira.util.collect.Transformed;
import com.atlassian.jira.web.action.util.SimpleSearchRequestDisplay;

import java.util.Collection;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

public class DefaultSearchRequestAdminService implements SearchRequestAdminService
{
    private final SearchRequestAdminManager searchRequestAdminManager;
    private final FavouritesManager<SearchRequest> favouritesManager;

    public DefaultSearchRequestAdminService(final SearchRequestAdminManager searchRequestAdminManager, final FavouritesManager<SearchRequest> favouritesManager)
    {
        this.searchRequestAdminManager = notNull("searchRequestAdminManager", searchRequestAdminManager);
        this.favouritesManager = notNull("favouritesManager", favouritesManager);
    }

    public Collection<SimpleSearchRequestDisplay> getFiltersSharedWithGroup(final Group group)
    {
        notNull("group", group);
        final Resolver<SearchRequest, SimpleSearchRequestDisplay> searchRequestToDisplayObject = new Resolver<SearchRequest, SimpleSearchRequestDisplay>()
        {
            public SimpleSearchRequestDisplay get(final SearchRequest input)
            {
                return new SimpleSearchRequestDisplay(input);
            }
        };
        final EnclosedIterable<SearchRequest> searchRequests = searchRequestAdminManager.getSearchRequests(group);
        final List<SimpleSearchRequestDisplay> displayObjects = new EnclosedIterable.ListResolver<SimpleSearchRequestDisplay>().get(Transformed.enclosedIterable(
            searchRequests, searchRequestToDisplayObject));
        return Collections.unmodifiableCollection(displayObjects);
    }

    private void deleteFilter(final SearchRequest request)
    {
        notNull("request", request);

        // TODO this should all be the responsibility of the MANAGER - copied from the SearchRequestService
        favouritesManager.removeFavouritesForEntityDelete(request);
        searchRequestAdminManager.delete(request.getId());
    }
}
