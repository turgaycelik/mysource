package com.atlassian.query.history;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * Represents a sequence of {@link HistoryPredicate HistoryPredicates} composed with AND operators such that the
 * resulting predicate is true only if ALL of the composed predicates are true.
 *
 * @since v4.3
 */
public class AndHistoryPredicate implements HistoryPredicate
{

    private final List<HistoryPredicate> predicates;

    public AndHistoryPredicate(List<HistoryPredicate> predicates)
    {
        this.predicates = new ArrayList<HistoryPredicate>(predicates);
    }

    @Override
    public String getDisplayString()
    {
        StringBuilder sb = new StringBuilder();
        for (HistoryPredicate predicate : predicates)
        {
            sb.append(predicate.getDisplayString()).append(" ");
        }
        return sb.toString();
    }

    @Override
    public <R> R accept(PredicateVisitor<R> visitor)
    {
           return visitor.visit(this);
    }

    public List<HistoryPredicate> getPredicates()
    {
        return Collections.unmodifiableList(predicates);
    }
}
