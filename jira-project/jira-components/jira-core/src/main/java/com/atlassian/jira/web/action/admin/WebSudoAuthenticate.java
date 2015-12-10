package com.atlassian.jira.web.action.admin;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.project.ProjectAction;
import com.atlassian.jira.bc.project.ProjectService;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.login.LoginManager;
import com.atlassian.jira.security.websudo.InternalWebSudoManager;
import com.atlassian.jira.util.UrlBuilder;
import com.atlassian.jira.util.velocity.VelocityRequestContext;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.jira.util.velocity.VelocityRequestSession;
import com.atlassian.jira.web.SessionKeys;
import com.atlassian.jira.web.action.JiraWebActionSupport;
import com.atlassian.seraph.util.RedirectUtils;
import org.apache.commons.lang.StringUtils;
import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Arrays;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class WebSudoAuthenticate extends JiraWebActionSupport
{
    private InternalWebSudoManager webSudoManager;
    private final JiraAuthenticationContext authenticationContext;
    private final VelocityRequestContextFactory contextFactory;
    private final ProjectService projectService;
    private final ApplicationProperties applicationProperties;
    private String password;
    private String destination;
    private LoginManager loginManager;
    private Map<String, List<String>> allParams = new HashMap<String, List<String>>();
    private boolean isPost;
    private boolean close;

    public WebSudoAuthenticate(LoginManager loginManager, InternalWebSudoManager webSudoManager,
            JiraAuthenticationContext authenticationContext, VelocityRequestContextFactory contextFactory, ProjectService projectService,
            ApplicationProperties applicationProperties)
    {
        this.loginManager = loginManager;
        this.webSudoManager = webSudoManager;
        this.authenticationContext = authenticationContext;
        this.contextFactory = contextFactory;
        this.projectService = projectService;
        this.applicationProperties = applicationProperties;
    }

    public String doDefault() throws Exception
    {
        final User loggedInUser = getLoggedInUser();
        HttpServletRequest request = ActionContext.getRequest();
        if (loggedInUser == null)
        {
            final String loginUrl = RedirectUtils.getLoginUrl(request);
            //seraph returns a URL including context path, so we need to strip it here!
            return getRedirect(stripContextPath(loginUrl, request));
        }

        password = null;
        this.setWebSudoIsPost(request.getMethod().equals("POST"));

        allParams.put("webSudoIsPost", Arrays.asList(Boolean.toString(this.getWebSudoIsPost())));

        for (Enumeration enumeration = request.getParameterNames(); enumeration.hasMoreElements();)
        {
            String name = (String) enumeration.nextElement();
            String[] values = request.getParameterValues(name);
            allParams.put(name, Arrays.asList(values));
        }

        return super.doDefault();
    }

    private String stripContextPath(String loginUrl, HttpServletRequest request)
    {
        final String contextPath = StringUtils.trimToEmpty(request.getContextPath());
        final int index = loginUrl.indexOf(contextPath);
        if (index == 0)
        {
            loginUrl = loginUrl.substring(index + contextPath.length());
        }
        return loginUrl;
    }

    protected String doExecute() throws Exception
    {
        webSudoManager.startSession(ServletActionContext.getRequest(), ServletActionContext.getResponse());
        if (getWebSudoIsPost())
        {
            for (Enumeration enumeration = request.getParameterNames(); enumeration.hasMoreElements();)
            {
                String name = (String) enumeration.nextElement();
                if (!(name.toLowerCase().startsWith("websudo")))
                {
                    String[] values = request.getParameterValues(name);
                    allParams.put(name, Arrays.asList(values));
                }
            }

            return "repostform";
        }
        else
        {
            // Make sure we don't accidentally redirect to a return URL that just happens to be in the parameter list.
            setReturnUrl(null);

            if (isClose()) {
                return returnComplete(getWebSudoDestination());
            } else {
                return getRedirect(getWebSudoDestination());
            }
        }
    }

    public void doValidation()
    {
        if (StringUtils.isBlank(password))
        {
            addError("webSudoPassword", getText("websudo.password.empty"));
            copyAllParams();
            return;
        }

        if (!loginManager.authenticateWithoutElevatedCheck(getLoggedInUser(), password).isOK())
        {
            addError("webSudoPassword", getText("websudo.password.wrong"));
            copyAllParams();
        }
    }

    public String getCancelUrl()
    {
        final VelocityRequestContext requestContext = contextFactory.getJiraVelocityRequestContext();
        final VelocityRequestSession session = requestContext.getSession();
        final String projectKey = (String) session.getAttribute(SessionKeys.CURRENT_ADMIN_PROJECT);
        final String baseUrl = requestContext.getBaseUrl();

        StringBuilder url = new StringBuilder(baseUrl);

        if (StringUtils.isNotBlank(projectKey))
        {
            final ProjectService.GetProjectResult projectResult = projectService.getProjectByKeyForAction(authenticationContext.getUser(), projectKey, ProjectAction.EDIT_PROJECT_CONFIG);

            if (projectResult.isValid())
            {
                url.append("/plugins/servlet/project-config");

                final Project project = projectResult.getProject();

                url.append('/').append(project.getKey());

                final String tab = (String) session.getAttribute(SessionKeys.CURRENT_ADMIN_PROJECT_TAB);

                if (StringUtils.isNotBlank(tab))
                {
                    url.append('/').append(tab);
                }
                else
                {
                    url.append("/summary");
                }
            }
            else
            {
                url.append("/secure/project/ViewProjects.jspa");
            }
        }
        else
        {
            url.append("/secure/project/ViewProjects.jspa");
        }

        return new UrlBuilder(url.toString(), applicationProperties.getEncoding(), false)
                .asUrlString();
    }

    private void copyAllParams()
    {
        for (Enumeration enumeration = request.getParameterNames(); enumeration.hasMoreElements();)
        {
            String name = (String) enumeration.nextElement();
            String[] values = request.getParameterValues(name);
            allParams.put(name, Arrays.asList(values));
        }

    }

    public String getUsername()
    {
        return this.getLoggedInUser().getName();
    }

    public String getWebSudoPassword()
    {
        return password;
    }

    public void setWebSudoPassword(String password)
    {
        this.password = password;
    }

    public String getWebSudoDestination()
    {
        if (StringUtils.isNotBlank(destination))
        {
            return destination;
        }
        else
        {
            return "/secure/project/ViewProjects.jspa";
        }
    }

    public void setWebSudoDestination(String destination)
    {
        this.destination = getRedirectSanitiser().makeSafeRedirectUrl(destination);
    }

    public Map<String, List<String>> getRequestParameters()
    {
        return allParams;
    }

    public boolean getWebSudoIsPost()
    {
        return isPost;
    }

    public void setWebSudoIsPost(boolean post)
    {
        isPost = post;
    }

    public boolean isClose()
    {
        return close;
    }

    public void setClose(boolean close)
    {
        this.close = close;
    }
}
