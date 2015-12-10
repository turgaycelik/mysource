/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.workflow.function.issue;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.changehistory.DefaultChangeHistoryManager;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataManager;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.history.ChangeLogUtils;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.workflow.WorkflowUtil;
import com.opensymphony.module.propertyset.PropertySet;
import com.opensymphony.workflow.FunctionProvider;
import com.opensymphony.workflow.loader.DescriptorFactory;
import com.opensymphony.workflow.loader.FunctionDescriptor;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.util.UtilDateTime;

import java.util.List;
import java.util.Map;

public class GenerateChangeHistoryFunction implements FunctionProvider
{
    private static final Logger log = Logger.getLogger(GenerateChangeHistoryFunction.class);

    public void execute(Map transientVars, Map args, PropertySet ps)
    {
        MutableIssue issue = (MutableIssue) transientVars.get("issue");
        IssueChangeHolder changeHolder = new DefaultIssueChangeHolder();
        if (transientVars.get("changeItems") != null)
        {
            changeHolder.setChangeItems((List) transientVars.get("changeItems"));
        }

        // Ensure the updated date is set
        issue.setUpdated(UtilDateTime.nowTimestamp());

        // Store the issue
        issue.store();

        Map<String, ModifiedValue> modifiedFields = issue.getModifiedFields();
        if (!modifiedFields.isEmpty())
        {
            // Maybe move this code to the issue.store() method
            FieldManager fieldManager = ComponentAccessor.getFieldManager();

            for (final String fieldId : modifiedFields.keySet())
            {
                if (fieldManager.isOrderableField(fieldId))
                {
                    OrderableField field = fieldManager.getOrderableField(fieldId);
                    FieldLayoutItem fieldLayoutItem = ComponentAccessor.getFieldLayoutManager().getFieldLayout(issue).getFieldLayoutItem(field);
                    field.updateValue(fieldLayoutItem, issue, modifiedFields.get(fieldId), changeHolder);
                }
            }
            // Reset the fields as they all have been persisted to the db. Maybe move this code to the "createValue"
            // method of the issue, so that the fields removes itself from the modified list as soon as it is persisted.
            issue.resetModifiedFields();
        }

        if (!changeHolder.getChangeItems().isEmpty())
        {
            final ApplicationUser user = WorkflowUtil.getCallerUser(transientVars);
            GenericValue changeGroup = ChangeLogUtils.createChangeGroup(user, issue.getGenericValue(), issue.getGenericValue(), changeHolder.getChangeItems(), false);
            transientVars.put("changeGroup", changeGroup);

            final Object historyMetadata = transientVars.get(DefaultChangeHistoryManager.HISTORY_METADATA_KEY);
            if (historyMetadata instanceof HistoryMetadata)
            {
                ComponentAccessor.getComponent(HistoryMetadataManager.class).saveHistoryMetadata(
                        changeGroup.getLong("id"),
                        user,
                        (HistoryMetadata) historyMetadata
                );
            }
        }
    }

    public static FunctionDescriptor makeDescriptor()
    {
        FunctionDescriptor descriptor = DescriptorFactory.getFactory().createFunctionDescriptor();
        descriptor.setType("class");
        descriptor.getArgs().put("class.name", GenerateChangeHistoryFunction.class.getName());
        return descriptor;
    }
}
