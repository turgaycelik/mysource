package com.atlassian.jira.issue.fields;

import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.RendererManager;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.fields.layout.field.FieldLayout;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.EnvironmentRestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.EnvironmentSearchHandlerFactory;
import com.atlassian.jira.issue.search.parameters.lucene.sort.StringSortComparator;
import com.atlassian.jira.issue.statistics.TextFieldSorter;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
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
public class EnvironmentSystemField extends AbstractTextSystemField implements HideableField, RequirableField, RestFieldOperations
{
    private static final String ENVIRONMENT_NAME_KEY = "issue.field.environment";
    private final RendererManager rendererManager;
    private static final LuceneFieldSorter SORTER = new TextFieldSorter(DocumentConstants.ISSUE_ENV);
    private final TextFieldCharacterLengthValidator textFieldCharacterLengthValidator;
    
    public EnvironmentSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext, RendererManager rendererManager, PermissionManager permissionManager, EnvironmentSearchHandlerFactory searchHandlerFactory, TextFieldCharacterLengthValidator textFieldCharacterLengthValidator)
    {
        super(IssueFieldConstants.ENVIRONMENT, ENVIRONMENT_NAME_KEY, templatingEngine, applicationProperties, authenticationContext, rendererManager, permissionManager, searchHandlerFactory);
        this.rendererManager = rendererManager;
        this.textFieldCharacterLengthValidator = textFieldCharacterLengthValidator;
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    /**
     * validate the field value
     *
     * @param operationContext OperationContext
     * @param errorCollectionToAddTo ErrorCollection
     * @param i18n I18nHelper
     * @param issue Issue
     * @param fieldScreenRenderLayoutItem FieldScreenRenderLayoutItem
     */
    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        final Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        final String environment = (String) fieldValuesHolder.get(getId());
        if (fieldScreenRenderLayoutItem.isRequired())
        {
            if (!TextUtils.stringSet(environment) || environment.trim().length() <= 0)
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())));
            }
        }
        if (textFieldCharacterLengthValidator.isTextTooLong(environment))
        {
            final long maximumNumberOfCharacters = textFieldCharacterLengthValidator.getMaximumNumberOfCharacters();
            errorCollectionToAddTo.addError(getId(), i18n.getText("field.error.text.toolong", maximumNumberOfCharacters));
        }
    }

    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    public String getValueFromIssue(Issue issue)
    {
        return issue.getEnvironment();
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
            // give the renderer a change to transform the incomming value
            String env = (String)rendererManager.getRendererForType(rendererType).transformFromEdit(getValueFromParams(fieldValueHolder));
            if (TextUtils.stringSet(env))
                issue.setEnvironment(env);
            else
                issue.setEnvironment(null);
        }
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setEnvironment(null);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public String getColumnHeadingKey()
    {
        return "issue.column.heading.environment";
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
        return Collections.singletonList(new SortField(DocumentConstants.ISSUE_SORT_ENV, new StringSortComparator(), sortOrder));
    }

    protected String getEditTemplateName()
    {
        return "environment-edit.vm";
    }

    protected String getColumnViewTemplateName()
    {
        return "environment-columnview.vm";
    }

    @Override
    protected boolean isInvertCollapsedState()
    {
        return true;
    }

    /////////////////////////////////////////// Bulk Edit //////////////////////////////////////////////////////////
    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        String rendererType = null;

        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        // Also check for different render types in use
        for (final FieldLayout fieldLayout: bulkEditBean.getFieldLayouts())
        {
            if (fieldLayout.isFieldHidden(getId()))
            {
                return "bulk.edit.unavailable.hidden";
            }

            // Check for different renderer type
            if (rendererType == null)
            {
                // First FieldLayout - remember the rendererType
                rendererType = fieldLayout.getRendererTypeForField(IssueFieldConstants.ENVIRONMENT);
            }
            else if (!rendererType.equals(fieldLayout.getRendererTypeForField(IssueFieldConstants.ENVIRONMENT)))
            {
                return "bulk.edit.unavailable.different.renderers";
            }
        }

        // If we got here then the field is visible in all field layouts
        // So check for permissions
        // Need to check for RESOLVE permission here rather than in the BulkEdit itself, as a user does not need the EDIT permission to edit the ASSIGNEE field,
        // just the ASSIGNEE permission, so the permissions to check depend on the field
        // hAv eto loop through all the issues incase the permission has been granted to current assignee/reporter (i.e. assigned ot a role)
        for (final Issue issue : bulkEditBean.getSelectedIssues())
        {
            if (!hasBulkUpdatePermission(bulkEditBean, issue) || !isShown(issue))
            {
                return "bulk.edit.unavailable.permission";
            }
        }

        // This field is available for bulk-editing, return null (i.e no unavailable message)
        return null;
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        return new EnvironmentRestFieldOperationsHandler(authenticationContext.getI18nHelper());
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        return null;
    }
}
