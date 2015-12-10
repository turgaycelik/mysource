package com.atlassian.jira.pageobjects.pages;

import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Tab component in JIRA.
 *
 * @since v4.4
 */
public interface Tab
{
    /**
     * ID of the link to the tab
     *
     * @return ID of the link to open the tab
     */
    String linkId();

    /**
     * </p>
     * Timed condition querying whether the tab is open.
     *
     * @return timed condition checking whether the tab is open
     */
    TimedCondition isOpen();
}
