package com.atlassian.jira.web.component.jql;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.util.json.JSONException;

import java.util.Locale;

/**
 * Used to generate JSON objects for the JQL autocomplete.
 *
 * @since v4.0
 */
public interface AutoCompleteJsonGenerator
{
    /**
     * Will return an array of JSON objects containing the field names that the user can see, listed in alphabetical order.
     *
     * The JSON object will contain:
     * value: value that will be autocompleted
     * displayName: the html escaped display name for the value
     * auto (optional) : if present indicates that the field can have values autocompleted
     * orderable (optional) : if present indicates that the field can participate in the order by clause
     * cfid (optional) : if present indicates that the field is a custom field that can be referenced by cf[xxxxx]
     *
     * @param user that the page is being rendered for.
     * @param locale the locale of the user.
     *
     * @return JSON string as described above.
     *
     * @throws JSONException if there is a problem generating the JSON object
     */
    String getVisibleFieldNamesJson(final User user, final Locale locale) throws JSONException;

    /**
     * Will return an array of JSON objects containing the functions names that are available in the system, listed in alphabetical order.
     *
     * The JSON object will contain:
     * value: value that will be autocompleted
     * displayName: the html escaped display name for the value
     * isList (optional) : true if the function generates a list of values, used to determine if it can work with the in operators.
     *
     * @param user that the page is being rendered for.
     * @param locale the locale of the user.
     *
     * @return JSON string as described above
     *
     * @throws JSONException if there is a problem generating the JSON object
     */
    String getVisibleFunctionNamesJson(final User user, final Locale locale) throws JSONException;

    /**
     * @return a JSON array that contains strings that are the JQL reserved words.
     *
     * @throws JSONException if there is a problem generating the JSON object
     */
    String getJqlReservedWordsJson() throws JSONException;
}
