package com.atlassian.jira.issue.customfields.searchers.renderer;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.IssueImpl;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.customfields.CustomFieldValueProvider;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigItem;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.SearchContext;
import com.atlassian.jira.issue.search.searchers.util.SearchContextRenderHelper;
import com.atlassian.jira.issue.transport.FieldValuesHolder;
import com.atlassian.jira.jql.util.JqlSelectOptionsUtil;
import com.atlassian.jira.plugin.customfield.CustomFieldSearcherModuleDescriptor;
import com.atlassian.jira.web.FieldVisibilityManager;
import com.google.common.base.Function;
import com.google.common.collect.Collections2;
import com.google.common.collect.Sets;

import webwork.action.Action;

import javax.annotation.Nullable;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.HashSet;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Renders the multi select searcher, including invalid values.
 *
 * @since v5.2
 */
public class MultiSelectCustomFieldSearchRenderer extends CustomFieldRenderer
{
    // Retrieves an option given its ID.
    private final Function<String, Option> GET_OPTION_FROM_ID =
            new Function<String, Option>()
    {
        @Override
        public Option apply(@Nullable String input)
        {
            return jqlSelectOptionsUtil.getOptionById(Long.parseLong(input));
        }
    };

    /**
     * Used to sort options by value (case insensitive).
     * Null options or options with null values will be sorted to the right hand side.
     */
    final static Comparator<Option> OPTION_VALUE_COMPARATOR =
            new Comparator<Option>()
    {
        @Override
        public int compare(final Option a, final Option b)
        {
            if (null == a && null == b) return 0;
            else if (null == a) return 1;
            else if (null == b) return -1;

            final String aValue = a.getValue();
            final String bValue = b.getValue();

            if (null == aValue && null == bValue) return 0;
            else if (null == aValue) return 1;
            else if (null == bValue) return -1;
            else
            {
                return aValue.compareToIgnoreCase(bValue);
            }
        }
    };

    private final CustomField customField;
    private final CustomFieldValueProvider customFieldValueProvider;
    private final JqlSelectOptionsUtil jqlSelectOptionsUtil;

    public MultiSelectCustomFieldSearchRenderer(
            ClauseNames clauseNames,
            CustomFieldSearcherModuleDescriptor customFieldSearcherModuleDescriptor,
            CustomField customField,
            CustomFieldValueProvider customFieldValueProvider,
            FieldVisibilityManager fieldVisibilityManager,
            JqlSelectOptionsUtil jqlSelectOptionsUtil)
    {
        super(clauseNames, customFieldSearcherModuleDescriptor, customField,
                customFieldValueProvider, fieldVisibilityManager);
        this.customField = customField;
        this.customFieldValueProvider = customFieldValueProvider;
        this.jqlSelectOptionsUtil = jqlSelectOptionsUtil;
    }

    @Override
    public String getEditHtml(
            final User user,
            SearchContext searchContext,
            FieldValuesHolder fieldValuesHolder,
            Map<?, ?> displayParameters,
            Action action)
    {
        HashMap<String, Object> velocityParams = new HashMap<String, Object>();

        Set<Option> invalidSet = getInvalidOptions(fieldValuesHolder, searchContext);
        Set<Option> validSelectedSet = getSelectedOptions(fieldValuesHolder);
        validSelectedSet.removeAll(invalidSet);
        Set<Option> invalidOptions = filterDuplicatesByLabels(invalidSet, validSelectedSet);

        // All options (including invalid ones), sorted alphabetically.
        List<Option> allOptions = new ArrayList<Option>(invalidOptions);
        allOptions.addAll(getValidOptions(searchContext));
        Collections.sort(allOptions, OPTION_VALUE_COMPARATOR);

        velocityParams.put("fieldkey", customField.getCustomFieldType().getKey());
        velocityParams.put("invalidOptions", invalidOptions);
        velocityParams.put("allOptions", allOptions);

        SearchContextRenderHelper.addSearchContextParams(searchContext, velocityParams);
        return getEditHtml(searchContext, fieldValuesHolder, displayParameters,
                action, velocityParams);
    }

