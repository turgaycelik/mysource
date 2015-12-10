package com.atlassian.jira.plugin.webfragment.model;

import java.util.Map;

/**
 *
 * @since v6.2
 */
public class MockSimpleLinkSection implements SimpleLinkSection
{
    protected String label;
    protected String title;
    protected String iconUrl;
    protected String styleClass;
    protected String id;
    protected Map<String,String> params;
    protected Integer weight;

    public MockSimpleLinkSection(final String id)
    {
        this.id = id;
    }

    public String getLabel()
    {
        return label;
    }

    public void setLabel(final String label)
    {
        this.label = label;
    }

    public String getTitle()
    {
        return title;
    }

    public void setTitle(final String title)
    {
        this.title = title;
    }

    public String getIconUrl()
    {
        return iconUrl;
    }

    public void setIconUrl(final String iconUrl)
    {
        this.iconUrl = iconUrl;
    }

    public String getStyleClass()
    {
        return styleClass;
    }

    public void setStyleClass(final String styleClass)
    {
        this.styleClass = styleClass;
    }

    public String getId()
    {
        return id;
    }

    public void setId(final String id)
    {
        this.id = id;
    }

    public Map<String, String> getParams()
    {
        return params;
    }

    public void setParams(final Map<String, String> params)
    {
        this.params = params;
    }

    public Integer getWeight()
    {
        return weight;
    }

    public void setWeight(final Integer weight)
    {
        this.weight = weight;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (!(o instanceof MockSimpleLinkSection)) { return false; }

        final MockSimpleLinkSection that = (MockSimpleLinkSection) o;

        if (iconUrl != null ? !iconUrl.equals(that.iconUrl) : that.iconUrl != null) { return false; }
        if (id != null ? !id.equals(that.id) : that.id != null) { return false; }
        if (label != null ? !label.equals(that.label) : that.label != null) { return false; }
        if (params != null ? !params.equals(that.params) : that.params != null) { return false; }
        if (styleClass != null ? !styleClass.equals(that.styleClass) : that.styleClass != null) { return false; }
        if (title != null ? !title.equals(that.title) : that.title != null) { return false; }
        if (weight != null ? !weight.equals(that.weight) : that.weight != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + (title != null ? title.hashCode() : 0);
        result = 31 * result + (iconUrl != null ? iconUrl.hashCode() : 0);
        result = 31 * result + (styleClass != null ? styleClass.hashCode() : 0);
        result = 31 * result + (id != null ? id.hashCode() : 0);
        result = 31 * result + (params != null ? params.hashCode() : 0);
        result = 31 * result + (weight != null ? weight.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return String.format("MockSimpleLinkSection[%s]", id);
    }
}
