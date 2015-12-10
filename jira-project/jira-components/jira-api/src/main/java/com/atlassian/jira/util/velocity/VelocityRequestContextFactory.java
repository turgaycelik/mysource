package com.atlassian.jira.util.velocity;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.InjectableComponent;
import com.google.common.base.Function;

import java.util.Map;
import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

/**
 * <p>Provides a request context that can be used to get the 'correct' baseurl.</p>
 *
 * <p>It will use information from the
 * HttpRequest if one was made or applicationProperties otherwise to determine the baseurl.</p>
 *
 * @since v4.0
 */
@PublicApi
@InjectableComponent
public interface VelocityRequestContextFactory
{
    /**
     * Get the request context.
     *
     * @return The request context.
     */
    VelocityRequestContext getJiraVelocityRequestContext();

    /**
     * Constructs a map with a number of common parameters used by velocity templates.
     *
     * @param startingParams        Map of parameters that may be used to override any of the parameters set here.
     * @param authenticationContext JiraAuthenticationContext
     * @return a Map with common velocity parameters
     */
    Map<String, Object> getDefaultVelocityParams(Map<String, Object> startingParams, JiraAuthenticationContext authenticationContext);

    /**
     * Update the thread-local storage with the given velocityRequestContext.
     *
     * @param velocityRequestContext The velocity request context to store.
     * @since 4.3
     * @deprecated Use {@link #setVelocityRequestContext(VelocityRequestContext)} instead. Since v5.0.
     */
    void cacheVelocityRequestContext(final VelocityRequestContext velocityRequestContext);

    /**
     * Resets the thread local storage as if no request has occurred, effectively nulling out the current
     * thread local velocity request context.
     *
     * @since 4.3
     */
    void clearVelocityRequestContext();

    /**
     * Update the thread-local storage with the given request information.
     *
     * @param request The http request context to store.
     * @since 5.0
     */
    void setVelocityRequestContext(HttpServletRequest request);

    /**
     * Update the thread-local storage with the given request information.
     *
     * @param baseUrl of the request.
     * @param request The http request context to store.
     * @since 5.0
     */
    void setVelocityRequestContext(String baseUrl, HttpServletRequest request);

    /**
     * Update the thread-local storage with the given velocityRequestContext.
     *
     * @param velocityRequestContext The velocity request context to store.
     * @since 5.0
     */
    void setVelocityRequestContext(VelocityRequestContext velocityRequestContext);

    /**
     * Run the passed function in an environment where JIRA's configured {@code baseURL} is always used. This basically
     * makes the passed function ignore any smart {@code baseURL} that can be generated from the request associated
     * with the calling thread.
     *
     * @param input input to pass to the function.
     * @param runnable the function to execute.
     * @return the result of the function.
     * @since 6.3.1
     */
    @Nullable
    <I, O> O runWithStaticBaseUrl(@Nullable I input, @Nonnull Function<I, O> runnable);
}
