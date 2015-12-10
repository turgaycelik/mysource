package com.atlassian.jira.web.action.setup;

import java.util.Collections;
import java.util.Set;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.web.HttpServletVariables;

import org.apache.commons.httpclient.Cookie;

/**
 * Stores items shared across setup steps. Future work should make this class vanish.
 */
public class SetupSharedVariables
{
    public static final String SETUP_CHOOSEN_BUNDLE = "setup-chosen-bundle";
    private static final String SETUP_BUNDLE_LICENSE_KEY = "setup-bundle-license-key";
    private static final String SETUP_BUNDLE_LICENSE_COOKIES = "setup-bundle-license-cookies";
    private static final String SETUP_BUNDLE_HAS_LICENSE_ERROR = "setup-bundle-has-license-error";

    private final ApplicationProperties applicationProperties;
    private final HttpServletVariables servletVariables;

    public SetupSharedVariables(final HttpServletVariables servletVariables, final ApplicationProperties applicationProperties)
    {
        this.servletVariables = servletVariables;
        this.applicationProperties = applicationProperties;
    }

    public String getSelectedBundle()
    {
        final String selectedBundle = (String) servletVariables.getHttpSession().getAttribute(SETUP_CHOOSEN_BUNDLE);

        if (selectedBundle == null)
        {
            return applicationProperties.getString(APKeys.JIRA_SETUP_CHOSEN_BUNDLE);
        }

        return selectedBundle;
    }

    public void setSelectedBundle(String selection)
    {
        servletVariables.getHttpSession().setAttribute(SETUP_CHOOSEN_BUNDLE, selection);
        applicationProperties.setString(APKeys.JIRA_SETUP_CHOSEN_BUNDLE, selection);
    }

    public String getBundleLicenseKey()
    {
        return (String) servletVariables.getHttpSession().getAttribute(SETUP_BUNDLE_LICENSE_KEY);
    }

    public void setBundleLicenseKey(String licenseKey)
    {
        servletVariables.getHttpSession().setAttribute(SETUP_BUNDLE_LICENSE_KEY, licenseKey);
    }

    public Set<Cookie> getBundleLicenseCookies()
    {
        final Object cookies = servletVariables.getHttpSession().getAttribute(SETUP_BUNDLE_LICENSE_COOKIES);

        if (cookies != null)
        {
            return (Set<Cookie>) cookies;
        }

        return Collections.emptySet();
    }

    public void setBundleLicenseCookies(final Set<Cookie> cookies)
    {
        servletVariables.getHttpSession().setAttribute(SETUP_BUNDLE_LICENSE_COOKIES, cookies);
    }

    public void removeBundleLicenseCookies()
    {
        servletVariables.getHttpSession().removeAttribute(SETUP_BUNDLE_LICENSE_COOKIES);
    }

    public String getBaseUrl()
    {
        final HttpServletRequest request = servletVariables.getHttpRequest();

        return request.getScheme() + "://localhost:" + request.getLocalPort() + request.getContextPath();
    }

    public boolean getBundleHasLicenseError()
    {
        Boolean hasError = (Boolean) servletVariables.getHttpSession().getAttribute(SETUP_BUNDLE_HAS_LICENSE_ERROR);

        return hasError == null ? false : hasError;
    }

    public void setBundleHasLicenseError(boolean hasError)
    {
        servletVariables.getHttpSession().setAttribute(SETUP_BUNDLE_HAS_LICENSE_ERROR, hasError);
    }

    public String getWebSudoToken()
    {
        return applicationProperties.getString(APKeys.JIRA_SETUP_WEB_SUDO_TOKEN);
    }

    public void setWebSudoToken(final String token)
    {
        applicationProperties.setString(APKeys.JIRA_SETUP_WEB_SUDO_TOKEN, token);
    }
}
