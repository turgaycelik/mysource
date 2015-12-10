package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.util.LuceneUtils;

import java.util.Comparator;

/**
 * @since v4.4
 */
public class LocalDateFieldSorter implements LuceneFieldSorter<LocalDate>
{
    private static final Comparator<LocalDate> SIMPLE_COMPARATOR = new SimpleLocalDateComparator();
    private final String documentConstant;

    public LocalDateFieldSorter(String documentConstant)
    {
        this.documentConstant = documentConstant;
    }

    @Override
    public String getDocumentConstant()
    {
        return documentConstant;
    }

    @Override
    public LocalDate getValueFromLuceneField(String documentValue)
    {
        return LuceneUtils.stringToLocalDate(documentValue);
    }

    @Override
    public Comparator<LocalDate> getComparator()
    {
        return SIMPLE_COMPARATOR;
    }

    private static class SimpleLocalDateComparator implements Comparator<LocalDate>
    {
        public int compare(final LocalDate o1, final LocalDate o2)
        {
            return o1.compareTo(o2);
        }
    }
}
