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
import com.google.common.base.Function;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Iterables;
import com.google.common.collect.Lists;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

import static com.google.common.base.Predicates.equalTo;
import static com.google.common.base.Predicates.not;
import static com.google.common.collect.Collections2.filter;
import static java.util.Collections.emptyList;

/**
 * @since v5.0
 */
public class MultiSelectCustomFieldOperationsHandler extends AbstractCustomFieldOperationsHandler<Collection<String>>
{
    private final OptionsManager optionsManager;

    public MultiSelectCustomFieldOperationsHandler(OptionsManager optionsManager, CustomField field, I18nHelper i18nHelper)
    {
        super(field, i18nHelper);
        this.optionsManager = optionsManager;
    }

    @Override
    public Set<String> getSupportedOperations()
    {
        return ImmutableSet.of(StandardOperation.ADD.getName(), StandardOperation.SET.getName(), StandardOperation.REMOVE.getName());
    }

    @Override
    protected Collection<String> handleAddOperation(IssueContext issueCtx, Issue issue, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        List<String> newList = new ArrayList<String>(currentFieldValue);
        // Options can be specified by Id or Value.  Id has priority as always.
        String optionId = operationValue.asObjectWithProperty("id", field.getId(), errors);
        if (optionId == null)
        {
            String value = operationValue.asObjectWithProperty("value", field.getId(), errors);
            {
                if (value != null)
                {
                    optionId = getOptionIdForValue(issueCtx, errors, value);
                }
            }
        }
        if (!newList.contains(optionId)) {
            newList.add(optionId);
        }
        return newList;
    }


    @Override
    protected Collection<String> handleRemoveOperation(IssueContext issueCtx, Issue issue, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        // Options can be specified by Id or Value.  Id has priority as always.
        String optionId = operationValue.asObjectWithProperty("id", field.getId(), errors);
        if (optionId == null)
        {
            String value = operationValue.asObjectWithProperty("value", field.getId(), errors);
            {
                if (value != null)
                {
                    optionId = getOptionIdForValue(issueCtx, errors, value);
                }
            }
        }
        return filter(currentFieldValue, not(equalTo(optionId)));
    }

    @Override
    protected Collection<String> handleSetOperation(IssueContext issueCtx, Issue issue, Collection<String> currentFieldValue, JsonData operationValue, ErrorCollection errors)
    {
        if (operationValue.isNull())
        {
            return Collections.emptyList();
        }
        Set<String> uniqueOptionIds = new HashSet<String>();
        List<String> optionIds = operationValue.asArrayOfObjectsWithId(field.getName(), errors);
        if (optionIds != null)
        {
            uniqueOptionIds.addAll(optionIds);
        }
        List<String> values = operationValue.asArrayOfObjectsWithProperty("value", field.getName(), errors);
        if (values != null)
        {
            for (String value : values)
            {
                String id = getOptionIdForValue(issueCtx, errors, value);
                if (id != null)
                {
                    uniqueOptionIds.add(id);
                }
                else
                {
                    errors.addError(field.getId(), i18nHelper.getText("rest.custom.field.option.value.invalid", value), ErrorCollection.Reason.VALIDATION_FAILED);
                }
            }
        }
        return uniqueOptionIds;
    }

    private String getOptionIdForValue(IssueContext issueCtx, ErrorCollection errors, String value)
    {
        String optionId = null;
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
            errors.addError(field.getId(), i18nHelper.getText("rest.custom.field.option.value.invalid", value), ErrorCollection.Reason.VALIDATION_FAILED);
        }
        return optionId;
    }

    @Override
    protected List<String> getInitialCreateValue(IssueContext issueCtx)
    {
        return Collections.emptyList();
    }

    @Override
    protected List<String> getInitialValue(Issue issue, ErrorCollection errors)
    {
        @SuppressWarnings ({ "unchecked" }) Collection<Option> fieldValue = (Collection<Option>) field.getValue(issue);
        if (fieldValue == null)
        {
            return emptyList();
        }

        Iterable<String> options = Iterables.transform(fieldValue, new Function<Option, String>()
        {
            @Override
            public String apply(Option from)
            {
                return from.getOptionId().toString();
            }
        });
        return Lists.newArrayList(options);
    }

    @Override
    protected void finaliseOperation(Collection<String> finalValue, IssueInputParameters parameters, ErrorCollection errors)
    {
        parameters.addCustomFieldValue(field.getId(), finalValue.toArray(new String[finalValue.size()]));
    }

}
