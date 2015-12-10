package com.atlassian.jira.issue.fields.rest;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.rest.json.JsonData;

/**
 *
 * @since v5.0
 */
@PublicApi
public class FieldOperationHolder
{
    private final String operation;
    private final JsonData data;

    public FieldOperationHolder(String operation, JsonData data)
    {
        this.operation = operation;
        this.data = data;
    }

    public String getOperation()
    {
        return operation;
    }

    public JsonData getData()
    {
        return data;
    }
}
