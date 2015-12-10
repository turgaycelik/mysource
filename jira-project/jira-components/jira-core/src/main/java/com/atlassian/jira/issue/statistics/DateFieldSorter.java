package com.atlassian.jira.issue.statistics;

import com.atlassian.jira.datetime.LocalDate;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.util.LuceneUtils;

import java.util.Comparator;
import java.util.Date;

public class DateFieldSorter implements LuceneFieldSorter<Date>
{
    public static final LuceneFieldSorter<Date> ISSUE_CREATED_STATSMAPPER = new DateFieldSorter(DocumentConstants.ISSUE_CREATED);
    public static final LuceneFieldSorter<Date> ISSUE_UPDATED_STATSMAPPER = new DateFieldSorter(DocumentConstants.ISSUE_UPDATED);
    public static final LuceneFieldSorter<LocalDate> ISSUE_DUEDATE_STATSMAPPER = new LocalDateFieldSorter(DocumentConstants.ISSUE_DUEDATE);
    public static final LuceneFieldSorter<Date> ISSUE_RESOLUTION_DATE_STATSMAPPER = new DateFieldSorter(DocumentConstants.ISSUE_RESOLUTION_DATE);
    public static final LuceneFieldSorter<Date> ISSUE_LAST_VIEWED_DATE_STATSMAPPER = new DateFieldSorter(DocumentConstants.ISSUE_LAST_VIEWED_DATE);

    private static final Comparator<Date> SIMPLE_COMPARATOR = new SimpleDateComparator();
    private final String documentConstant;

    public DateFieldSorter(final String documentConstant)
    {
        this.documentConstant = documentConstant;
    }

    public String getDocumentConstant()
    {
        return documentConstant;
    }

    public Date getValueFromLuceneField(final String documentValue)
    {
        if (documentValue == null)
        {
            return null;
        }
        else
        {
            return LuceneUtils.stringToDate(documentValue);
        }
    }

    public Comparator<Date> getComparator()
    {
        return SIMPLE_COMPARATOR;
    }

    private static class SimpleDateComparator implements Comparator<Date>
    {
        public int compare(final Date o1, final Date o2)
        {
            return o1.compareTo(o2);
        }
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

        final DateFieldSorter that = (DateFieldSorter) o;

        return (documentConstant != null ? documentConstant.equals(that.documentConstant) : that.documentConstant == null);
    }

    @Override
    public int hashCode()
    {
        return (documentConstant != null ? documentConstant.hashCode() : 0);
    }
}
