package com.atlassian.jira.issue.util;

import java.util.ArrayList;
import java.util.List;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.event.issue.IssueEventBundle;
import com.atlassian.jira.event.issue.IssueEventBundleFactory;
import com.atlassian.jira.event.issue.IssueEventManager;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.Txn;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;

import com.google.common.collect.ImmutableList;

import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

public class DefaultIssueUpdater implements IssueUpdater
{
    private final OfBizDelegator ofBizDelegator;
    private final IssueEventManager issueEventManager;
    private final IssueEventBundleFactory issueEventBundleFactory;

    public DefaultIssueUpdater(OfBizDelegator ofBizDelegator, IssueEventManager issueEventManager,
            final IssueEventBundleFactory issueEventBundleFactory)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.issueEventManager = issueEventManager;
        this.issueEventBundleFactory = issueEventBundleFactory;
    }

    public void doUpdate(IssueUpdateBean iub, boolean generateChangeItems)
    {
        iub.getChangedIssue().set(IssueFieldConstants.UPDATED, UtilDateTime.nowTimestamp());

        // We want to store the fields that have changed, not the whole issue. Unfortunately, IssueUpdateBean.getChangeItems() can be incomplete,
        // so we need to compare the before and after issues to compute the list of modified fields.
        List<ChangeItemBean> modifiedFields = new ArrayList<ChangeItemBean>();
        modifiedFields.addAll(ChangeLogUtils.generateChangeItems(iub.getOriginalIssue(), iub.getChangedIssue()));
        if (iub.getChangeItems() != null)
        {
            modifiedFields.addAll(iub.getChangeItems());
        }
        if (!(modifiedFields.isEmpty() && iub.getComment() == null))
        {
            storeModifiedFields(iub, generateChangeItems, modifiedFields);
        }
    }

    private void storeModifiedFields(final IssueUpdateBean iub, boolean generateChangeItems, List<ChangeItemBean> modifiedFields)
    {
        // Now construct an empty GenericValue and populate it with only the fields that have been modified
        final GenericValue updateIssueGV = new GenericValue(iub.getChangedIssue().getDelegator(), iub.getChangedIssue().getModelEntity());
        updateIssueGV.setPKFields(iub.getChangedIssue().getPrimaryKey().getAllFields());
        for (ChangeItemBean modifiedField : modifiedFields)
        {
            String fieldName = modifiedField.getField();
            if (IssueFieldConstants.ISSUE_TYPE.equals(fieldName))
            {
                // issuetype is the only field whose name inside the GenericValue is different (type in this case).
                // I haven't found a generic way to map external field names to GV internal names, hence this ugly if...
                fieldName = "type";
            }
            if (updateIssueGV.getModelEntity().isField(fieldName))
            {
                updateIssueGV.put(fieldName, iub.getChangedIssue().get(fieldName));
            }
        }
        // Also add the "update" field
        updateIssueGV.put(IssueFieldConstants.UPDATED, iub.getChangedIssue().get(IssueFieldConstants.UPDATED));

        final Transaction txn = Txn.begin();

        try
        {
            ofBizDelegator.storeAll(ImmutableList.of(updateIssueGV));

            final ApplicationUser user = iub.getApplicationUser();
            final GenericValue changeGroup = ChangeLogUtils.createChangeGroup(user,
                    iub.getOriginalIssue(), iub.getChangedIssue(),
                    iub.getChangeItems(), generateChangeItems);

            if (changeGroup != null && iub.getHistoryMetadata() != null)
            {
                ComponentAccessor.getComponent(HistoryMetadataManager.class)
                        .saveHistoryMetadata(changeGroup.getLong("id"), user, iub.getHistoryMetadata());
            }
            txn.commit();

            //only fire events if something has changed, comment on its own counts as special case
            if (changeGroup != null || iub.getComment() != null)
            {
                if (iub.isDispatchEvent())
                {
                    // Get the full newly updated issue from the database to make sure indexing is always accurate. (Status could be different)
                    final IssueFactory issueFactory = ComponentAccessor.getComponentOfType(IssueFactory.class);
                    final GenericValue updatedIssue = ofBizDelegator.findByPrimaryKey("Issue", updateIssueGV.getLong("id"));
                    Issue issue = issueFactory.getIssue(updatedIssue);
                    issueEventManager.dispatchRedundantEvent(iub.getEventTypeId(), issueFactory.getIssue(updatedIssue),
                            ApplicationUsers.toDirectoryUser(user), iub.getComment(), iub.getWorklog(), changeGroup, iub.getParams(),
                            iub.isSendMail(), iub.isSubtasksUpdated());
                    // Publish new events
                    IssueEventBundle issueEventBundle = issueEventBundleFactory.createIssueUpdateEventBundle(issue,
                            changeGroup, iub, user);
                    issueEventManager.dispatchEvent(issueEventBundle);
                }
            }
        }
        finally
        {
            txn.finallyRollbackIfNotCommitted();
        }
    }
}