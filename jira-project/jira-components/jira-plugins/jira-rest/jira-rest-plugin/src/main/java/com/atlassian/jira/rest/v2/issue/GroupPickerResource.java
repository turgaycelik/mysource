package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.rest.v2.issue.groups.GroupPickerResourceHelper;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.List;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * REST endpoint for searching groups in a group picker
 *
 * @since v4.4
 */
@Path ("groups")
@AnonymousAllowed
@Produces ( { MediaType.APPLICATION_JSON })
public class GroupPickerResource
{
    private GroupPickerResourceHelper groupPickerHelper;

    @SuppressWarnings ({ "UnusedDeclaration" })
    private GroupPickerResource()
    {
    }

    @SuppressWarnings ({ "UnusedDeclaration" })
    public GroupPickerResource(GroupPickerResourceHelper groupPickerHelper)
    {
        this.groupPickerHelper = groupPickerHelper;
    }

    /**
     * Returns groups with substrings matching a given query. This is mainly for use with
     * the group picker, so the returned groups contain html to be used as picker suggestions.
     * The groups are also wrapped in a single response object that also contains a header for
     * use in the picker, specifically <i>Showing X of Y matching groups</i>.
     *
     * The number of groups returned is limited by the system property "jira.ajax.autocomplete.limit"
     *
     * The groups will be unique and sorted.
     *
     * @param query a String to match groups agains
     * @return a collection of matching groups
     *
     * @response.representation.200.qname
     *      groupsuggestions
     *
     * @response.representation.200.mediaType
     *      application/json
     *
     * @response.representation.200.doc
     *      Returned even if no groups match the given substring
     * @response.representation.200.example
     *      {@link GroupSuggestionsBean#DOC_EXAMPLE}
     */
    @Path("/picker")
    @GET
    public Response findGroups(@QueryParam("query") final String query, @QueryParam("exclude") List<String> excludeGroups,
            @QueryParam("maxResults") Integer maxResults)
    {
        final GroupSuggestionsBean suggestions = groupPickerHelper.findGroupsAsBean(query, excludeGroups, maxResults);
        return Response.ok(suggestions)
            .cacheControl(never())
            .build();
    }

}
