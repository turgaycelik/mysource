package com.atlassian.jira.web.action.filter;

import com.atlassian.jira.bc.filter.SearchRequestService;
import com.atlassian.jira.util.json.JSONEscaper;
import com.atlassian.jira.web.action.JiraWebActionSupport;

import java.util.Collection;

/**
 * Action implementation for the Favourite Filters popup.
 *
 * @since v3.13
 */
public class FavouriteFilters extends JiraWebActionSupport
{
    private Collection /*<SearchRequest>*/favouriteFilters = null;

    private final SearchRequestService searchRequestService;
    private boolean json;

    public boolean isJson()
    {
        return json;
    }

    public void setJson(final boolean json)
    {
        this.json = json;
    }

    public FavouriteFilters(final SearchRequestService searchRequestService)
    {
        this.searchRequestService = searchRequestService;
    }

    public Collection /*<SearchRequest>*/getFavouriteFilters()
    {
        if (favouriteFilters == null)
        {
            favouriteFilters = searchRequestService.getFavouriteFilters(getLoggedInApplicationUser());
        }
        return favouriteFilters;

    }

    /**
     * Encodes the String from a JSON point of view
     *
     * @param jsonStr a String to JSON escape
     * @return an escaped string
     */
    public String jsonEscape(final String jsonStr)
    {
        return JSONEscaper.escape(jsonStr);
    }

}
