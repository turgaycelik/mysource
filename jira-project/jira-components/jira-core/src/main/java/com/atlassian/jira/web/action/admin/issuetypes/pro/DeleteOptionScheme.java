package com.atlassian.jira.web.action.admin.issuetypes.pro;

import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.bulkedit.operation.BulkMoveOperation;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.fields.option.OptionSetManager;
import com.atlassian.jira.issue.search.SearchProvider;
import com.atlassian.jira.security.xsrf.RequiresXsrfCheck;
import com.atlassian.jira.web.action.admin.issuetypes.AbstractManageIssueTypeOptionsAction;
import com.atlassian.jira.web.action.admin.issuetypes.IssueTypeManageableOption;
import com.atlassian.jira.web.action.admin.issuetypes.events.IssueTypeSchemeDeletedThroughActionEvent;
import com.atlassian.sal.api.websudo.WebSudoRequired;

@WebSudoRequired
public class DeleteOptionScheme extends AbstractManageIssueTypeOptionsAction
{
    private final EventPublisher eventPublisher;

    // ------------------------------------------------------------------------------------------------------- Constants
    // ------------------------------------------------------------------------------------------------- Type Properties
    // ---------------------------------------------------------------------------------------------------- Dependencies
    // ---------------------------------------------------------------------------------------------------- Constructors
    public DeleteOptionScheme(FieldConfigSchemeManager configSchemeManager, IssueTypeSchemeManager issueTypeSchemeManager,
            FieldManager fieldManager, OptionSetManager optionSetManager, IssueTypeManageableOption manageableOptionType,
            BulkMoveOperation bulkMoveOperation, SearchProvider searchProvider, IssueManager issueManager,
            EventPublisher eventPublisher)
    {
        super(configSchemeManager, issueTypeSchemeManager, fieldManager, optionSetManager, manageableOptionType,
                bulkMoveOperation, searchProvider, issueManager);
        this.eventPublisher = eventPublisher;
    }

    // -------------------------------------------------------------------------------------------------- Action Methods
    public String doDefault() throws Exception
    {
        return super.doDefault();
    }

    @RequiresXsrfCheck
    protected String doExecute() throws Exception
    {
        FieldConfigScheme configScheme = getConfigScheme();

        // Remove them all!
        issueTypeSchemeManager.deleteScheme(configScheme);
        eventPublisher.publish(new IssueTypeSchemeDeletedThroughActionEvent());
        return getRedirect("ManageIssueTypeSchemes!default.jspa");
    }

    // -------------------------------------------------------------------------------------------------- Helper Methods
}
