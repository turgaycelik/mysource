package com.atlassian.jira.web.action.filter;

/**
 * Implemented by actions that wish to display and customise filter operations.
 *
 * @since v4.0
 */
public interface FilterOperationsAction
{
    /**
     * Return the filter operations beans that describes the operations that should be displayed for this action.
     *
     * @return the filter operation bean for the actions.
     */
    FilterOperationsBean getFilterOperationsBean();

    /**
     * Tells if the filter state is valid and can bee saved or run. If there is no filter relevant to the action,
     * return true.
     *
     * @return false only if there is a filter that is invalid; no relevant filter results in true.
     */
    boolean isFilterValid();
}
