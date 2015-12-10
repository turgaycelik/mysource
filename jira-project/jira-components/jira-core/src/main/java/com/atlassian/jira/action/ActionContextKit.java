package com.atlassian.jira.action;

import webwork.action.ActionContext;
import webwork.action.ServletActionContext;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 * A helper class to clean up the viral {@link webwork.action.ActionContext} thread locals
 *
 * @since v4.0
 */
public class ActionContextKit
{
    private ActionContextKit()
    {
    }

    /**
     * This sets up the {@link webwork.action.ActionContext} inside JIRA.  Make sure you have a strategy in clearing
     * this ActionContext via {@link #resetContext()}
     *
     * @param httpServletRequest the request in play
     * @param httpServletResponse the response in play
     * @param servletContext the servlet context in play
     */
    public static void setContext(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, ServletContext servletContext)
    {
        setContext(httpServletRequest, httpServletResponse, servletContext, null);
    }

    /**
     * This sets up the {@link webwork.action.ActionContext} inside JIRA.  Make sure you have a strategy in clearing
     * this ActionContext via {@link #resetContext()}
     *
     * @param httpServletRequest the request in play
     * @param httpServletResponse the response in play
     * @param actionName the current action name to run
     */
    public static void setContext(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, String actionName)
    {
        setContext(httpServletRequest, httpServletResponse, null, actionName);
    }

    /**
     * This sets up the {@link webwork.action.ActionContext} inside JIRA.  Make sure you have a strategy in clearing
     * this ActionContext via {@link #resetContext()}
     *
     * @param httpServletRequest the request in play
     * @param httpServletResponse the response in play
     * @param servletContext the servlet context in play
     * @param actionName the current action name to run
     */
    public static void setContext(HttpServletRequest httpServletRequest, HttpServletResponse httpServletResponse, ServletContext servletContext, String actionName)
    {
        ServletActionContext.setContext(httpServletRequest, httpServletResponse, servletContext, actionName);
    }


    /**
     * This will reset the current {@link webwork.action.ActionContext} ThreadLocal to be a clean slate eg with no
     * references to any Http objects such as request, sessions or responses.  And calls to getter method will return
     * null.
     * <p/>
     * This is here to help stop JRA-8009
     */
    public static void resetContext()
    {
        ActionContext.setContext(new ActionContext());
    }
}
