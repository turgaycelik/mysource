package com.atlassian.jira.functest.framework.admin;

/**
 * Represents the Send Bulk Mail Page available in the administration section.
 *
 * @since v4.4
 */
public interface SendBulkMail
{
    /**
     * Navigates to the Send Bulk Mail Page.
     * @return this instance of the Send Bulk Mail Page.
     */
    SendBulkMail goTo();
}
