package com.atlassian.jira.pageobjects.pages.admin.subtask;

import com.atlassian.jira.pageobjects.components.IconPicker;
import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;

import java.util.Map;

/**
 * Allows the user to interact with the add subtask type dialog.
 *
 * @since v5.0.1
 */
public class AddSubtaskTypeDialog extends FormDialog implements AddSubtaskType
{
    public static final String ID = "add-subtask-type-dialog";
    private AddSubtaskTypeForm form;

    public AddSubtaskTypeDialog()
    {
        super(ID);
    }

    @Init
    public void init()
    {
        form = binder.bind(AddSubtaskTypeForm.class);
    }

    @Override
    public AddSubtaskTypeDialog setName(String name)
    {
        assertDialogOpen();
        form.setName(name);
        return this;
    }

    @Override
    public AddSubtaskTypeDialog setDescription(String description)
    {
        assertDialogOpen();
        form.setDescription(description);
        return this;
    }
    
    @Override
    public AddSubtaskTypeDialog setIconUrl(String iconUrl)
    {
        return this;
    }

    @Override
    public String getIconUrl()
    {
        return form.getIconUrl();
    }

    @Override
    public IconPicker.IconPickerPopup openIconPickerPopup()
    {
        return form.openIconPickerPopup();
    }

    @Override
    public ManageSubtasksPage submitSuccess()
    {
        return submit(ManageSubtasksPage.class);
    }

    @Override
    public AddSubtaskTypeDialog submitFail()
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
    public <P> P submit(Class<P> klazz)
    {
        form.submit();
        waitWhileSubmitting();
        assertDialogClosed();
        return binder.bind(klazz);
    }

    @Override
    public Map<String, String> getErrors()
    {
        return form.getErrors();
    }

    @Override
    public ManageSubtasksPage cancel()
    {
        form.cancel();
        return binder.bind(ManageSubtasksPage.class);
    }
}
