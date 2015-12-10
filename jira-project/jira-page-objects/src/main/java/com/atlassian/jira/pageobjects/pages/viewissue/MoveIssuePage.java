package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.components.fields.SingleSelect;
import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.binder.Init;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.google.common.base.Function;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;
import org.openqa.selenium.By;

/**
 *
 * @since v5.0
 */
public class MoveIssuePage extends AbstractJiraPage
{
    private static final String URI = "/secure/MoveIssue!default.jspa";

    private final String issueKey;

    @ElementBy (id = "project-field")
    protected PageElement newProjectField;

    @ElementBy (cssSelector = "#project-single-select .drop-menu")
    protected PageElement newProjectDropMenuTrigger;

    @ElementBy (id = "next_submit")
    protected PageElement nextButton;

    @ElementBy (id = "issuetype")
    protected SelectElement issueTypeSelectDataHolder;

    protected SingleSelect issueTypeSelect;

    public MoveIssuePage(String issueKey)
    {
        this.issueKey = issueKey;
    }

    @Override
    public TimedCondition isAt()
    {
        return newProjectField.timed().isPresent();
    }

    @Override
    public String getUrl()
    {
        return URI + "?key=" + issueKey;
    }

    @Init
    public void initIssueTypeSelect()
    {
        this.issueTypeSelect = pageBinder.bind(SingleSelect.class, elementFinder.find(By.id("issuetype_container")));
    }

    public MoveIssuePage setNewProject(String newProject)
    {
        newProjectDropMenuTrigger.click();

        final SingleSelect singleSelect = pageBinder.bind(SingleSelect.class, elementFinder.find(By.id("project_container")));
        singleSelect.select(newProject);
        return this;
    }

    public Iterable<String> getIssueTypes()
    {
        return Iterables.transform(issueTypeSelectDataHolder.findAll(By.tagName("option")),new Function<PageElement, String>()
        {
            @Override
            public String apply(PageElement option)
            {
                // a bit hacky - can't use Option cause this is hidden on the page and WebDriver doesn't acknowledge existence of
                // hidden elements...
                return StringUtils.trim(option.getAttribute("innerHTML"));
            }
        });
    }

    public MoveIssueUpdateFields next()
    {
        nextButton.click();

        return pageBinder.bind(MoveIssueUpdateFields.class);
    }

    public MoveIssueUpdateStatus submitAndGoToSetNewIssueStatus(String issueID, String assignee)
    {
        nextButton.click();
        return pageBinder.bind(MoveIssueUpdateStatus.class, issueID, assignee);
    }

    public MoveIssuePage setNewIssueType(String newIssueType)
    {
        issueTypeSelect.select(newIssueType);
        return this;
    }
}
