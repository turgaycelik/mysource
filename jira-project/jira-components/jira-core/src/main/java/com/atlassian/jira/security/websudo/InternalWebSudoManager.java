package com.atlassian.jira.security.websudo;

import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import webwork.action.Action;

/**
 * Manages the WebSudo related access to Request, Response and Session objects and provides a method to determine
 * if an XWork action should be treated as a WebSudo resource ({@link #matches(Class}.
 */
public interface InternalWebSudoManager
{
    /**
     * @return true if WebSudo is enabled, false otherwise.
     */
    boolean isEnabled();

    /**
     * Check if the action method should be WebSudo protected for the given requestURI.
     *
     *
     * @param actionClass - the current action class
     * @return true if the action method should be WebSudo protected, false otherwise.
     */
    boolean matches(Class<? extends Action> actionClass);

    /**
     * Check if this is a valid WebSudo session.
     *
     * @param session the current {@link javax.servlet.http.HttpSession}. Can be null
     * @return true if the {@link javax.servlet.http.HttpSession} is a WebSudo session.
     */
    boolean hasValidSession(@Nullable HttpSession session);

    /**
     * Checks if the {@code request} is a WebSudo request.
     *
     * @param request the current {@link javax.servlet.http.HttpServletRequest}
     * @return true if the current request is requesting a WebSudo protected web resource, false otherwise.
     *
     * @since 3.4
     */
    boolean isWebSudoRequest(@Nullable HttpServletRequest request);

    /**
     * Start a new WebSudo session. Creates a new {@link javax.servlet.http.HttpSession} if necessary.
     *
     * @param request the current {@link javax.servlet.http.HttpServletRequest}
     * @param response the current {@link javax.servlet.http.HttpServletResponse}
     *
     * @since 3.4
     */
    void startSession(HttpServletRequest request, HttpServletResponse response);

    /**
     * Marks the {@code request} as a request for a WebSudo resource.
     *
     * @param request the current {@link javax.servlet.http.HttpServletRequest}
     *
     * @since 3.4
     */
    void markWebSudoRequest(@Nullable HttpServletRequest request);

    /**
     * Invalidate the current WebSudo session. This does <strong>NOT</strong> invalidate the {@link javax.servlet.http.HttpSession}.
     *
     * @param request the current {@link javax.servlet.http.HttpServletRequest}
     * @param response the current {@link javax.servlet.http.HttpServletResponse}
     *
     * @since 3.4
     */
    void invalidateSession(HttpServletRequest request, HttpServletResponse response);

}
