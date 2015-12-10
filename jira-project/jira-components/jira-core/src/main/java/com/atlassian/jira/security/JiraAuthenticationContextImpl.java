package com.atlassian.jira.security;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.security.login.LoginLoggers;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.log.Log4jKit;
import com.atlassian.jira.web.bean.I18nBean;
import com.atlassian.jira.web.util.OutlookDate;
import com.atlassian.jira.web.util.OutlookDateManager;
import com.atlassian.seraph.auth.AuthenticationContext;
import org.apache.commons.lang.StringUtils;

import java.security.Principal;
import java.util.HashMap;
import java.util.Locale;
import java.util.Map;

public class JiraAuthenticationContextImpl implements JiraAuthenticationContext
{
    private static final ThreadLocal<Map<String, Object>> REQUEST_CACHE = new ThreadLocal<Map<String, Object>>()
    {
        @Override
        protected Map<String, Object> initialValue()
        {
            return new HashMap<String, Object>();
        }
    };

    public static void clearRequestCache()
    {
        REQUEST_CACHE.remove();
    }

    public static Map<String, Object> getRequestCache()
    {
        return REQUEST_CACHE.get();
    }

    //
    // members
    //

    private final AuthenticationContext authenticationContext;
    private final I18nHelper.BeanFactory i18n;

    //
    // ctors
    //

    public JiraAuthenticationContextImpl(final AuthenticationContext authenticationContext, I18nHelper.BeanFactory i18n)
    {
        this.authenticationContext = authenticationContext;
        this.i18n = i18n;
    }

    //
    // methods
    //

    @Override
    public User getLoggedInUser()
    {
        final Principal principal = authenticationContext.getUser();
        if (principal == null)
        {
            return null;
        }
        if (principal instanceof ApplicationUser)
        {
            return ((ApplicationUser) principal).getDirectoryUser();
        }
        return getUserManager().getUser(principal.getName());
    }

    protected UserManager getUserManager()
    {
        return ComponentAccessor.getUserManager();
    }

    @Override
    public boolean isLoggedInUser()
    {
        return getLoggedInUser() != null;
    }

    @Override
    public ApplicationUser getUser()
    {
        return ApplicationUsers.from(getLoggedInUser());
    }

    @Override
    public Locale getLocale()
    {
        return I18nBean.getLocaleFromUser(getLoggedInUser());
    }

    @Override
    public OutlookDate getOutlookDate()
    {
        return ComponentAccessor.getComponent(OutlookDateManager.class).getOutlookDate(getLocale());
    }

    @Override
    public String getText(final String key)
    {
        return getI18nHelper().getText(key);
    }

    @Override
    public I18nHelper getI18nHelper()
    {
        return i18n.getInstance(getLoggedInUser());
    }

    @Override
    public I18nHelper getI18nBean()
    {
        return getI18nHelper();
    }

    @Override
    @Deprecated
    public void setLoggedInUser(final User user)
    {
        setLoggedInUserImpl(user);
    }

    @Override
    public void setLoggedInUser(final ApplicationUser user)
    {
        setLoggedInUserImpl(ApplicationUsers.toDirectoryUser(user));
    }

    @Override
    public void clearLoggedInUser()
    {
        setLoggedInUserImpl(null);
    }

    private void setLoggedInUserImpl(final User user)
    {
        //
        // make log4j aware of who is making the request
        // if we are calling setUser then we are typically
        // 'impersonating" some one different
        //
        final String userName = user == null ? null : user.getName();
        Log4jKit.putUserToMDC(userName);
        if (LoginLoggers.LOGIN_SETAUTHCTX_LOG.isDebugEnabled())
        {
            LoginLoggers.LOGIN_SETAUTHCTX_LOG.debug("Setting JIRA Auth Context to be  '" + (StringUtils.isBlank(userName) ? "anonymous" : userName) + "'");
        }
        if (user == null)
        {
            authenticationContext.clearUser();
        }
        else
        {
            authenticationContext.setUser(user);
        }
    }
}
