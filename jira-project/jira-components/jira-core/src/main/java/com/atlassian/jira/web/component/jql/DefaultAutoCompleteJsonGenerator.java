package com.atlassian.jira.web.component.jql;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.fugue.Option;
import com.atlassian.jira.JiraDataType;
import com.atlassian.jira.issue.comparator.LocaleSensitiveStringComparator;
import com.atlassian.jira.issue.fields.CustomField;
import com.atlassian.jira.issue.fields.Field;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.NavigableField;
import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.issue.search.managers.SearchHandlerManager;
import com.atlassian.jira.jql.ClauseHandler;
import com.atlassian.jira.jql.ClauseInformation;
import com.atlassian.jira.jql.NoOpClauseHandler;
import com.atlassian.jira.jql.ValueGeneratingClauseHandler;
import com.atlassian.jira.jql.operand.FunctionOperandHandler;
import com.atlassian.jira.jql.operand.registry.JqlFunctionHandlerRegistry;
import com.atlassian.jira.jql.util.JqlCustomFieldId;
import com.atlassian.jira.jql.util.JqlStringSupport;
import com.atlassian.jira.plugin.jql.function.CurrentUserFunction;
import com.atlassian.jira.util.json.JSONArray;
import com.atlassian.jira.util.json.JSONException;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.ozymandias.SafePluginPointAccess;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operator.Operator;

import com.opensymphony.util.TextUtils;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;
import java.util.concurrent.Callable;

/**
 * @since v4.0
 */
public class DefaultAutoCompleteJsonGenerator implements AutoCompleteJsonGenerator
{
    private final SearchHandlerManager searchHandlerManager;
    private final JqlStringSupport jqlStringSupport;
    private final FieldManager fieldManager;
    private final JqlFunctionHandlerRegistry jqlFunctionHandlerRegistry;

    public DefaultAutoCompleteJsonGenerator(final SearchHandlerManager searchHandlerManager, final JqlStringSupport jqlStringSupport,
            final FieldManager fieldManager, final JqlFunctionHandlerRegistry jqlFunctionHandlerRegistry)
    {
        this.searchHandlerManager = searchHandlerManager;
        this.jqlStringSupport = jqlStringSupport;
        this.fieldManager = fieldManager;
        this.jqlFunctionHandlerRegistry = jqlFunctionHandlerRegistry;
    }

