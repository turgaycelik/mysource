package com.atlassian.jira.rest.exception;

import org.codehaus.jackson.map.JsonMappingException;

import javax.ws.rs.ext.Provider;

/**
 * @since v5.0
 */
@Provider
public class JiraJsonMappingExceptionMapper extends JiraExceptionMapper<JsonMappingException>
{
}
