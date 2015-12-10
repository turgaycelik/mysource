package com.atlassian.jira.issue.search.util;

import org.junit.Before;
import org.junit.Test;

import static org.hamcrest.CoreMatchers.is;
import static org.junit.Assert.assertThat;

public class TestTextTermEscaper
{
    private static final String[] ALLOWED_LUCENE_OPERATORS = { "+", "-", "&", "|", "!", "(", ")", "{", "}", "[", "]", "^", "~", "*", "?", "\"" };

    private TextTermEscaper escaper;

    @Before
    public void setUp()
    {
        escaper = new TextTermEscaper();
    }

    @Test
    public void doesNotEscapeLuceneOperatorsAllowedForUsers() throws Exception
    {
        for (String allowedLuceneOperator : ALLOWED_LUCENE_OPERATORS)
        {
            String inputQuery = "priority" + allowedLuceneOperator;

            String escapedQuery = escaper.get(inputQuery);

            assertThat(escapedQuery, is(inputQuery));
        }
    }

    @Test
    public void escapesColonLuceneOperatorSinceItIsDisallowedForUsers() throws Exception
    {
        String inputQuery = "priority:1";

        String escapedQuery = escaper.get(inputQuery);

        // the colon gets escaped, so the resulting query ("priority\:1") is interpreted as "text containing the word "priority:1")
        assertThat(escapedQuery, is("priority\\:1"));
    }

    @Test
    public void escapesBackslashWhenIsFollowedBySomethingThatIsNotALuceneOperator() throws Exception
    {
        String inputQuery = "priority\\1";

        String escapedQuery = escaper.get(inputQuery);

        // the backslash gets escaped, so the resulting query ("priority\\1") is interpreted as "text containing the word "priority\1")
        assertThat(escapedQuery, is("priority\\\\1"));
    }

    @Test
    public void escapesBackslashWhenIsFollowedByADisallowedLuceneOperator()
    {
        String inputQuery = "priority\\:1";

        String escapedQuery = escaper.get(inputQuery);

        // the leading backslash and the colon get escaped, so the resulting query ("priority\\\:1") is interpreted as "text containing the word "priority\:1")
        assertThat(escapedQuery, is("priority\\\\\\:1"));
    }

    @Test
    public void doesNotEscapeBackslashWhenIsFollowedByAnAllowedLuceneOperator()
    {
        for (String allowedLuceneOperator : ALLOWED_LUCENE_OPERATORS)
        {
            String inputQuery = "priority\\" + allowedLuceneOperator;

            String escapedQuery = escaper.get(inputQuery);

            assertThat(escapedQuery, is(inputQuery));
        }
    }

    @Test
    public void escapesBackslashWithoutGoingOutOfBoundsWhenItIsAtTheEndOfTheInputQuery()
    {
        String inputQuery = "priority\\";

        String escapedQuery = escaper.get(inputQuery);

        assertThat(escapedQuery, is("priority\\\\"));
    }
}
