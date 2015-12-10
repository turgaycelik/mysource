/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields.layout.field;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.OrderableField;

/**
 * Holds hidden and required attributes and renderer type of a specific {@link OrderableField}, corresponds
 * to a single entry of a the Field Configuration in the user interface.
 */
@PublicApi
public interface FieldLayoutItem extends Comparable<FieldLayoutItem>
{
    /**
     * Gets the field in question.
     * @return the field.
     */
    public OrderableField getOrderableField();


    public String getFieldDescription();
    

    public String getRawFieldDescription();

    /**
     * Whether the field should be invisible in the UI.
     * @return true only if the field should be hidden.
     */
    public boolean isHidden();

    /**
     * Whether a field value is mandatory.
     * @return true only if a value is required.
     */
    public boolean isRequired();

    /**
     * Returns the renderer type, e.g. {@link com.atlassian.jira.issue.fields.renderer.text.DefaultTextRenderer#RENDERER_TYPE}.
     * @return the renderer for the field configuration for this field.
     */
    public String getRendererType();

    /**
     * Returns the {@link FieldLayout} that this item is a part of.
     *
     * @return the field layout; may be null.
     * @since v4.2
     */
    public FieldLayout getFieldLayout();
}
