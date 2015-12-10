package com.atlassian.jira.portal;

import com.atlassian.configurable.ValuesGenerator;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.search.SearchRequest;
import com.atlassian.jira.user.ApplicationUsers;
import org.apache.commons.collections.map.ListOrderedMap;
import org.apache.log4j.Logger;

import java.util.Collection;
import java.util.Map;

/**
 * A ValuesGenerator for creating a list of SearchRequests (a.k.a Filters) with IDs and names.
 * The filterpicker Configurable Objects type should be used instead of a select and this ValuesGenerator since the
 * number of filters returned could be too large for a select control.
 */
public class SearchRequestValuesGenerator implements ValuesGenerator
{
    private static final Logger log = Logger.getLogger(SearchRequestValuesGenerator.class);

    /**
     * Returns a map of the filter id to the filter name, requiring only the "User" parameter in the params map.
     */
    public Map getValues(Map params)
    {
        Map savedFilters = null;
        User u = (User) params.get("User");

        Collection<SearchRequest> savedFiltersList = ComponentAccessor.getComponent(SearchRequestService.class).getFavouriteFilters(ApplicationUsers.from(u));
        savedFilters = new ListOrderedMap();

        for (final SearchRequest request : savedFiltersList)
        {
            savedFilters.put(request.getId().toString(), request.getName());
        }


        return savedFilters;
    }
}
