package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.issue.search.LuceneFieldSorter;

import java.util.Comparator;

public class TextFieldSorter implements LuceneFieldSorter<String>
{
    private static final Comparator<String> STRING_COMPARATOR = new Comparator<String>()
    {
        public int compare(final String o1, final String o2)
        {
            return o1.compareTo(o2);
        }
    };

    private final String documentConstant;

    public TextFieldSorter(final String documentConstant)
    {
        this.documentConstant = documentConstant;
    }

    public String getDocumentConstant()
    {
        return documentConstant;
    }

    public String getValueFromLuceneField(final String documentValue)
    {
        return documentValue;
    }

    public Comparator<String> getComparator()
    {
        return STRING_COMPARATOR;
    }

    @Override
    public boolean equals(final Object o)
    {
        if (this == o)
        {
            return true;
        }
        if ((o == null) || (getClass() != o.getClass()))
        {
            return false;
        }

        final TextFieldSorter that = (TextFieldSorter) o;

        return (documentConstant != null ? documentConstant.equals(that.documentConstant) : that.documentConstant == null);

    }

    @Override
    public int hashCode()
    {
        return (documentConstant != null ? documentConstant.hashCode() : 0);
    }
}
