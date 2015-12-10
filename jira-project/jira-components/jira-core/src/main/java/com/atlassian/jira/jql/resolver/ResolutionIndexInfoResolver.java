package com.atlassian.jira.jql.resolver;

import com.atlassian.jira.issue.index.indexers.impl.BaseFieldIndexer;
import com.atlassian.jira.issue.resolution.Resolution;

import java.util.Collections;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Customisation of the {@link IssueConstantInfoResolver} to allow handling of the "unresolved" operand in a special case.
 *
 * @since v4.0
 */
public class ResolutionIndexInfoResolver extends IssueConstantInfoResolver<Resolution>
{
    public static final String UNRESOLVED_OPERAND = "unresolved";
    public static final String QUOTED_UNRESOLVED_VALUE = "\"unresolved\"";

    /**
     * @param resolutionNameResolver the name resolver for resolutions.
     */
    public ResolutionIndexInfoResolver(NameResolver<Resolution> resolutionNameResolver)
    {
        super(resolutionNameResolver);
    }

    @Override
    public List<String> getIndexedValues(final String singleValueOperand)
    {
        // if this is the "unresolved" operand, we will return -1, because it is equivalent to empty.
        // this is a pretty big hack, but it came late in the piece.
        if (isUnresolvedOperand(singleValueOperand))
        {
            return Collections.singletonList(BaseFieldIndexer.NO_VALUE_INDEX_VALUE);
        }
        else
        {
            return super.getIndexedValues(cleanOperand(singleValueOperand));
        }
    }

    private static boolean isUnresolvedOperand(final String operand)
    {
        return UNRESOLVED_OPERAND.equalsIgnoreCase(operand);
    }

    /**
     * @param operand the option argument
     * @return the non-quoted string of the {@link #UNRESOLVED_OPERAND} argument if it was specified; the input otherwise.
     */
    static String cleanOperand(final String operand)
    {
        Pattern p = Pattern.compile("['\"]+unresolved['\"]+", Pattern.CASE_INSENSITIVE);
        final Matcher m = p.matcher(operand);
        if (m.find() &&
            ((operand.startsWith("'") && operand.endsWith("'"))
            || (operand.startsWith("\"") && operand.endsWith("\""))))
        {
            return operand.substring(1, operand.length() - 1);
        }
        else
        {
            return operand;
        }
    }
}
