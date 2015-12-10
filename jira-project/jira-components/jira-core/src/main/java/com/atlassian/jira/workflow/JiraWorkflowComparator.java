package com.atlassian.jira.workflow;

import java.util.Comparator;

/**
 * A Comparator for the JiraWorkflow class.
 */
public class JiraWorkflowComparator implements Comparator<JiraWorkflow>
{
    public static final Comparator<JiraWorkflow> COMPARATOR = new JiraWorkflowComparator();

    /**
     * The natural order of JiraWorkflow objects should be:
     * 1) All instances of DefaultJiraWorkflow (sorted alphabetically ignoring case); followed by
     * 2) all other instances of JiraWorkflow (also sorted alphabetically ignoring case).
     */
    public int compare(final JiraWorkflow workflow1, final JiraWorkflow workflow2)
    {
        if ((workflow1 instanceof DefaultJiraWorkflow) && !(workflow2 instanceof DefaultJiraWorkflow))
        {
            return -1;
        }

        if ((workflow2 instanceof DefaultJiraWorkflow) && !(workflow1 instanceof DefaultJiraWorkflow))
        {
            return 1;
        }

        final String name1 = (workflow1).getName();
        final String name2 = (workflow2).getName();

        if (name1 == null)
        {
            if (name2 == null)
            {
                return 0;
            }
            else
            {
                return -1;
            }
        }

        if (name2 == null)
        {
            return 1;
        }

        return name1.compareToIgnoreCase(name2);
    }

}
