package com.atlassian.jira.bc;

import com.atlassian.annotations.PublicApi;

@PublicApi
public class EntityNotFoundException extends Exception
{
    public EntityNotFoundException()
    {
    }

    public EntityNotFoundException(String message)
    {
        super(message);
    }

}
