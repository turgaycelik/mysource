package com.atlassian.jira.web.action.setup;


import java.io.IOException;
import java.util.Map;
import java.util.Set;

import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;

import org.apache.commons.codec.digest.DigestUtils;
import org.apache.commons.httpclient.Cookie;
import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.cookie.CookiePolicy;
import org.apache.commons.httpclient.methods.DeleteMethod;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.httpclient.methods.PutMethod;
import org.apache.commons.httpclient.methods.StringRequestEntity;
import org.apache.log4j.Logger;

public class SetupProductBundleHelper
{
    private static final Logger log = Logger.getLogger(SetupProductBundleHelper.class);

    private final SetupSharedVariables sharedVariables;

    public SetupProductBundleHelper(final SetupSharedVariables sharedVariables)
    {
        this.sharedVariables = sharedVariables;
    }

    public void authenticateUser(final String username, final String password)
    {
        final HttpClient httpClient = prepareClient();
        final PostMethod method = new PostMethod(getAuthenticationUrl());

        try
        {
            final StringRequestEntity requestEntity = new StringRequestEntity(
                    prepareJSON(ImmutableMap.of("username", username, "password", password)),
                    "application/json", "UTF-8");

            method.setRequestEntity(requestEntity);

            final int status = httpClient.executeMethod(method);
            if (status == 200)
            {
                saveCookies(httpClient.getState().getCookies());
            }
            else
            {
                log.warn("Problem authenticating user during product bundle license installation, status code: " + status);
            }
        }
        catch (final IOException e)
        {
            log.warn("Problem authenticating user during product bundle license installation", e);
        }
        finally
        {
            method.releaseConnection();
        }
    }

    public void enableWebSudo()
    {
        final HttpClient httpClient = prepareClient();
        final PostMethod method = new PostMethod(getWebSudoUrl());

        final String token = DigestUtils.shaHex(String.valueOf(System.currentTimeMillis() + String.valueOf(Math.random())));
        sharedVariables.setWebSudoToken(token);

        try
        {
            method.setParameter("webSudoToken", token);

            final int status = httpClient.executeMethod(method);
            if (status == 200)
            {
                saveCookies(httpClient.getState().getCookies());
            }
            else
            {
                log.warn("Problem when enabling websudo during product bundle license installation, status code: " + status);
            }
        }
        catch (final IOException e)
        {
            log.warn("Problem when enabling websudo during product bundle license installation", e);
        }
        finally
        {
            method.releaseConnection();
        }
    }

    public boolean saveLicense()
    {
        int status = 0;
        final String licenseUrl;
        final String licenseKey = sharedVariables.getBundleLicenseKey();

        if (licenseKey == null)
        {
            sharedVariables.setBundleHasLicenseError(true);
            return false;
        }

        try
        {
            licenseUrl = getLicenseUrl();
        }
        catch (final IllegalStateException e)
        {
            sharedVariables.setBundleHasLicenseError(true);
            return false;
        }

        final HttpClient httpClient = prepareClient();
        final PutMethod method = new PutMethod(licenseUrl);

        try
        {
            final StringRequestEntity requestEntity = new StringRequestEntity(
                    prepareJSON(ImmutableMap.of("rawLicense", licenseKey)),
                    "application/vnd.atl.plugins+json", "UTF-8");

            method.setRequestEntity(requestEntity);
            status = httpClient.executeMethod(method);
        }
        catch (final IOException e)
        {
            log.warn("Problem with saving licence during product bundle license installation", e);
        }
        finally
        {
            method.releaseConnection();
        }

        if (status != 200)
        {
            log.warn("Problem with saving licence during product bundle license installation, status code: " + status);
            sharedVariables.setBundleHasLicenseError(true);
            return false;
        }

        return true;
    }

    public void destroySession()
    {
        final HttpClient httpClient = prepareClient();
        final DeleteMethod method = new DeleteMethod(getAuthenticationUrl());
        method.setRequestHeader("Content-Type", "application/json");

        try
        {
            httpClient.executeMethod(method);
        }
        catch (final IOException e)
        {
            log.warn("Problem with destroying session during product bundle license installation", e);
        }
        finally
        {
            method.releaseConnection();
            removeCookies();
            clearWebSudoToken();
        }
    }

    private String getAuthenticationUrl()
    {
        return sharedVariables.getBaseUrl() + "/rest/auth/1/session";
    }

    private String getWebSudoUrl()
    {
        return sharedVariables.getBaseUrl() + "/secure/SetupAdminAccount!enableWebSudo.jspa";
    }

    private String getLicenseUrl()
    {
        if (isDevelopmentBundleSelected())
        {
            return sharedVariables.getBaseUrl() + "/rest/plugins/1.0/com.pyxis.greenhopper.jira-key/license";
        }

        if (isServiceDeskBundleSelected())
        {
            return sharedVariables.getBaseUrl() + "/rest/plugins/1.0/com.atlassian.servicedesk-key/license";
        }

        throw new IllegalStateException("No REST resource available to put license for selected product bundle");
    }

    private void saveCookies(final Cookie[] cookies)
    {
        sharedVariables.setBundleLicenseCookies(ImmutableSet.copyOf(cookies));
    }

    private Cookie[] getCookies()
    {
        final Set<Cookie> cookies = sharedVariables.getBundleLicenseCookies();

        return cookies.toArray(new Cookie[cookies.size()]);
    }

    private void removeCookies()
    {
        sharedVariables.removeBundleLicenseCookies();
    }

    private void clearWebSudoToken()
    {
        sharedVariables.setWebSudoToken(null);
    }

    public boolean isDevelopmentBundleSelected()
    {
        return SetupProductBundle.BUNDLE_DEVELOPMENT.equals(sharedVariables.getSelectedBundle());
    }

    public boolean isServiceDeskBundleSelected()
    {
        return SetupProductBundle.BUNDLE_SERVICEDESK.equals(sharedVariables.getSelectedBundle());
    }

    public boolean isNoBundleSelected()
    {
        return !isDevelopmentBundleSelected() && !isServiceDeskBundleSelected();
    }

    private String prepareJSON(final Map<String, String> params)
    {
        final JSONObject jsonObject = new JSONObject();

        for (final Map.Entry<String, String> entry : params.entrySet())
        {
            try
            {
                jsonObject.put(entry.getKey(), entry.getValue());
            }
            catch (final JSONException e)
            {
                log.warn("Problem preparing json", e);
            }
        }

        return jsonObject.toString();
    }

    private HttpClient prepareClient()
    {
        final HttpClient httpClient = new HttpClient();

        httpClient.getHttpConnectionManager().getParams().setConnectionTimeout(10000);
        httpClient.getParams().setCookiePolicy(CookiePolicy.BROWSER_COMPATIBILITY);
        httpClient.getState().addCookies(getCookies());

        return httpClient;
    }

    public boolean hasLicenseError()
    {
        return sharedVariables.getBundleHasLicenseError();
    }

    public void cleanLicenseError()
    {
        sharedVariables.setBundleHasLicenseError(false);
    }
}
