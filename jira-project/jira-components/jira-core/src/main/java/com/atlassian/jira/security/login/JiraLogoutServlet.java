package com.atlassian.jira.security.login;

import com.atlassian.jira.ComponentManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.xsrf.XsrfCheckResult;
import com.atlassian.jira.security.xsrf.XsrfInvocationChecker;
import com.atlassian.seraph.logout.LogoutServlet;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Responsible for processing log-out requests. <br /> It performs an XSRF check and delegates to the underlying Seraph
 * log-out servlet.
 *
 * @see com.atlassian.seraph.logout.LogoutServlet
 * @since v4.1.1
 */
public class JiraLogoutServlet extends HttpServlet
{
    static final String ALREADY_LOGGED_OUT_PAGE = "/alreadyloggedout.jsp";
    static final String LOG_OUT_CONFIRM_PAGE = "/logoutconfirm.jsp";

    private final HttpServlet seraphLogoutServlet = new LogoutServlet();

    @Override
    public void init() throws ServletException
    {
        getSeraphLogoutServlet().init();
    }

    @Override
    public void init(final ServletConfig servletConfig) throws ServletException
    {
        getSeraphLogoutServlet().init(servletConfig);
    }

    /**
     * <p>Processes a request to log-out a User. If there's a valid XSRF token, it delegates to the underlying Seraph
     * log-out servlet.</p>
     *
     * <p>Otherwise, it is possible that the XSRF token has expired (due to a session timeout), or that this is a dodgy
     * token.</p>
     *
     * <p>When the session times out, there are users who have remember me or Crowd SSO turned on and they will be
     * authenticated. We need to confirm that they are who they say they are in order to perform a log-out,
     * this is because we need to perform clean-up operations for them (e.g removing the remember me cookie, we don't
     * want attacker to trick you into deleting your own remember me cookie).</p>
     *
     * <p>On the other hand, if there's no authenticated user and the session is gone, we tell the user that he's
     * already logged out.<p>
     *
     * @param request The request in play.
     * @param response The response in play.
     * @throws ServletException
     * @throws IOException
     */
    @Override
    protected void service(final HttpServletRequest request, final HttpServletResponse response)
            throws ServletException, IOException
    {
        XsrfInvocationChecker invocationChecker = getXsrfInvocationChecker();
        final XsrfCheckResult result = invocationChecker.checkWebRequestInvocation(request);
        if (result.isValid())
        {
            XsrfTokenAppendingResponse wrappedResponse = createXsrfTokenAppendingResponse(request, response);
            getSeraphLogoutServlet().service(request, wrappedResponse);
        }
        else if (isUserAuthenticated())
        {
            response.sendRedirect(request.getContextPath() + LOG_OUT_CONFIRM_PAGE);
        }
        else
        {
            response.sendRedirect(request.getContextPath() + ALREADY_LOGGED_OUT_PAGE);
        }
    }

    @Override
    public ServletConfig getServletConfig()
    {
        return getSeraphLogoutServlet().getServletConfig();
    }

    @Override
    public void destroy()
    {
        super.destroy();
    }

    /**
     * Determines whether the user in this request has been authenticated.
     * @return true if the user in this request has been authenticated; otherwise, false.
     */
    boolean isUserAuthenticated()
    {
        return getAuthenticationContext().getLoggedInUser() != null;
    }

    JiraAuthenticationContext getAuthenticationContext()
    {
        return ComponentAccessor.getComponentOfType(JiraAuthenticationContext.class);
    }

    XsrfTokenAppendingResponse createXsrfTokenAppendingResponse(final HttpServletRequest request, final HttpServletResponse response)
    {
        return new XsrfTokenAppendingResponse(request, response);
    }

    XsrfInvocationChecker getXsrfInvocationChecker()
    {
        return ComponentAccessor.getComponentOfType(XsrfInvocationChecker.class);
    }

    HttpServlet getSeraphLogoutServlet()
    {
        return seraphLogoutServlet;
    }
}