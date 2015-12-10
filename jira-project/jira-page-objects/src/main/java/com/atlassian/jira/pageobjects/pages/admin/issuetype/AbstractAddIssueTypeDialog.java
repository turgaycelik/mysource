package com.atlassian.jira.pageobjects.pages.admin.issuetype;

import com.atlassian.jira.pageobjects.components.IconPicker;
import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;

/**
 * @since v5.0.1
 */
abstract class AbstractAddIssueTypeDialog extends FormDialog implements AddIssueType
{
    private AddIssueTypeForm form;

    AbstractAddIssueTypeDialog(String id)
    {
        super(id);
    }

    @Init
    public void init()
    {
        form = binder.bind(AddIssueTypeForm.class);
    }

    @Override
    public AddIssueType setName(String name)
    {
        assertDialogOpen();
        form.setName(name);
        return this;
    }

    @Override
    public AddIssueType setIconUrl(String iconUrl)
    {
        return this;
    }

    @Override
    public AddIssueType setDescription(String description)
    {
        assertDialogOpen();
        form.setDescription(description);
        return this;
    }

    @Override
    public AddIssueType setSubtask(boolean subtask)
    {
        assertDialogOpen();
        form.setSubtask(subtask);
        return this;
    }

    @Override
    public String getIconUrl()
    {
        return form.getIconUrl();
    }

    @Override
    public boolean isSubtasksEnabled()
    {
        return form.isSubtasksEnabled();
    }

    @Override
    public IconPicker.IconPickerPopup openIconPickerPopup()
    {
        return form.openIconPickerPopup();
    }

    @Override
    public AddIssueType submitFail()
    {
        form.submit();
        waitWhileSubmitting();
        assertDialogOpen();
        return this;
    }

    @Override
    public <P> P submitFail(Class<P> page, Object... args)
    {
        form.submit();
        waitWhileSubmitting();
        assertDialogOpen();
        return binder.bind(page, args);
    }

    @Override
    public <P> P cancel(Class<P> page)
    {
        close();
        return binder.bind(page);
    }

    @Override
    public <T> T submit(Class<T> klazz)
    {
        form.submit();
        waitWhileSubmitting();
        assertDialogClosed();
        return binder.bind(klazz);
    }
}
