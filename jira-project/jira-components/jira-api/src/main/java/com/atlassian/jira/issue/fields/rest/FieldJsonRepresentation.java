package com.atlassian.jira.issue.fields.rest;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.rest.json.JsonData;

import javax.annotation.Nullable;

/**
 * For json representation of Field data.  Contains a raw format version and if required a rendered version for direct display.
 * For example the rendered data may contain html instead of raw wiki markup.  Or pretty dates rather than timestamps.
 *
 * @since v5.0
 */
@PublicApi
public class FieldJsonRepresentation
{
    private JsonData plainData;
    private JsonData renderedData;

    public FieldJsonRepresentation(@Nullable JsonData plainData)
    {
        this.plainData = plainData;
    }

    public FieldJsonRepresentation(@Nullable JsonData plainData, @Nullable JsonData renderedData)
    {
        this.renderedData = renderedData;
        this.plainData = plainData;
    }

    public void setRenderedData(JsonData renderedData)
    {
        this.renderedData = renderedData;
    }

    @Nullable
    public JsonData getStandardData()
    {
        return plainData;
    }

    @Nullable
    public JsonData getRenderedData()
    {
        return renderedData;
    }
}
