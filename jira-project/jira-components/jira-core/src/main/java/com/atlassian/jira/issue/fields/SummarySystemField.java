package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.SummaryRestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.SummarySearchHandlerFactory;
import com.atlassian.jira.issue.search.parameters.lucene.sort.StringSortComparator;
import com.atlassian.jira.issue.statistics.TextFieldSorter;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.web.bean.BulkEditBean;
import com.opensymphony.util.TextUtils;
import org.apache.lucene.search.SortField;

import java.util.Collections;
import java.util.List;
import java.util.Map;

/**
 * Copyright (c) 2002-2004
 * All rights reserved.
 */
public class SummarySystemField extends AbstractTextSystemField implements NavigableField, MandatoryField, SummaryField, RestFieldOperations
{
    private static final String SUMMARY_NAME_KEY = "issue.field.summary";
    private static final LuceneFieldSorter SORTER = new TextFieldSorter(DocumentConstants.ISSUE_SORT_SUMMARY);

    public SummarySystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
            RendererManager rendererManager, PermissionManager permissionManager, SummarySearchHandlerFactory searchHandlerFactory)
    {
        super(IssueFieldConstants.SUMMARY, SUMMARY_NAME_KEY, templatingEngine, applicationProperties, authenticationContext, rendererManager, permissionManager, searchHandlerFactory);
    }

    protected String getEditTemplateName()
    {
        return "summary-edit.vm";
    }

    protected String getColumnViewTemplateName()
    {
        return "summary-columnview.vm";
    }

    protected void populateVelocityParams(Map fieldValuesHolder, Map params)
    {
        super.populateVelocityParams(fieldValuesHolder, params);
    }

    protected void populateVelocityParams(FieldLayoutItem fieldLayoutItem, Issue issue, Map<String, Object> params)
    {
        super.populateVelocityParams(fieldLayoutItem, issue, params);
        if (issue.isSubTask())
        {
            params.put("subTask", Boolean.TRUE);
            Issue parentIssue = issue.getParentObject();
            // do they have permission to see the parent issue, if not just show the key and not the summary
            params.put("parentIssueKey", parentIssue.getKey());
            params.put("subTaskParentIssueLinkDisabled", Boolean.TRUE);
            if (getPermissionManager().hasPermission(Permissions.BROWSE, parentIssue, getAuthenticationContext().getUser()))
            {
                params.put("parentIssueSummary", parentIssue.getSummary());
                params.put("subTaskParentIssueLinkDisabled", Boolean.FALSE);
            }
        }
    }

    public String getValueFromIssue(Issue issue)
    {
        return issue.getSummary();
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        issue.setSummary((String) getValueFromParams(fieldValueHolder));
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setSummary(null);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public void populateFromIssue(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        fieldValuesHolder.put(getId(), issue.getSummary());
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    /**
     * validate the field value
     *
     * @param operationContext            OperationContext
     * @param errorCollectionToAddTo      ErrorCollection
     * @param i18n                        I18nHelper
     * @param issue                       Issue
     * @param fieldScreenRenderLayoutItem FieldScreenRenderLayoutItem
     */
    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        String summary = (String) getValueFromParams(fieldValuesHolder);
        if (summary == null)
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.error.specify.a.summary"));
            return;
        }
        //JRADEV-1867 User can create summary with "  "
        summary = summary.trim();
        if (!TextUtils.stringSet(summary))
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.error.specify.a.summary"));
        }
        if (summary.contains("\n") || summary.contains("\r"))
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.invalidsummary.newlines"));
        }
        if (summary.length() > MAX_LEN)
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("createissue.error.summary.less.than", MAX_LEN.toString()));
        }
    }

    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    public String getColumnHeadingKey()
    {
        return "issue.column.heading.summary";
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_ASCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return SORTER;
    }

    @Override
    public List<SortField> getSortFields(final boolean sortOrder)
    {
        return Collections.singletonList(new SortField(DocumentConstants.ISSUE_SORT_SUMMARY, new StringSortComparator(), sortOrder));
    }

    public boolean isRenderable()
    {
        return false;
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        return "bulk.edit.unavailable";
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        return new SummaryRestFieldOperationsHandler(authenticationContext.getI18nHelper());
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        return null;
    }
}
