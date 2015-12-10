package com.atlassian.jira.issue.fields;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.OperationContext;
import com.atlassian.jira.issue.customfields.impl.FieldValidationException;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.LabelsRestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.RestAwareField;
import com.atlassian.jira.issue.fields.rest.RestFieldOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.screen.FieldScreenRenderLayoutItem;
import com.atlassian.jira.issue.fields.util.MessagedResult;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.issue.label.LabelUtil;
import com.atlassian.jira.issue.search.LuceneFieldSorter;
import com.atlassian.jira.issue.search.handlers.LabelsSearchHandlerFactory;
import com.atlassian.jira.issue.statistics.TextFieldSorter;
import com.atlassian.jira.issue.util.IssueChangeHolder;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.template.VelocityTemplatingEngine;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.web.bean.BulkEditBean;
import org.apache.commons.lang.StringUtils;
import webwork.action.Action;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * A field implementation to render Labels.
 *
 * @since 4.2
 */
public class LabelsSystemField extends AbstractOrderableNavigableFieldImpl implements HideableField, RequirableField, LabelsField, RestAwareField, RestFieldOperations
{

    private static final LuceneFieldSorter SORTER = new TextFieldSorter(DocumentConstants.ISSUE_LABELS);
    private final LabelManager labelManager;
    private final IssueManager issueManager;
    private final JiraBaseUrls jiraBaseUrls;
    private LabelUtil labelUtil;

    public LabelsSystemField(VelocityTemplatingEngine templatingEngine, ApplicationProperties applicationProperties, JiraAuthenticationContext authenticationContext,
            PermissionManager permissionManager, LabelsSearchHandlerFactory searchHandlerFactory, final LabelManager labelManager, final IssueManager issueManager, JiraBaseUrls jiraBaseUrls)
    {
        super(IssueFieldConstants.LABELS, LABELS_NAME_KEY, templatingEngine, applicationProperties, authenticationContext, permissionManager, searchHandlerFactory);
        this.labelManager = labelManager;
        this.issueManager = issueManager;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    protected LabelUtil getLabelUtil()
    {
        if (labelUtil == null)
        {
            labelUtil = ComponentAccessor.getComponentOfType(LabelUtil.class);
        }

        return labelUtil;
    }

    public String getCreateHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map displayParameters)
    {
        return getEditHtml(fieldLayoutItem, operationContext, action, issue, displayParameters);
    }

    public String getEditHtml(final FieldLayoutItem fieldLayoutItem, final OperationContext operationContext, final Action action, final Issue issue, final Map displayParameters)
    {
        Map<String, Object> velocityParams = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        final Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        if (fieldValuesHolder.containsKey(getId()))
        {
            velocityParams.put("labels", fieldValuesHolder.get(getId()));
        }
        else if (!(issue == null || issue.getId() == null))
        {
            // Values to edit weren't passed in the action, e.g., bulk edit.
            velocityParams.put("labels", labelManager.getLabels(issue.getId()));
        }
        return renderTemplate("labels-edit.vm", velocityParams);
    }

    public String getViewHtml(FieldLayoutItem fieldLayoutItem, Action action, Issue issue, Map displayParameters)
    {
        final Map<String, Object> velocityParameters = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);
        velocityParameters.put("labels", issue.getLabels());

        velocityParameters.put("canEdit", Boolean.FALSE);

