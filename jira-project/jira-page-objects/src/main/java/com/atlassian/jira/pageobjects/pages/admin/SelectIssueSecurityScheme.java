package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.Option;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.SelectElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import org.hamcrest.Matchers;

/**
 * @since v4.4
 */
public class SelectIssueSecurityScheme extends AbstractJiraPage
{
    @ElementBy(id = "newSchemeId_select")
    private PageElement selectElement;

    @ElementBy(id = "newSchemeId_select")
    private SelectElement select;

    @ElementBy(id = "projectId")
    private PageElement projectElement;

    private long projectId;

    public SelectIssueSecurityScheme(long projectId)
    {
        this.projectId = projectId;
    }

    @Override
    public String getUrl()
    {
        return "secure/project/SelectProjectIssueSecurityScheme!default.jspa?projectId=" + projectId;
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.and(selectElement.timed().isPresent(),
                        Conditions.forMatcher(projectElement.timed().getValue(), Matchers.equalTo(String.valueOf(projectId))));
    }

    public boolean isNone()
    {
        return "-1".equalsIgnoreCase(getSelectedOption().value());
    }

    public Option getSelectedOption()
    {
        return select.getSelected();
    }

    public String getSelectedText()
    {
        return getSelectedOption().text();
    }
}
