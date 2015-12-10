package com.atlassian.jira.issue.customfields.impl;

import com.atlassian.annotations.PublicApi;

@PublicApi
public class FieldValidationException extends RuntimeException
{
    public FieldValidationException(String message)
    {
        super(message);
    }
}
