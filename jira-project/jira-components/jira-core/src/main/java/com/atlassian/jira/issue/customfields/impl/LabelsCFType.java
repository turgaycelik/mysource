package com.atlassian.jira.issue.customfields.impl;

import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.jira.exception.ParseException;
import com.atlassian.jira.imports.project.core.EntityRepresentation;
import com.atlassian.jira.imports.project.customfield.ExternalCustomFieldValue;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomFieldParser;
import com.atlassian.jira.imports.project.mapper.ProjectImportMapper;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.ProjectImportLabelFieldParser;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.impl.rest.LabelsCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.LabelsField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.rest.FieldJsonRepresentation;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfo;
import com.atlassian.jira.issue.fields.rest.FieldTypeInfoContext;
import com.atlassian.jira.issue.fields.rest.RestAwareCustomFieldType;
import com.atlassian.jira.issue.fields.rest.RestCustomFieldTypeOperations;
import com.atlassian.jira.issue.fields.rest.RestFieldOperationsHandler;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.issue.fields.rest.json.JsonType;
import com.atlassian.jira.issue.fields.rest.json.JsonTypeBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.label.Label;
import com.atlassian.jira.issue.label.LabelManager;
import com.atlassian.jira.issue.label.LabelParser;
import com.atlassian.jira.issue.label.LabelUtil;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.I18nHelper;
import com.atlassian.jira.util.MessageSet;
import com.atlassian.jira.util.MessageSetImpl;

import com.google.common.base.Function;

import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import static com.google.common.collect.Collections2.transform;

/**
 * Labels Custom field
 *
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link Set} of {@link Label}s</dd>
 *  <dt><strong>Singular Object Type</strong></dt>
 * <dd>{@link Label}</dd>
 * <dt><Strong>Database Storage Type</Strong></dt>
 * <dd>Not stored with Custom Field Data @see {@link LabelManager#setLabels(com.atlassian.crowd.embedded.api.User, Long, Long, java.util.Set, boolean, boolean)}</dd>
 * </dl>
 *
 * @since v4.2
 */
