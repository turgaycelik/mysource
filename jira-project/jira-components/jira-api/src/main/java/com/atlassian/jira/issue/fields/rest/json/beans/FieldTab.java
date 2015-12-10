package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.annotations.ExperimentalApi;
import org.apache.commons.lang.builder.ToStringBuilder;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Represents a tab on the edit or create screen.
 *
 * @since v5.0.3
 */
@ExperimentalApi
@XmlRootElement (name = "tab")
public class FieldTab
{
    @XmlElement (name = "label")
    private String label;

    @XmlElement (name = "position")
    private int position;

    private FieldTab() {}

    public FieldTab(final String label, final int position)
    {
        this.label = label;
        this.position = position;
    }

    public String getLabel()
    {
        return label;
    }

    public int getPosition()
    {
        return position;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o) { return true; }
        if (o == null || getClass() != o.getClass()) { return false; }

        final FieldTab fieldTab = (FieldTab) o;

        if (position != fieldTab.position) { return false; }
        if (label != null ? !label.equals(fieldTab.label) : fieldTab.label != null) { return false; }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = label != null ? label.hashCode() : 0;
        result = 31 * result + position;
        return result;
    }

    @Override
    public String toString()
    {
        return new ToStringBuilder(this).
                append("label", label).
                append("position", position).
                toString();
    }
}
