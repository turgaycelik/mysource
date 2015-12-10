package com.atlassian.jira.rest.api.issue;

import org.codehaus.jackson.annotate.JsonAnySetter;
import org.codehaus.jackson.map.annotate.JsonSerialize;

/**
 * @since v5.0
 */
@JsonSerialize (using = FieldOperationSerializer.class)
public class FieldOperation
{
    private String operation;
    private Object value;

    public FieldOperation()
    {
    }

    public FieldOperation(String operation, Object value)
    {
        this.operation = operation;
        this.value = value;
    }

    @JsonAnySetter
    public void init(String operation, Object value)
    {
        this.operation = operation;
        this.value = value;
    }

    public String getOperation()
    {
        return operation;
    }

    public Object getValue()
    {
        return value;
    }

    public FieldOperation operation(String operation)
    {
        return new FieldOperation(operation, value);
    }

    public FieldOperation value(Object value)
    {
        return new FieldOperation(operation, value);
    }
}
