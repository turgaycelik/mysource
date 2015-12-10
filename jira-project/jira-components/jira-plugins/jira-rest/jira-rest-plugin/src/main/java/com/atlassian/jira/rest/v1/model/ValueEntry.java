package com.atlassian.jira.rest.v1.model;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

/**
 * Simple value label pair
 *
 * @since v4.0
 */
@XmlRootElement
public class ValueEntry
{
    @XmlElement
    private final String value;

    @XmlElement
    private final String label;

    @SuppressWarnings({"UnusedDeclaration", "unused"})
    private ValueEntry()
    {
        value = null;
        label = null;
    }

    public ValueEntry(final String value, final String label)
    {
        this.value = value;
        this.label = label;
    }

    public String getKey()
    {
        return value;
    }

    public String getValue()
    {
        return label;
    }
}
