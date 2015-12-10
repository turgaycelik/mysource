package com.atlassian.jira.issue.customfields.impl.rest;

import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueInputParameters;
import com.atlassian.jira.issue.context.IssueContext;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.rest.StandardOperation;
import com.atlassian.jira.issue.fields.rest.json.JsonData;
import com.atlassian.jira.util.ErrorCollection;
import com.atlassian.jira.util.I18nHelper;
import com.google.common.collect.ImmutableSet;

import java.util.Collection;
import java.util.Set;

/**
 * @since v5.0
 */
public class SelectCustomFieldOperationsHandler extends AbstractCustomFieldOperationsHandler<String>
{
    private final OptionsManager optionsManager;

    public SelectCustomFieldOperationsHandler(OptionsManager optionsManager, CustomField field, I18nHelper i18nHelper)
    {
        super(field, i18nHelper);
        this.optionsManager = optionsManager;
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.SET.getName());
    }

    @Override
    protected String handleSetOperation(IssueContext issueCtx, Issue issue, String currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (operationValue.isNull())
        {
            return null;
        }
        // Options can be specified by Id or Value.  Id has priority as always.
        String optionId = operationValue.asObjectWithProperty("id", field.getId(), errors);
        if (optionId == null)
        {
            String value = operationValue.asObjectWithProperty("value", field.getId(), errors);
            {
                if (value != null)
                {
                    FieldConfig config = field.getRelevantConfig(issueCtx);
                    Collection<Option> options = optionsManager.getOptions(config);
                    for (Option option : options)
                    {
                        if (option.getValue().equals(value))
                        {
                            optionId = option.getOptionId().toString();
                            break;
                        }
                    }
                    if (optionId == null)
                    {
                        errors.addError(field.getId(), i18nHelper.getText("rest.custom.field.option.id.invalid", optionId), ErrorCollection.Reason.VALIDATION_FAILED);
                    }
                }
                else
                {
                    errors.addError(field.getId(), i18nHelper.getText("rest.custom.field.option.parent.no.name.or.id"), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        return optionId;
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     */
    protected String getInitialValue(Issue issue, ErrorCollection errors)
    {
        Option option = (Option) field.getValue(issue);
        return option == null ? null : option.getOptionId().toString();
    }

    /**
     * compute the "currentValue" to be passed to applyOperation()
     * @param issueCtx
     */
    protected String getInitialCreateValue(IssueContext issueCtx)
    {
        return null;
    }

    @Override
    protected void finaliseOperation(String finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.addCustomFieldValue(field.getId(), (finalValue != null) ? finalValue.toString() : null);
    }

}
