package com.atlassian.jira.rest.util;

import com.atlassian.jira.rest.exception.BadRequestWebException;
import com.atlassian.jira.rest.exception.ForbiddenWebException;
import com.atlassian.jira.rest.exception.NotAuthorisedWebException;
import com.atlassian.jira.rest.exception.NotFoundWebException;
import com.atlassian.jira.rest.exception.ServerErrorWebException;
import com.atlassian.jira.util.ErrorCollection;

import javax.ws.rs.core.Response;

import static com.atlassian.jira.util.ErrorCollection.Reason.SERVER_ERROR;
import static com.atlassian.jira.util.ErrorCollection.Reason.getWorstReason;

/**
 * Builds response objects for REST service
 *
 * @since v6.1
 */
public class ResponseUtils
{
    private ResponseUtils()
    {
    }

    /**
     * Throws exceptions that corresponds to a ErrorCollection.Reason
     *
     * @param errorCollection error list
     * @return never, only throws exceptions
     *
     * @throws NotFoundWebException
     * @throws NotAuthorisedWebException
     * @throws ForbiddenWebException
     * @throws BadRequestWebException
     * @throws ServerErrorWebException
     */
    public static Response throwEx(final ErrorCollection errorCollection)
    {
        ErrorCollection.Reason reason = getWorstReason(errorCollection.getReasons());
        if (reason == null)
        {
            reason = SERVER_ERROR;
        }

        com.atlassian.jira.rest.api.util.ErrorCollection errorEntity = com.atlassian.jira.rest.api.util.ErrorCollection.of(errorCollection);

        switch (reason)
        {
            case NOT_FOUND:
                throw new NotFoundWebException(errorEntity);
            case NOT_LOGGED_IN:
                throw new NotAuthorisedWebException(errorEntity);
            case FORBIDDEN:
                throw new ForbiddenWebException(errorEntity);
            case VALIDATION_FAILED:
                throw new BadRequestWebException(errorEntity);
            case SERVER_ERROR:
            default:
                throw new ServerErrorWebException(errorEntity);
        }
    }
}