public class LabelsCFType extends AbstractCustomFieldType<Set<Label>, Label>
        implements SortableCustomField<Set<Label>>, ProjectImportableCustomFieldParser, ProjectImportableCustomField, RestAwareCustomFieldType, RestCustomFieldTypeOperations
{
    private static final Logger log = Logger.getLogger(LabelsCFType.class);

    private final JiraAuthenticationContext authContext;
    private final IssueManager issueManager;
    private final GenericConfigManager genericConfigManager;
    private final LabelUtil labelUtil;
    private final LabelManager labelManager;
    private final ProjectImportableCustomFieldParser projectImportLabelFieldParser;
    private final JiraBaseUrls jiraBaseUrls;

    public LabelsCFType
            (
                    final JiraAuthenticationContext authenticationContext,
                    final IssueManager issueManager,
                    final GenericConfigManager genericConfigManager,
                    final LabelUtil labelUtil,
                    final LabelManager labelManager,
                    final ProjectImportLabelFieldParser projectImportableCustomFieldParser,
                    final JiraBaseUrls jiraBaseUrls)
    {
        this.authContext = authenticationContext;
        this.issueManager = issueManager;
        this.genericConfigManager = genericConfigManager;
        this.labelUtil = labelUtil;
        this.labelManager = labelManager;
        this.projectImportLabelFieldParser = projectImportableCustomFieldParser;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    @Nonnull
    @Override
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem)
    {
        final Map<String, Object> velocityParameters = super.getVelocityParameters(issue, field, fieldLayoutItem);

        if (issue == null || issue.getId() == null)
        {
            velocityParameters.put("canEdit", Boolean.FALSE);
        }
        else
        {
            final Issue issueFromDb = issueManager.getIssueObject(issue.getId());
            velocityParameters.put("canEdit", issueManager.isEditable(issueFromDb, authContext.getLoggedInUser()));
            velocityParameters.put("labels", getValueFromIssue(field, issue));
        }

        velocityParameters.put("fieldId", field.getId());
        velocityParameters.put("i18n", authContext.getI18nHelper());
        velocityParameters.put("field", field);
        velocityParameters.put("labelUtil", labelUtil);
        velocityParameters.put("issue", issue);
        velocityParameters.put("labelParser", new LabelParser());

        return velocityParameters;
    }

    @Override
    public Set<Label> getValueFromIssue(CustomField field, Issue issue)
    {
        final Set<Label> labels = labelManager.getLabels(issue.getId(), field.getIdAsLong());
        // We should return null if there are no labels.
        if (labels.isEmpty())
        {
            return null;
        }
        return labels;
    }

    @Override
    public Set<Label> getDefaultValue(final FieldConfig fieldConfig)
    {
        Object databaseValue = genericConfigManager.retrieve(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        if (databaseValue != null)
        {
            try
            {
                final String labelsString = (String) databaseValue;
                return convertStringsToLabels(Collections.singleton(labelsString));
            }
            catch (FieldValidationException e)
            {
                log.error("Invalid default value encountered", e);
                return null;
            }
        }
        else
        {
            return null;
        }
    }

    @Override
    public void setDefaultValue(final FieldConfig fieldConfig, final Set<Label> value)
    {
        genericConfigManager.update(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), convertLabelsToString(value));
    }

    @Override
    public String getChangelogValue(final CustomField field, final Set<Label> value)
    {
        if (value == null)
        {
            return "";
        }
        return convertLabelsToString(value);
    }

    @Override
    public String getStringFromSingularObject(final Label singularObject)
    {
        if (singularObject == null)
        {
            return null;
        }
        return singularObject.getLabel();
    }

    @Override
    public Label getSingularObjectFromString(final String string) throws FieldValidationException
    {
        if (string == null)
        {
            return null;
        }
        final int labelLength = StringUtils.length(string.trim());
        if (LabelParser.isValidLabelName(string) && labelLength <= LabelParser.MAX_LABEL_LENGTH)
        {
            return new Label(null, null, string);
        }
        else
        {
            if (labelLength > LabelParser.MAX_LABEL_LENGTH)
            {
                throw new FieldValidationException(authContext.getI18nHelper().getText("label.service.error.label.toolong", string));
            }
            else
            {
                throw new FieldValidationException(authContext.getI18nHelper().getText("label.service.error.label.invalid", string));
            }
        }
    }

    @Override
    public Set<Long> remove(final CustomField field)
    {
        return labelManager.removeLabelsForCustomField(field.getIdAsLong());
    }

    @Override
    public void validateFromParams(final CustomFieldParams relevantParams, final ErrorCollection errorCollectionToAddTo, final FieldConfig config)
    {
        try
        {
            getValueFromCustomFieldParams(relevantParams);
        }
        catch (FieldValidationException e)
        {
            errorCollectionToAddTo.addError(config.getCustomField().getId(), e.getMessage(), Reason.VALIDATION_FAILED);
        }
    }

    @Override
    public void createValue(final CustomField field, final Issue issue, @Nonnull final Set<Label> value)
    {
        setLabels(field, issue, value);
    }

    @Override
    public void updateValue(final CustomField field, final Issue issue, final Set<Label> value)
    {
        setLabels(field, issue, value);
    }

    private void setLabels(final CustomField field, final Issue issue, final Set<Label> value)
    {
        final Set<String> labelStrings = new LinkedHashSet<String>();
        if (value != null)
        {
            for (Label label : value)
            {
                labelStrings.add(label.getLabel());
            }
        }
        //validation should have already happened by now
        labelManager.setLabels(authContext.getLoggedInUser(), issue.getId(), field.getIdAsLong(), labelStrings, false, false);
    }

    @Override
    public Set<Label> getValueFromCustomFieldParams(CustomFieldParams customFieldParams) throws FieldValidationException
    {
        if (customFieldParams == null || customFieldParams.isEmpty())
        {
            return null;
        }

        final Collection<String> normalParams = (Collection<String>) customFieldParams.getValuesForKey(null); //single field types should not scope their parameters
        if (normalParams == null || normalParams.isEmpty())
        {
            return null;
        }

        return convertStringsToLabels(normalParams);
    }

    @Override
    public Object getStringValueFromCustomFieldParams(final CustomFieldParams parameters)
    {
        @SuppressWarnings ("unchecked")
        final Collection<String> valuesForNullKey = parameters.getValuesForNullKey();
        if (valuesForNullKey != null)
        {
            final StringBuilder ret = new StringBuilder();
            for (String value : valuesForNullKey)
            {
                ret.append(value).append(LabelsField.SEPARATOR_CHAR);
            }
            return ret.toString().trim();
        }
        return null;
    }

    @Override
    public int compare(@Nonnull final Set<Label> customFieldObjectValue1, @Nonnull final Set<Label> customFieldObjectValue2, final FieldConfig fieldConfig)
    {
        final String stringValue1 = convertLabelsToString(customFieldObjectValue1);
        final String stringValue2 = convertLabelsToString(customFieldObjectValue2);
        if (stringValue1 == null && stringValue2 == null)
        {
            return 0;
        }

        if (stringValue1 == null)
        {
            return 1;
        }

        if (stringValue2 == null)
        {
            return -1;
        }

        return stringValue1.compareTo(stringValue2);
    }


    private String convertLabelsToString(final Set<Label> newLabels)
    {
        if (newLabels == null)
        {
            return null;
        }
        final StringBuilder ret = new StringBuilder();
        for (Label newLabel : newLabels)
        {
            ret.append(newLabel).append(LabelsField.SEPARATOR_CHAR);
        }
        return ret.toString().trim();
    }

    private Set<Label> convertStringsToLabels(final Collection<String> labelStrings)
    {
        final Set<Label> ret = new LinkedHashSet<Label>();
        for (String labelString : labelStrings)
        {
            if (labelString.length() > LabelParser.MAX_LABEL_LENGTH)
            {
                throw new FieldValidationException(authContext.getI18nHelper().getText("label.service.error.label.toolong", labelString));
            }
            ret.addAll(LabelParser.buildFromString(labelString));
        }
        return ret;
    }

    @Override
    public String getEntityName()
    {
        return projectImportLabelFieldParser.getEntityName();
    }

    @Override
    public ExternalCustomFieldValue parse(final Map attributes) throws ParseException
    {
        return projectImportLabelFieldParser.parse(attributes);
    }

    @Override
    public EntityRepresentation getEntityRepresentation(final ExternalCustomFieldValue customFieldValue)
    {
        return projectImportLabelFieldParser.getEntityRepresentation(customFieldValue);
    }

    @Override
    public ProjectCustomFieldImporter getProjectImporter()
    {
        return new LabelCustomFieldImporter();
    }

    @Override
    public boolean valuesEqual(final Set<Label> oldLabels, final Set<Label> newLabels)
    {
        if (oldLabels == newLabels)
        {
            return true;
        }
        if (oldLabels == null && newLabels.isEmpty())
        {
            return true;
        }
        if (newLabels == null && oldLabels.isEmpty())
        {
            return true;
        }
        return super.valuesEqual(oldLabels, newLabels);
    }

    static class LabelCustomFieldImporter implements ProjectCustomFieldImporter
    {
        public MessageSet canMapImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig, final I18nHelper i18n)
        {
            //add warnings for invalid labels
            final MessageSet messageSet = new MessageSetImpl();
            final String label = customFieldValue.getValue();
            if (!LabelParser.isValidLabelName(label))
            {
                messageSet.addWarningMessage(i18n.getText("label.project.import.error", label));
                messageSet.addWarningMessageInEnglish("Dropping label '" + label + "' because it contains invalid characters.");
            }
            return messageSet;
        }

        public MappedCustomFieldValue getMappedImportValue(final ProjectImportMapper projectImportMapper, final ExternalCustomFieldValue customFieldValue, final FieldConfig fieldConfig)
        {
            final String label = customFieldValue.getValue();
            // strip out invalid labels
            if (LabelParser.isValidLabelName(label))
            {
                return new MappedCustomFieldValue(label);
            }
            return new MappedCustomFieldValue(null);
        }
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitLabels(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitLabels(LabelsCFType labelsCustomFieldType);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        CustomField customField = ((CustomField) fieldTypeInfoContext.getOderableField());
        if (fieldTypeInfoContext.getIssue() == null)
        {
            String autoCompleteUrl = String.format("%s/rest/api/1.0/labels/suggest?customFieldId=%s&query=", jiraBaseUrls.baseUrl(), customField.getIdAsLong());
            return new FieldTypeInfo(null, autoCompleteUrl);
        }
        String autoCompleteUrl = String.format("%s/rest/api/1.0/labels/" + fieldTypeInfoContext.getIssue().getId() + "/suggest?customFieldId=%s&query=", jiraBaseUrls.baseUrl(), customField.getIdAsLong());
        return new FieldTypeInfo(null, autoCompleteUrl);

    }

    @Override
    public JsonType getJsonSchema(CustomField customField)
    {
        return JsonTypeBuilder.customArray(JsonType.STRING_TYPE, getKey(), customField.getIdAsLong());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        Set<Label> labels = getValueFromIssue(field, issue);
        if (labels == null)
        {
            return new FieldJsonRepresentation(new JsonData(null));
        }
        Collection<String> values = transform(labels, new Function<Label, String>()
        {
            @Override
            public String apply(Label from)
            {
                return from.getLabel();
            }
        });

        return new FieldJsonRepresentation(new JsonData(values));
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation(CustomField field)
    {
        return new LabelsCustomFieldOperationsHandler(field, getI18nBean());
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx, CustomField field)
    {
        FieldConfig config = field.getRelevantConfig(issueCtx);
        Object defaultValue = field.getCustomFieldType().getDefaultValue(config);
        return defaultValue == null ? null : new JsonData(defaultValue);
    }
}
