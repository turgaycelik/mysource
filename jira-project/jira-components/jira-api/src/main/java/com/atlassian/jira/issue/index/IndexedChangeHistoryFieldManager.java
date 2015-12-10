package com.atlassian.jira.issue.index;

import com.atlassian.annotations.PublicApi;
import com.atlassian.query.operator.Operator;

import java.util.Collection;
import java.util.Set;

/**
 * allows you to add and remove fields that will be indexed in the change history index
 *
 * @since v4.3
 */
@PublicApi
public interface IndexedChangeHistoryFieldManager
{
    /**
     *
     * @return a collection that contains all of the {@link IndexedChangeHistoryField}
     * that will be indexed
     */
    Collection<IndexedChangeHistoryField> getIndexedChangeHistoryFields();

    /**
     *
     * @param field  A {@link IndexedChangeHistoryField} that describes a field you want to
     * add to the index
     */
    void addIndexedChangeHistoryField(IndexedChangeHistoryField field);

    /**
     *
     * @param field A {@link IndexedChangeHistoryField} that describes a field you want to
     * delete from being indesed.
     */
    void deleteIndexedChangeHistoryField(IndexedChangeHistoryField field);

    /**
     *
     * @return a collection that contains all of the field names that will be indexed.
     *
     */
    Collection<String> getIndexedChangeHistoryFieldNames();

    Set<Operator> getSupportedOperators (String fieldName, Set<Operator> supportedOperators);
}
