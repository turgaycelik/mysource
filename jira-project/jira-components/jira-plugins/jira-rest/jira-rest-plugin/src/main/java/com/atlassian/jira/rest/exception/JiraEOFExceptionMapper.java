package com.atlassian.jira.rest.exception;

import javax.ws.rs.ext.Provider;
import java.io.EOFException;

/**
 * @since v5.0
 */
@Provider
public class JiraEOFExceptionMapper extends JiraExceptionMapper<EOFException>
{
}
