package com.atlassian.jira.pageobjects.pages.setup;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * Page object implementation for the database setup page in JIRA, the first step
 * in the JIRA setup.
 *
 * @since 4.4
 */
public class DatabaseSetupPage extends AbstractJiraPage
{
    private static final String URI = "/secure/SetupDatabase!default.jspa";

    @ElementBy (id = "jira-setupwizard-database-internal")
    private PageElement internalDbOption;

    @ElementBy (id = "jira-setupwizard-database-internal")
    private PageElement externalDbOption;

    @ElementBy (id = "jira-setupwizard-submit")
    private PageElement submitButton;

    public String getUrl()
    {
        return URI;
    }

    @Override
    public TimedCondition isAt()
    {
        return internalDbOption.timed().isPresent();
    }

    public DatabaseSetupPage setInternalDb()
    {
        internalDbOption.click();
        return this;
    }

    public DatabaseSetupPage setExternalDb()
    {
        externalDbOption.select();
        return this;
    }

    /**
     * Submit this page selecting internal DB.
     *
     * @return next page in the setup process
     */
    public ApplicationSetupPage submitInternalDb()
    {
        setInternalDb();
        return submit();
    }

    public ApplicationSetupPage submit()
    {
        submitButton.click();
        return pageBinder.bind(ApplicationSetupPage.class);
    }

}
