package com.atlassian.jira.pageobjects.pages.admin.fields.configuration.schemes;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * @since v6.2
 */
public class DeleteFieldConfigurationSchemePage extends AbstractJiraPage
{
    @ElementBy(id = "delete_submit")
    PageElement submit;

    public ViewFieldConfigurationSchemesPage submitSuccess()
    {
        submit.click();
        return pageBinder.bind(ViewFieldConfigurationSchemesPage.class);
    }

    @Override
    public TimedCondition isAt()
    {
        return submit.timed().isVisible();
    }

    @Override
    public String getUrl()
    {
        return null;
    }
}
