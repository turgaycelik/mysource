/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.bc;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

public class ValidationErrorsException extends RuntimeException
{

    private final Map<String, Exception> errorExceptions = new HashMap<String, Exception>();

    public ValidationErrorsException()
    {
    }

    public ValidationErrorsException(String message)
    {
        super(message);
    }

    public void addError(String fieldName, Exception ex)
    {
        errorExceptions.put(fieldName, ex);
    }

    public boolean hasErrors()
    {
        return !errorExceptions.isEmpty();
    }

    public Collection getFieldNames()
    {
        return errorExceptions.keySet();
    }

    public String getErrorMessage(String fieldName)
    {
        Exception ex = errorExceptions.get(fieldName);
        return ex == null ? null : ex.getMessage();
    }

}
