package com.atlassian.jira.jql.values;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.fields.ResolutionSystemField;
import com.atlassian.jira.util.Function;
import com.atlassian.jira.util.collect.CollectionUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Gets all resolution values
 *
 * @since v4.0
 */
public class ResolutionClauseValuesGenerator extends AbstractIssueConstantValuesGenerator
{
    private static final Pattern UNRESOLVED_PATTERN = Pattern.compile("['\"]*unresolved['\"]*", Pattern.CASE_INSENSITIVE);

    private final ConstantsManager constantsManager;

    public ResolutionClauseValuesGenerator(final ConstantsManager constantsManager)
    {
        this.constantsManager = constantsManager;
    }

    @Override
    protected List<String> getAllConstantNames()
    {
        // if any existing resolutions were called Unresolved, we need to quote and escape them
        final List<String> constantNames = new ArrayList<String>(CollectionUtil.transform(getAllConstants().iterator(), resolutionToNameFunction));
        // add the Unresolved value as an option
        constantNames.add(ResolutionSystemField.UNRESOLVED_OPERAND);
        return constantNames;
    }

    protected List<IssueConstant> getAllConstants()
    {
        return new ArrayList<IssueConstant>(constantsManager.getResolutionObjects());
    }

    static String quoteName(final String name)
    {
        final Matcher m = UNRESOLVED_PATTERN.matcher(name);
        if (m.find())
        {
            // simply surround with quotes. autocomplete code will take care of appropriate escaping
            return "\"" + name + "\"";
        }
        else
        {
            return name;
        }
    }

    private static final Function<IssueConstant, String> resolutionToNameFunction = new Function<IssueConstant, String>()
    {
        public String get(final IssueConstant input)
        {
            return quoteName(input.getName());
        }
    };
}
