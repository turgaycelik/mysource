package com.atlassian.jira.issue.customfields.impl;

import java.util.ArrayList;
import java.util.Collection;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;
import javax.servlet.http.HttpServletRequest;

import com.atlassian.jira.imports.project.customfield.CascadingSelectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectCustomFieldImporter;
import com.atlassian.jira.imports.project.customfield.ProjectImportableCustomField;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.customfields.MultipleSettableCustomFieldType;
import com.atlassian.jira.issue.customfields.SortableCustomField;
import com.atlassian.jira.issue.customfields.config.item.SettableOptionsConfigItem;
import com.atlassian.jira.issue.customfields.impl.rest.CascadingSelectCustomFieldOperationsHandler;
import com.atlassian.jira.issue.customfields.manager.GenericConfigManager;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.OptionUtils;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.customfields.persistence.CustomFieldValuePersister;
import com.atlassian.jira.issue.customfields.persistence.PersistenceFieldType;
import com.atlassian.jira.issue.customfields.view.CustomFieldParams;
import com.atlassian.jira.issue.customfields.view.CustomFieldParamsImpl;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItemType;
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
import com.atlassian.jira.issue.fields.rest.json.beans.CustomFieldOptionJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.ErrorCollection.Reason;
import com.atlassian.jira.util.ObjectUtils;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.Lists;

import org.apache.log4j.Logger;

import webwork.action.ServletActionContext;

/**
 * <p>Cascading Select Custom Field Type allows for multiple dependent select lists.</p>
 * <dl>
 * <dt><strong>Transport Object Type</strong></dt>
 * <dd>{@link Map<String, Option>, Option} The <em>key</em> in the map represents the field depth of the
 * select list. eg. a key of null is the root parent, and key of "1" is the first level select list. As at JIRA 3.0,
 * there can only be one level.</dd>
 * <dt><strong>Singular Object Type</strong></dt>
 * <dd>{@link Option}</dd>
 * <dt><Strong>Database Storage Type</Strong></dt>
 * <dd>{@link String} of option id</dd>
 * </dl>
 */
public class CascadingSelectCFType extends AbstractCustomFieldType<Map<String, Option>, Option> implements  MultipleSettableCustomFieldType<Map<String, Option>, Option>, SortableCustomField<Map<String, Option>>, ProjectImportableCustomField, RestAwareCustomFieldType, RestCustomFieldTypeOperations
{
    public static final String PARENT_KEY = null;
    public static final String CHILD_KEY = "1";

    private static final Logger log = Logger.getLogger(CascadingSelectCFType.class);
    public static final PersistenceFieldType CASCADE_VALUE_TYPE = PersistenceFieldType.TYPE_LIMITED_TEXT;

    private final ProjectCustomFieldImporter projectCustomFieldImporter;
    private final OptionsManager optionsManager;
    private final CustomFieldValuePersister customFieldValuePersister;
    private final GenericConfigManager genericConfigManager;
    private final JiraBaseUrls jiraBaseUrls;

    public CascadingSelectCFType(OptionsManager optionsManager, CustomFieldValuePersister customFieldValuePersister, GenericConfigManager genericConfigManager, JiraBaseUrls jiraBaseUrls)
    {
        this.customFieldValuePersister = customFieldValuePersister;
        this.optionsManager = optionsManager;
        this.genericConfigManager = genericConfigManager;
        this.jiraBaseUrls = jiraBaseUrls;
        this.projectCustomFieldImporter = new CascadingSelectCustomFieldImporter();
    }

    // -----------------------------------------------------------------------------------------------------  Field Configruation

    public void removeValue(CustomField field, Issue issue, Option option)
    {
        if (option != null)
        {
            customFieldValuePersister.removeValue(field, issue.getId(), PersistenceFieldType.TYPE_LIMITED_TEXT, option.getOptionId().toString());
        }
    }

    public Set<Long> remove(final CustomField field)
    {
        optionsManager.removeCustomFieldOptions(field);
        return customFieldValuePersister.removeAllValues(field.getId());
    }

    public Options getOptions(final FieldConfig fieldConfig, final JiraContextNode jiraContextNode)
    {
        return optionsManager.getOptions(fieldConfig);
    }

