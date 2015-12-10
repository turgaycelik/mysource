package com.atlassian.jira.plugin.webfragment.model;

import java.util.Map;

/**
 * A simple link section representation
 *
 * @since v4.0
 */
public interface SimpleLinkSection
{
    /**
     * The label for the section
     *
     * @return The label for the section
     */
    String getLabel();

    /**
     * The title (tooltip) for the section
     *
     * @return The title (tooltip) for the section
     */
    String getTitle();

    /**
     * The url for the icon for the section
     *
     * @return The url for the icon for the section
     */
    String getIconUrl();

    /**
     * The style to aply to the section
     *
     * @return The style to aply to the section
     */
    String getStyleClass();

    /**
     * The unique id for the section.  This should not be null.
     *
     * @return The unique id for the section.
     */
    String getId();

    /**
     * Get params associated with this section
     *
     * @return untyped params of this section
     */
    Map<String,String> getParams();

    /**
     * The weight for the section
     *
     * @return The weight for the section
     */
    Integer getWeight();
}
