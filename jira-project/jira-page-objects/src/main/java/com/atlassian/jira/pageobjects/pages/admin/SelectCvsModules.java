package com.atlassian.jira.pageobjects.pages.admin;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.MultiSelectElement;
import com.atlassian.pageobjects.elements.Option;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.Conditions;
import com.atlassian.pageobjects.elements.query.TimedCondition;

import java.util.ArrayList;
import java.util.List;

/**
 * Represents the CVS module page.
 *
 * @since v4.4
 */
public class SelectCvsModules extends AbstractJiraPage
{
    @ElementBy(id="cvs-no-modules")
    private PageElement createLink;

    @ElementBy(name="multipleRepositoryIds")
    private PageElement multiSelectElement;

    @ElementBy(name="multipleRepositoryIds")
    private MultiSelectElement selectElement;

    private long pid;

    public SelectCvsModules(long pid)
    {
        this.pid = pid;
    }

    public List<Option> getSelectedOptions()
    {
        return selectElement.getSelected();
    }

    public List<String> getSelectedNames()
    {
        List<String> names = new ArrayList<String>();
        for (final Option option : getSelectedOptions())
        {
            names.add(option.text());
        }
        return names;
    }

    @Override
    public String getUrl()
    {
        return "secure/project/EnterpriseSelectProjectRepository!default.jspa?projectId=" + pid;
    }

    @Override
    public TimedCondition isAt()
    {
        return Conditions.or(createLink.timed().isPresent(), multiSelectElement.timed().isPresent());
    }
}
