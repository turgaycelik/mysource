package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.ArrayList;
import java.util.List;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * A collection of random, crazy JQL queries that don't fit in anywhere else.
 * These should primarily be about asserting the query returned the expected results.
 * 
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestRandomQueries extends AbstractJqlFuncTest
{
    public void testJqlMultipleValuesWithEmpty() throws Exception
    {
        administration.restoreData("TestJqlMultipleValuesWithEmpty.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        navigation.issueNavigator().createSearch("\"number\" = echo(9, none)");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-12", "MKY-11");

        navigation.issueNavigator().createSearch("duedate = echo(\"0009/9/9\", none)");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-11");

        navigation.issueNavigator().createSearch("\"number\" != echo(9, none)");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-10");

        navigation.issueNavigator().createSearch("duedate != echo(\"0009/9/9\", none)");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-12", "MKY-10");

        navigation.issueNavigator().createSearch("\"number\" = echo(9, 10) order by key desc");
        assertions.getIssueNavigatorAssertions().assertExactIssuesInResults("MKY-12", "MKY-10");
    }

    public void testJqlPrecendenceOfAndOrNot() throws Exception
    {
        administration.restoreData("TestRandomQueries.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);

        List<String> homosapienIssueKeys = CollectionBuilder.newBuilder("HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2","HSP-1").asList();
        List<String> monkeyIssueKeys     = CollectionBuilder.newBuilder("MKY-10", "MKY-9", "MKY-8", "MKY-7", "MKY-6", "MKY-5", "MKY-4", "MKY-3", "MKY-2","MKY-1").asList();
        
        String MKY_1_HSP_1JQL   = "affectedVersion IS EMPTY AND assignee IS EMPTY AND component IS EMPTY AND description IS EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY";
        String MKY_2_HSP_2JQL   = "affectedVersion IS NOT EMPTY AND assignee IS EMPTY AND component IS EMPTY AND description IS EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY";
        String MKY_3_HSP_3JQL   = "assignee IS NOT EMPTY AND component IS EMPTY AND description IS EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY";
        String MKY_4_HSP_4JQL   = "component IS NOT EMPTY AND description IS EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY";
        String MKY_5_HSP_5JQL   = "description IS NOT EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY";
        String MKY_6_HSP_6JQL   = "environment IS NOT EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY";
        String MKY_7_HSP_7JQL   = "fixVersion IS NOT EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY";
        String MKY_8_HSP_8JQL   = "level IS NOT EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY";
        String MKY_9_HSP_9JQL   = "originalEstimate IS NOT EMPTY AND reporter IS EMPTY";
        String MKY_10_HSP_10JQL = "reporter IS NOT EMPTY";

        List<String> resultIssueKeys = new ArrayList<String>();
        resultIssueKeys.addAll(monkeyIssueKeys);
        resultIssueKeys.addAll(homosapienIssueKeys);

        assertSearchWithResults(MKY_1_HSP_1JQL + " OR " + MKY_2_HSP_2JQL +  " OR " + MKY_3_HSP_3JQL + " OR " + MKY_4_HSP_4JQL + " OR " +
                MKY_5_HSP_5JQL + " OR " + MKY_6_HSP_6JQL + " OR " + MKY_7_HSP_7JQL + " OR " + MKY_8_HSP_8JQL + " OR " + MKY_9_HSP_9JQL + " OR " + MKY_10_HSP_10JQL, resultIssueKeys.toArray(new String[]{}));

        assertSearchWithResults(MKY_1_HSP_1JQL + " OR " + MKY_9_HSP_9JQL + " OR " + MKY_10_HSP_10JQL, "MKY-10", "MKY-9", "MKY-1", "HSP-10", "HSP-9", "HSP-1");

        assertSearchWithResults("affectedVersion IS EMPTY OR affectedVersion = \"New Version 1\" AND " + "assignee IS EMPTY AND component IS EMPTY AND description IS EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY ", "MKY-2", "MKY-1", "HSP-2", "HSP-1");
        assertSearchWithResults("affectedVersion = \"New Version 1\" AND assignee IS EMPTY AND component IS EMPTY AND description IS EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY ", "MKY-2", "HSP-2");

        //associativity
        //A v ( B v C ) = (A v B ) v C
        assertSearchWithResults("(" + MKY_1_HSP_1JQL + " ) OR (  (" + MKY_2_HSP_2JQL + ") OR ( " + MKY_3_HSP_3JQL +" )  )", "MKY-3", "MKY-2", "MKY-1", "HSP-3", "HSP-2", "HSP-1");
        assertSearchWithResults("( (" + MKY_1_HSP_1JQL + " ) OR   (" + MKY_2_HSP_2JQL + ") ) OR ( " + MKY_3_HSP_3JQL +" )  ", "MKY-3", "MKY-2", "MKY-1", "HSP-3", "HSP-2", "HSP-1");
        //A ^ ( B ^ C ) = (A ^ B) ^ C
        assertSearchWithResults("(reporter IS NOT EMPTY AND level IS NOT EMPTY) AND originalEstimate IS NOT EMPTY", "MKY-10", "HSP-10");
        assertSearchWithResults("reporter IS NOT EMPTY AND (level IS NOT EMPTY AND originalEstimate IS NOT EMPTY)", "MKY-10", "HSP-10");
        //Commutativity
        //A v B = B v A
        assertSearchWithResults( MKY_9_HSP_9JQL   + " OR " + MKY_10_HSP_10JQL, "MKY-10", "MKY-9", "HSP-10", "HSP-9");
        assertSearchWithResults( MKY_10_HSP_10JQL + " OR " + MKY_9_HSP_9JQL  , "MKY-10", "MKY-9", "HSP-10", "HSP-9");
        //Absorption
        //a v (a ^ b) = a
        //a ^ (a v b) = a
        assertSearchWithResults(MKY_7_HSP_7JQL + " OR ( " + MKY_7_HSP_7JQL + " AND " + MKY_9_HSP_9JQL + " )","MKY-7", "HSP-7");
        assertSearchWithResults(MKY_7_HSP_7JQL + " AND ( " + MKY_7_HSP_7JQL + " OR " + MKY_9_HSP_9JQL + " )","MKY-7", "HSP-7");
        //Distributivity
        //a v ( b ^ c) = (a v b) ^ (a v c)
        //a ^ (b v c) = (a ^ b) v (a ^ c)
        assertSearchWithResults(MKY_1_HSP_1JQL + " OR ( " + MKY_2_HSP_2JQL + " AND "+ MKY_10_HSP_10JQL + " )", "MKY-1", "HSP-1");
        assertSearchWithResults("( " + MKY_1_HSP_1JQL + " OR "+ MKY_2_HSP_2JQL + ") AND ("+ MKY_1_HSP_1JQL + " OR " + MKY_10_HSP_10JQL + " )", "MKY-1", "HSP-1");
        //Complements
        //a v -a = 1
        //a ^ -a = 0
        assertSearchWithResults(MKY_10_HSP_10JQL + " OR reporter IS EMPTY", resultIssueKeys.toArray(new String[resultIssueKeys.size()]));
        assertSearchWithResults(MKY_10_HSP_10JQL + " AND reporter IS EMPTY");
        //Idempotency
        // a v a = a
        // a ^ a = a
        assertSearchWithResults(MKY_8_HSP_8JQL + " OR "+ MKY_8_HSP_8JQL, "MKY-8", "HSP-8");
        assertSearchWithResults(MKY_8_HSP_8JQL + " AND "+ MKY_8_HSP_8JQL, "MKY-8", "HSP-8");

        final String JQL_P = "(not (not assignee is empty) or affectedversion = \"New Version 1\") and Description ~ \"Blub\" and (status = \"Open\" or ((originalEstimate is not empty or resolution = \"Duplicate\") or issuekey = \"MKY-5\"))";
        final String JQL_Q = "resolution in (\"Fixed\", \"Won't Fix\", \"Duplicate\", \"Incomplete\")";

        assertSearchWithResults(JQL_P + " OR " + JQL_Q, "MKY-10", "MKY-9", "MKY-8", "MKY-7", "MKY-6", "MKY-5", "MKY-4", "MKY-3", "MKY-2", "MKY-1", "HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-4", "HSP-3", "HSP-2", "HSP-1");

        //DeMorgan Laws
        /*
            NOT (P OR Q) = (NOT P) AND (NOT Q)
            NOT (P AND Q) = (NOT P) OR (NOT Q)
         */
        assertSearchWithResults("NOT ( " +  JQL_P + " OR " + JQL_Q + " )", "HSP-5");
        assertSearchWithResults("( NOT ( " +  JQL_P + " ) ) AND ( NOT (" + JQL_Q + ") )", "HSP-5");

        String negatedQuery = "not ( ((not (not assignee is empty) or affectedversion = \"New Version 1\") and Description ~ \"Blub\" and (status = \"Open\" or ((originalEstimate is not empty or resolution = \"Duplicate\") or issuekey = \"MKY-5\"))) AND (resolution in (\"Fixed\", \"Won't Fix\",\"Duplicate\", \"Incomplete\")))";
        String nonnegatedQuery = "((assignee is not empty AND affectedversion != \"New Version 1\") OR Description !~ \"Blub\" OR (status != \"Open\" AND originalEstimate is empty AND resolution != \"Duplicate\" AND issuekey != \"MKY-5\")) or (resolution not in (\"Fixed\", \"Won't Fix\", \"Duplicate\",\"Incomplete\")   )";
        assertSearchWithResults(negatedQuery   ,"MKY-5", "MKY-4", "MKY-2", "MKY-1", "HSP-5", "HSP-4", "HSP-2", "HSP-1");
        assertSearchWithResults(nonnegatedQuery,"MKY-5", "MKY-4", "MKY-2", "MKY-1", "HSP-5", "HSP-4", "HSP-2", "HSP-1");

        assertSearchWithResults("NOT ( (" +  JQL_P + ") AND (" + JQL_Q + ") )", "MKY-5", "MKY-4", "MKY-2", "MKY-1", "HSP-5", "HSP-4", "HSP-2", "HSP-1");
        assertSearchWithResults("( NOT ( " +  JQL_P + " ) ) OR ( NOT (" + JQL_Q + ") )",  "MKY-5", "MKY-4", "MKY-2", "MKY-1", "HSP-5", "HSP-4", "HSP-2", "HSP-1");

        String JQL = "description is not empty AND assignee = \"" + ADMIN_USERNAME + "\"";

        assertSearchWithResults("not ( " + JQL + " )", "MKY-4", "MKY-3", "MKY-2", "MKY-1", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
        assertSearchWithResults(JQL, "MKY-10", "MKY-9", "MKY-8", "MKY-7", "MKY-6", "MKY-5", "HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5");
        assertSearchWithResults("reporter IS NOT EMPTY OR originalEstimate IS NOT EMPTY", "MKY-10", "MKY-9", "HSP-10", "HSP-9");
        assertSearchWithResults("NOT (reporter IS EMPTY AND originalEstimate IS EMPTY)", "MKY-10", "MKY-9", "HSP-10", "HSP-9");
        assertSearchWithResults(JQL_P, "MKY-10", "MKY-9", "MKY-8", "MKY-7", "MKY-6", "MKY-5", "HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6");
    }
}
