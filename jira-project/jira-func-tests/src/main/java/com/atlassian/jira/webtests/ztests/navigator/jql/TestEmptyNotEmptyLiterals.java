package com.atlassian.jira.webtests.ztests.navigator.jql;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.util.collect.CollectionBuilder;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;

import static com.atlassian.jira.functest.framework.backdoor.IssuesControl.LIST_VIEW_LAYOUT;

/**
 * Tests using the EMPTY and NOT EMPTY literals.
 *
 * @since v4.0
 */
@WebTest ({ Category.FUNC_TEST, Category.JQL })
public class TestEmptyNotEmptyLiterals extends AbstractJqlFuncTest
{

    @Override
    protected void setUpTest()
    {
        administration.restoreData("TestEmptyNotEmptyLiterals.xml");
        backdoor.issueNavControl().setPreferredSearchLayout(LIST_VIEW_LAYOUT, ADMIN_USERNAME);
    }

    public void testIsEmpty() throws Exception
    {
        List<String> clauseNames = new ArrayList<String>();
        clauseNames.add("affectedVersion");
        clauseNames.add("assignee");
        clauseNames.add("component");
        clauseNames.add("description");
        clauseNames.add("environment");
        clauseNames.add("fixVersion");
        clauseNames.add("level");
        clauseNames.add("originalEstimate");
        clauseNames.add("reporter");

        //The first issue has no values for all fields, the second has a value for the first field....
        // ...the last issue has values for all fields
        List<String> homosapienIssueKeys = CollectionBuilder.newBuilder("HSP-1", "HSP-2", "HSP-3", "HSP-4", "HSP-5", "HSP-6", "HSP-7", "HSP-8", "HSP-9","HSP-10").asList();
        List<String> monkeyIssueKeys     = CollectionBuilder.newBuilder("MKY-1", "MKY-2", "MKY-3", "MKY-4", "MKY-5", "MKY-6", "MKY-7", "MKY-8", "MKY-9","MKY-10").asList();

        int count = clauseNames.size();

        for(int i = 0; i <= count; i++)
        {
            final Iterator<String> iterator = clauseNames.iterator();
            StringBuilder jqlQuery = new StringBuilder();

            for(int j = 0; j < i; j++)
            {
                jqlQuery.append(iterator.next()+" IS NOT EMPTY ");
                if(iterator.hasNext())
                {
                    jqlQuery.append("AND ");
                }
            }

            for(int k = i; k < count; k ++)
            {
                jqlQuery.append(iterator.next()+" IS EMPTY ");
                if(iterator.hasNext())
                {
                    jqlQuery.append("AND ");
                }
            }

            assertSearchWithResults(jqlQuery.toString(), monkeyIssueKeys.get(i), homosapienIssueKeys.get(i));
        }

        for (String clauseName : clauseNames)
        {
            StringBuilder jqlQuery = new StringBuilder();
            jqlQuery.append(clauseName + " IS EMPTY AND "+ clauseName + " IS NOT EMPTY");
            assertSearchWithResults(jqlQuery.toString());
        }
    }
    

