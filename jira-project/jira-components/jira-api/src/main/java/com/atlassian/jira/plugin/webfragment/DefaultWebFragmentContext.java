package com.atlassian.jira.plugin.webfragment;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.plugin.webfragment.model.JiraHelper;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.ExecutingHttpRequest;
import com.google.common.collect.ImmutableMap;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import java.util.Map;

/**
 * Default JIRA context for webfragments.
 *
 * @since v5.1
 */
public class DefaultWebFragmentContext
{

    public static Map<String,Object> get(@Nullable String location)
    {
        final HttpServletRequest currentRequest = ExecutingHttpRequest.get();
        final User currentUser = ComponentAccessor.getComponent(JiraAuthenticationContext.class).getLoggedInUser();
        final ImmutableMap.Builder<String,Object> builder = ImmutableMap.builder();
        if (currentRequest != null)
        {
            builder.put("request", currentRequest).put(JiraWebInterfaceManager.CONTEXT_KEY_HELPER, new JiraHelper(currentRequest));
        }
        if (currentUser != null)
        {
            builder.put(JiraWebInterfaceManager.CONTEXT_KEY_USER, currentUser)
                    .put(JiraWebInterfaceManager.CONTEXT_KEY_USERNAME, currentUser.getName());
        }
        if (location != null)
        {
            builder.put(JiraWebInterfaceManager.CONTEXT_KEY_LOCATION, location);
        }
        return builder.build();
    }

    public static Map<String,Object> get()
    {
        return get(null);
    }
}
