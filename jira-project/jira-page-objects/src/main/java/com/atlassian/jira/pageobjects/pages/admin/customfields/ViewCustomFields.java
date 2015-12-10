package com.atlassian.jira.pageobjects.pages.admin.customfields;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;

/**
 * @since v6.1
 */
public class ViewCustomFields extends AbstractJiraPage
{
    @ElementBy(id = "add_custom_fields")
    private PageElement addElement;

    @Override
    public TimedCondition isAt()
    {
        return addElement.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/ViewCustomFields.jspa";
    }

    public TypeSelectionCustomFieldDialog addCustomField()
    {
        addElement.click();
        return pageBinder.bind(TypeSelectionCustomFieldDialog.class);
    }
}