    public String getVisibleFieldNamesJson(final User user, final Locale locale) throws JSONException
    {
        Map<String, ClauseNameValue> visibleNames = new TreeMap<String, ClauseNameValue>(String.CASE_INSENSITIVE_ORDER);

        // The key in the map is the actual custom field name, the value is a list of ClauseNames that correspond
        // to that custom field name (remember you can have multiple custom fields with the same name).
        Map<String, Set<ClauseNameValue>> customFieldNameToClauseNames = new HashMap<String, Set<ClauseNameValue>>();

        final Collection<ClauseHandler> handlers = searchHandlerManager.getVisibleClauseHandlers(user);
        for (ClauseHandler clauseHandler : handlers)
        {
            final ClauseInformation information = clauseHandler.getInformation();
            final ClauseNames visibleClauseName = information.getJqlClauseNames();
            final String fieldId = information.getFieldId();
            final Set<Operator> supportedOperators = information.getSupportedOperators();
            final JiraDataType supportedType = information.getDataType();
            final Field field = fieldManager.getField(fieldId);
            final boolean isAutoCompleteable = clauseHandler instanceof ValueGeneratingClauseHandler;
            final boolean isOrderByable = field instanceof NavigableField;
            // We do not want to include NoOpClauseHandlers since they are really just place-holders for fields
            // that are sortable but not searchable
            final boolean isSearchable = !(clauseHandler instanceof NoOpClauseHandler);

            if (isCustomFieldClauseNames(visibleClauseName) && field != null)
            {
                // Lets record the fieldName-cf[xxxxx]
                final String name;
                if (field instanceof CustomField)
                {
                    name = ((CustomField) field).getUntranslatedName();
                }
                else
                {
                    name = field.getName();
                }
                Set<ClauseNameValue> namesList = customFieldNameToClauseNames.get(name);
                if (namesList == null)
                {
                    namesList = new HashSet<ClauseNameValue>();
                    customFieldNameToClauseNames.put(name, namesList);
                }
                namesList.add(new ClauseNameValue(visibleClauseName.getPrimaryName(), isAutoCompleteable, isOrderByable, isSearchable,
                        visibleClauseName.getJqlFieldNames().size() == 1, supportedOperators, supportedType));

                // Lets record any clauseNameAliases-cf[xxxxx], this will not happen at the moment since custom fields never have more than one alias
                for (String s : visibleClauseName.getJqlFieldNames())
                {
                    if (!JqlCustomFieldId.isJqlCustomFieldId(s) && !s.equalsIgnoreCase(name))
                    {
                        Set<ClauseNameValue> clauseNamesList = customFieldNameToClauseNames.get(name);
                        if (clauseNamesList == null)
                        {
                            clauseNamesList = new HashSet<ClauseNameValue>();
                            customFieldNameToClauseNames.put(s, clauseNamesList);
                        }
                        clauseNamesList.add(new ClauseNameValue(visibleClauseName.getPrimaryName(), isAutoCompleteable,
                                isOrderByable, isSearchable, visibleClauseName.getJqlFieldNames().size() == 1, supportedOperators, supportedType));
                    }
                }
            }
            else
            {
                for (String clauseName : visibleClauseName.getJqlFieldNames())
                {
                    // Lets always add the system clause names
                    visibleNames.put(clauseName, new ClauseNameValue(clauseName, isAutoCompleteable, isOrderByable, isSearchable, false, supportedOperators, supportedType));
                }
            }
        }

        // Lets run through the custom fields stuff and decide what the displayName and values will be
        for (Map.Entry<String, Set<ClauseNameValue>> entry : customFieldNameToClauseNames.entrySet())
        {
            final String customFieldName = entry.getKey();
            final Set<ClauseNameValue> clauseNameValues = entry.getValue();
            if (clauseNameValues.size() == 1)
            {
                final ClauseNameValue cfClauseValue = clauseNameValues.iterator().next();
                String displayName = customFieldName + " - " + cfClauseValue.getClauseNameValue();
                // Right, we don't conflict with any other custom field names, lets make sure we don't conflict with sys field names
                if (!visibleNames.containsKey(customFieldName) && !cfClauseValue.isMustUseNameValue())
                {
                    // We know we are cool to use our user supplied name
                    visibleNames.put(displayName, new ClauseNameValue(customFieldName, cfClauseValue.getClauseNameValue(), cfClauseValue.isAutoCompleteable(),
                            cfClauseValue.isOrderByable(), cfClauseValue.isSearchable(), cfClauseValue.isMustUseNameValue(), cfClauseValue.getSupportedOperators(),
                            cfClauseValue.getSupportedType()));
                }
                else
                {
                    visibleNames.put(displayName, cfClauseValue);
                }
            }
            else
            {
                // Our custom field name is conflicting with another field name so we want to use the cf[xxxxx] as the value
                for (ClauseNameValue cfClauseValue : clauseNameValues)
                {
                    String displayName = customFieldName + " - " + cfClauseValue.getClauseNameValue();
                    visibleNames.put(displayName, cfClauseValue);
                }
            }
        }


        List<String> visibleNamesList = new ArrayList<String>(visibleNames.keySet());
        // Lets sort all the names in a case-insensitive way
        Collections.sort(visibleNamesList, new LocaleSensitiveStringComparator(locale));

        // Now lets put it into a JSONArray
        JSONArray results = new JSONArray();

        for (String fieldName : visibleNamesList)
        {
            final JSONObject jsonObj = new JSONObject();
            final ClauseNameValue clauseNameValue = visibleNames.get(fieldName);
            jsonObj.put("value", jqlStringSupport.encodeFieldName(clauseNameValue.getClauseNameValue()));
            jsonObj.put("displayName", htmlEncode(fieldName));
            if (clauseNameValue.isAutoCompleteable())
            {
                jsonObj.put("auto", "true");
            }
            if (clauseNameValue.isOrderByable())
            {
                jsonObj.put("orderable", "true");
            }
            if (clauseNameValue.isSearchable())
            {
                jsonObj.put("searchable", "true");
            }
            if (clauseNameValue.getCustomFieldIdClauseName() != null)
            {
                jsonObj.put("cfid", clauseNameValue.getCustomFieldIdClauseName());
            }
            JSONArray supOpers = new JSONArray();
            for (Operator operator : clauseNameValue.getSupportedOperators())
            {
                supOpers.put(operator.getDisplayString());
            }
            jsonObj.put("operators", supOpers);
            // Include the clauseTypes type
            JSONArray supportedTypes = new JSONArray();
            for (String typeString : clauseNameValue.getSupportedType().asStrings())
            {
                supportedTypes.put(typeString);
            }
            jsonObj.put("types", supportedTypes);


            results.put(jsonObj);
        }

        return results.toString();
    }

