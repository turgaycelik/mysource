package com.atlassian.jira.issue.fields.screen;

import com.atlassian.annotations.PublicApi;

import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
@PublicApi
public interface FieldScreenRenderTab extends Comparable<FieldScreenRenderTab>
{
    String getName();

    int getPosition();

    List<FieldScreenRenderLayoutItem> getFieldScreenRenderLayoutItems();

    /**
     * Should be used while actually processing the field input rather than displaying it. Ensures that fields are processed
     * in the correct order. For example, the components field is processed before the assignee field, so that the component
     * assignees are handled correctly.
     *
     * @return the layout items to be used when processing input.
     */
    List<FieldScreenRenderLayoutItem> getFieldScreenRenderLayoutItemsForProcessing();
}
