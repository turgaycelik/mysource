package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.project.Project;

import java.util.Comparator;
import java.util.Locale;

/**
 * Comparator for Project objects that only compares the names, uses the constructed locale to correctly sort
 * constants names taking into account i18n strings.
 *
 * @since v4.0
 */
public class LocaleSensitiveProjectNameComparator implements Comparator<Project>
{
    private final LocaleSensitiveStringComparator stringComparator;

    public LocaleSensitiveProjectNameComparator(final Locale locale)
    {
        this.stringComparator = new LocaleSensitiveStringComparator(locale);
    }

    public int compare(final Project project1, final Project project2)
    {
        if (project1 == null && project2 == null)
        {
            return 0;
        }

        if (project1 == null)
        {
            return 1;
        }

        if (project2 == null)
        {
            return -1;
        }

        String projName1 = project1.getName();
        String projName2 = project2.getName();

        return this.stringComparator.compare(projName1, projName2);
    }
}