    /**
     * Returns a list of Issue Ids matching the "value" note that the value in this instance is the single object
     */
    public Set<Long> getIssueIdsWithValue(CustomField field, Option option)
    {
        Set<Long> allIssues = new HashSet<Long>();

        // Add for current option
        if (option != null)
        {
            allIssues.addAll(customFieldValuePersister.getIssueIdsWithValue(field, CASCADE_VALUE_TYPE, option.getOptionId().toString()));

            // Add for children
            List<Option> childOptions = option.retrieveAllChildren(null);
            if (childOptions != null && !childOptions.isEmpty())
            {
                for (Option childOption : childOptions)
                {
                    allIssues.addAll(customFieldValuePersister.getIssueIdsWithValue(field, CASCADE_VALUE_TYPE, childOption.getOptionId().toString()));
                }
            }
        }

        return allIssues;
    }
    // -----------------------------------------------------------------------------------------------------  Validation

    public void validateFromParams(CustomFieldParams relevantParams, ErrorCollection errorCollectionToAddTo, FieldConfig config)
    {
        if (relevantParams == null || relevantParams.isEmpty())
        {
            return;
        }

        String customFieldId = config.getCustomField().getId();

        Option parentOption;
        try
        {
            // Get the parent option
            parentOption = extractOptionFromParams(PARENT_KEY, relevantParams);
        }
        catch (FieldValidationException e)
        {
            parentOption = null;
        }

        // If the selected parent option does not resolve to a value in the DB we should throw an error
        if(parentOption == null)
        {
            List params = new ArrayList(relevantParams.getValuesForKey(null));
            // If there was no value selected for the parent or the 'None/All' option was selected we let them pass
            // and in this case we do not care about what the child values are since the parent is none.
            if (!params.isEmpty() && !isNoneOptionSelected(params))
            {
                errorCollectionToAddTo.addError(customFieldId, getI18nBean().getText("admin.errors.option.invalid.parent", "'" + params.get(0).toString() + "'"), Reason.VALIDATION_FAILED);
            }
        }
        else
        {
            // Since we are sure that the parent value is non-null and resovles to a valid option lets make sure that
            // it is valid in the FieldConfig for where we are.
            if(!parentOptionValidForConfig(config, parentOption))
            {
                errorCollectionToAddTo.addError(customFieldId, getI18nBean().getText("admin.errors.option.invalid.for.context",
                        "'" + parentOption.getValue() + "'", "'" + config.getName() + "'"), Reason.VALIDATION_FAILED);
            }
            else
            {
                try
                {
                    // Get the param for this current option
                    Collection valuesForChild = relevantParams.getValuesForKey(CHILD_KEY);
                    if (valuesForChild != null)
                    {
                        List<String> params = new ArrayList<String>(valuesForChild);

                        // Get the option object from the params only if they have not selected the "None/All" option
                        Option currentOption = null;

                        // If the user has not selected 'None/All' then we should try to resolve the option into an
                        // object and then check that the object is valid in the FieldConfig for where we are.
                        if(!isNoneOptionSelected(params))
                        {
                            // get the option from the params
                            currentOption = extractOptionFromParams(CHILD_KEY, relevantParams);

                            // check that the supplied option is valid in the config supplied
                            if(!currentOptionValidForConfig(config, currentOption))
                            {
                                String optionValue = (currentOption == null) ?  params.get(0).toString() : currentOption.getValue();
                                errorCollectionToAddTo.addError(customFieldId, getI18nBean().getText("admin.errors.option.invalid.for.context",
                                        "'" + optionValue + "'", "'" + config.getName() + "'"), Reason.VALIDATION_FAILED);
                                return;
                            }
                        }

                        // make certain that the current option (if it exists) has a parent, that the parent is what we
                        // expect it to be (the parent that was submitted as a param)
                        if (currentOption != null && currentOption.getParentOption() != null
                                && !parentOption.equals(currentOption.getParentOption()) )
                        {
                            errorCollectionToAddTo.addError(customFieldId, getI18nBean().getText("admin.errors.option.invalid.for.parent","'" + currentOption.getValue() + "'", "'" + parentOption.getValue() + "'"), Reason.VALIDATION_FAILED);
                        }
                    }
                }
                catch (FieldValidationException e)
                {
                    errorCollectionToAddTo.addError(customFieldId, e.getMessage(), Reason.VALIDATION_FAILED);
                }
            }
        }
    }