    public String getVisibleFunctionNamesJson(final User user, final Locale locale) throws JSONException
    {
        final List<String> functionNames = jqlFunctionHandlerRegistry.getAllFunctionNames();

        // Lets sort all the names in a case-insensitive way
        Collections.sort(functionNames, new LocaleSensitiveStringComparator(locale));

        // Now lets put it into a JSONArray
        JSONArray results = new JSONArray();

        for (String functionName : functionNames)
        {
            // This check ensures that the currentUser function is only available when there is a logged in user.
            if (!CurrentUserFunction.FUNCTION_CURRENT_USER.equals(functionName) || user != null)
            {
                final FunctionOperandHandler functionHandler = jqlFunctionHandlerRegistry.getOperandHandler(new FunctionOperand(functionName));
                final JSONObject jsonObj = new JSONObject();
                final Option<Integer> minArgumentsFromPlugin = SafePluginPointAccess.call(new Callable<Integer>()
                {
                    @Override
                    public Integer call() throws Exception
                    {
                        return functionHandler.getJqlFunction().getMinimumNumberOfExpectedArguments();
                    }
                });
                if (minArgumentsFromPlugin.isEmpty()) // error in plugin code, pretend the jqlFunction was not found
                {
                    continue;
                }
                final int minArguments = minArgumentsFromPlugin.get();
                StringBuilder argPart = new StringBuilder("(");
                for (int i = 0; i < minArguments; i++)
                {
                    if (i != 0)
                    {
                        argPart.append(", ");
                    }
                    argPart.append("\"\"");
                }
                argPart.append(")");
                jsonObj.put("value", jqlStringSupport.encodeFunctionName(functionName) + argPart.toString());
                jsonObj.put("displayName", htmlEncode(functionName) + argPart.toString());
                if (functionHandler.isList())
                {
                    jsonObj.put("isList", "true");
                }
                // Include the functions type
                final JSONArray functionTypes = new JSONArray();
                for (final String typeString : SafePluginPointAccess.call(new Callable<Collection<String>>()
                        {
                            @Override
                            public Collection<String> call() throws Exception
                            {
                                return functionHandler.getJqlFunction().getDataType().asStrings();
                            }
                        }).getOrElse(Collections.<String>emptyList()))
                {
                    functionTypes.put(typeString);
                }
                jsonObj.put("types", functionTypes);
                results.put(jsonObj);
            }

        }

        return results.toString();
    }

    public String getJqlReservedWordsJson() throws JSONException
    {
        final Set<String> reservedWords = jqlStringSupport.getJqlReservedWords();
        JSONArray results = new JSONArray();
        for (String reservedWord : reservedWords)
        {
            results.put(reservedWord);
        }
        return results.toString();
    }

    String htmlEncode(String string)
    {
        return TextUtils.htmlEncode(string);
    }

    private boolean isCustomFieldClauseNames(final ClauseNames clauseNames)
    {
        for (String clauseName : clauseNames.getJqlFieldNames())
        {
            if (JqlCustomFieldId.isJqlCustomFieldId(clauseName))
            {
                return true;
            }
        }
        return false;
    }

