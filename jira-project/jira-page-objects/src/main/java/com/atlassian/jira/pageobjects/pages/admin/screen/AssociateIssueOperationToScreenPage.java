package com.atlassian.jira.pageobjects.pages.admin.screen;

import com.atlassian.pageobjects.Page;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.binder.ValidateState;

import javax.inject.Inject;
import java.util.List;

import static java.lang.String.format;

/**
 * @since v5.0.2
 */
public class AssociateIssueOperationToScreenPage implements Page, AssociateIssueOperationToScreen
{
    private final long screenSchemeId;

    @Inject
    private PageBinder pageBinder;
    private AssociateIssueOperationToScreenForm form;

    public AssociateIssueOperationToScreenPage(long screenSchemeId) 
    {
        this.screenSchemeId = screenSchemeId;
    }

    @ValidateState
    private void validateForm()
    {
        // workaround for issue in atl-selenium (canBind does not call @Init)
        pageBinder.bind(AssociateIssueOperationToScreenForm.class);
    }

    @Init
    public void init()
    {
        form = pageBinder.bind(AssociateIssueOperationToScreenForm.class);
    }

    @Override
    public List<String> getScreens() {return form.getScreens();}

    @Override
    public String getScreen() {return form.getScreen();}

    @Override
    public AssociateIssueOperationToScreenPage setScreen(String name) 
    {
        form.setScreen(name);
        return this;
    }

    @Override
    public List<ScreenOperation> getOperations() {return form.getOperations();}

    @Override
    public ScreenOperation getSelectedOperation() {return form.getSelectedOperation();}

    @Override
    public AssociateIssueOperationToScreenPage setOperation(ScreenOperation name) 
    {
        form.setOperation(name);
        return this;
    }

    @Override
    public AssociateIssueOperationToScreen submitFail()
    {
        form.submit();
        return pageBinder.bind(AssociateIssueOperationToScreenPage.class, screenSchemeId);
    }

    @Override
    public <P> P submitFail(Class<P> page, Object... args)
    {
        form.submit();
        return pageBinder.bind(page, args);
    }

    @Override
    public <P> P cancel(Class<P> page, Object...args)
    {
        form.cancel();
        return pageBinder.bind(page,args);
    }

    @Override
    public <P> P submit(Class<P> page, Object... args)
    {
        form.submit();
        return pageBinder.bind(page, args);
    }

    @Override
    public String getUrl()
    {
        return format("/secure/admin/AddFieldScreenSchemeItem!input.jspa?id=%d", screenSchemeId);
    }
}
