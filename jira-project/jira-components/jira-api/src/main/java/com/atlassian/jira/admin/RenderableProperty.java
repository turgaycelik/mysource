package com.atlassian.jira.admin;

import com.atlassian.annotations.PublicApi;

/**
 * Represents a String field that accepts user input and is rendered in JIRA pages. A {@code RenderableProperty} has
 * two modes of operation, depending on whether JIRA is running in OnDemand or not.
 * <ul>
 *     <li>in BTF: the property accepts raw HTML and renders it as-is</li>
 *     <li>in OnDemand: the property accepts Wiki markup and renders it as HTML</li>
 * </ul>
 * Implementations of this interface are capable of rendering their own "view", "edit", and "description" HTML.
 *
 * @since v5.0.7
 */
@PublicApi
public interface RenderableProperty
{
    /**
     * Returns the view HTML for this renderable application property.
     *
     * @return a String containing the view HTML
     * @since v5.0.7
     */
    String getViewHtml();

    /**
     * Returns the edit HTML for this renderable application property.
     *
     * @param fieldName the field name to use in the generated HTML
     * @return a String containing the edit HTML
     * @since v5.0.7
     */
    String getEditHtml(String fieldName);

    /**
     * Returns the description HTML for this property.
     *
     * @return a String containing the description HTML
     * @since v5.0.7
     */
    String getDescriptionHtml();
}
