package com.atlassian.jira.web;

import com.atlassian.annotations.PublicApi;

import javax.servlet.ServletContext;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 * This component can provide the variables that a typical {@link javax.servlet.http.HttpServlet } would receive.
 * <p/>
 * Note this only makes sense to use inside a HTTP request thread of execution and hence if you are not inside one
 * it will throw an {@link IllegalStateException}.
 *
 * @since v6.0
 */
@PublicApi
public interface HttpServletVariables
{

    /**
     * @return the {@link HttpServletRequest} in play
     * @throws IllegalStateException if you are not inside a HTTP request thread
     */
    HttpServletRequest getHttpRequest();

    /**
     * @return the {@link HttpSession} in play
     * @throws IllegalStateException if you are not inside a HTTP request thread
     */
    HttpSession getHttpSession();

    /**
     * @return the {@link HttpServletResponse} in play
     * @throws IllegalStateException if you are not inside a HTTP request thread
     */
    HttpServletResponse getHttpResponse();

    /**
     * @return the {@link ServletContext} of the JIRA web application
     */
    ServletContext getServletContext();
}
