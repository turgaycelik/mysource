package com.atlassian.jira.rest.v1.model;

import com.atlassian.jira.util.dbc.Assertions;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Map;

/**
 * A simple collection of {@link com.atlassian.jira.rest.v1.model.ValueEntry}s.
 *
 * @since v4.0
 */
@XmlRootElement
public class ValueCollection
{
    @XmlElement
    private Collection<ValueEntry> values;

    @SuppressWarnings({"UnusedDeclaration", "unused"})
    private ValueCollection() {}

    public ValueCollection(final Map<String, String> values)
    {
        Assertions.notNull("values", values);

        this.values = convertToValueEntryCollection(values);
    }

    public Collection<ValueEntry> getValues()
    {
        return values;
    }

    private Collection<ValueEntry> convertToValueEntryCollection(final Map<String, String> values)
    {
        final Collection<ValueEntry> entries = new ArrayList<ValueEntry>();
        for (Map.Entry<String, String> mapEntry : values.entrySet())
        {
            entries.add(new ValueEntry(mapEntry.getKey(), mapEntry.getValue()));
        }
        return entries;
    }
}
