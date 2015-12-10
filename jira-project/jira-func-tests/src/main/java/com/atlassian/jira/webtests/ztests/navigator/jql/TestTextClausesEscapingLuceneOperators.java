package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.collect.ImmutableMap;

import java.util.Map;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestTextClausesEscapingLuceneOperators extends AbstractJqlFuncTest
{
    @Override
    protected void setUpTest()
    {
        administration.restoreBlankInstance();
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testQueryingForAnIssueThatContainsALuceneOperatorOnItsSummary()
    {
        Map<String, String> summaryPrefixAndOperatorToTest = ImmutableMap.<String, String>builder()
                .put("one", "+")
                .put("two", "-")
                .put("three", "&")
                .put("four", "|")
                .put("five", "!")
                .put("six", "(")
                .put("seven", ")")
                .put("eight", "{")
                .put("nine", "}")
                .put("ten", "[")
                .put("eleven", "]")
                .put("twelve", "^")
                .put("thirteen", "~")
                .put("fourteen", "*")
                .put("fifteen", "?")
                .build();

        for(Map.Entry<String, String> summaryPrefixAndOperator : summaryPrefixAndOperatorToTest.entrySet())
        {
            String summaryPrefix = summaryPrefixAndOperator.getKey();
            String luceneOperator = summaryPrefixAndOperator.getValue();

            // the issue will have a summary like: one+
            String issue = createIssueWithSummary(summaryPrefix + luceneOperator);

            // the query to test will be: text ~ "one\\+"
            String query = queryWithLuceneOperatorEscaped(summaryPrefix, luceneOperator);

            assertSearchWithResults(query, issue);
        }
    }

    public void testQueryingForAnIssueThatContainsABackslashOnItsSummary() throws Exception
    {
        String issue = createIssueWithSummary("issue\\");

        String queryWithAnExplicitBackslash = "text ~ \"issue\\\\\"";
        assertSearchWithResults(queryWithAnExplicitBackslash, issue);

        String queryWithTheUnicodeCharacterForBackslash = "text ~ \"issue\\u005C\"";
        assertSearchWithResults(queryWithTheUnicodeCharacterForBackslash, issue);
    }

    private String createIssueWithSummary(final String summary)
    {
        return navigation.issue().createIssue("homosapien", "Bug", summary);
    }

    private String queryWithLuceneOperatorEscaped(final String summary, final String luceneOperator)
    {
        return "text ~ \"" + summary + "\\\\" + luceneOperator + "\"";
    }
}
