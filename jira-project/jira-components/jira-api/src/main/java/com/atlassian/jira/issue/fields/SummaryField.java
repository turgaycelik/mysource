package com.atlassian.jira.issue.fields;

import com.atlassian.jira.issue.fields.renderer.RenderableField;

/**
 * @since v5.0
 */
public interface SummaryField extends  NavigableField, MandatoryField, RenderableField, OrderableField, Field
{
    public static Long MAX_LEN = 255L;
}
