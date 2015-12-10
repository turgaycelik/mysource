package com.atlassian.jira.dev.backdoor.noalert;

import com.atlassian.core.filters.AbstractHttpFilter;
import com.atlassian.plugin.webresource.WebResourceManager;

import java.io.IOException;
import javax.servlet.FilterChain;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * Whether NoAlert mode is on.
 *
 * @since v6.0
 */
public class NoAlertFilter extends AbstractHttpFilter
{
    private final NoAlertMode noAlerts;
    private final WebResourceManager webResourceManager;

    public NoAlertFilter(final NoAlertMode noAlerts, final WebResourceManager webResourceManager)
    {
        this.noAlerts = noAlerts;
        this.webResourceManager = webResourceManager;
    }

    @Override
    protected void doFilter(final HttpServletRequest request, final HttpServletResponse response, final FilterChain filterChain)
            throws IOException, ServletException
    {
        if (noAlerts.isOn())
        {
            webResourceManager.requireResource("com.atlassian.jira.dev.func-test-plugin:noalert-js");
        }

        filterChain.doFilter(request, response);
    }
}
