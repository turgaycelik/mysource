package com.atlassian.jira.plugin.webfragment.model;

import java.util.Map;

/**
 * Default implementation of {@link com.atlassian.jira.plugin.webfragment.model.SimpleLinkSection}.
 * Simple bean containing no real logic.
 *
 * @since v4.0
 */
public class SimpleLinkSectionImpl implements SimpleLinkSection
{
    protected final String label;
    protected final String title;
    protected final String iconUrl;
    protected final String style;
    protected final String id;
    protected final Map<String,String> params;
    protected final Integer weight;

    /**
     * Constructor taking all attributes of a section
     *
     * @param id      The unique id of the section
     * @param label   The optional label to display for the section
     * @param title   The optional title (tooltip) of the section
     * @param iconUrl The optional url pointing an image for the section
     * @param style   The optional style to apply to the section
     */
    public SimpleLinkSectionImpl(String id, String label, String title, String iconUrl, String style,
            Map<String,String> params)
    {
        this(id, label, title, iconUrl, style, params, null);
    }

    /**
     * Constructor taking all attributes of a section
     *
     * @param id      The unique id of the section
     * @param label   The optional label to display for the section
     * @param title   The optional title (tooltip) of the section
     * @param iconUrl The optional url pointing an image for the section
     * @param style   The optional style to apply to the section
     */
    public SimpleLinkSectionImpl(String id, String label, String title, String iconUrl, String style,
            Map<String,String> params, Integer weight)
    {
        this.label = label;
        this.title = title;
        this.iconUrl = iconUrl;
        this.style = style;
        this.id = id;
        this.params = params;
        this.weight = weight;
    }

    public SimpleLinkSectionImpl(String id, SimpleLinkSectionImpl copy)
    {
        this(id, copy.label, copy.title, copy.iconUrl, copy.style, copy.params, copy.weight);
    }

    public String getLabel()
    {
        return label;
    }

    public String getTitle()
    {
        return title;
    }

    public String getIconUrl()
    {
        return iconUrl;
    }

    public String getStyleClass()
    {
        return style;
    }

    public String getId()
    {
        return id;
    }

    @Override
    public Map<String, String> getParams()
    {
        return params;
    }

    public Integer getWeight()
    {
        return weight;
    }
}