    @Override
    public String getViewHtml(
            final User user,
            SearchContext searchContext,
            FieldValuesHolder fieldValuesHolder,
            Map<?, ?> displayParameters,
            Action action)
    {
        HashMap<String, Object> velocityParams = new HashMap<String, Object>();

        Set<Option> invalidSet = getInvalidOptions(fieldValuesHolder, searchContext);
        Set<Option> validSelectedSet = getSelectedOptions(fieldValuesHolder);
        validSelectedSet.removeAll(invalidSet);
        Set<Option> invalidOptions = filterDuplicatesByLabels(invalidSet, validSelectedSet);

        // Sort the options alphabetically.
        List<Option> selectedOptions = new ArrayList<Option>(invalidOptions);
        selectedOptions.addAll(validSelectedSet);
        Collections.sort(selectedOptions, OPTION_VALUE_COMPARATOR);

        velocityParams.put("fieldkey", customField.getCustomFieldType().getKey());
        velocityParams.put("invalidOptions", invalidOptions);
        velocityParams.put("selectedOptions", selectedOptions);

        SearchContextRenderHelper.addSearchContextParams(searchContext, velocityParams);
        return super.getViewHtml(searchContext, fieldValuesHolder,
                displayParameters, action, velocityParams);
    }

    /**
     * @param fieldValuesHolder The field values holder containing the values.
     * @param searchContext The context in which the searcher is to be shown.
     * @return selected options that aren't in the current context.
     */
    private Set<Option> getInvalidOptions(
            final FieldValuesHolder fieldValuesHolder,
            final SearchContext searchContext)
    {
        Set<Option> selectedOptions = getSelectedOptions(fieldValuesHolder);
        selectedOptions.removeAll(getValidOptions(searchContext));
        return selectedOptions;
    }

    private Set<Option> filterDuplicatesByLabels(Set<Option> applyToList, Set<Option> usingList)
    {
        Set<Option> filteredList = Sets.newHashSet(applyToList);
        //Remove all invalid options that have duplicate labels from valid and invalid options
        //Using iterator so elements can be removed in place of the for loop
        Set<String> validLabels = Sets.newHashSet(Collections2.transform(usingList, new Function<Option, String>() {
            @Override
            public String apply(@Nullable final Option input)
            {
                return input.getValue();
            }
        }));
        Set<String> invalidLabels = Sets.newHashSet();
        for (Iterator<Option> it = filteredList.iterator(); it.hasNext();)
        {
            Option invalidOption = it.next();
            if (validLabels.contains(invalidOption.getValue()) ||
                    invalidLabels.contains(invalidOption.getValue()))
            {
                it.remove();
            }
            else
            {
                invalidLabels.add(invalidOption.getValue());
            }
        }

        return filteredList;
    }

    /**
     * @param fieldValuesHolder The field values holder containing the values.
     * @return the set of selected options in {@code fieldValuesHolder}.
     */
    private Set<Option> getSelectedOptions(
            final FieldValuesHolder fieldValuesHolder)
    {
        @SuppressWarnings({"unchecked"})
        Collection<String> selectedOptionIds = (Collection<String>)
                customFieldValueProvider.getStringValue(customField, fieldValuesHolder);

        if (selectedOptionIds != null && !selectedOptionIds.isEmpty())
        {
            // Transform the option IDs into actual options.
            return new HashSet<Option>(Collections2.transform(
                    selectedOptionIds, GET_OPTION_FROM_ID));
        }
        else
        {
            return new HashSet<Option>();
        }
    }

    /**
     * @param searchContext The context in which the searcher is to be shown.
     * @return the set of valid options in the given search context.
     */
    private Set<Option> getValidOptions(final SearchContext searchContext)
    {
        FieldConfig fieldConfig = customField.getReleventConfig(searchContext);
        if (fieldConfig != null)
        {
            MutableIssue issue = null;
            if (searchContext.isSingleProjectContext())
            {
                issue = IssueImpl.getIssueObject(null);
                issue.setProjectObject(searchContext.getSingleProject());
            }

            List<FieldConfigItem> configItems = fieldConfig.getConfigItems();
            if (configItems != null && !configItems.isEmpty())
            {
                for (final FieldConfigItem configItem : configItems)
                {
                    if (configItem.getObjectKey().equals("options"))
                    {
                        Options options = (Options)configItem.getConfigurationObject(issue);
                        if (options != null)
                        {
                            return new HashSet<Option>(options);
                        }
                    }
                }
            }
        }

        return new HashSet<Option>();
    }
}