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
import com.atlassian.jira.issue.fields.rest.DescriptionRestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.DescriptionSearchHandlerFactory;
import com.atlassian.jira.issue.search.parameters.lucene.sort.StringSortComparator;
import com.atlassian.jira.issue.statistics.TextFieldSorter;
import com.atlassian.jira.mention.MentionService;
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
 * Represents the field which renders an {@link Issue} description.
 */
public class DescriptionSystemField extends AbstractTextSystemField implements HideableField, RequirableField,
        RestFieldOperations
{
    private static final String DESCRIPTION_NAME_KEY = "issue.field.description";
    private static final LuceneFieldSorter SORTER = new TextFieldSorter(DocumentConstants.ISSUE_SORT_DESC);
    private final RendererManager rendererManager;
    private final MentionService mentionService;
    private final TextFieldCharacterLengthValidator textFieldCharacterLengthValidator;

    public DescriptionSystemField(final VelocityTemplatingEngine templatingEngine, final ApplicationProperties applicationProperties,
            final JiraAuthenticationContext authenticationContext, final RendererManager rendererManager,
            final PermissionManager permissionManager, final DescriptionSearchHandlerFactory searchHandlerFactory,
            final MentionService mentionService, final TextFieldCharacterLengthValidator textFieldCharacterLengthValidator)
    {
        super(IssueFieldConstants.DESCRIPTION, DESCRIPTION_NAME_KEY, templatingEngine, applicationProperties,
                authenticationContext, rendererManager, permissionManager, searchHandlerFactory);
        this.rendererManager = rendererManager;
        this.mentionService = mentionService;
        this.textFieldCharacterLengthValidator = textFieldCharacterLengthValidator;
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo,
            I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        final Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        final String description = (String) fieldValuesHolder.get(getId());
        if (fieldScreenRenderLayoutItem.isRequired())
        {
            if (!TextUtils.stringSet(description) || description.trim().length() <= 0)
            {
                errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())));
            }
        }
        if (textFieldCharacterLengthValidator.isTextTooLong(description))
        {
            final long maximumNumberOfCharacters = textFieldCharacterLengthValidator.getMaximumNumberOfCharacters();
            errorCollectionToAddTo.addError(getId(), i18n.getText("field.error.text.toolong", maximumNumberOfCharacters));
        }
    }

    @Override
    protected void populateVelocityParams(Map fieldValuesHolder, Map params)
    {
        super.populateVelocityParams(fieldValuesHolder, params);
        params.put("mentionable", mentionService.isUserAbleToMention(authenticationContext.getLoggedInUser()));
    }

    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    public String getValueFromIssue(Issue issue)
    {
        return issue.getDescription();
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        if (fieldValueHolder.containsKey(getId()))
        {
            final String rendererType = (fieldLayoutItem != null) ? fieldLayoutItem.getRendererType() : null;
            // give the renderer a change to transform the incomming value
            final String desc = (String) rendererManager.getRendererForType(rendererType).transformFromEdit(getValueFromParams(fieldValueHolder));
            if (TextUtils.stringSet(desc))
                issue.setDescription(desc);
            else
                issue.setDescription(null);
        }
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setDescription(null);
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public String getColumnHeadingKey()
    {
        return "issue.column.heading.description";
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
        return Collections.singletonList(new SortField(DocumentConstants.ISSUE_SORT_DESC, new StringSortComparator(), sortOrder));
    }

    protected String getEditTemplateName()
    {
        return "description-edit.vm";
    }

    protected String getColumnViewTemplateName()
    {
        return "description-columnview.vm";
    }

    /////////////////////////////////////////// Bulk Edit //////////////////////////////////////////////////////////
    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        String rendererType = null;

        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        for (FieldLayout fieldLayout : bulkEditBean.getFieldLayouts())
        {
            if (fieldLayout.isFieldHidden(getId()))
            {
                return "bulk.edit.unavailable.hidden";
            }

            // Check for different renderer type
            if (rendererType == null)
            {
                // rendererType not set yet - set it to rendererType for this Field Layout.
                rendererType = fieldLayout.getRendererTypeForField(IssueFieldConstants.DESCRIPTION);
            }
            else if (!rendererType.equals(fieldLayout.getRendererTypeForField(IssueFieldConstants.DESCRIPTION)))
            {
                // We have found two different Renderer Types.
                return "bulk.edit.unavailable.different.renderers";
            }
        }

        // If we got here then the field is visible in all field layouts
        // So check for permissions
        // Need to check for EDIT permission here rather than in the BulkEdit itself, as a user does not need the EDIT permission to edit the ASSIGNEE field,
        // just the ASSIGNEE permission, so the permissions to check depend on the field
        // hAv eto loop through all the issues incase the permission has been granted to current assignee/reporter (i.e. assigned ot a role)
        for (Issue issue : bulkEditBean.getSelectedIssues())
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
        return new DescriptionRestFieldOperationsHandler(authenticationContext.getI18nHelper());
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        return null;
    }
}
