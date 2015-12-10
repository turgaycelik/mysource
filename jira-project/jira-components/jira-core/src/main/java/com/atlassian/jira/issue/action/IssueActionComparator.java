package com.atlassian.jira.issue.action;

import com.atlassian.jira.issue.comparator.NullComparator;
import com.atlassian.jira.plugin.issuetabpanel.IssueAction;

import java.util.Comparator;
import java.util.Date;

public class IssueActionComparator implements Comparator<Object>
{
    private final NullComparator nullComparator = new NullComparator();
    public static final Comparator<Object> COMPARATOR = new IssueActionComparator();

    public int compare(Object o1, Object o2)
    {
        if (o1 instanceof IssueAction && o2 == null)
            return 1;

        if (o1 == null && o2 instanceof IssueAction)
            return -1;

        if (o1 instanceof IssueAction && o2 instanceof IssueAction)
        {
            IssueAction a1 = (IssueAction) o1;
            IssueAction a2 = (IssueAction) o2;

            //first see if the objects implement comparable themselves
            int returnValue = nullComparator.compare(a1, a2);
            if (returnValue != 0)
                return returnValue;
            else
            {
                Date timePerformed1 = getIssueActionTimePerformed(a1);
                Date timePerformed2 = getIssueActionTimePerformed(a2);

                // Ensure compareTo method does not die with null timestamps - JRA-7710
                return this.nullComparator.compare(timePerformed1, timePerformed2);
            }
        }
        else
        {
            throw new IllegalArgumentException("Can only compare with '" + IssueAction.class.getName() + "'.");
        }
    }

    /**
     * Creates and returns new Date object representing the time action was performed.
     * Some actions (GenericMessageAction) may not implement getTimePerformed and throw
     * {@link UnsupportedOperationException}. In such case a null is returned.
     * @param issueAction issue action
     * @return new Date object representing the time action was performed
     */
    private Date getIssueActionTimePerformed(IssueAction issueAction)
    {
        if (issueAction == null)
            return null;

        try
        {
            Date date = issueAction.getTimePerformed();
            return date == null ? null : new Date(date.getTime());
        }
        catch (UnsupportedOperationException usoe)
        {
            return null;
        }
    }
}