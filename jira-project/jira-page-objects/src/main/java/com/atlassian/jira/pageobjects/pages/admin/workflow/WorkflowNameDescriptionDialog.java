package com.atlassian.jira.pageobjects.pages.admin.workflow;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.PageElement;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

/**
 * @since v5.1
 */
abstract class WorkflowNameDescriptionDialog<T extends WorkflowNameDescriptionDialog<T>> extends FormDialog
{
    public static final String FIELD_NAME = "newWorkflowName";
    public static final String FIELD_DESCRIPTION = "description";

    private final String submitId;

    private PageElement elementName;
    private PageElement elementDescription;
    private PageElement elementSubmit;

    WorkflowNameDescriptionDialog(String id, String submitId)
    {
        super(id);
        this.submitId = submitId;
    }

    public boolean canEditName()
    {
        return elementName.isPresent() && elementName.isEnabled();
    }

    @Init
    public void init()
    {
        elementName = find(By.name(FIELD_NAME));
        elementDescription = find(By.name(FIELD_DESCRIPTION));
        elementSubmit = find(By.id(submitId));
    }

    public T setName(String name)
    {
        assertDialogOpen();
        setElement(elementName, name);
        return getThis();
    }

    public T setDescription(String description)
    {
        assertDialogOpen();
        setElement(elementDescription, description);
        return getThis();
    }

    public String getName()
    {
        return StringUtils.trimToNull(elementName.getValue());
    }

    public String getDescription()
    {
        return StringUtils.trimToNull(elementDescription.getValue());
    }

    public T submitFail()
    {
        elementSubmit.click();
        waitWhileSubmitting();
        assertDialogOpen();
        return getThis();
    }

    public void submit()
    {
        elementSubmit.click();
        waitWhileSubmitting();
        assertDialogClosed();
    }

    abstract T getThis();
}
