package com.atlassian.jira.util.i18n;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import static org.apache.commons.lang.StringUtils.defaultString;

/**
 * This implementation class can turn on the translations mode so that i18n produced messages get sent in a specially
 * marked up way
 *
 * @since 4.3
 */
public class I18nTranslationModeImpl implements I18nTranslationMode
{
    private static final ThreadLocal<Boolean> translationMode = new ThreadLocal<Boolean>();

    /**
     * This uses a ThreadLocal under the covers so we don't care how many instances of us there are.
     * <p/>
     * We always set the thread local value to false on instantiation.
     */
    public I18nTranslationModeImpl()
    {
        translationMode.set(false);
    }

    @Override
    public boolean isTranslationMode()
    {
        // FEAR LEADS TO GENERICS - GENERICS LEADS TO AUTOBOXING - AUTOBOXING LEADS TO NPE!
        Boolean flag = translationMode.get();
        return flag == null ? false : flag;
    }

    @Override
    public void setTranslationsModeOff()
    {
        translationMode.set(Boolean.FALSE);
    }

    @Override
    public void setTranslationsModeOn(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
    {
        // if they are trying to turn on translation mode, we double check the type of request to see if it makes sense
        // for example it rarely makes sense for AJAX requests
        if (!sensibleRequest(httpServletRequest))
        {
            return;
        }
        translationMode.set(Boolean.TRUE);
        httpServletRequest.setAttribute("X-Atlassian-I18nTranslationMode", true);
        httpServletResponse.setHeader("X-Atlassian-I18nTranslationMode", "true");
    }

    private boolean sensibleRequest(HttpServletRequest httpServletRequest)
    {
        String requestedWith = defaultString(httpServletRequest.getHeader("X-Requested-With")).toLowerCase();
        if (requestedWith.contains("xmlhttprequest"))
        {
            return false;
        }
        String urlPath = defaultString(httpServletRequest.getRequestURI()).toLowerCase();
        if (urlPath.contains("/rest/") || urlPath.contains("/sr/"))
        {
            return false;
        }
        String accept = defaultString(httpServletRequest.getHeader("Accept")).toLowerCase();
        return !accept.contains("application/json");
    }
}
