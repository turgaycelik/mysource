package com.atlassian.jira.pageobjects.pages.admin.screen;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.binder.Init;

import java.util.List;

/**
 * @since v5.0.2
 */
public class AssociateIssueOperationToScreenDialog extends FormDialog implements AssociateIssueOperationToScreen
{
    public static final String ID = "add-screen-scheme-item-dialog";

    private AssociateIssueOperationToScreenForm form;

    public AssociateIssueOperationToScreenDialog()
    {
        super(ID);
    }

    @Init
    public void init()
    {
        form = binder.bind(AssociateIssueOperationToScreenForm.class);
    }

    @Override
    public List<String> getScreens()
    {
        return form.getScreens();
    }

    @Override
    public String getScreen()
    {
        return form.getScreen();
    }

    @Override
    public AssociateIssueOperationToScreenDialog setScreen(String name)
    {
        form.setScreen(name);
        return this;
    }

    @Override
    public List<ScreenOperation> getOperations()
    {
        return form.getOperations();
    }

    @Override
    public ScreenOperation getSelectedOperation()
    {
        return form.getSelectedOperation();
    }

    @Override
    public AssociateIssueOperationToScreenDialog setOperation(ScreenOperation name)
    {
        form.setOperation(name);
        return this;
    }

    @Override
    public AssociateIssueOperationToScreenDialog submitFail()
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
    public <P> P cancel(Class<P> page, Object...args)
    {
        close();
        return binder.bind(page, args);
    }

    @Override
    public <T> T submit(Class<T> page, Object... args)
    {
        form.submit();
        waitWhileSubmitting();
        assertDialogClosed();
        return binder.bind(page, args);
    }
}