        velocityParameters.put("fieldId", "label");
        velocityParameters.put("i18n", authenticationContext.getI18nHelper());
        velocityParameters.put("labelUtil", getLabelUtil());
        return renderTemplate("labels-view.vm", velocityParameters);
    }

    public String getViewHtml(final FieldLayoutItem fieldLayoutItem, final Action action, final Issue issue, final Object value, final Map displayParameters)
    {
        final Map<String, Object> velocityParameters = getVelocityParams(fieldLayoutItem, action, issue, displayParameters);

        velocityParameters.put("canEdit", Boolean.FALSE);

        Set<Label> labels = (value instanceof Set ? (Set<Label>)value : CollectionBuilder.<Label>newBuilder(new Label(null, issue.getId(), (String)value)).asHashSet() );
        velocityParameters.put("labels", labels);

        velocityParameters.put("fieldId", "label");
        velocityParameters.put("i18n", authenticationContext.getI18nHelper());
        velocityParameters.put("labelUtil", getLabelUtil());

        return renderTemplate("labels-view.vm", velocityParameters);
    }

    public Object getValueFromParams(final Map params)
    {
        return params.get(getId());
    }

    public void populateParamsFromString(final Map<String, Object> fieldValuesHolder, final String stringValue, final Issue issue)
            throws FieldValidationException
    {
        final String[] labels = StringUtils.split(stringValue, SEPARATOR_CHAR);
        if(labels != null)
        {
            final Set<Label> labelSet = new LinkedHashSet<Label>();
            for (String label : labels)
            {
                labelSet.add(new Label(null, issue.getId(), label));
            }
            fieldValuesHolder.put(getId(), labelSet);
        }
    }

    public void updateIssue(FieldLayoutItem fieldLayoutItem, MutableIssue issue, Map fieldValueHolder)
    {
        @SuppressWarnings("unchecked")
        final Set<Label> labels = (Set<Label>) getValueFromParams(fieldValueHolder);
        issue.setLabels(labels);
    }

    public void removeValueFromIssueObject(MutableIssue issue)
    {
        issue.setLabels(Collections.<Label>emptySet());
    }

    public boolean canRemoveValueFromIssueObject(Issue issue)
    {
        return true;
    }

    public MessagedResult needsMove(final Collection originalIssues, final Issue targetIssue, final FieldLayoutItem targetFieldLayoutItem)
    {
        //check if the target layout requires the field. If it does and the original issue doesn't have any labels, we
        //need to ask the user to provide labels!
        for (Issue originalIssue : (Collection<Issue>) originalIssues)
        {
            if(originalIssue.getLabels().isEmpty() && targetFieldLayoutItem.isRequired())
            {
                return new MessagedResult(true);
            }
        }
        return new MessagedResult(false);
    }

    public void populateForMove(final Map<String, Object> fieldValuesHolder, final Issue originalIssue, final Issue targetIssue)
    {
        fieldValuesHolder.put(getId(), originalIssue.getLabels());
    }

    public void populateFromIssue(Map<String, Object> fieldValuesHolder, Issue issue)
    {
        fieldValuesHolder.put(getId(), issue.getLabels());
    }

    public boolean hasValue(final Issue issue)
    {
        return issue.getLabels().size() > 0;
    }

    public boolean isShown(Issue issue)
    {
        return true;
    }

    public void populateDefaults(final Map<String, Object> fieldValuesHolder, final Issue issue)
    {
        fieldValuesHolder.put(getId(), Collections.<Label>emptySet());
    }

    public void validateParams(OperationContext operationContext, ErrorCollection errorCollectionToAddTo, I18nHelper i18n, Issue issue, FieldScreenRenderLayoutItem fieldScreenRenderLayoutItem)
    {
        final Map fieldValuesHolder = operationContext.getFieldValuesHolder();
        @SuppressWarnings("unchecked")
        final Set<Label> labels = (Set<Label>) fieldValuesHolder.get(getId());
        if(labels != null && !labels.isEmpty())
        {
            for (Label theLabel : labels)
            {
                final String label = theLabel.getLabel().trim();
                if (!LabelParser.isValidLabelName(label))
                {
                    errorCollectionToAddTo.addError(IssueFieldConstants.LABELS, i18n.getText("label.service.error.label.invalid", label));
                    break;
                }
                if (label.length() > LabelParser.MAX_LABEL_LENGTH)
                {
                    errorCollectionToAddTo.addError(IssueFieldConstants.LABELS, i18n.getText("label.service.error.label.toolong", label));
                    break;
                }
            }
        }
        else if (fieldScreenRenderLayoutItem.isRequired())
        {
            errorCollectionToAddTo.addError(getId(), i18n.getText("issue.field.required", i18n.getText(getNameKey())));
        }
    }

    public Object getDefaultValue(Issue issue)
    {
        return null;
    }

    public void createValue(final Issue issue, final Object value)
    {
        String labelString = (String) value;
        final Set<String> labels = getLabelSet(labelString);

        //validation should have already happened by now. No need to check the result
        labelManager.setLabels(authenticationContext.getLoggedInUser(), issue.getId(), labels, false, false);
    }

    public void updateValue(final FieldLayoutItem fieldLayoutItem, final Issue issue, final ModifiedValue modifiedValue,
            final IssueChangeHolder issueChangeHolder)
    {
        final String oldLabelString = (String) modifiedValue.getOldValue();
        final String labelString = (String) modifiedValue.getNewValue();
        final Set<String> oldLabels = getLabelSet(oldLabelString);
        final Set<String> labels = getLabelSet(labelString);

        if (valueChanged(oldLabels, labels))
        {
            final Set<Label> newLabels = labelManager.setLabels(authenticationContext.getLoggedInUser(), issue.getId(), labels, false, false);
            issueChangeHolder.addChangeItem(new ChangeItemBean(ChangeItemBean.STATIC_FIELD, IssueFieldConstants.LABELS,
                    null, oldLabelString, null, StringUtils.join(newLabels, SEPARATOR_CHAR)));
        }

    }

    private boolean valueChanged(final Set<String> oldLabels, final Set<String> labels)
    {
        return !oldLabels.equals(labels);
    }

    private Set<String> getLabelSet(final String labelString)
    {
        final Set<String> labels = new LinkedHashSet<String>();
        final String[] labelArray = StringUtils.split(labelString, SEPARATOR_CHAR);
        if(labelArray != null)
        {
            for (String label : labelArray)
            {
                labels.add(label.trim());
            }
        }
        return labels;
    }

    public String getColumnHeadingKey()
    {
        return "issue.column.heading.labels";
    }

    public String getDefaultSortOrder()
    {
        return NavigableField.ORDER_ASCENDING;
    }

    public LuceneFieldSorter getSorter()
    {
        return SORTER;
    }

    public String getColumnViewHtml(final FieldLayoutItem fieldLayoutItem, final Map displayParams, final Issue issue)
    {
        final Map<String, Object> velocityParams = getVelocityParams(getAuthenticationContext().getI18nHelper(), displayParams, issue);
        velocityParams.put("labels", issue.getLabels());
        velocityParams.put("labelUtil", getLabelUtil());
        velocityParams.put("remoteUser", authenticationContext.getLoggedInUser());
        velocityParams.put("canEdit", issueManager.isEditable(issue, authenticationContext.getLoggedInUser()));
        return renderTemplate("labels-columnview.vm", velocityParams);
    }

    public String availableForBulkEdit(BulkEditBean bulkEditBean)
    {
        // Ensure that this field is not hidden in any Field Layouts the selected issues belong to
        if (isHidden(bulkEditBean.getFieldLayouts()))
        {
            return "bulk.edit.unavailable.hidden";
        }

        // Have to look through all the issues in case permission has been given to current assignee/reporter (i.e. role based)
        for (Issue issue : bulkEditBean.getSelectedIssues())
        {
            // If we got here then the field is visible in all field layouts
            // So check for permission
            if (!hasBulkUpdatePermission(bulkEditBean, issue) || !isShown(issue))
            {
                return "bulk.edit.multiproject.unavailable.permission";
            }
        }

        // This field is available for bulk-editing, return null (i.e no unavailble message)
        return null;
    }

    @Override
    protected Object getRelevantParams(final Map<String, String[]> params)
    {
        String[] value = params.get(getId());
        if (value != null && value.length > 0)
        {
            final Set<Label> labels = new LinkedHashSet<Label>();
            for (String labelString : value)
            {
                labels.add(new Label(null, null, labelString));
            }
            return labels;
        }
        else
        {
            return Collections.<Label>emptySet();
        }
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        return new FieldTypeInfo(null, String.format("%s/rest/api/1.0/labels/suggest?query=", jiraBaseUrls.baseUrl()));
    }

    @Override
    public JsonType getJsonSchema()
    {
        return JsonTypeBuilder.systemArray(JsonType.STRING_TYPE, getId());
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation()
    {
        return new LabelsRestFieldOperationsHandler(authenticationContext.getI18nHelper());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(Issue issue, boolean renderedVersionRequired, FieldLayoutItem fieldLayoutItem)
    {
        final List<String> list = new ArrayList<String>();
        for (Label label : issue.getLabels())
        {
            list.add(label.getLabel());
        }
        return new FieldJsonRepresentation(new JsonData(list));
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx)
    {
        return null;
    }
}
