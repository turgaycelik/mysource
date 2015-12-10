package com.atlassian.jira.pageobjects.project;

import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import org.openqa.selenium.By;

import javax.inject.Inject;
import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.assertTrue;

/**
 * Represents the "Shared By" project dropdown shown within JIRA.
 *
 * @since v4.4
 */
public class ProjectSharedBy
{
    private PageElement container;

    @Inject
    private PageElementFinder elementFinder;

    public ProjectSharedBy(PageElement container)
    {
        this.container = container;
    }

    public boolean isPresent()
    {
        return container.isPresent();
    }

    public boolean isTiggerPresent()
    {
        return isPresent() && getTriggerElement().isPresent();
    }

    public String getTriggerText()
    {
        return getTriggerElement().getText();
    }

    public List<String> getProjects()
    {
        openDialog();
        
        String href = getTriggerElement().getAttribute("href");
        int triggerTargetStart = href.indexOf("#");

        String dialogId = "inline-dialog-" + href.substring(triggerTargetStart + 1);

        PageElement dialog = elementFinder.find(By.id(dialogId));
        assertTrue("The dialog did not appear to open.", dialog.isPresent());

        List<PageElement> elements = dialog.findAll(By.tagName("li"));
        List<String> projects = new ArrayList<String>(elements.size());
        for (PageElement element : elements)
        {
            projects.add(element.getText());
        }

        closeDialog();

        return projects;
    }

    private PageElement getTriggerElement()
    {
        return container.find(By.cssSelector(".shared-item-trigger"));
    }

    public void openDialog()
    {
        assertTrue("The trigger could not be found.", getTriggerElement().isPresent());
        getTriggerElement().click();
    }

    public void closeDialog()
    {
        container.click();
    }
}
