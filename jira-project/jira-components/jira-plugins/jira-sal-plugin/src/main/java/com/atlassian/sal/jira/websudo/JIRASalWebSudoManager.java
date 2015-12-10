package com.atlassian.sal.jira.websudo;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.oauth.util.RequestAnnotations;
import com.atlassian.sal.api.websudo.WebSudoManager;
import com.atlassian.sal.api.websudo.WebSudoSessionException;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.net.URLEncoder;

import static com.atlassian.jira.util.dbc.Assertions.notNull;


public class JIRASalWebSudoManager implements WebSudoManager
{
    private final InternalWebSudoManager internalWebSudoManager;
    private final ApplicationProperties applicationProperties;

    public JIRASalWebSudoManager(final ApplicationProperties settingsManager, final InternalWebSudoManager internalWebSudoManager)
    {
        this.internalWebSudoManager = notNull("internalWebSudoManager", internalWebSudoManager);
        this.applicationProperties = notNull("applicationProperties", settingsManager);
    }

    /**
     * {@inheritDoc}
     */
    public boolean canExecuteRequest(final HttpServletRequest httpServletRequest)
    {
        final HttpSession session = httpServletRequest.getSession(false);

        return !internalWebSudoManager.isEnabled() ||
               RequestAnnotations.isOAuthRequest(httpServletRequest) ||
               internalWebSudoManager.hasValidSession(session);
    }

    /**
     * {@inheritDoc}
     */
    public void enforceWebSudoProtection(final HttpServletRequest request, final HttpServletResponse response)
    {
        final String encoding = applicationProperties.getEncoding();
        try
        {
            final String queryString = request.getQueryString();
            final String pathInfo = request.getPathInfo();
            final String destination = request.getServletPath() +
                ((null != pathInfo) ? pathInfo : "") +
                ((null != queryString) ? "?" + queryString : "");

            response.sendRedirect(request.getContextPath() + "/secure/admin/WebSudoAuthenticate!default.jspa?webSudoDestination=" + URLEncoder.encode(destination, encoding));
        } catch (IOException e)
        {
            throw new IllegalStateException("Failed to redirect to /authenticate.action");
        }
    }

    /**
     * {@inheritDoc}
     */
    public void willExecuteWebSudoRequest(final HttpServletRequest httpServletRequest) throws WebSudoSessionException
    {
        if (!canExecuteRequest(httpServletRequest))
        {
            throw new WebSudoSessionException("Invalid request: Not in a WebSudo session");
        }
        internalWebSudoManager.markWebSudoRequest(httpServletRequest);
    }
}
