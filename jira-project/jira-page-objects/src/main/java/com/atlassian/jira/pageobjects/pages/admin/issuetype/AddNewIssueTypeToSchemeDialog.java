package com.atlassian.jira.pageobjects.pages.admin.issuetype;

/**
 * Represents the Add Issue Type dialog on the Manage Issue Types Scheme page.
 *
 * @since v5.0.1
 */
public class AddNewIssueTypeToSchemeDialog extends AbstractAddIssueTypeDialog
{
    public static final String ID = "add-new-issue-type-to-scheme-dialog";

    public AddNewIssueTypeToSchemeDialog()
    {
        super(ID);
    }

    public EditIssueTypeSchemePage submit()
    {
        return submit(EditIssueTypeSchemePage.class);
    }

    public EditIssueTypeSchemePage cancel()
    {
        return cancel(EditIssueTypeSchemePage.class);
    }
}
