package com.atlassian.jira.ofbiz;

import java.util.List;

import org.junit.Test;

import static java.util.Arrays.asList;
import static java.util.Collections.emptyList;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;

/**
 * @since v5.1.7
 */
public class TestOfBizLogHelper
{
    @Test
    public void formatSqlShouldHandleEmptyListOfValues() throws Exception
    {
        final List<String> parameterValues = emptyList();
        assertFormattedSql("SELECT 'X' FROM DUAL", parameterValues, "\"SELECT 'X' FROM DUAL\"");
    }

    @Test
    public void formatSqlShouldHandleNullListOfValues() throws Exception
    {
        assertFormattedSql("SELECT 'X' FROM DUAL", null, "\"SELECT 'X' FROM DUAL\"");
    }

    @Test
    public void formatSqlShouldHandleNonEmptyListOfValues() throws Exception
    {
        final List<String> parameterValues = asList("fred", "1");
        assertFormattedSql("SELECT * FROM STUFF WHERE USERNAME = ? AND TYPE = 'BLAH' AND FOO_ID = ?", parameterValues,
                "\"SELECT * FROM STUFF WHERE USERNAME = 'fred' AND TYPE = 'BLAH' AND FOO_ID = '1'\"");
    }

    @Test
    public void formatSqlShouldHandleValuesWithDollarSigns() throws Exception
    {
        final List<String> parameterValues = asList("abc\\$(!#*&$*(*@&", "Whoops! here's dollar: $x",
                "And here's another $1 $823");
        assertFormattedSql("SELECT * FROM stuff WHERE text1 = ? AND text2 = ? AND text3 = ?", parameterValues,
                "\"SELECT * FROM stuff WHERE text1 = 'abc\\$(!#*&$*(*@&' AND text2 = 'Whoops! here's dollar: $x' "
                + "AND text3 = 'And here's another $1 $823'\"");
    }

    @Test
    public void formatSqlShouldLeaveForUpdateClauseIntact() {
        assertFormattedSql("SELECT pcounter FROM project WHERE ID = ? FOR UPDATE", singletonList("6"),
                "\"SELECT pcounter FROM project WHERE ID = '6' FOR UPDATE\"");
    }

    @Test
    public void formatSqlShouldLeaveOrderByClauseIntact() {
        assertFormattedSql("SELECT foo FROM bar WHERE id = ? ORDER BY UPPER(foo)", singletonList("7"),
                "\"SELECT foo FROM bar WHERE id = '7' ORDER BY UPPER(foo)\"");
    }

    private void assertFormattedSql(
            final String originalSql, final List<String> bindValues, final String expectedMessage)
    {
        assertEquals(expectedMessage, OfBizLogHelper.formatSQL(originalSql, bindValues));
    }
}
