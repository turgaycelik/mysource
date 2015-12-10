package com.atlassian.jira.rest.exception;

import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.plugins.rest.common.interceptor.MethodInvocation;
import com.atlassian.plugins.rest.common.interceptor.ResourceInterceptor;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.ws.rs.WebApplicationException;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import java.lang.reflect.InvocationTargetException;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * This interceptor creates responses for any exceptions thrown from a resource method.
 * <p/>
 * When creating a response, the client's <code>Accept</code> header is matched to the resource's {@link
 * javax.ws.rs.Produces} annotations and the first matching media type is used.
 * <p/>
 * See <a href="https://jdog.atlassian.com/browse/JRADEV-2862">JRADEV-2862</a> and <a
 * href="https://jdog.atlassian.com/browse/JRADEV-3240">JRADEV-3240</a>.
 *
 * @since v4.2
 */
public class ExceptionInterceptor implements ResourceInterceptor
{
    /**
     * Logger for this MediaTypeInterceptor instance.
     */
    private final Logger log = LoggerFactory.getLogger(ExceptionInterceptor.class);

    /**
     * A I18nHelper.
     */
    private final I18nHelper i18n;

    /**
     * Creates a new ExceptionInterceptor.
     *
     * @param i18n a I18nHelper
     */
    public ExceptionInterceptor(I18nHelper i18n)
    {
        this.i18n = i18n;
    }

    /**
     * Intercepts the method invocation, setting the media type correctly in case of an exception.
     *
     * @param invocation Context information about the invocation
     */
    public void intercept(MethodInvocation invocation) throws IllegalAccessException, InvocationTargetException
    {
        try
        {
            invocation.invoke();
        }
        catch (InvocationTargetException e)
        {
            Response response = createResponseFor(invocation, e.getCause());
            if (response.getStatus() >= 500)
            {
                log.error("Returning internal server error in response", e);
            }

            // just set the error response
            invocation.getHttpContext().getResponse().setResponse(response);
        }
    }

    /**
     * @param invocation a MethodInvocation
     * @param cause a Throwable
     * @return a Response
     */
    Response createResponseFor(MethodInvocation invocation, Throwable cause)
    {
        MediaType mediaType = determineMediaTypeFor(invocation);
        log.debug("Setting response mediaType to: {}", mediaType);

        if (cause instanceof WebApplicationException)
        {
            WebApplicationException appException = (WebApplicationException) cause;

            // create a new response with the correct media type
            return Response.fromResponse(appException.getResponse()).type(mediaType).build();
        }
        else
        {
            ErrorCollection errors = ErrorCollection.of(i18n.getText("rest.error.internal"));
            return Response.serverError().entity(errors).cacheControl(never()).build();
        }
    }

    /**
     * Determines the correct media type to use for a response based on the HTTP request Accept header and the called
     * method's {@link @javax.ws.rs.Produces} annotations.
     *
     * @param invocation a MethodInvocation representing the ongoing request
     * @return a MediaType
     */
    private MediaType determineMediaTypeFor(MethodInvocation invocation)
    {
        for (MediaType accepted : invocation.getHttpContext().getRequest().getAcceptableMediaTypes())
        {
            for (MediaType produces : invocation.getMethod().getSupportedOutputTypes())
            {
                if (accepted.isCompatible(produces))
                {
                    return produces;
                }
            }
        }

        // this can't happen because Jersey has already validated this at this point
        throw new IllegalStateException("Failed to negotiate correct media type for response");
    }
}
