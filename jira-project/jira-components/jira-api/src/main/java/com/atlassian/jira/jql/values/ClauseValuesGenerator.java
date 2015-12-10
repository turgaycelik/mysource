package com.atlassian.jira.jql.values;

import com.atlassian.annotations.PublicSpi;
import com.atlassian.crowd.embedded.api.User;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.List;

import static com.atlassian.jira.util.dbc.Assertions.notNull;

/**
 * Generates the possible values for a field.
 *
 * @since v4.0
 */
@PublicSpi
public interface ClauseValuesGenerator
{

    /**
     * Will return a string representation of only the possible values that match the value prefix for this clause.
     * This should not return more results than specified in maxNumResults. If it is possible this should use the
     * maxNumResults to efficiently generate the results.
     *
     * The contract of this method is that if the valuePrefix exactly (minus case) matches the suggestion then we suggest it.
     * This will allow users to verify in their own minds that even though they have typed
     * the full value, it is still valid.
     *
     * @param searcher the user preforming the search.
     * @param jqlClauseName the jql clause name that was entered by the user, represents the identifier that was used
     * to find this values generator. Note: for custom fields this can be used to identify the custom field we are
     * dealing with.
     * @param valuePrefix the portion of the value that has already been provided by the user.
     * @param maxNumResults the maximun number of results to return.
     * @return a string value of the clause values that match the provided value prefix, empty list if none
     * match.
     */
    Results getPossibleValues(User searcher, String jqlClauseName, String valuePrefix, int maxNumResults);

    public static class Results
    {
        private final List<Result> results;

        public Results(final List<Result> results)
        {
            this.results = new ArrayList<Result>(notNull("results", results));
        }

        public List<Result> getResults()
        {
            return Collections.unmodifiableList(results);
        }
    }

    public static class Result
    {
        private final String value;
        private final String [] displayNameParts;

        public Result(final String value)
        {
            this.value = notNull("value", value);
            this.displayNameParts = new String[] {value};
        }

        public Result(final String value, final String displayName)
        {
            this.value = notNull("value", value);
            this.displayNameParts = new String[] {notNull("displayName", displayName)};
        }

        /**
         * Use this if you want to have multiple portions of your display string that will be concatenated with
         * a space in between. Each portion of this display part will be anaylized for the matching string
         * and will have bold tags around the matching parts.
         *
         * @param value the value that will be used to complete.
         * @param displayNameParts the parts of the display name that will be used to show the user what matched,
         * will be searched for matching string.
         */
        public Result(final String value, final String [] displayNameParts)
        {
            this.value = notNull("value", value);
            this.displayNameParts = notNull("displayNameParts", displayNameParts);
        }

        public String getValue()
        {
            return value;
        }

        public String[] getDisplayNameParts()
        {
            return displayNameParts;
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

            final Result result = (Result) o;

            if (!Arrays.equals(displayNameParts, result.displayNameParts))
            {
                return false;
            }
            if (value != null ? !value.equals(result.value) : result.value != null)
            {
                return false;
            }

            return true;
        }

        @Override
        public int hashCode()
        {
            int result = value != null ? value.hashCode() : 0;
            result = 31 * result + (displayNameParts != null ? Arrays.hashCode(displayNameParts) : 0);
            return result;
        }

        @Override
        public String toString()
        {
            return "Result{" +
                   "value='" + value + '\'' +
                   ", displayNameParts=" + (displayNameParts == null ? null : Arrays.asList(displayNameParts)) +
                   '}';
        }
    }
}
