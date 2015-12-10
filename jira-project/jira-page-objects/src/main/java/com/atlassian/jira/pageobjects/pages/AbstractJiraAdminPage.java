package com.atlassian.jira.pageobjects.pages;

/**
 * Abstract administration page with reference to the administration menu that provides access to all other admin pages.
 *
 * @since 4.4
 */
public abstract class AbstractJiraAdminPage extends AbstractJiraPage
{
    /**
     * ID of the link in the admin menu.
     *
     * @return ID of the link to this page
     */
    public abstract String linkId();

}
