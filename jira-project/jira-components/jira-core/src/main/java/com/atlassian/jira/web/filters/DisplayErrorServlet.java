package com.atlassian.jira.web.filters;

import com.atlassian.jira.bc.security.login.DeniedReason;
import com.atlassian.jira.bc.security.login.LoginResult;
import com.atlassian.jira.bc.security.login.LoginService;
import com.atlassian.jira.web.dispatcher.JiraWebworkActionDispatcher;

import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;

/**
 * Generic error servlet. Displays prettier errors than the default Tomcat ones.
 *
 * @since v5.0
 */
public class DisplayErrorServlet extends JiraWebworkActionDispatcher
{
    private static final String STATUS_CODE = "javax.servlet.error.status_code";

    private ServletConfig config;

    @Override
    public void service(ServletRequest req, ServletResponse res) throws ServletException, IOException
    {
        HttpServletResponse response = (HttpServletResponse) res;

        Integer code = (Integer) req.getAttribute(STATUS_CODE);
        if (code == null)
        {
            // if there is no code this means the user is trying to access this servlet directly. just treat it as a 404
            code = 404;
        }
        else if (code == 401)
        {
            if (req.getAttribute(LoginService.LOGIN_RESULT) instanceof LoginResult)
            {
                // special handling for CAPTCHA authentication failure
                LoginResult result = (LoginResult) req.getAttribute(LoginService.LOGIN_RESULT);
                for (DeniedReason reason : result.getDeniedReasons())
                {
                    code = 403;
                    response.setHeader(DeniedReason.X_DENIED_HEADER, reason.asString());
                }
            }
        }

        // set the real code and display the generic error page
        response.setStatus(code);
        req.setAttribute(STATUS_CODE, code);
        req.getRequestDispatcher("/displayError.jsp").forward(req, res);
    }

    @Override
    public void init(ServletConfig config) throws ServletException
    {
        this.config = config;
    }

    @Override
    public ServletConfig getServletConfig()
    {
        return config;
    }

    @Override
    public String getServletInfo()
    {
        return getClass().getSimpleName();
    }

    @Override
    public void destroy()
    {
    }
}
