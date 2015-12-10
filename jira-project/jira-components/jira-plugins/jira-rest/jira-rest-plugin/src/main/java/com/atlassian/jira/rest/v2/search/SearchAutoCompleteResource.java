package com.atlassian.jira.rest.v2.search;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.RESTException;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.web.component.jql.AutoCompleteJsonGenerator;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;

import javax.ws.rs.Consumes;
import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.util.Locale;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * Resource for auto complete data for searches.
 *
 * @since v5.1
 */
@Path ("jql/autocompletedata")
@AnonymousAllowed
@Consumes ( { MediaType.APPLICATION_JSON })
@Produces ( { MediaType.APPLICATION_JSON })
public class SearchAutoCompleteResource
{
    public static final String DOC_EXAMPLE =
            "{" +
                "\"visibleFieldNames\": [{\"value\":\"affectedVersion\",\"displayName\":\"affectedVersion\",\"auto\":\"true\",\"orderable\":\"true\",\"searchable\":\"true\",\"operators\":[\"=\",\"!=\",\"in\",\"not in\",\"is\",\"is not\",\"<\",\"<=\",\">\",\">=\"],\"types\":[\"com.atlassian.jira.project.version.Version\"]},{\"value\":\"assignee\",\"displayName\":\"assignee\",\"auto\":\"true\",\"orderable\":\"true\",\"searchable\":\"true\",\"operators\":[\"!=\",\"was not in\",\"not in\",\"was not\",\"is\",\"was in\",\"was\",\"=\",\"in\",\"changed\",\"is not\"],\"types\":[\"com.atlassian.crowd.embedded.api.User\"]}]," +
                "\"visibleFunctionNames\": {\"value\":\"currentLogin()\",\"displayName\":\"currentLogin()\",\"types\":[\"java.util.Date\"]},{\"value\":\"currentUser()\",\"displayName\":\"currentUser()\",\"types\":[\"com.atlassian.crowd.embedded.api.User\"]}]," +
                "\"jqlReservedWords\": \"empty\",\"and\",\"or\",\"in\",\"distinct\"]" +
            "}";

    private final AutoCompleteJsonGenerator autoCompleteJsonGenerator;
    private final JiraAuthenticationContext authContext;
    private final I18nHelper i18n;

    public SearchAutoCompleteResource(final AutoCompleteJsonGenerator autoCompleteJsonGenerator,
            final JiraAuthenticationContext authContext, final I18nHelper i18n)
    {
        this.autoCompleteJsonGenerator = autoCompleteJsonGenerator;
        this.authContext = authContext;
        this.i18n = i18n;
    }

    /**
     * Returns the auto complete data required for JQL searches.
     *
     * @return the auto complete data required for JQL searches.
     *
     * @response.representation.200.mediaType application/json
     *
     * @response.representation.200.doc
     *      The auto complete data required for JQL searches.
     *
     * @response.representation.200.example
     *      {@link #DOC_EXAMPLE}
     *
     * @response.representation.401.doc
     *      Returned if the calling user is not authenticated.
     *
     * @response.representation.500.doc
     *      Returned if an error occurs while generating the response.
     */
    @GET
    public Response getAutoComplete()
    {
        final User user = authContext.getLoggedInUser();
        final Locale locale = authContext.getLocale();

        try
        {
            final String entity =
                    "{" +
                        "\"visibleFieldNames\": " + autoCompleteJsonGenerator.getVisibleFieldNamesJson(user, locale) + "," +
                        "\"visibleFunctionNames\": " + autoCompleteJsonGenerator.getVisibleFunctionNamesJson(user, locale) + "," +
                        "\"jqlReservedWords\": " + autoCompleteJsonGenerator.getJqlReservedWordsJson() +
                    "}";

            return Response.ok(entity).cacheControl(never()).build();

        }
        catch (JSONException e)
        {
            throw new RESTException(ErrorCollection.of(i18n.getText("rest.error.generating.response"))
                    .reason(com.atlassian.jira.util.ErrorCollection.Reason.SERVER_ERROR));
        }
    }
}
