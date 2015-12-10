package com.atlassian.jira.issue.customfields;

import com.atlassian.jira.issue.fields.config.FieldConfig;
import javax.annotation.Nonnull;

/**
 * Allow a custom field to be natively sortable in the Issue Navigator.
 * <b>Warning:</b> This sort method is a fallback. Generally custom fields will have an associated {@link CustomFieldSearcher},
 * whose {@link SortableCustomFieldSearcher#getSorter(com.atlassian.jira.issue.fields.CustomField)} method is responsible for sorting. This interface's compare() is
 * only called if no searcher is associated with a custom field. It is an order of magnitude
 * slower than {@link com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher}.
 *
 * @see com.atlassian.jira.issue.customfields.SortableCustomFieldSearcher
 */
public interface SortableCustomField<T>
{
    /**
     * Compares the two custom field objects.
     * @param customFieldObjectValue1 Never null
     * @param customFieldObjectValue2 Never null
     * @param fieldConfig
     * @return 0, 1 or -1
     */
    int compare(@Nonnull T customFieldObjectValue1, @Nonnull T customFieldObjectValue2, FieldConfig fieldConfig);
}
