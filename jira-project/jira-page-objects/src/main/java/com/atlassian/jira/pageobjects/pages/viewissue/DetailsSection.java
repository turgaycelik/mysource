package com.atlassian.jira.pageobjects.pages.viewissue;

import com.atlassian.jira.pageobjects.pages.viewissue.fields.ViewIssueField;
import com.atlassian.pageobjects.PageBinder;
import com.atlassian.pageobjects.elements.ElementBy;
import com.atlassian.pageobjects.elements.PageElement;
import com.atlassian.pageobjects.elements.PageElementFinder;
import com.atlassian.pageobjects.elements.timeout.Timeouts;
import org.openqa.selenium.By;

import javax.inject.Inject;

import static com.atlassian.jira.pageobjects.framework.fields.CustomFields.jiraCustomFieldId;

/**
 * @since v5.0
 */
public class DetailsSection
{
    @Inject protected PageBinder pageBinder;
    @Inject protected Timeouts timeouts;
    @Inject protected PageElementFinder elementFinder;

    @ElementBy (id = "type-val")
    protected PageElement issueType; // TODO should be moved to impl of ViewIssueField


    public String getIssueType()
    {
        return issueType.getText().trim();
    }


    public <F extends ViewIssueField<?,?>> F getLabelsSystemField(Class<F> type)
    {
        return pageBinder.bind(type, elementFinder.find(By.id("wrap-labels")));
    }


    public <F extends ViewIssueField<?,?>> F getLabelsCustomField(Class<F> type, int fieldId)
    {
        return pageBinder.bind(type, elementFinder.find(By.id("rowFor" + jiraCustomFieldId(fieldId))), fieldId);
    }
}