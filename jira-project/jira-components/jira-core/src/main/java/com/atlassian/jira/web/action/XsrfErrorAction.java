package com.atlassian.jira.web.action;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.web.util.HelpUtil;
import com.google.common.collect.ImmutableSortedSet;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.UnsupportedEncodingException;
import java.net.URLEncoder;
import java.util.Arrays;
import java.util.Comparator;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

import static com.atlassian.jira.user.util.Users.isAnonymous;
import static java.lang.Math.max;
import static org.apache.commons.lang.StringUtils.isNotBlank;

/**
 * This action is usually run via a servlet FORWARD. It will look at the request attributes under
 * <code>javax.servlet.forward</code> to figure out what the original request was.
 *
 * @since v4.1
 */
@SuppressWarnings ("UnusedDeclaration")
public class XsrfErrorAction extends JiraWebActionSupport
{
    /**
     * The path to use when forwarding to this action.
     */
    public static final String FORWARD_PATH = "/secure/" + XsrfErrorAction.class.getSimpleName() + ".jspa";
    private static final Logger log = Logger.getLogger(XsrfErrorAction.class);

    private final RedirectSanitiser safeRedirectChecker;
    private int timeoutSeconds;

    public XsrfErrorAction(RedirectSanitiser safeRedirectChecker)
    {
        this.safeRedirectChecker = safeRedirectChecker;
        final User user = getAuthContext().getLoggedInUser();
        if (user != null)
        {
            request.setAttribute("loggedin", Boolean.TRUE);
        }

        request.setAttribute("xsrfToken", getXsrfToken());
        timeoutSeconds = request.getSession(true).getMaxInactiveInterval();
        request.setAttribute("maxInactiveIntervalMinutes", request.getSession(true).getMaxInactiveInterval() / 60);


        request.setAttribute("contextpath", request.getContextPath());
        request.setAttribute("helpUtil", new HelpUtil());
        int parameterCount = getRequestParameters().size();
        log.info("The security token is missing for '" + (isAnonymous(user) ? "anonymous" : user.getName()) + "'. " + (parameterCount == 0 ? "The browser has provided ZERO parameters.  Probably BUG! " : "") + "User-Agent : '" + getBrowserAgent(request) + "'");
    }

    @Override
    public String execute() throws Exception
    {
        return isSessionExpired() ? "session_expired" : "xsrf_missing";
    }

    public boolean isHasRedirectUrl()
    {
        return getReturnUrl() != null || safeRedirectChecker.canRedirectTo(getReferer());
    }

    //Warning: do not put HtmlSave here, prossible problem with XSS in referer header in IE
    public String getEncodedRedirectUrl()
    {
        if (getReturnUrl() != null)
        {
            try
            {
                return URLEncoder.encode(insertContextPath(getReturnUrl()), "utf-8");
            }
            catch (UnsupportedEncodingException e)
            {
                throw new RuntimeException(e);
            }
        }
        
        if (safeRedirectChecker.canRedirectTo(getReferer()))
        {
            return getReferer();
        }

        return insertContextPath("/");
    }
    
    private String getReferer()
    {
        return request.getHeader("Referer");
    }
    
    public String getSessionTimeoutDuration()
    {
        // use either minutes or hours and just round to the nearest whole number
        return String.valueOf(Math.round(timeoutSeconds > 3600 ? timeoutSeconds / 3600.0 : max(1, timeoutSeconds / 60.0)));
    }

    public String getSessionTimeoutUnit()
    {
        boolean singular = "1".equals(getSessionTimeoutDuration());
        if (timeoutSeconds > 3600)
        {
            return singular ? getText("common.words.hour") : getText("common.words.hours");
        }

        return singular ? getText("common.words.minute") : getText("common.words.minutes");
    }

    private String getBrowserAgent(final HttpServletRequest request)
    {
        return StringUtils.defaultIfEmpty(request.getHeader("User-Agent"), "Not Provided");
    }

    private JiraAuthenticationContext getAuthContext()
    {
        return ComponentAccessor.getComponentOfType(JiraAuthenticationContext.class);
    }

    public boolean isSessionExpired()
    {
        String requestedSessionId = request.getRequestedSessionId();
        if (isNotBlank(requestedSessionId))
        {
            HttpSession session = request.getSession(false);
            if (session != null)
            {
                return !requestedSessionId.equals(session.getId());
            }
        }

        return false;
    }

    public String getRequestURL()
    {
        String forwardRequestURI = (String) request.getAttribute("javax.servlet.forward.request_uri");
        if (forwardRequestURI != null)
        {
            return forwardRequestURI;
        }

        return request.getRequestURI();
    }

    public String getRequestMethod()
    {
        return request.getMethod();
    }

    public boolean getNoRequestParameters()
    {
        return request.getParameterMap().isEmpty();
    }

    public Set<Map.Entry<String, List<String>>> getRequestParameters()
    {
        Map<String, List<String>> allParams = new HashMap<String, List<String>>();
        for (Enumeration enumeration = request.getParameterNames(); enumeration.hasMoreElements(); )
        {
            String name = (String) enumeration.nextElement();
            String[] values = request.getParameterValues(name);
            allParams.put(name, Arrays.asList(values));
        }

        return ImmutableSortedSet.copyOf(new BiggestValueFirstComparator(), allParams.entrySet());
    }

    private static class BiggestValueFirstComparator implements Comparator<Map.Entry<String, List<String>>>
    {
        public int compare(final Map.Entry<String, List<String>> entry1, final Map.Entry<String, List<String>> entry2)
        {
            int valLen1 = 0;
            int valLen2 = 0;
            if (entry1.getValue() != null)
            {
                for (String s : entry1.getValue())
                {
                    valLen1 += s != null ? s.length() : 0;
                }
            }
            if (entry2.getValue() != null)
            {
                for (String s : entry2.getValue())
                {
                    valLen2 += s != null ? s.length() : 0;
                }
            }
            if (valLen1 == valLen2)
            {
                // respect the keys in this case
                return entry1.getKey().compareTo(entry2.getKey());
            }
            // longest first
            return valLen2 - valLen1;
        }
    }
}
