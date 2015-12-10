package com.atlassian.jira.pageobjects.pages.admin.customfields;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.apache.commons.lang.StringUtils;

/**
 * @since v6.1
 */
public class AddCustomFieldPage extends AbstractJiraPage
{
    @ElementBy(id="customfield-details")
    private PageElement form;

    @ElementBy(id="customfield-type")
    private PageElement customFieldType;

    @Override
    public TimedCondition isAt()
    {
        return form.timed().isPresent();
    }

    public String getCustomFieldType()
    {
        return StringUtils.stripToNull(customFieldType.getText());
    }

    @Override
    public String getUrl()
    {
        throw new UnsupportedOperationException("Need to be directed to this page.");
    }
}
