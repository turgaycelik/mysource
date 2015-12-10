/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.fields;

import com.atlassian.annotations.Internal;
import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.util.I18nHelper;
import org.apache.lucene.search.FieldComparatorSource;
import org.apache.lucene.search.SortField;

import java.util.List;
import java.util.Map;

/**
 * Fields in JIRA which are able to be placed in the Issue Navigator implement this interface.
 */
@PublicApi
public interface NavigableField extends Field
{
    String TEMPLATE_DIRECTORY_PATH = OrderableField.TEMPLATE_DIRECTORY_PATH;

    public final static String ORDER_ASCENDING = "ASC";
    public final static String ORDER_DESCENDING = "DESC";

    public String getColumnHeadingKey();
    
    public String getColumnCssClass();

    /**
     * The order in which to sort the field when it is sorted for the first time.
     *
     * @return  Either {@link #ORDER_ASCENDING} or {@link #ORDER_DESCENDING}
     */
    public String getDefaultSortOrder(); //used by issuetable.vm

    /**
     * A sortComparatorSource object to be used for sorting columns in a table.  In most cases this will use a
     * {@link com.atlassian.jira.issue.search.parameters.lucene.sort.MappedSortComparator} using the {@link #getSorter()}
     * method.  However, fields can provide any sorting mechanism that they wish.
     *
     * If a field can be sorted directly using terms in the Lucene index then the field should implement {@link #getSortFields(boolean sortOrder)}
     * rather than this method.
     *
     * In large installations custom sorting may incur a maor performance penalty.
     *
     * @return  A SortComparatorSource that can be used to sort, or null if this field does not use custom sorting
     */
    public FieldComparatorSource getSortComparatorSource();

    /**
     * Return a list of Lucene SortFields to be used for sorting search results.
     *
     * Using this method allows the field to specify the most performant way to perform a search.  If a field can be
     * sorted directly using the term in the index then this should just return a singleton list with the sort field.
     * <p>
     * {@code return Collections.singletonList(new SortField(fieldName, sortOrder));  }<br>
     * </p>
     *
     * The default implementation builds this using the FieldComparatorSource returned by {@link #getSortComparatorSource()}
     *
     * If you implement this method there is no need to implement {@link #getSortComparatorSource()}.
     *
     * @return  The name of the indexed term to be used for native Lucene sorting.
     */
    @Internal
    public List<SortField> getSortFields(boolean sortOrder);

    /**
     * A sorter to be used when sorting columns in a table.  This sort uses the Lucene Document Collection
     * and is therefore a lot faster than sorting the issues in memory.
     *
     * @return  A sorter that can be used to sort this field, or null depending on the value of {@link #getSortComparatorSource()}
     * @see com.atlassian.jira.issue.DocumentIssueImpl
     * @see com.atlassian.jira.issue.search.parameters.lucene.sort.MappedSortComparator 
     */
    public LuceneFieldSorter getSorter();

    public String getColumnViewHtml(FieldLayoutItem fieldLayoutItem, Map displayParams, Issue issue);

    /**
     * Returns the id of the field to check for visibility. For example, with original estimate field
     * need to ensure that the timetracking field is not hidden. With most fields, this is the same as their
     * id.
     */
    public String getHiddenFieldId();

    public String prettyPrintChangeHistory(String changeHistory);

    /**
     * Used for email notification templates - allows changelog to be displayed in language of the recipient.
     * @param changeHistory
     * @return String   change history formatted according to locale in i18nHelper
     */
    public String prettyPrintChangeHistory(String changeHistory, I18nHelper i18nHelper);
}
