package com.atlassian.jira.util.velocity;

/**
 * A context that allows for different implementations depending on whether it is running in the scope of a web
 * request, or via email.
 *
 * @see VelocityRequestContextFactory
 */
public interface VelocityRequestContext
{
    /**
     * @return The base URL for this instance, also known as the context path.  If running in the context of a web request, this will return a url relative
     *         to the server root (ie "/jira/").  If running via email, it will return an absolute URL (eg. "http://example.com/jira/").
     */
    String getBaseUrl();


    /**
     * @return The canonical base URL for this instance.  It will return an absolute URL (eg. "http://example.com/jira/").
     */
    String getCanonicalBaseUrl();

    /**
     * Returns a RequestContextParameterHolder with various HttpServletRequest parameters.  This will be null
     * if no HttpRequest is available.
     *
     * @return Null if no HttpRequest is available. RequestContextParameterHolder with various parameters otherwise
     */
    RequestContextParameterHolder getRequestParameters();

    /**
     * Convenience method to return the value from the requestParameter map stored with the name
     * parameter.
     *
     * @param name parameter name
     * @return parameter value, or null
     * @since v3.10
     */
    String getRequestParameter(String name);


    /**
     * Retrieve the {@link com.atlassian.jira.util.velocity.VelocityRequestSession} for the current user.
     *
     * This will be null if you did not come in through a Http Request
     *
     * @return null if no session is available
     */
    VelocityRequestSession getSession();
}