    private static class ClauseNameValue
    {
        private final String clauseNameValue;
        private final String customFieldIdClauseName;
        private final boolean autoCompleteable;
        private final boolean orderByable;
        private final boolean searchable;
        private final boolean mustUseNameValue;
        private final Set<Operator> supportedOperators;
        private final JiraDataType supportedType;

        private ClauseNameValue(final String clauseNameValue, final boolean isAutoCompleteable,
                final boolean isOrderByable, final boolean isSearchable, final boolean mustUseNameValue,
                final Set<Operator> supportedOperators, final JiraDataType supportedType)
        {
            this.clauseNameValue = clauseNameValue;
            this.autoCompleteable = isAutoCompleteable;
            this.orderByable = isOrderByable;
            this.searchable = isSearchable;
            this.mustUseNameValue = mustUseNameValue;
            this.supportedOperators = supportedOperators;
            this.supportedType = supportedType;
            this.customFieldIdClauseName = null;
        }

        private ClauseNameValue(final String clauseNameValue, final String customFieldIdClauseName,
                final boolean isAutoCompleteable, final boolean isOrderByable, final boolean isSearchable,
                final boolean mustUseNameValue, final Set<Operator> supportedOperators,
                final JiraDataType supportedType)
        {
            this.clauseNameValue = clauseNameValue;
            this.customFieldIdClauseName = customFieldIdClauseName;
            this.autoCompleteable = isAutoCompleteable;
            this.orderByable = isOrderByable;
            this.searchable = isSearchable;
            this.mustUseNameValue = mustUseNameValue;
            this.supportedOperators = supportedOperators;
            this.supportedType = supportedType;
        }

        public String getClauseNameValue()
        {
            return clauseNameValue;
        }

        public String getCustomFieldIdClauseName()
        {
            return customFieldIdClauseName;
        }

        public boolean isAutoCompleteable()
        {
            return autoCompleteable;
        }

        public boolean isOrderByable()
        {
            return orderByable;
        }

        public boolean isMustUseNameValue()
        {
            return mustUseNameValue;
        }

        public boolean isSearchable()
        {
            return searchable;
        }

        public Set<Operator> getSupportedOperators()
        {
            return supportedOperators;
        }

        public JiraDataType getSupportedType()
        {
            return supportedType;
        }

        @SuppressWarnings ({ "RedundantIfStatement" })
        @Override
        public boolean equals(final Object o)
        {
            if (this == o)
            {
                return true;
            }
            if (o == null || getClass() != o.getClass())
            {
                return false;
            }

            final ClauseNameValue that = (ClauseNameValue) o;

            if (autoCompleteable != that.autoCompleteable)
            {
                return false;
            }
            if (mustUseNameValue != that.mustUseNameValue)
            {
                return false;
            }
            if (orderByable != that.orderByable)
            {
                return false;
            }
            if (searchable != that.searchable)
            {
                return false;
            }
            if (clauseNameValue != null ? !clauseNameValue.equals(that.clauseNameValue) : that.clauseNameValue != null)
            {
                return false;
            }
            if (customFieldIdClauseName != null ? !customFieldIdClauseName.equals(that.customFieldIdClauseName) : that.customFieldIdClauseName != null)
            {
                return false;
            }
            if (supportedOperators != null ? !supportedOperators.equals(that.supportedOperators) : that.supportedOperators != null)
            {
                return false;
            }
            if (supportedType != null ? !supportedType.equals(that.supportedType) : that.supportedType != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = clauseNameValue != null ? clauseNameValue.hashCode() : 0;
            result = 31 * result + (customFieldIdClauseName != null ? customFieldIdClauseName.hashCode() : 0);
            result = 31 * result + (autoCompleteable ? 1 : 0);
            result = 31 * result + (orderByable ? 1 : 0);
            result = 31 * result + (searchable ? 1 : 0);
            result = 31 * result + (mustUseNameValue ? 1 : 0);
            result = 31 * result + (supportedOperators != null ? supportedOperators.hashCode() : 0);
            result = 31 * result + (supportedType != null ? supportedType.hashCode() : 0);
            return result;
        }
    }
}
