package com.atlassian.jira.pageobjects.pages.admin.customfields;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Optional;

/**
 * @since v6.1
 */
public class AssociateCustomFieldToScreenPage extends AbstractJiraPage
{
    private final Optional<Long> customFieldId;

    @ElementBy (id = "add-field-to-screen")
    private PageElement element;

    public AssociateCustomFieldToScreenPage(final long customFieldId)
    {
        this.customFieldId = Optional.of(customFieldId);
    }

    public AssociateCustomFieldToScreenPage()
    {
        this.customFieldId = Optional.absent();
    }

    @Override
    public TimedCondition isAt()
    {
        return element.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        if (!customFieldId.isPresent())
        {
            throw new IllegalStateException("No CustomFieldId passed when created.");
        }
        return String.format("/secure/admin/AssociateFieldToScreens!default.jspa?fieldId=customfield_%d", customFieldId.get());
    }
}
