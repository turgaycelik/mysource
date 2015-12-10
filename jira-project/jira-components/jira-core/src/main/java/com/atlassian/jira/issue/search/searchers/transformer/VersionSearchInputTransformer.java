package com.atlassian.jira.issue.search.searchers.transformer;

import com.atlassian.jira.issue.search.ClauseNames;
import com.atlassian.jira.jql.operand.JqlOperandResolver;
import com.atlassian.jira.jql.operand.QueryLiteral;
import com.atlassian.jira.jql.resolver.VersionResolver;
import com.atlassian.jira.plugin.jql.function.AllReleasedVersionsFunction;
import com.atlassian.jira.plugin.jql.function.AllUnreleasedVersionsFunction;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.project.version.VersionManager;
import com.atlassian.query.operand.EmptyOperand;
import com.atlassian.query.operand.FunctionOperand;
import com.atlassian.query.operand.Operand;
import com.atlassian.query.operand.SingleValueOperand;

import java.util.List;
import java.util.Set;
import javax.annotation.Nonnull;

/**
 * A version-specific {@link IdIndexedSearchInputTransformer}.
 *
 * @since v4.0
 */
public class VersionSearchInputTransformer extends AbstractProjectConstantsSearchInputTransformer<Version, VersionSearchInput>
{
    public VersionSearchInputTransformer(ClauseNames clauseNames, String urlParameterName, JqlOperandResolver operandResolver,
            FieldFlagOperandRegistry fieldFlagOperandRegistry, VersionResolver versionResolver)
    {
        super(clauseNames, urlParameterName, operandResolver, fieldFlagOperandRegistry, versionResolver);
    }

    @Nonnull
    @Override
    VersionSearchInput parseInputParam(String[] parts)
    {
        if (parts[0].equals(VersionManager.NO_VERSIONS))
        {
            return VersionSearchInput.noVersions();
        }
        if (parts[0].equals(VersionManager.ALL_RELEASED_VERSIONS))
        {
            return VersionSearchInput.allReleased();
        }
        if (parts[0].equals(VersionManager.ALL_UNRELEASED_VERSIONS))
        {
            return VersionSearchInput.allUnreleased();
        }
        if (parts[0].equals("id"))
        {
            return VersionSearchInput.version(parts[1]);
        }

        return VersionSearchInput.version(parts[0]);
    }

    @Nonnull
    @Override
    VersionSearchInput noValueInput()
    {
        return VersionSearchInput.noVersions();
    }

    @Nonnull
    @Override
    VersionSearchInput inputValue(String value)
    {
        return VersionSearchInput.version(value);
    }

    @Override
    void parseFunctionOperand(FunctionOperand operand, Set<VersionSearchInput> values)
    {
        if (AllReleasedVersionsFunction.FUNCTION_RELEASED_VERSIONS.equals(operand.getName()))
        {
            values.add(VersionSearchInput.allReleased());
        }
        else if (AllUnreleasedVersionsFunction.FUNCTION_UNRELEASED_VERSIONS.equals(operand.getName()))
        {
            values.add(VersionSearchInput.allUnreleased());
        }
    }

    @Nonnull
    @Override
    Operand parseInputValue(VersionSearchInput value)
    {
        if (value.isAllUnreleased())
        {
            return new FunctionOperand(AllUnreleasedVersionsFunction.FUNCTION_UNRELEASED_VERSIONS);
        }
        if (value.isAllReleased())
        {
            return new FunctionOperand(AllReleasedVersionsFunction.FUNCTION_RELEASED_VERSIONS);
        }
        if (value.isNoVersion())
        {
            return EmptyOperand.EMPTY;
        }

        return new SingleValueOperand(value.getValue());
    }

    boolean checkClauseValuesForBasic(QueryLiteral queryLiteral)
    {
        if (queryLiteral.isEmpty())
        {
            return false;
        }
        List<String> ids;
        if (queryLiteral.getStringValue() != null)
        {
            ids = nameResolver.getIdsFromName(queryLiteral.asString());
        }
        else if (queryLiteral.getLongValue() != null)
        {
            //Basic view will only allow an number if it is used as a version name and not a version ID.
            if (nameResolver.idExists(queryLiteral.getLongValue()))
            {
                return false;
            }
            else
            {
                ids = nameResolver.getIdsFromName(queryLiteral.asString());
            }
        }
        else
        {
            return false;
        }
        //The basic nagivator will only accept names.
        for (String id : ids)
        {
            Long lid = parseLong(id);
            if (lid != null)
            {
                final Version version = nameResolver.get(lid);
                if (version != null && !version.isArchived())
                {
                    return true;
                }
            }
        }
        return false;
    }

    private static Long parseLong(String str)
    {
        try
        {
            return Long.valueOf(str);
        }
        catch (NumberFormatException ignored)
        {
            return null;
        }
    }
}
