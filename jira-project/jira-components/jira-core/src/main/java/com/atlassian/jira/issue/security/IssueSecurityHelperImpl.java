package com.atlassian.jira.issue.security;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;

/**
 * Implementation of IssueSecurityHelper
 *
 * @since v3.13
 * @see IssueSecurityHelper
 */
public class IssueSecurityHelperImpl implements IssueSecurityHelper
{
    private final FieldLayoutManager fieldLayoutManager;

    public IssueSecurityHelperImpl(final FieldLayoutManager fieldLayoutManager)
    {
        this.fieldLayoutManager = fieldLayoutManager;
    }

    public boolean securityLevelNeedsMove(final Issue sourceIssue, final Issue targetIssue)
    {
        final FieldLayout layout = fieldLayoutManager.getFieldLayout(sourceIssue);
        //Don't have to do an explicit check for isEnterprise() here, as the FieldLayoutItem will
        //be null anyways in non-enterprise editions.
        final FieldLayoutItem fieldLayoutItem = layout.getFieldLayoutItem(IssueFieldConstants.SECURITY);
        if (fieldLayoutItem == null)
        {
            return false;
        }
        final OrderableField orderableField = fieldLayoutItem.getOrderableField();
        return orderableField.needsMove(EasyList.build(sourceIssue), targetIssue, fieldLayoutItem).getResult();
    }
}
