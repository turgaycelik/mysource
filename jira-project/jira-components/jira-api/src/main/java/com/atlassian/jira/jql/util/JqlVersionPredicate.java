package com.atlassian.jira.jql.util;

import com.atlassian.jira.issue.comparator.VersionComparator;
import com.atlassian.jira.project.version.Version;
import com.atlassian.jira.util.EvaluateAllPredicate;
import com.atlassian.jira.util.Predicate;
import com.atlassian.query.operator.Operator;

/**
 * The JQL relational predicate for {@link com.atlassian.jira.project.version.Version}s. If two versions
 * are in different projects, all comparisons are false. 
 *
 * @since v4.0
 */
public class JqlVersionPredicate extends EvaluateAllPredicate<Version>
{
    public JqlVersionPredicate(final Operator operator, final Version version)
    {
        super(operator.getPredicateForValue(VersionComparator.COMPARATOR, version), new Predicate<Version>()
        {
            public boolean evaluate(final Version input)
            {
                Long inputProjId = input.getProjectObject().getId();
                Long domainProjId = version.getProjectObject().getId();
                return inputProjId.equals(domainProjId);
            }
        });
    }
}