    private boolean isNoneOptionSelected(List<String> params)
    {
        String parentOptionParam = params.iterator().next();
        return parentOptionParam == null || "-1".equals(parentOptionParam);
    }

    private boolean parentOptionValidForConfig(FieldConfig config, Option parentOption)
    {
        final Options options = optionsManager.getOptions(config);
        if(options != null)
        {
            Collection rootOptions = options.getRootOptions();
            if(rootOptions != null)
            {
                return rootOptions.contains(parentOption);
            }
        }
        return false;
    }

    private boolean currentOptionValidForConfig(FieldConfig config, Option currentOption)
    {
        final Options options = optionsManager.getOptions(config);
        if(options != null)
        {
            Collection rootOptions = options.getRootOptions();
            if(rootOptions != null)
            {
                if (currentOption != null)
                {
                    return options.getOptionById(currentOption.getOptionId()) != null;
                }
            }
        }
        return false;
    }

    // --------------------------------------------------------------------------------------------- Persistance Methods

    //these methods all operate on the object level

    /**
     * Create a cascading select-list instance for an issue.
     *
     * @param cascadingOptions
     */
    public void createValue(CustomField field, Issue issue, @Nonnull Map<String, Option> cascadingOptions)
    {
        Option parent = cascadingOptions.get(PARENT_KEY);
        Option child = cascadingOptions.get(CHILD_KEY);

        if (parent != null)
        {
            customFieldValuePersister.createValues(field, issue.getId(), CASCADE_VALUE_TYPE, Lists.newArrayList(parent.getOptionId().toString()), null);
            if (child != null)
            {
                customFieldValuePersister.createValues(field, issue.getId(), CASCADE_VALUE_TYPE, Lists.newArrayList(child.getOptionId().toString()), parent.getOptionId().toString());
            }
        }
    }

    public void updateValue(CustomField field, Issue issue, Map<String, Option> cascadingOptions)
    {
        // clear old stuff first
        customFieldValuePersister.updateValues(field, issue.getId(), CASCADE_VALUE_TYPE, null);
        if (cascadingOptions != null)
        {
            Option parent = cascadingOptions.get(PARENT_KEY);
            Option child = cascadingOptions.get(CHILD_KEY);

            if (parent != null)
            {
                customFieldValuePersister.updateValues(field, issue.getId(), CASCADE_VALUE_TYPE, Lists.newArrayList(parent.getOptionId().toString()), null);
                if (child != null)
                {
                    customFieldValuePersister.updateValues(field, issue.getId(), CASCADE_VALUE_TYPE, Lists.newArrayList(child.getOptionId().toString()), parent.getOptionId().toString());
                }
            }
        }
    }

    // --------------------------------------------------------------------------------------  CustomFieldParams methods

    public Map<String, Option> getValueFromIssue(CustomField field, Issue issue)
    {
        Option parentOption = getOptionValueForParentId(field, null, issue);
        if (parentOption != null)
        {
            Option childOption = getOptionValueForParentId(field, parentOption.getOptionId().toString(), issue);
            Map<String, Option> options = new HashMap<String, Option>();
            options.put(PARENT_KEY, parentOption);
            if (childOption != null)
            {
                options.put(CHILD_KEY, childOption);
            }
            return options;
        }
        else
        {
            return null;
        }
    }

    public Map<String, Option> getValueFromCustomFieldParams(CustomFieldParams relevantParams) throws FieldValidationException
    {
        if (relevantParams != null && !relevantParams.isEmpty())
        {
            try
            {
                return getOptionMapFromCustomFieldParams(relevantParams);
            }
            catch (final FieldValidationException e)
            {
                return null;
            }
        }
        else
        {
            return null;
        }

    }

    public Object getStringValueFromCustomFieldParams(CustomFieldParams parameters)
    {
        return parameters;
    }

    // -------------------------------------------------------------------------------------------------------- Defaults

