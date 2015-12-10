package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Option;
import com.atlassian.pageobjects.elements.Options;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.hamcrest.Matchers;

/**
 * @since v4.4
 */
public class SelectIssueTypeScreenScheme extends AbstractJiraPage
{
    private final long projectId;

    @ElementBy(id = "schemeId_select")
    private PageElement selectElement;

    @ElementBy(id = "schemeId_select")
    private SelectElement select;

    @ElementBy(id = "associate_submit")
    private PageElement submit;

    @ElementBy(name = "projectId")
    private PageElement projectIdElement;

    public SelectIssueTypeScreenScheme(long projectId)
    {
        this.projectId = projectId;
    }

    @Override
    public String getUrl()
    {
        return "secure/project/SelectIssueTypeScreenScheme!default.jspa?projectId=" + projectId;
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(selectElement.timed().isPresent(),
                Conditions.forMatcher(projectIdElement.timed().getValue(), Matchers.equalTo(String.valueOf(projectId))));
    }

    public Option getSelectedOption()
    {
        return select.getSelected();
    }

    public String getSelectedText()
    {
        return getSelectedOption().text();
    }

    public void setSchemeByName(String name)
    {
        select.select(Options.text(name));
    }

    public void submit()
    {
        submit.click();
    }
}
