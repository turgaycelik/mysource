package com.atlassian.jira.issue.search.constants;

import com.atlassian.jira.issue.index.DocumentConstants;
import com.atlassian.jira.issue.search.ClauseNames;
import static com.atlassian.jira.util.dbc.Assertions.notBlank;
import com.atlassian.query.operator.Operator;
import net.jcip.annotations.ThreadSafe;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import java.util.Set;

/**
 * Holds searching constants for user system fields.
 *
 * @since v4.0
 */
@ThreadSafe
public final class UserFieldSearchConstantsWithEmpty extends UserFieldSearchConstants
{

    private final String emptySelectFlag;
    private final String emptyIndexValue;

    public UserFieldSearchConstantsWithEmpty(final String indexField, final ClauseNames names, final String fieldUrlParameter, final String selectUrlParameter,
                                    final String searcherId, final String emptySelectFlag, final String fieldId, final String currentUserSelectFlag,
                                    final String specificUserSelectFlag, final String specificGroupSelectFlag, final String emptyIndexValue,
                                    final Set<Operator> supportedOperators)
    {
        super(indexField, names, fieldUrlParameter, selectUrlParameter, searcherId, fieldId, currentUserSelectFlag, specificUserSelectFlag, specificGroupSelectFlag, supportedOperators);
        this.emptySelectFlag = emptySelectFlag;
        this.emptyIndexValue = emptyIndexValue;
    }

    public UserFieldSearchConstantsWithEmpty(final String indexField, final String jqlClauseName,
            final String fieldUrlParameter, final String selectUrlParameter,
            final String searcherId, final String emptySelectFlag,
            final String fieldId, final Set<Operator> supportedOperators)
    {
        this(indexField, new ClauseNames(notBlank("jqlClauseNames", jqlClauseName)), fieldUrlParameter, selectUrlParameter,
             searcherId, emptySelectFlag, fieldId, DocumentConstants.ISSUE_CURRENT_USER,
                DocumentConstants.SPECIFIC_USER, DocumentConstants.SPECIFIC_GROUP, emptySelectFlag, supportedOperators);
    }

    public String getEmptySelectFlag()
    {
        return emptySelectFlag;
    }

    public String getEmptyIndexValue()
    {
        return emptyIndexValue;
    }

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
        if (!super.equals(o))
        {
            return false;
        }

        final UserFieldSearchConstantsWithEmpty that = (UserFieldSearchConstantsWithEmpty) o;

        if (!emptyIndexValue.equals(that.emptyIndexValue))
        {
            return false;
        }
        if (!emptySelectFlag.equals(that.emptySelectFlag))
        {
            return false;
        }

        return true;
    }

    @Override
    public int hashCode()
    {
        int result = super.hashCode();
        result = 31 * result + emptySelectFlag.hashCode();
        result = 31 * result + emptyIndexValue.hashCode();
        return result;
    }

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }
}
