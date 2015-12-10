package com.atlassian.jira.pageobjects.pages.admin.issuetype;

import com.atlassian.jira.pageobjects.pages.AbstractJiraPage;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.query.TimedCondition;
import com.atlassian.webdriver.utils.by.ByJquery;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Represents the Change Issue Type Scheme page
 *
 * @since v4.4
 */
public class ChangeIssueTypeSchemePage extends AbstractJiraPage
{
    private static final String URI_TEMPLATE = "/secure/project/SelectIssueTypeSchemeForProject!default.jspa?projectId=%s";
    private String uri;

    @ElementBy(id = "choose-section")
    private PageElement chooseSection;

    @ElementBy(id = "createType_chooseScheme")
    private PageElement chooseSchemeRadioOption;

    @ElementBy(id = "schemeId_select")
    private PageElement schemeSelect;

    @ElementBy(id = "ok_submit")
    private PageElement submit;


    public ChangeIssueTypeSchemePage()
    {
    }

    @Override
    public TimedCondition isAt()
    {
        return chooseSection.timed().isPresent();
    }


    public ChangeIssueTypeSchemePage(final String projectId)
    {
        notNull(projectId);
        this.uri = String.format(URI_TEMPLATE, projectId);
    }

    public String getUrl()
    {
        return uri;
    }

    /**
     * Chooses an existing issue type scheme to associate with the currently selected project
     * @param scheme The new issue type scheme name
     */
    public void chooseExistingIssueTypeScheme(final String scheme)
    {
        chooseSchemeRadioOption.select();
        schemeSelect.find(ByJquery.$("option:contains(\"" + scheme + "\")")).select();
        submit.click();
    }

}
