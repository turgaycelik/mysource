package com.atlassian.jira.bc.project.component;

import com.atlassian.jira.issue.comparator.LocaleSensitiveStringComparator;

import java.util.Comparator;
import java.util.Locale;

/**
 * Comparator for ProjectComponent objects that only compares the names, does not take into account the project id
 * in the components, uses the constructed locale to correctly sort component names taking
 * into account i18n strings.
 *
 * @since v4.0
 */
public class LocaleSensitiveProjectComponentNameComparator implements Comparator<ProjectComponent>
{
    private final LocaleSensitiveStringComparator stringComparator;

    public LocaleSensitiveProjectComponentNameComparator(final Locale locale)
    {
        this.stringComparator = new LocaleSensitiveStringComparator(locale);
    }

    public int compare(final ProjectComponent projectComponent1, final ProjectComponent projectComponent2)
    {
        if (projectComponent1 == null && projectComponent2 == null)
        {
            return 0;
        }

        if (projectComponent1 == null)
        {
            return 1;
        }

        if (projectComponent2 == null)
        {
            return -1;
        }

        String componentName1 = projectComponent1.getName();
        String componentName2 = projectComponent2.getName();

        return this.stringComparator.compare(componentName1, componentName2);
    }
}
