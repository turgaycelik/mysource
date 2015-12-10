package com.atlassian.jira.pageobjects.dialogs.quickedit;

import com.atlassian.jira.pageobjects.dialogs.FormDialog;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.Poller;
import com.google.inject.Inject;
import org.openqa.selenium.By;

/**
 * Author: Geoffrey Wong
 * Dialog which displays when triggering a workflow transition for a JIRA issue
 */
public class WorkflowTransitionDialog extends FormDialog
{


    @Inject
    PageElementFinder pageElementFinder;
    
    @ElementBy (id = "issue-workflow-transition-submit")
    PageElement submitWorkflowTransitionButton;

    @javax.inject.Inject
    PageBinder pageBinder;

    @Init
    public void init()
    {
        Poller.waitUntilTrue(submitWorkflowTransitionButton.timed().isPresent());
    }

    public WorkflowTransitionDialog(String transitionId)
    {
        super("workflow-transition-" + transitionId + "-dialog");
    }

    public WorkflowTransitionDialog(long transitionId)
    {
        this(Long.toString(transitionId));
    }

    public <T> T getCustomField(Class<T> fieldTypeClass, String fullCustomFieldId)
    {
        return pageBinder.bind(fieldTypeClass, getDialogElement(), fullCustomFieldId);
    }

    public <T> T getCustomField(Class<T> fieldTypeClass, long customFieldId)
    {
        return getCustomField(fieldTypeClass, "customfield_" + customFieldId);
    }

    public boolean submitWorkflowTransition()
    {
        return submit(submitWorkflowTransitionButton);
    }
    
    public String getWorkflowTransitionButtonUiName()
    {
        return submitWorkflowTransitionButton.getValue();
    }

    public WorkflowTransitionDialog setResolution(String resolution)
    {
        SelectElement resolutionSelectList = pageElementFinder.find(By.id("resolution"), SelectElement.class);
        resolutionSelectList.select(Options.text(resolution));
        return this;
    }
}