    public void testIsEmptyORHasSpecificValue() throws Exception
    {
            assertSearchWithResults("affectedVersion IS EMPTY OR affectedVersion = \"New Version 1\" AND assignee IS EMPTY AND component IS EMPTY AND description IS EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY", "MKY-2", "MKY-1", "HSP-2", "HSP-1");
            assertSearchWithResults("affectedVersion IS NOT EMPTY AND assignee IS EMPTY OR assignee = \"" + ADMIN_USERNAME + "\" AND component IS EMPTY AND description IS EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY", "MKY-3", "MKY-2", "HSP-3", "HSP-2");
            assertSearchWithResults("affectedVersion IS NOT EMPTY AND assignee IS NOT EMPTY AND component IS EMPTY OR component = \"New Component 1\" AND description IS EMPTY AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY", "MKY-4", "MKY-3", "HSP-4", "HSP-3");
            assertSearchWithResults("affectedVersion IS NOT EMPTY AND assignee IS NOT EMPTY AND component IS NOT EMPTY AND description IS EMPTY OR description ~ \"Blub\" AND environment IS EMPTY AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY","MKY-5", "MKY-4", "HSP-5", "HSP-4");
            assertSearchWithResults("affectedVersion IS NOT EMPTY AND assignee IS NOT EMPTY AND component IS NOT EMPTY AND description IS NOT EMPTY AND environment IS EMPTY OR environment ~ \"some environment information\" AND fixVersion IS EMPTY AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY", "MKY-6", "MKY-5", "HSP-6", "HSP-5");
            assertSearchWithResults("affectedVersion IS NOT EMPTY AND assignee IS NOT EMPTY AND component IS NOT EMPTY AND description IS NOT EMPTY AND environment IS NOT EMPTY AND fixVersion IS EMPTY OR fixVersion = \"New Version 1\" AND level IS EMPTY AND originalEstimate IS EMPTY AND reporter IS EMPTY", "MKY-7", "MKY-6", "HSP-7", "HSP-6");
            assertSearchWithResults("affectedVersion IS NOT EMPTY AND assignee IS NOT EMPTY AND component IS NOT EMPTY AND description IS NOT EMPTY AND environment IS NOT EMPTY AND fixVersion IS NOT EMPTY AND level IS EMPTY OR level = \"Level 1\" AND originalEstimate IS EMPTY AND reporter IS EMPTY", "MKY-8", "MKY-7", "HSP-8", "HSP-7");
            assertSearchWithResults("affectedVersion IS NOT EMPTY AND assignee IS NOT EMPTY AND component IS NOT EMPTY AND description IS NOT EMPTY AND environment IS NOT EMPTY AND fixVersion IS NOT EMPTY AND level IS NOT EMPTY AND originalEstimate IS EMPTY OR originalEstimate = \"12h\" AND reporter IS EMPTY", "MKY-9", "MKY-8", "HSP-9", "HSP-8");
            assertSearchWithResults("affectedVersion IS NOT EMPTY AND assignee IS NOT EMPTY AND component IS NOT EMPTY AND description IS NOT EMPTY AND environment IS NOT EMPTY AND fixVersion IS NOT EMPTY AND level IS NOT EMPTY AND originalEstimate IS NOT EMPTY AND reporter IS EMPTY OR reporter = \"admin\"", "MKY-10", "MKY-9", "HSP-10", "HSP-9");
            assertSearchWithResults("affectedVersion IS NOT EMPTY AND assignee IS NOT EMPTY AND component IS NOT EMPTY AND description IS NOT EMPTY AND environment IS NOT EMPTY AND fixVersion IS NOT EMPTY AND level IS NOT EMPTY AND originalEstimate IS NOT EMPTY AND reporter IS NOT EMPTY", "MKY-10", "HSP-10");

            assertSearchWithResults("resolution is empty OR resolution = \"Fixed\"", "MKY-10", "MKY-9", "MKY-8", "MKY-7", "MKY-6", "MKY-1", "HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-1");
            assertSearchWithResults("resolution is empty OR resolution = \"Won't Fix\"", "MKY-10", "MKY-9", "MKY-8", "MKY-7", "MKY-6", "MKY-2", "HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-2");
            assertSearchWithResults("resolution is empty OR resolution = \"Duplicate\"", "MKY-10", "MKY-9", "MKY-8", "MKY-7", "MKY-6", "MKY-3", "HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-3");
            assertSearchWithResults("resolution is empty OR resolution = \"Incomplete\"", "MKY-10", "MKY-9", "MKY-8", "MKY-7", "MKY-6", "MKY-4", "HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-4");
            assertSearchWithResults("resolution is empty OR resolution = \"Cannot Reproduce\"", "MKY-10", "MKY-9", "MKY-8", "MKY-7", "MKY-6", "MKY-5", "HSP-10", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5");

            assertSearchWithResults("remainingEstimate = \"12h\" or remainingEstimate is empty", "MKY-9", "MKY-8", "MKY-7", "MKY-6", "MKY-5", "MKY-4", "MKY-3", "MKY-2", "MKY-1", "HSP-9", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
            assertSearchWithResults("duedate = \"2009/07/22\" OR duedate is empty", "MKY-10", "MKY-9", "MKY-7", "MKY-6", "MKY-5", "MKY-4", "MKY-3", "MKY-2", "MKY-1", "HSP-10", "HSP-9", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
            assertSearchWithResults("workRatio = \"16\" OR workRatio is empty", "MKY-10", "MKY-8", "MKY-7", "MKY-6", "MKY-5", "MKY-4", "MKY-3", "MKY-2", "MKY-1", "HSP-10", "HSP-8", "HSP-7", "HSP-6", "HSP-5", "HSP-4", "HSP-3", "HSP-2", "HSP-1");
    }


}
