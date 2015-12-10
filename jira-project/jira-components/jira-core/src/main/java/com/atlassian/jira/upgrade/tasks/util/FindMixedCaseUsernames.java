
package com.atlassian.jira.upgrade.tasks.util;

import com.atlassian.crowd.embedded.impl.IdentifierUtils;
import com.atlassian.jira.entity.EntityListConsumer;
import com.atlassian.jira.entity.Select;
import org.ofbiz.core.entity.GenericValue;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Factory for various consumers that are handy in upgrade tasks that need to force
 * old username references to lowercase.  Null values are tolerated/ignored, and an
 * internal cache is used to reduce redundant effort where possible.
 */
public class FindMixedCaseUsernames
{
    /**
     * Consumer that returns a map of mixed-case usernames that were found from a
     * {@link Select#distinctString(String)} query to their lowercase equivalents.
     * <p/>
     * The resulting map might, for example, have a key of {@code "Fred"} with
     * a corresponding value of {@code "fred"}.
     */
    public static EntityListConsumer<String, Map<String,String>> fromStrings()
    {
        return new FromStringsConsumer();
    }

    static class FromStringsConsumer implements EntityListConsumer<String, Map<String,String>>
    {
        private final Set<String> seen = new HashSet<String>();
        private final Map<String,String> usernames = new HashMap<String,String>();

        @Override
        public void consume(String username)
        {
            if (username != null && seen.add(username))
            {
                final String lowerUsername = IdentifierUtils.toLowerCase(username);
                if (!username.equals(lowerUsername))
                {
                    usernames.put(username, lowerUsername);
                }
            }
        }

        @Override
        public Map<String,String> result()
        {
            return usernames;
        }
    }



    /**
     * Consumer that returns a map of mixed-case usernames that were found from a
     * {@link Select#columns(String...)} query to their lowercase equivalents.  The
     * column list must include the same {@code columnName}, but need not include
     * any others.
     * <p/>
     * The resulting map might, for example, have a key of {@code "Fred"} with a
     * corresponding value of {@code "fred"}.
     */
    public static EntityListConsumer<GenericValue, Map<String,String>> fromColumn(final String columnName)
    {
        return new FromColumnConsumer(columnName);
    }

    static class FromColumnConsumer implements EntityListConsumer<GenericValue,Map<String,String>>
    {
        private final String columnName;
        private final FromStringsConsumer delegate = new FromStringsConsumer();

        FromColumnConsumer(String columnName)
        {
            this.columnName = columnName;
        }

        @Override
        public void consume(GenericValue gv)
        {
            delegate.consume(gv.getString(columnName));
        }

        @Override
        public Map<String,String> result()
        {
            return delegate.result();
        }
    }



    /**
     * Consumer that returns a map of lowercase versions of the mixed-case usernames
     * that were found from a {@link Select#columns(String...)} query to a list of
     * the associated entity IDs for the rows that contained a mixed-case username.
     * The column list must include the same {@code columnName} and {@code "id"},
     * but need not include any others.
     * <p/>
     * The resulting map might, for example, have a key of {@code "fred"} with a
     * corresponding value list of {@code [10100, 10300, 10201, 10210, 10200]}.
     * The list should not be assumed to be in any particular order.
     */
    public static EntityListConsumer<GenericValue,Map<String,List<Long>>> fromColumnAndReturnIds(final String columnName)
    {
        return new FromColumnAndReturnIdsConsumer(columnName);
    }

    static class FromColumnAndReturnIdsConsumer implements EntityListConsumer<GenericValue,Map<String,List<Long>>>
    {
        private final String columnName;
        private final Map<String,List<Long>> results = new HashMap<String,List<Long>>();

        FromColumnAndReturnIdsConsumer(String columnName)
        {
            this.columnName = columnName;
        }

        @Override
        public void consume(GenericValue gv)
        {
            final String username = gv.getString(columnName);
            if (username == null)
            {
                // No username; ignore it
                return;
            }

            String lowerUsername = IdentifierUtils.toLowerCase(username);
            if (username.equals(lowerUsername))
            {
                // Username is already lowercase; ignore it
                return;
            }

            Long id = gv.getLong("id");
            if (id == null)
            {
                // No id should never happen, but if it did we couldn't do anything with it...
                return;
            }

            List<Long> ids = results.get(lowerUsername);
            if (ids == null)
            {
                ids = new ArrayList<Long>();
                results.put(lowerUsername, ids);
            }

            ids.add(id);
        }

        @Override
        public Map<String,List<Long>> result()
        {
            return results;
        }
    }
}
