package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.google.common.base.Predicate;
import com.google.common.collect.Sets;

import javax.annotation.Nullable;
import java.util.Set;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.REST;
import static com.google.common.collect.Collections2.filter;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.Matchers.containsString;
import static org.junit.Assert.assertThat;

@WebTest ( { FUNC_TEST, REST })
public class TestIssueResourceNames extends FuncTestCase
{
    private IssueClient issueClient;

    public void testIssueRepresentationShouldContainExpandableNamesField() throws Exception
    {
        administration.restoreData("TestIssueResourceCustomFields.xml");

        Issue minimal = issueClient.get("HSP-1");
        assertThat("names should not be expanded by default", minimal.names, equalTo(null));
        assertThat(minimal.expand, containsString(Issue.Expand.names.name()));

        Issue expanded = issueClient.get("HSP-1", Issue.Expand.names);
        Set<String> fields = Sets.newHashSet(filter(expanded.fields.idSet(), new NonNullFields(expanded)));
        Set<String> names = expanded.names.keySet();
        assertTrue("Found in 'fields' but not in 'names': " + Sets.difference(fields, names), Sets.difference(fields, names).isEmpty());
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }

    private static class NonNullFields implements Predicate<String>
    {
        private final Issue expanded;

        public NonNullFields(Issue expanded) {this.expanded = expanded;}

        @Override
        public boolean apply(@Nullable String fieldId)
        {
            return expanded.fields.get(fieldId) != null;
        }
    }
}
