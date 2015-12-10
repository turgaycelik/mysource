package com.atlassian.jira.plugin.report;

import org.apache.commons.lang.StringUtils;

import java.text.Collator;
import java.util.Comparator;
import java.util.Locale;

import javax.annotation.Nullable;

/**
 * Comparators for ReportModuleDescriptor.
 *
 * @since 6.0.7
 */
public class ReportModuleDescriptorComparators
{
    /**
     * Returns a comparator that can be used to sort {@code ReportModuleDescriptor} instances by name (case insensitive)
     * using the default locale.
     *
     * @return a Comparator
     * @since 6.0.7
     */
    public static Comparator<ReportModuleDescriptor> byName()
    {
        return byName(null);
    }

    /**
     * Returns a comparator that can be used to sort {@code ReportModuleDescriptor} instances by name (case insensitive)
     * using the given locale.
     *
     * @param locale a Locale to use for sorting, or null to use the default Locale
     * @return a Comparator
     * @since 6.0.7
     */
    public static Comparator<ReportModuleDescriptor> byName(@Nullable Locale locale)
    {
        return new ByNameComparator(locale != null ? locale : Locale.getDefault());
    }

    private ReportModuleDescriptorComparators()
    {
        // prevent instantiation
    }

    /**
     * Compares ReportModuleDescriptor by user-visible name (i.e. label).
     */
    static class ByNameComparator implements Comparator<ReportModuleDescriptor>
    {
        private final Collator collator;

        public ByNameComparator(Locale locale)
        {
            this.collator = Collator.getInstance(locale);
        }

        @Override
        public int compare(ReportModuleDescriptor r1, ReportModuleDescriptor r2)
        {
            String label1 = StringUtils.defaultString(r1.getLabel());
            String label2 = StringUtils.defaultString(r2.getLabel());

            return collator.compare(label1, label2);
        }
    }
}
