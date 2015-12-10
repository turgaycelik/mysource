package com.atlassian.jira.issue.customfields;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;

import java.util.Set;

/**
 * Custom field which can have multiple Options to select from.
 * Provides support for removing a possible option from the Field configuration.
 *
 * @param <T> Transport Object See {@link CustomFieldType} for more information.
 * @param <S> Single Form of Transport Object. See {@link CustomFieldType} for more information.
 *
 */
@PublicSpi
public interface MultipleSettableCustomFieldType<T, S> extends MultipleCustomFieldType<T, S>
{
    /**
     * Returns a Set of issue ids ({@link Long}) that have the given option selected.
     *
     * @param field       the CustomField to search on
     * @param option the Object representing a single value to search on.
     * @return Set of Longs
     */
    public Set<Long> getIssueIdsWithValue(CustomField field, Option option);

    /**
     * Perform any actions required if the option selected by the issue is removed.
     * @param field being edited
     * @param issue to remove stuff from
     * @param optionObject option being removed.
     */
    public void removeValue(CustomField field, Issue issue, Option optionObject);

}