    @Nullable
    public Map<String, Option> getDefaultValue(FieldConfig fieldConfig)
    {
        final Object o = genericConfigManager.retrieve(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        if (o != null)
        {
            final CustomFieldParams params = new CustomFieldParamsImpl(fieldConfig.getCustomField(), o);
            return getOptionMapFromCustomFieldParams(params);
        }
        else
        {
            return null;
        }
    }

    public void setDefaultValue(FieldConfig fieldConfig, Map<String, Option> cascadingOptions)
    {
        if (cascadingOptions != null)
        {
            final CustomFieldParams customFieldParams = new CustomFieldParamsImpl(fieldConfig.getCustomField(), cascadingOptions);
            customFieldParams.transformObjectsToStrings();
            customFieldParams.setCustomField(null);

            genericConfigManager.update(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), customFieldParams);
        }
        else
        {
            genericConfigManager.update(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString(), null);
        }
    }

    // --------------------------------------------------------------------------------------------------  Miscellaneous

    public String getChangelogValue(CustomField field, Map<String, Option> cascadingOptions)
    {
        if (cascadingOptions != null)
        {
            StringBuilder sb = new StringBuilder();
            sb.append("Parent values: ");
            Option parent = cascadingOptions.get(PARENT_KEY);
            sb.append(parent.getValue()).append("(").append(parent.getOptionId()).append(")");
            Option child = cascadingOptions.get(CHILD_KEY);
            if (child != null)
            {
                sb.append("Level ").append(CHILD_KEY).append(" values: ");
                sb.append(child.getValue()).append("(").append(child.getOptionId()).append(")");
            }
            return sb.toString();
        }
        else
        {
            return "";
        }
    }

    public String getStringFromSingularObject(Option optionObject)
    {
        if (optionObject != null)
        {
            return optionObject.getOptionId().toString();
        }
        else
        {
            log.warn("Object passed '" + optionObject + "' is not an Option but is null");
            return null;
        }
    }

    public Option getSingularObjectFromString(String string) throws FieldValidationException
    {
        return getOptionFromStringValue(string);
    }

    @Nonnull
    public List<FieldConfigItemType> getConfigurationItemTypes()
    {
        final List<FieldConfigItemType> configurationItemTypes = super.getConfigurationItemTypes();
        configurationItemTypes.add(new SettableOptionsConfigItem(this, optionsManager));
        return configurationItemTypes;
    }

    @Nonnull
    public Map<String, Object> getVelocityParameters(Issue issue, CustomField field, FieldLayoutItem fieldLayoutItem)
    {
        final HttpServletRequest request = ServletActionContext.getRequest();
        return MapBuilder.<String, Object>build("request", request);
    }

    //----------------------------------------------------------------------------------------- - Private Helper Methods

    private Map<String, Option> getOptionMapFromCustomFieldParams(CustomFieldParams params) throws FieldValidationException
    {
        Option parentOption = extractOptionFromParams(PARENT_KEY, params);
        Option childOption = extractOptionFromParams(CHILD_KEY, params);

        Map<String, Option> options = new HashMap<String, Option>();
        options.put(PARENT_KEY, parentOption);
        if (childOption != null)
        {
            options.put(CHILD_KEY, childOption);
        }

        return options;
    }

    @Nullable
    private Option extractOptionFromParams(String key, CustomFieldParams relevantParams) throws FieldValidationException
    {
        Collection<String> params = relevantParams.getValuesForKey(key);
        if (params != null && !params.isEmpty())
        {
            String selectValue = params.iterator().next();
            if (ObjectUtils.isValueSelected(selectValue) && selectValue != null && !selectValue.equalsIgnoreCase("none"))
            {
                return getOptionFromStringValue(selectValue);
            }
        }

        return null;
    }

    @Nullable
    private Option getOptionFromStringValue(String selectValue) throws FieldValidationException
    {
        final Long aLong = OptionUtils.safeParseLong(selectValue);
        if (aLong != null)
        {
            return optionsManager.findByOptionId(aLong);
        }
        else
        {
            List<Option> options = optionsManager.findByOptionValue(selectValue);
            if (options.size() == 0)
            {
                throw new FieldValidationException("Value: '" + selectValue + "' is an invalid Option");
            }
            else if (options.size() == 1)
            {
                return options.get(0);
            }
            else
            {
                throw new FieldValidationException("Value: '" + selectValue + "' has multiple possible options, please use the custom field id instead");
            }
        }
    }

