package com.atlassian.jira.pageobjects.pages.admin.subtask;

import com.atlassian.jira.pageobjects.components.IconPicker;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import java.util.Map;

/**
 * Allows the caller to interact with the add subtask type page.
 *
 * @since v5.0.1
 */
public class AddSubtaskTypePage extends AbstractJiraPage implements AddSubtaskType
{
    private AddSubtaskTypeForm form;
    private DelayedBinder<AddSubtaskTypeForm> delayedForm;

    @Init
    public void init()
    {
        form = getDeplayed().bind();
    }

    @Override
    public AddSubtaskTypePage setName(String name)
    {
        form.setName(name);
        return this;
    }

    @Override
    public AddSubtaskTypePage setDescription(String description)
    {
        form.setDescription(description);
        return this;
    }

    @Override
    public AddSubtaskTypePage setIconUrl(String iconUrl)
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
        form.submit();
        return pageBinder.bind(ManageSubtasksPage.class);
    }

    @Override
    public AddSubtaskTypePage submitFail()
    {
        form.submit();
        return pageBinder.bind(AddSubtaskTypePage.class);
    }

    @Override
    public <P> P submitFail(Class<P> page, Object... args)
    {
        form.submit();
        return pageBinder.bind(page, args);
    }

    @Override
    public <P> P submit(Class<P> klazz)
    {
        form.submit();
        return pageBinder.bind(klazz);
    }

    @Override
    public TimedCondition isAt()
    {
        return getDeplayed().get().isAt();
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/subtasks/AddNewSubTaskIssueType.jspa";
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
        return pageBinder.bind(ManageSubtasksPage.class);
    }

    private DelayedBinder<AddSubtaskTypeForm> getDeplayed()
    {
        if (delayedForm == null)
        {
            delayedForm = pageBinder.delayedBind(AddSubtaskTypeForm.class).inject();
        }
        return delayedForm;
    }
}
