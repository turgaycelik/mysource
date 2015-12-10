package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.dialogs.quickedit.CreateIssueDialog;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.binder.WaitUntil;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static com.atlassian.pageobjects.elements.query.Poller.waitUntilFalse;
import static com.atlassian.pageobjects.elements.query.Poller.waitUntilTrue;

/**
 * Subtask module of view issue page
 *
 * @since v5.0
 */
public class SubtaskModule
{

    @ElementBy (id = "view-subtasks")
    private PageElement module;

    @ElementBy (id = "subtask-view-options")
    protected PageElement optionsDropDown;

    @Inject
    private PageElementFinder pageElementFinder;

    @Inject
    private PageBinder binder;


    @WaitUntil
    final public void ready()
    {
        waitUntilTrue(module.timed().isPresent());
        waitUntilFalse(module.timed().hasClass("updating"));
    }

    public List<Subtask> getSubtasks()
    {

        List<Subtask> subtasks = new ArrayList<Subtask>();

        final List<PageElement> issues = module.findAll(By.className("issuerow"));
        for (PageElement issue : issues)
        {
            subtasks.add(binder.bind(Subtask.class, issue));
        }

        return subtasks;
    }

    public CreateIssueDialog openCreateSubtaskDialog()
    {
        module.find(By.className("issueaction-create-subtask")).click();
        return binder.bind(CreateIssueDialog.class, CreateIssueDialog.Type.SUBTASK);
    }

    public void showAll()
    {
        module.find(By.className("drop-menu")).click();
        waitUntilTrue(optionsDropDown.timed().isPresent());
        optionsDropDown.find(By.id("subtasks-show-all")).click();
    }

    public void showOpen()
    {
        module.find(By.className("drop-menu")).click();
        waitUntilTrue(optionsDropDown.timed().isPresent());
        optionsDropDown.find(By.id("subtasks-show-open")).click();
    }

}