    @Nullable
    private Option getOptionValueForParentId(CustomField field, @Nullable String sParentOptionId, Issue issue)
    {
        Collection values;

        values = customFieldValuePersister.getValues(field, issue.getId(), CASCADE_VALUE_TYPE, sParentOptionId);


        if (values != null && !values.isEmpty())
        {
            String optionId = (String) values.iterator().next();
            return optionsManager.findByOptionId(OptionUtils.safeParseLong(optionId));
        }
        else
        {
            return null;
        }
    }

    // -------------------------------------------------------------------------------------------------- Compare
    public int compare(@Nonnull Map<String, Option> o1, @Nonnull Map<String, Option> o2, FieldConfig fieldConfig)
    {
        Option option1 = o1.get(PARENT_KEY);
        Option option2 = o2.get(PARENT_KEY);

        int parentCompare = compareOption(option1, option2);
        if (parentCompare == 0)
        {
            // Compare child Options, if parents are the same
            Option childOption1 = o1.get(CHILD_KEY);
            Option childOption2 = o2.get(CHILD_KEY);

            return compareOption(childOption1, childOption2);
        }
        else
        {
            return parentCompare;
        }
    }

    public int compareOption(@Nullable Option option1, @Nullable Option option2)
    {
        if (option1 == null && option2 == null) return 0;
        else if (option1 == null) return -1;
        else if (option2 == null) return 1;
        else return option1.getSequence().compareTo(option2.getSequence());
    }

    public ProjectCustomFieldImporter getProjectImporter()
    {
        return this.projectCustomFieldImporter;
    }

    @Override
    public Object accept(VisitorBase visitor)
    {
        if (visitor instanceof Visitor)
        {
            return ((Visitor) visitor).visitCascadingSelect(this);
        }

        return super.accept(visitor);
    }

    public interface Visitor<T> extends VisitorBase<T>
    {
        T visitCascadingSelect(CascadingSelectCFType cascadingSelectCustomFieldType);
    }

    @Override
    public FieldTypeInfo getFieldTypeInfo(FieldTypeInfoContext fieldTypeInfoContext)
    {
        // Get the allowed options
        FieldConfig config = ((CustomField) fieldTypeInfoContext.getOderableField()).getRelevantConfig(fieldTypeInfoContext.getIssueContext());
        Options options = optionsManager.getOptions(config);
        Collection<CustomFieldOptionJsonBean> optionBeans = CustomFieldOptionJsonBean.shortBeans(options, jiraBaseUrls);

        return new FieldTypeInfo(optionBeans, null);
    }

    @Override
    public JsonType getJsonSchema(CustomField customField)
    {
        return JsonTypeBuilder.customArray(JsonType.STRING_TYPE, getKey(), customField.getIdAsLong());
    }

    @Override
    public FieldJsonRepresentation getJsonFromIssue(CustomField field, Issue issue, boolean renderedVersionRequested, @Nullable FieldLayoutItem fieldLayoutItem)
    {
        Map<String, Option> options = getValueFromIssue(field, issue);
        if (options == null)
        {
            return new FieldJsonRepresentation(new JsonData(null));
        }

        Option parent = options.get(PARENT_KEY);
        Option child = options.get(CHILD_KEY);
        return new FieldJsonRepresentation(new JsonData(CustomFieldOptionJsonBean.shortBean(parent, child, jiraBaseUrls)));
    }

    @Override
    public RestFieldOperationsHandler getRestFieldOperation(CustomField field)
    {
        return new CascadingSelectCustomFieldOperationsHandler(optionsManager, field, getI18nBean());
    }

    @Override
    public JsonData getJsonDefaultValue(IssueContext issueCtx, CustomField field)
    {
        FieldConfig fieldConfig = field.getRelevantConfig(issueCtx);
        final Object o = genericConfigManager.retrieve(DEFAULT_VALUE_TYPE, fieldConfig.getId().toString());
        if (o != null)
        {
            final CustomFieldParams params = new CustomFieldParamsImpl(fieldConfig.getCustomField(), o);
            Map<String, Option> options = getOptionMapFromCustomFieldParams(params);
            if (options == null)
            {
                return new JsonData(null);
            }
            Option parent = options.get(PARENT_KEY);
            Option child = options.get(CHILD_KEY);
            return new JsonData(CustomFieldOptionJsonBean.shortBean(parent, child, jiraBaseUrls));
        }
        else
        {
            return null;
        }
    }
}
