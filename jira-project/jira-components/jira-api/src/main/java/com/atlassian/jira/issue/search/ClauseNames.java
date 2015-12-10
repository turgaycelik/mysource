package com.atlassian.jira.issue.search;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.util.dbc.Assertions;
import static com.atlassian.jira.util.dbc.Assertions.containsNoBlanks;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Arrays;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Set;
import java.util.TreeSet;

/**
 * Represents a JQL clause name. The primary name is the name which uniquely identifies the clause, the names
 * is a set of alternate names that the clause can be known as.
 *
 * @since v4.0
 */
@PublicApi
public final class ClauseNames
{
    private final String primaryName;
    private final Set<String> names;

    public ClauseNames(final String primaryName)
    {
        this(primaryName, Collections.<String>emptySet());
    }

    public ClauseNames(final String primaryName, final String... names)
    {
        this(primaryName, new LinkedHashSet<String>(Arrays.asList(Assertions.notNull("names", names))));
    }

    public ClauseNames(final String primaryName, final Set<String> names)
    {
        Set<String> newNames = new TreeSet<String>(String.CASE_INSENSITIVE_ORDER);
        newNames.addAll(containsNoBlanks("names", names));
        this.primaryName = Assertions.notBlank("primaryName", primaryName);
        // Always make sure the names contains the primary name as well
        newNames.add(this.primaryName);
        this.names = Collections.unmodifiableSet(newNames);
    }

    public Set<String> getJqlFieldNames()
    {
        return names;
    }

    public String getPrimaryName()
    {
        return primaryName;
    }

    public boolean contains(String name)
    {
        return names.contains(name);
    }

    public static ClauseNames forCustomField(CustomField field)
    {
        notNull("field", field);

        final String name = field.getUntranslatedName();
        if (!SystemSearchConstants.isSystemName(name) && !JqlCustomFieldId.isJqlCustomFieldId(name))
        {
            return new ClauseNames(JqlCustomFieldId.toString(field.getIdAsLong()), name);
        }
        else
        {
            return new ClauseNames(JqlCustomFieldId.toString(field.getIdAsLong()));
        }
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if (o == null || getClass() != o.getClass())
        {
            return false;
        }

        final ClauseNames names1 = (ClauseNames) o;

        if (names != null ? !names.equals(names1.names) : names1.names != null)
        {
            return false;
        }
        if (primaryName != null ? !primaryName.equals(names1.primaryName) : names1.primaryName != null)
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = primaryName != null ? primaryName.hashCode() : 0;
        result = 31 * result + (names != null ? names.hashCode() : 0);
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
