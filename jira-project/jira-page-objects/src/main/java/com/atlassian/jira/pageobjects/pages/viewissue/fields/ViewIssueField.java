package com.atlassian.jira.pageobjects.pages.viewissue.fields;

import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.pageobjects.elements.query.TimedQuery;

/**
 * <p/>
 * Represents any field on the view issue page (the details section).
 *
 * <p/>
 * Implementing page object are expected to have a construtor taking a {@link com.atlassian.pageobjects.elements.PageElement}
 * instance that represents system field's HTML on the view issue page. For custom fields, the constructor should also
 * accept an int param representing the field's ID
 *
 * @param <V> type of the field's value
 * @param <E> type of the field's edit page object
 *
 * @since v5.2
 */
public interface ViewIssueField<V,E>
{
    TimedQuery<V> getValue();

    TimedCondition hasValue();

    /**
     * Triggers edit for the field from bview issue (if applicable) and returns the page object responsible for
     * editing. That object should be initialized and ready to use.
     *
     * @return edit page object
     */
    E edit();
}
