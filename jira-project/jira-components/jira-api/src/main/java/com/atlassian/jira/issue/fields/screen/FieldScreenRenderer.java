package com.atlassian.jira.issue.fields.screen;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;

import java.util.Collection;
import java.util.List;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 * <p>
 * A FieldScreenRenderer glues a FieldLayout (which governs requireability and hideability) with a
 * FieldScreen (which governs tabs, and ordering within tabs, and screen-level hideability).  For example, it
 * is possible for a field to be hidden on this screen, but still visible on other screens.
 * <p>
 * FieldScreenRenderers are usually constructed with a user, and a particular operation.
 */
@PublicApi
public interface FieldScreenRenderer
{
    /**
     * @return A list of {@link FieldScreenRenderTab} objects which are visible and contain at least one
     * {@link FieldScreenRenderLayoutItem}
     */
    List<FieldScreenRenderTab> getFieldScreenRenderTabs();

    /**
     * A convenience method to locate the tab, which contains the {@link FieldScreenRenderLayoutItem} which has a field
     * which has an id matching parameter fieldId
     *
     * @param   fieldId     The id of an the {@link OrderableField} to match against
     * @return  The tab which contains the field.
     * @see #getFieldScreenRenderLayoutItem(com.atlassian.jira.issue.fields.OrderableField)
     */
    FieldScreenRenderTab getFieldScreenRenderTabPosition(String fieldId);

    /**
     * A convenience method to locate the {@link FieldScreenRenderLayoutItem} which has a field
     * which has an id matching parameter orderableField
     *
     * @param   orderableField  The field to find
     * @return  The {@link FieldScreenRenderLayoutItem} which contains the {@link OrderableField}
     * @see #getFieldScreenRenderTabPosition(String)
     */
    FieldScreenRenderLayoutItem getFieldScreenRenderLayoutItem(OrderableField orderableField);

    /**
     * Get the underlying {@link FieldLayout} which backs this {@link FieldScreenRenderer}.  This is currently used to find <em>all</em>
     * the visible fields, not just the ones visible on this screen.  This allows the 'default' values to be set on
     * the fields not shown on this screen.
     *
     * @return the underlying {@link FieldLayout}.
     */
    FieldLayout getFieldLayout();

    /**
     * A convenience method which loops through all the {@link FieldScreenRenderLayoutItem} on all tabs, and returns
     * the ones that are required.
     *
     * @return A collection of {@link FieldScreenRenderLayoutItem} objects
     */
    Collection<FieldScreenRenderLayoutItem> getRequiredFieldScreenRenderItems();

    /**
     * Return a list of all the {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem}s contained
     * on any of the {@link com.atlassian.jira.issue.fields.screen.FieldScreenRenderTab}s.
     * It returns the items in order from first tab to last tab.
     *
     * @return all the layout items from all tabs. 
     */
    List<FieldScreenRenderLayoutItem> getAllScreenRenderItems();
}
