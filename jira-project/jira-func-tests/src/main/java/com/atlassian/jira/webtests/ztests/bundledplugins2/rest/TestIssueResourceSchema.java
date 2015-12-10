package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.google.common.collect.Sets;

import java.util.Set;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.REST;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@WebTest ( { FUNC_TEST, REST })
public class TestIssueResourceSchema extends FuncTestCase
{
    private IssueClient issueClient;

    public void testIssueRepresentationShouldContainExpandableSchemaField() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");

        Issue minimal = issueClient.get("HSP-1");
        assertThat("names should not be expanded by default", minimal.schema, equalTo(null));
        assertThat(minimal.expand, containsString(Issue.Expand.schema.name()));

        Issue hsp1_expanded = issueClient.get("HSP-1", Issue.Expand.schema);
        Set<String> fields = hsp1_expanded.fields.idSet();
        Set<String> schema = hsp1_expanded.schema.keySet();
        assertTrue("Found in 'schema' but not in 'fields'" + Sets.difference(schema, fields), Sets.difference(schema, fields).isEmpty());
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }
}
