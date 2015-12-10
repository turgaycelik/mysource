package com.atlassian.jira.jql.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.issue.customfields.manager.OptionsManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfig;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.jql.context.ClauseContext;
import com.atlassian.jira.jql.context.FieldConfigSchemeClauseContextUtil;
import com.atlassian.jira.jql.context.QueryContext;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.util.InjectableComponent;
import com.atlassian.util.concurrent.LazyReference;
import org.apache.commons.collections.CollectionUtils;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;

/**
 * Contains utility methods for processing select option clauses
 *
 * @since v4.0
 */
@InjectableComponent
public class JqlSelectOptionsUtil
{
    private final OptionsManager optionsManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil;

    public JqlSelectOptionsUtil(OptionsManager optionsManager, final FieldConfigSchemeManager fieldConfigSchemeManager, final FieldConfigSchemeClauseContextUtil fieldConfigSchemeClauseContextUtil)
    {
        this.optionsManager = optionsManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.fieldConfigSchemeClauseContextUtil = fieldConfigSchemeClauseContextUtil;
    }

    /**
     * Returns the options that are represented by the {@link com.atlassian.jira.jql.operand.QueryLiteral} that are visible in
     * the {@link com.atlassian.jira.jql.context.QueryContext} for a particular {@link CustomField}.
     *
     * @param customField the customField to retreive options from
     * @param queryContext the context to check if the {@link com.atlassian.jira.issue.customfields.option.Option}s are visible in
     * @param literal the literal to search for {@link com.atlassian.jira.issue.customfields.option.Option}s for
     * @param checkOptionIds If true, the method tries to resolve long literals to option Ids before option values, and
     * vice versa for string literals. If false, literals are only resolved to option values.
     * @return the list of found options, empty literals are ignored; never null.
     */
    public List<Option> getOptions(final CustomField customField, final QueryContext queryContext, final QueryLiteral literal, boolean checkOptionIds)
    {
        if (literal.isEmpty())
        {
            return Collections.emptyList();
        }

        final FieldConfigScheme scheme = fieldConfigSchemeClauseContextUtil.getFieldConfigSchemeFromContext(queryContext, customField);
        if (scheme == null)
        {
            return Collections.emptyList();
        }
        else
        {
            final List<Option> customFieldOptions = getOptions(customField, literal, checkOptionIds);
            return new ArrayList<Option>(CollectionUtils.intersection(customFieldOptions, getOptionsForScheme(scheme)));
        }
    }

    /**
     * Returns the options that are represented by the {@link com.atlassian.jira.jql.operand.QueryLiteral} that are visible to
     * the {@link User} on the {@link com.atlassian.jira.issue.fields.CustomField}.
     *
     * @param customField the customField to retreive options from
     * @param user the User to check if the option is visible to
     * @param literal the literal to search for {@link com.atlassian.jira.issue.customfields.option.Option}s for
     * @param checkOptionIds If true, the method tries to resolve long literals to option Ids before option values, and
     * vice versa for string literals. If false, literals are only resolved to option values.
     * @return the list of found options, empty literals are ignored; never null.
     */
    public List<Option> getOptions(final CustomField customField, final User user, final QueryLiteral literal, boolean checkOptionIds)
    {
        final List<Option> options = getOptions(customField, literal, checkOptionIds);
        final List<Option> visibleOptions = new ArrayList<Option>();
        for (Option option : options)
        {
            if (option != null && optionIsVisible(option, user))
            {
                visibleOptions.add(option);
            }
        }
        return visibleOptions;
    }

    /**
     * @param optionId the id of the option
     * @return the {@link com.atlassian.jira.issue.customfields.option.Option} found or null.
     */
    public Option getOptionById(final Long optionId)
    {
        return optionsManager.findByOptionId(optionId);
    }

