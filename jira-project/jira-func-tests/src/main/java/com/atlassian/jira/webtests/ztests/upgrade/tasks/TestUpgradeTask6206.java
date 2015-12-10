package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import java.net.URISyntaxException;
import java.util.List;

import javax.annotation.Nullable;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;

import com.google.common.base.Function;
import com.google.common.collect.Lists;

import org.hamcrest.Matchers;
import org.w3c.dom.Node;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;
import static org.hamcrest.CoreMatchers.equalTo;
import static org.hamcrest.MatcherAssert.assertThat;

/**
 * @since v6.2
 */
@WebTest ({ FUNC_TEST, UPGRADE_TASKS })
public class TestUpgradeTask6206 extends FuncTestCase
{
    private JiraRestClient restClient;

    @Override
    public void setUpTest()
    {
        restClient = createRestClient();
    }

    public void testStatusesAssignedAsExpected()
    {
        administration.restoreDataWithBuildNumber("JDEV-24290-TestStatusCategoryAssignment.xml", 6204);
        backdoor.darkFeatures().enableForSite("jira.issue.status.lozenge");
        navigation.gotoPage("/secure/admin/ViewStatuses.jspa");

        List<String> lozenges = getStatusNamesFromStatusLozengeClass(".jira-issue-status-lozenge");
        assertThat(lozenges.size(), equalTo(15));

        List<String> undefined = getStatusNamesFromStatusLozengeClass(".jira-issue-status-lozenge-undefined");
        assertThat(undefined.size(), equalTo(1));
        assertThat(undefined.get(0), equalTo("The loneliest state"));

        List<String> done = getStatusNamesFromStatusLozengeClass(".jira-issue-status-lozenge-done");
        assertThat(done.size(), equalTo(4));
        assertThat(done, Matchers.<String>hasItems(equalTo("Resolved"), equalTo("Closed"), equalTo("RCRT - Hired"), equalTo("RCRT - Rejected")));

        List<String> todo = getStatusNamesFromStatusLozengeClass(".jira-issue-status-lozenge-new");
        assertThat(todo.size(), equalTo(3));
        assertThat(todo, Matchers.<String>hasItems(equalTo("Open"), equalTo("Reopened"), equalTo("RCRT - Resume Check")));
    }

    private List<String> getStatusNamesFromStatusLozengeClass(final String selector)
    {
        Node[] nodes = locator.css(selector).getNodes();
        return Lists.transform(Lists.newArrayList(nodes), new Function<Node, String>()
        {
            @Override
            public String apply(@Nullable final Node item)
            {
                return (item == null) ? null : String.valueOf(item.getParentNode().getPreviousSibling().getFirstChild().getNodeValue());
            }
        });
    }
}
