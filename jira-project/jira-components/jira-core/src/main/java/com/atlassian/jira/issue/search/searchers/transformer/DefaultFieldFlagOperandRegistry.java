package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.config.ConstantsManager;
import com.atlassian.jira.issue.fields.ResolutionSystemField;
import com.atlassian.jira.issue.search.constants.SystemSearchConstants;
import com.atlassian.jira.plugin.jql.function.AllReleasedVersionsFunction;
import com.atlassian.jira.plugin.jql.function.AllStandardIssueTypesFunction;
import com.atlassian.jira.plugin.jql.function.AllSubIssueTypesFunction;
import com.atlassian.jira.plugin.jql.function.AllUnreleasedVersionsFunction;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;
import org.apache.commons.collections.map.CaseInsensitiveMap;

import java.util.Collections;
import java.util.LinkedHashSet;
import java.util.Map;
import java.util.Set;

/**
 * The default field flag operand registry that maps field name and
 * navigator flag value pairs to their appropiate operand.
 *
 * @since v4.0
 */
public class DefaultFieldFlagOperandRegistry implements FieldFlagOperandRegistry
{
    private static final Map<String, Map<String, Operand>> REGISTRY;

    static
    {
        final Map<String, Operand> issueTypeFlags = MapBuilder.<String, Operand> newBuilder() //
        .add(ConstantsManager.ALL_STANDARD_ISSUE_TYPES, new FunctionOperand(AllStandardIssueTypesFunction.FUNCTION_STANDARD_ISSUE_TYPES)) //
        .add(ConstantsManager.ALL_SUB_TASK_ISSUE_TYPES, new FunctionOperand(AllSubIssueTypesFunction.FUNCTION_SUB_ISSUE_TYPES)) //
        .toMap();

        final Map<String, Operand> versionFlags = MapBuilder.<String, Operand> newBuilder() //
        .add(VersionManager.ALL_RELEASED_VERSIONS, new FunctionOperand(AllReleasedVersionsFunction.FUNCTION_RELEASED_VERSIONS)) //
        .add(VersionManager.ALL_UNRELEASED_VERSIONS, new FunctionOperand(AllUnreleasedVersionsFunction.FUNCTION_UNRELEASED_VERSIONS)) //
        .add(VersionManager.NO_VERSIONS, EmptyOperand.EMPTY) //
        .toMap();

        final Map<String, Operand> componentFlags = MapBuilder.<String, Operand> newBuilder() //
        .add(SystemSearchConstants.forComponent().getEmptySelectFlag(), EmptyOperand.EMPTY) //
        .toMap();

        final Map<String, Operand> resolutionFlags = MapBuilder.<String, Operand> newBuilder() //
        .add(ResolutionSystemField.UNRESOLVED_VALUE.toString(), new SingleValueOperand(ResolutionSystemField.UNRESOLVED_OPERAND)) //
        .toMap();

        @SuppressWarnings ({ "unchecked" }) Map<String, Map<String, Operand>> tmpRegistry = new CaseInsensitiveMap();
        
        tmpRegistry.put(SystemSearchConstants.forIssueType().getJqlClauseNames().getPrimaryName(), issueTypeFlags);
        tmpRegistry.put(SystemSearchConstants.forComponent().getJqlClauseNames().getPrimaryName(), componentFlags);
        tmpRegistry.put(SystemSearchConstants.forAffectedVersion().getJqlClauseNames().getPrimaryName(), versionFlags);
        tmpRegistry.put(SystemSearchConstants.forFixForVersion().getJqlClauseNames().getPrimaryName(), versionFlags);
        tmpRegistry.put(SystemSearchConstants.forResolution().getJqlClauseNames().getPrimaryName(), resolutionFlags);

        REGISTRY = Collections.unmodifiableMap(tmpRegistry);
    }

    public Operand getOperandForFlag(final String fieldName, final String flagValue)
    {
        final Map<String, Operand> fieldFlags = REGISTRY.get(fieldName);
        if (fieldFlags != null)
        {
            return fieldFlags.get(flagValue);
        }
        return null;
    }

    public Set<String> getFlagForOperand(final String fieldName, final Operand operand)
    {
        final Map<String, Operand> fieldFlags = REGISTRY.get(fieldName);
        if (fieldFlags != null)
        {
            final Set<String> flags = new LinkedHashSet<String>();
            for (final Map.Entry<String, Operand> entry : fieldFlags.entrySet())
            {
                if (entry.getValue().equals(operand))
                {
                    flags.add(entry.getKey());
                }
            }
            if (!flags.isEmpty())
            {
                return flags;
            }
        }
        return null;
    }
}
