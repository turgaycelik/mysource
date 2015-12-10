package com.atlassian.jira.issue.search.searchers.util;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.query.clause.Clause;

import java.util.Map;

/**
 * Inteface for classues that parse JQL clauses and determine if they are suitable for usage in the Navigator or Search URL.
 *
 * @since v4.0
 */
public interface DateSearcherInputHelper
{
    /**
     * Take the passed clause and try and get the equivalent navigator parameters.
     *
     * Note: this also performs a validity check on the structure of the clause to determine if it fits for the Navigator.
     * Therefore, it is not required to check this before the call is made.
     *
     * @param clause the clause to convert.
     * @param user the user trying to convert the clause.
     * @param allowTimeComponent if true, date values which aren't midnight dates will be returned as midnight
     * dates (thereby losing precision)
     * @return on success a map of navigator param -> value, or null on failure. The map will only contain the params
     * that were present in the clause.
     */
    ConvertClauseResult convertClause(Clause clause, User user, boolean allowTimeComponent);

    public static class ConvertClauseResult
    {
        private Map<String, String> fields;
        private boolean fitsFilterForm;

        public ConvertClauseResult(final Map<String, String> fields, final boolean fitsFilterForm)
        {
            this.fields = fields;
            this.fitsFilterForm = fitsFilterForm;
        }

        public Map<String, String> getFields()
        {
            return fields;
        }

        public boolean fitsFilterForm()
        {
            return fitsFilterForm;
        }
    }
}
