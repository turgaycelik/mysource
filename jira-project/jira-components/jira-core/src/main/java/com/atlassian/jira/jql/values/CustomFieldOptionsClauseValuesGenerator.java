package com.atlassian.jira.jql.values;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.CustomFieldManager;
import com.atlassian.jira.issue.comparator.LocaleSensitiveStringComparator;
import com.atlassian.jira.issue.context.JiraContextNode;
import com.atlassian.jira.issue.context.manager.JiraContextTreeManager;
import com.atlassian.jira.issue.customfields.option.Option;
import com.atlassian.jira.issue.customfields.option.Options;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.config.FieldConfigScheme;
import com.atlassian.jira.issue.fields.config.manager.FieldConfigSchemeManager;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.web.bean.I18nBean;
import org.apache.commons.lang.StringUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Locale;
import java.util.Set;

/**
 * Generates possible values for custom fields which use custom field options.
 *
 * @since v4.0
 */
public class CustomFieldOptionsClauseValuesGenerator implements ClauseValuesGenerator
{
    private final CustomFieldManager customFieldManager;
    private final SearchHandlerManager searchHandlerManager;
    private final FieldConfigSchemeManager fieldConfigSchemeManager;
    private final PermissionManager permissionManager;

    public CustomFieldOptionsClauseValuesGenerator(final CustomFieldManager customFieldManager, final SearchHandlerManager searchHandlerManager,
            final FieldConfigSchemeManager fieldConfigSchemeManager, final PermissionManager permissionManager)
    {
        this.customFieldManager = customFieldManager;
        this.searchHandlerManager = searchHandlerManager;
        this.fieldConfigSchemeManager = fieldConfigSchemeManager;
        this.permissionManager = permissionManager;
    }

    public Results getPossibleValues(final User searcher, final String jqlClauseName, final String valuePrefix, final int maxNumResults)
    {
        final Set<String> optionValues = new LinkedHashSet<String>();

        // We have to get all the options since we can not get the individual values in a way that is naturally ordered
        final Collection<String> fieldIds = searchHandlerManager.getFieldIds(searcher, jqlClauseName);
        if (!fieldIds.isEmpty())
        {
            final Collection<Project> visibleProjects = permissionManager.getProjectObjects(Permissions.BROWSE, searcher);
            for (String fieldId : fieldIds)
            {
                final CustomField customField = customFieldManager.getCustomFieldObject(fieldId);
                if (customField != null)
                {
                    final List<JiraContextNode> contexts = getContextsForCustomField(customField);
                    for (JiraContextNode context : contexts)
                    {
                        final Project contextProject = context.getProjectObject();
                        // We can see the options if it is a global context or it is constrained by a project we have
                        // permission to see.
                        if (contextProject == null || visibleProjects.contains(contextProject))
                        {
                            final Options options = customField.getOptions(null, context);
                            // JRA-19422 - options can be null when we have bad data
                            if (options != null)
                            {
                                for (Option option : options)
                                {
                                    addOptionIfMatch(option, optionValues, valuePrefix);
                                    if (option.getChildOptions() != null)
                                    {
                                        for (Option childOption : option.getChildOptions())
                                        {
                                            addOptionIfMatch(childOption, optionValues, valuePrefix);
                                        }
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
        final ArrayList<String> orderedValues = new ArrayList<String>(optionValues);
        
        Collections.sort(orderedValues, new LocaleSensitiveStringComparator(getLocale(searcher)));

        final List<Result> results = new ArrayList<Result>();
        for (String orderedValue : orderedValues)
        {
            if (results.size() == maxNumResults)
            {
                break;
            }
            results.add(new Result(orderedValue));
        }

        return new Results(results);
    }

    private void addOptionIfMatch(final Option option, final Set<String> optionValues, final String valuePrefix)
    {
        final String lowerCaseOptionVal = option.toString().toLowerCase();
        if (StringUtils.isBlank(valuePrefix) || lowerCaseOptionVal.startsWith(valuePrefix.toLowerCase()))
        {
            optionValues.add(option.toString());
        }
    }

    List<JiraContextNode> getContextsForCustomField(final CustomField customField)
    {
        final List<JiraContextNode> contextNodes = new ArrayList<JiraContextNode>();
        final List<FieldConfigScheme> configSchemes = fieldConfigSchemeManager.getConfigSchemesForField(customField);
        for (FieldConfigScheme configScheme : configSchemes)
        {
            final List<JiraContextNode> contexts = configScheme.getContexts();
            if (contexts != null)
            {
                contextNodes.addAll(contexts);
            }
        }
        if (contextNodes.isEmpty())
        {
            contextNodes.add(getRootContext());
        }
        return contextNodes;
    }

    ///CLOVER:OFF
    JiraContextNode getRootContext()
    {
        return JiraContextTreeManager.getRootContext();
    }
    ///CLOVER:ON

    ///CLOVER:OFF
    Locale getLocale(final User searcher)
    {
        return new I18nBean(searcher).getLocale();
    }
    ///CLOVER:ON

}
