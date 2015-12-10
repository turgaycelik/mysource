package com.atlassian.jira.plugin.viewissue.issuelink;

import com.atlassian.jira.issue.link.RemoteIssueLink;
import com.atlassian.jira.util.NaturalOrderStringComparator;
import org.apache.commons.collections.ComparatorUtils;
import org.apache.commons.lang.StringUtils;

import java.util.Comparator;

/**
 * Compares {@link RemoteIssueLink}.
 *
 * @since v5.0
 */
public class RemoteIssueLinkComparator implements Comparator<RemoteIssueLink>
{
    private static final String ATLASSIAN_APPLICATION_TYPE_PREFIX = "com.atlassian.";
    @SuppressWarnings("unchecked")
    private static final Comparator<Object> NULL_HIGH_COMPARATOR = ComparatorUtils.nullHighComparator(ComparatorUtils.NATURAL_COMPARATOR);

    private final String defaultRelationship;

    public RemoteIssueLinkComparator(String defaultRelationship)
    {
        this.defaultRelationship = defaultRelationship;
    }

    @Override
    public int compare(RemoteIssueLink o1, RemoteIssueLink o2)
    {
        int result = getRelationship(o1.getRelationship()).compareToIgnoreCase(getRelationship(o2.getRelationship()));
        if (result != 0)
        {
            return result;
        }

        if (RemoteIssueLink.APPLICATION_TYPE_JIRA.equals(o1.getApplicationType()) && !RemoteIssueLink.APPLICATION_TYPE_JIRA.equals(o2.getApplicationType()))
        {
            return -1;
        }
        else if (!RemoteIssueLink.APPLICATION_TYPE_JIRA.equals(o1.getApplicationType()) && RemoteIssueLink.APPLICATION_TYPE_JIRA.equals(o2.getApplicationType()))
        {
            return 1;
        }

        if (StringUtils.startsWith(o1.getApplicationType(), ATLASSIAN_APPLICATION_TYPE_PREFIX) && !StringUtils.startsWith(o2.getApplicationType(), ATLASSIAN_APPLICATION_TYPE_PREFIX))
        {
            return -1;
        }
        else if (!StringUtils.startsWith(o1.getApplicationType(), ATLASSIAN_APPLICATION_TYPE_PREFIX) && StringUtils.startsWith(o2.getApplicationType(), ATLASSIAN_APPLICATION_TYPE_PREFIX))
        {
            return 1;
        }

        result = NULL_HIGH_COMPARATOR.compare(o1.getApplicationType(), o2.getApplicationType());
        if (result != 0)
        {
            return result;
        }

        result = NULL_HIGH_COMPARATOR.compare(o1.getApplicationName(), o2.getApplicationName());
        if (result != 0)
        {
            return result;
        }

        String titleSummary1 = StringUtils.defaultString(o1.getTitle()) + " " + StringUtils.defaultString(o1.getSummary());
        String titleSummary2 = StringUtils.defaultString(o2.getTitle()) + " " + StringUtils.defaultString(o2.getSummary());
        if (!titleSummary1.equals(titleSummary2))
        {
            return NaturalOrderStringComparator.CASE_INSENSITIVE_ORDER.compare(titleSummary1, titleSummary2);
        }
        return 0;
    }

    /**
     * Returns the relationship as-is if not empty, otherwise return the default relationship.
     *
     * @param relationship relationship
     * @return relationship as-is if not empty, otherwise return the default relationship
     */
    private String getRelationship(String relationship)
    {
        return StringUtils.defaultIfEmpty(relationship, defaultRelationship);
    }
}
