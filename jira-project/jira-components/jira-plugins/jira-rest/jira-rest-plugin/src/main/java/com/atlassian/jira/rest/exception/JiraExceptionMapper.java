package com.atlassian.jira.rest.exception;

import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.ws.rs.ext.ExceptionMapper;
import java.util.Collections;

import static com.atlassian.jira.rest.api.http.CacheControl.never;

/**
 * Abstract class that allows for simple formatting of errors as BAD_REQUEST responses.
 *
 * @since v5.0
 */
public abstract class JiraExceptionMapper<E extends Throwable> implements ExceptionMapper<E>
{
    @Override
    public Response toResponse(E exception)
    {
        JSONObject json = new JSONObject();
        try
        {
            json.put("errorMessages", Collections.singletonList(exception.getMessage()));
        }
        catch (JSONException e)
        {
            // This is just a realfallback if all goes bad
            return Response.status(Response.Status.BAD_REQUEST).entity(exception.getMessage()).cacheControl(never()).build();
        }
        return Response.status(Response.Status.BAD_REQUEST).entity(json.toString()).type(MediaType.APPLICATION_JSON_TYPE).cacheControl(never()).build();
    }
}
