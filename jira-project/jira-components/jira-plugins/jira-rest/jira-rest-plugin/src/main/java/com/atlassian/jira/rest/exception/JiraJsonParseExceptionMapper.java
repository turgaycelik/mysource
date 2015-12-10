package com.atlassian.jira.rest.exception;

import org.codehaus.jackson.JsonParseException;

import javax.ws.rs.ext.ExceptionMapper;
import javax.ws.rs.ext.Provider;

/**
 * Implementation of {@link ExceptionMapper} to send down a "400 Bad Request" in the event unparsable JSON is received.
 */
@Provider
public class JiraJsonParseExceptionMapper extends JiraExceptionMapper<JsonParseException>
{
}
