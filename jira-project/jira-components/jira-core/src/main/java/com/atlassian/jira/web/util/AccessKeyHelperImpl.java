package com.atlassian.jira.web.util;

import com.atlassian.jira.util.BrowserUtils;
import com.atlassian.jira.util.UserAgentUtil;
import com.atlassian.jira.util.UserAgentUtilImpl;
import com.atlassian.jira.util.collect.CollectionBuilder;
import webwork.action.ActionContext;

import javax.servlet.http.HttpServletRequest;
import java.util.Set;

/**
 * @since v4.0
 */
public class AccessKeyHelperImpl implements AccessKeyHelper
{
    private static final Set<String> MSIE_RESERVED_KEYS;

    static
    {
        MSIE_RESERVED_KEYS = CollectionBuilder.newBuilder("d").asSet();
    }

    /*
     * So far there is only one clash we're concerned with: Alt+D in IE is "select the location bar", but Alt+D is also
     * the Dashboard Dropdown access key. Note that this only works under the assumption that the browser and JIRA's locale
     * are both English - access keys and browser shortcuts do change under different languages.
     */
    public boolean isAccessKeySafe(final String accessKey)
    {
        final UserAgentUtil.UserAgent userAgent = getUserAgent();
        if (userAgent != null)
        {
            if (userAgent.getBrowser().getBrowserFamily() == UserAgentUtil.BrowserFamily.MSIE)
            {
                if (MSIE_RESERVED_KEYS.contains(accessKey))
                {
                    return false;
                }
            }
        }

        return true;
    }

    UserAgentUtil.UserAgent getUserAgent()
    {
        final HttpServletRequest request = ActionContext.getRequest();
        if (request != null)
        {
            final String userAgent = request.getHeader(BrowserUtils.USER_AGENT_HEADER);
            final UserAgentUtil userAgentUtil = new UserAgentUtilImpl();
            return userAgentUtil.getUserAgentInfo(userAgent);
        }
        return null;
    }
}
