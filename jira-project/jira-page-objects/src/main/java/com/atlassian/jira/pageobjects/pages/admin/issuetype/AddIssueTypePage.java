package com.atlassian.jira.pageobjects.pages.admin.issuetype;

import com.atlassian.jira.pageobjects.components.IconPicker;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.DelayedBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import java.util.Map;

/**
 * Represents the Add(New)?IssueTypePage.
 *
 * @since v5.1
 */
public class AddIssueTypePage extends AbstractJiraPage implements AddIssueType
{
    private AddIssueTypeForm form;
    private DelayedBinder<AddIssueTypeForm> delayedForm;
    
    @Init
    public void init()
    {
        form = getDeplayed().bind();
    }
    
    @Override
    public AddIssueTypePage setName(String name)
    {
        form.setName(name);
        return this;
    }

    @Override
    public AddIssueTypePage setDescription(String description)
    {
        form.setDescription(description);
        return this;
    }

    @Override
    public AddIssueTypePage setIconUrl(String iconUrl)
    {
        return this;
    }

    @Override
    public AddIssueTypePage setSubtask(boolean subtask)
    {
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
    @Deprecated
    public IconPicker.IconPickerPopup openIconPickerPopup()
    {
        return form.openIconPickerPopup();
    }

    @Override
    public <P> P submit(Class<P> klazz)
    {
        form.submit();
        return pageBinder.bind(klazz);
    }

    @Override
    public AddIssueTypePage submitFail()
    {
        form.submit();
        return this;
    }

    @Override
    public <P> P submitFail(Class<P> page, Object... args)
    {
        form.submit();
        return pageBinder.bind(page, args);
    }

    @Override
    public <P> P cancel(Class<P> page)
    {
        form.cancel();
        return pageBinder.bind(page);
    }

    @Override
    public TimedCondition isAt()
    {
        final AddIssueTypeForm typeForm = getDeplayed().get();
        return typeForm.isAt();
    }

    @Override
    public String getUrl()
    {
        return "/secure/admin/AddNewIssueType.jspa";
    }

    public Map<String, String> getFormErrors()
    {
        return form.getFormErrors();
    }

    private DelayedBinder<AddIssueTypeForm> getDeplayed()
    {
        if (delayedForm == null)
        {
            delayedForm = pageBinder.delayedBind(AddIssueTypeForm.class).inject();
        }
        return delayedForm;
    }
}
