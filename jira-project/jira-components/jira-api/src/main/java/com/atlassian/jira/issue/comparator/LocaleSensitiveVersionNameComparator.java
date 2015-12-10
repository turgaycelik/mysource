package com.atlassian.jira.issue.comparator;

import com.atlassian.jira.project.version.Version;

import java.util.Comparator;
import java.util.Locale;

/**
 * Comparator for Version objects that only compares the names, uses the constructed locale to correctly sort
 * constants names taking into account i18n strings.
 *
 * @since v4.0
 */
public class LocaleSensitiveVersionNameComparator implements Comparator<Version>
{
    private final LocaleSensitiveStringComparator stringComparator;

    public LocaleSensitiveVersionNameComparator(final Locale locale)
    {
        this.stringComparator = new LocaleSensitiveStringComparator(locale);
    }

    public int compare(final Version version1, final Version version2)
    {
        if (version1 == version2)
        {
            return 0;
        }

        if (version1 == null)
        {
            return 1;
        }

        if (version2 == null)
        {
            return -1;
        }

        String versionName1 = version1.getName();
        String versionName2 = version2.getName();

        return this.stringComparator.compare(versionName1, versionName2);
    }
}
