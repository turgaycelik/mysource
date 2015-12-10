package com.atlassian.jira.event.web.action.admin;

import com.google.common.collect.ImmutableMap;

import java.util.Map;

/**
 * Denotes that an user has been added by the submission of the add user form.
 * <p/>
 * The http request parameters are made available by {@link #getRequestParameters()}. It allows to add some behavior on
 * form submission. This event is fired only if the user is successfully added.
 *
 * @since v5.1
 */
public final class UserAddedEvent
{
    private final Map<String, String[]> requestParameters;

    public UserAddedEvent(Map<String, String[]> requestParameters)
    {
        this.requestParameters = ImmutableMap.copyOf(requestParameters);
    }

    public Map<String, String[]> getRequestParameters()
    {
        return requestParameters;
    }
}
