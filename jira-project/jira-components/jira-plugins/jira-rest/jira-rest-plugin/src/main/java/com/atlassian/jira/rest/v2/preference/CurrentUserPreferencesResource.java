package com.atlassian.jira.rest.v2.preference;

import com.atlassian.core.AtlassianCoreException;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.user.preferences.ExtendedPreferences;
import com.atlassian.jira.user.preferences.UserPreferencesManager;
import com.atlassian.jira.util.SimpleErrorCollection;

import javax.ws.rs.Consumes;
import javax.ws.rs.DELETE;
import javax.ws.rs.GET;
import javax.ws.rs.PUT;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.QueryParam;
import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;

import static com.atlassian.jira.rest.api.http.CacheControl.never;


/**
 * Provide preferences of the currently logged in user.
 *
 * @since v6.0
 */
@Path ("mypreferences")
@Consumes (MediaType.APPLICATION_JSON)
@Produces (MediaType.APPLICATION_JSON)
public class CurrentUserPreferencesResource
{
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final UserPreferencesManager userPreferencesManager;


    public CurrentUserPreferencesResource(JiraAuthenticationContext jiraAuthenticationContext, UserPreferencesManager userPreferencesManager)
    {
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.userPreferencesManager = userPreferencesManager;
    }

    /**
     * Returns preference of the currently logged in user. Preference key must be provided as input parameter (key). The
     * value is returned exactly as it is. If key parameter is not provided or wrong - status code 404. If value is
     * found  - status code 200.
     *
     * @param key - key of the preference to be returned.
     * @return response with value of one preference of currently logged in user.
     * @since v6.0
     */
    @GET
    public Response getPreference(@QueryParam ("key") final String key)
    {
        fieldValueMustBeProvided("key", key);

        final ExtendedPreferences preferences = userPreferencesManager.getExtendedPreferences(jiraAuthenticationContext.getUser());

        final String value = preferences.getString(key);
        if (value == null)
        {
            throwError("key not found: '" + key + "'");
        }

        return Response.ok(value).cacheControl(never()).build();
    }

    /**
     * Sets preference of the currently logged in user. Preference key must be provided as input parameters (key). Value
     * must be provided as post body. If key or value parameter is not provided - status code 404. If preference is set
     * - status code 204.
     *
     * @param key - key of the preference to be set.
     * @param value - value of the preference to be set.
     * @return empty response
     * @since v6.0
     */
    @PUT
    public Response setPreference(@QueryParam ("key") final String key, final String value)
            throws AtlassianCoreException
    {
        fieldValueMustBeProvided("key", key);
        fieldValueMustBeProvided("value", value);

        final ExtendedPreferences preferences = userPreferencesManager.getExtendedPreferences(jiraAuthenticationContext.getUser());

        preferences.setString(key, value);

        return Response.noContent().cacheControl(never()).build();
    }


    /**
     * Removes preference of the currently logged in user. Preference key must be provided as input parameters (key). If
     * key parameter is not provided or wrong - status code 404. If preference is unset - status code 204.
     *
     * @param key - key of the preference to be removed.
     * @return empty response
     * @since v6.0
     */
    @DELETE
    public Response removePreference(@QueryParam ("key") final String key) throws AtlassianCoreException
    {
        fieldValueMustBeProvided("key", key);

        final ExtendedPreferences preferences = userPreferencesManager.getExtendedPreferences(jiraAuthenticationContext.getUser());

        if (!preferences.containsValue(key))
        {
            throwError("key not found: '" + key + "'");
        }

        preferences.remove(key);

        return Response.noContent().cacheControl(never()).build();
    }


    private void fieldValueMustBeProvided(String fieldName, String fieldValue)
    {
        if (fieldValue == null || fieldValue.length() == 0)
        {
            throwError("input parameter '" + fieldName + "' must be provided");
        }
    }

    private void throwError(String message)
    {
        final SimpleErrorCollection errorCollection = new SimpleErrorCollection();
        errorCollection.addErrorMessage(message);
        throw new WebApplicationException(Response.status(Response.Status.NOT_FOUND).entity(ErrorCollection.of(errorCollection)).cacheControl(never()).build());
    }
}
