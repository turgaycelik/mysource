package com.atlassian.jira.util.i18n;

import java.util.Enumeration;

import javax.annotation.Nonnull;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This will try to switch i18n translations mode on or off depending on whether the URL has a magic parameter OR the
 * session contains the magic attribute.
 * <p/>
 * It will set it each time while these conditions are true and reverse the situation if the magic request parameter is
 * set to off
 */
public class I18nTranslationModeSwitch
{
    private static final String MAGIC_PARAMETER_NAME = "i18ntranslate";

    /**
     * This can be called to turn on translations mode or turn it off depending on the presence of the URL parameter.
     *
     * @param httpServletRequest the request in play
     * @param httpServletResponse the response in play
     */
    public void switchTranslationsMode(final HttpServletRequest httpServletRequest, final HttpServletResponse httpServletResponse)
    {
        boolean turnItOn;
        String i18nTranslateStr = getMagicParameter(httpServletRequest);
        if (i18nTranslateStr != null)
        {
            turnItOn = parseBoolean(i18nTranslateStr);
        }
        else
        {
            Object sessionFlag = getSessionFlag(httpServletRequest);
            if (sessionFlag == null)
            {
                // no change in the status quo
                return;
            }
            // the presence of the session attribute indicates thats its on
            turnItOn = true;
        }
        final I18nTranslationModeImpl translationMode = new I18nTranslationModeImpl();
        if (turnItOn)
        {
            translationMode.setTranslationsModeOn(httpServletRequest, httpServletResponse);
            setSessionAttribute(httpServletRequest, Boolean.TRUE);
        }
        else
        {
            translationMode.setTranslationsModeOff();
            clearSessionAttribute(httpServletRequest);
        }

    }

    private String getMagicParameter(HttpServletRequest httpServletRequest)
    {
        final Enumeration parameterNames = httpServletRequest.getParameterNames();
        while (parameterNames.hasMoreElements())
        {
            final String paramName = (String) parameterNames.nextElement();
            if (MAGIC_PARAMETER_NAME.toLowerCase().equals(paramName))
            {
                return httpServletRequest.getParameter(paramName);
            }
        }
        return null;
    }

    private void clearSessionAttribute(HttpServletRequest httpServletRequest)
    {
        setSessionAttribute(httpServletRequest, null);
    }

    private void setSessionAttribute(HttpServletRequest httpServletRequest, final Boolean flag)
    {
        HttpSession httpSession = httpServletRequest.getSession(false);
        if (httpSession != null)
        {
            httpSession.setAttribute(getClass().getName(), flag);
        }
    }

    private Object getSessionFlag(HttpServletRequest httpServletRequest)
    {
        Object sessionFlag = null;
        HttpSession httpSession = httpServletRequest.getSession(false);
        if (httpSession != null)
        {
            sessionFlag = httpSession.getAttribute(getClass().getName());
        }
        return sessionFlag;
    }

    private boolean parseBoolean(@Nonnull String i18nTranslateStr)
    {
        String lowerStr = i18nTranslateStr.toLowerCase();
        if ("on".equals(lowerStr))
        {
            return true;
        }
        else if ("off".equals(lowerStr))
        {
            return false;
        }
        return Boolean.parseBoolean(i18nTranslateStr);
    }
}