    /**
     * Returns all the options possible represented by the {@link com.atlassian.jira.jql.operand.QueryLiteral} for a particular {@link CustomField}.
     *
     * @param customField the customField to retreive options from
     * @param literal the literal to find options for
     * @param checkOptionIds If true, the method tries to resolve long literals to option Ids before option values, and
     * vice versa for string literals. If false, literals are only resolved to option values.
     * @return the list of found options, it will contain only a single null if the literal is empty.
     */
    public List<Option> getOptions(final CustomField customField, final QueryLiteral literal, boolean checkOptionIds)
    {
        if (literal.getLongValue() != null)
        {
            Long value = literal.getLongValue();
            if (checkOptionIds)
            {
                final List<Option> options = getOptionFromLong(customField, value);
                if (!options.isEmpty())
                {
                    return options;
                }
            }
            return getOptionFromString(customField, value.toString());
        }
        else if (literal.getStringValue() != null)
        {
            final String value = literal.getStringValue();
            final List<Option> options = getOptionFromString(customField, value);
            if (options.isEmpty() && checkOptionIds)
            {
                Long lValue = getLong(value);
                if (lValue != null)
                {
                    return getOptionFromLong(customField, lValue);
                }
            }
            return options;
        }
        else
        {
            return Collections.singletonList(null);
        }
    }
    
    /**
     * Retreives all the options for the {@link FieldConfigScheme}.
     *
     * @param fieldConfigScheme the config scheme to retrieve the options from.
     * @return the options for the {@link FieldConfigScheme} in a mutable {@link java.util.List}; never null.
     */
    public List<Option> getOptionsForScheme(final FieldConfigScheme fieldConfigScheme)
    {
        List<Option> optionList = new ArrayList<Option>();
        final Set<FieldConfig> configSet = fieldConfigScheme.getConfigsByConfig().keySet();
        for (FieldConfig fieldConfig : configSet)
        {
            final Options options = optionsManager.getOptions(fieldConfig);
            if (options != null)
            {
                for (Object o : options)
                {
                    final Option option = (Option) o;
                    optionList.add(option);
                    optionList.addAll(option.getChildOptions());
                }
            }
        }
        return optionList;
    }

    boolean optionIsVisible(final Option option, final User user)
    {
        final FieldConfig fieldConfig = option.getRelatedCustomField();
        final FieldConfigScheme scheme = fieldConfigSchemeManager.getConfigSchemeForFieldConfig(fieldConfig);
        if (scheme != null)
        {
            final ClauseContext context = fieldConfigSchemeClauseContextUtil.getContextForConfigScheme(user, scheme);
            return !context.getContexts().isEmpty();
        }
        else
        {
            return false;
        }
    }

    private Long getLong(final String value)
    {
        try
        {
            return Long.parseLong(value);
        }
        catch (NumberFormatException e)
        {
            return null;
        }
    }

    private List<Option> getOptionFromString(final CustomField customField, final String value)
    {
        List<Option> customFieldOptions = new ArrayList<Option>();
        final List<Option> options = optionsManager.findByOptionValue(value);
        for (Option option : options)
        {
            final CustomField field = getFieldFromOption(option);
            if (field != null && field.equals(customField))
            {
                customFieldOptions.add(option);
            }
        }
        return customFieldOptions;
    }

    private List<Option> getOptionFromLong(final CustomField customField, final Long value)
    {
        final Option option = optionsManager.findByOptionId(value);
        if (option != null)
        {
            final CustomField field = getFieldFromOption(option);
            if (field != null && field.equals(customField))
            {
                return Collections.singletonList(option);
            }
        }
        return Collections.emptyList();
    }

    private CustomField getFieldFromOption(Option option)
    {
        try
        {
            final FieldConfig relatedCustomField = option.getRelatedCustomField();
            if (relatedCustomField == null)
            {
                return null;
            }
            return relatedCustomField.getCustomField();
        }
        catch (DataAccessException e)
        {
            return null;
        }
        // JRA-19422 - there can be orphan options in the data such that trying to get the custom field will cause NPE,
        // treat these options as if they do not exist.
        catch (LazyReference.InitializationException ie)
        {
            return null;
        }
    }
}
