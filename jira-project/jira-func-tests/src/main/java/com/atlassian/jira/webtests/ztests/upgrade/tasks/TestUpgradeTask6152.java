package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.IssueLink;
import com.atlassian.jira.rest.client.api.domain.IssueLinkType;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.hamcrest.BaseMatcher;
import org.hamcrest.Description;
import org.hamcrest.Matchers;

import java.net.URISyntaxException;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;
import static com.atlassian.jira.rest.client.api.domain.IssueLinkType.Direction.INBOUND;
import static org.junit.Assert.assertThat;

/**
 * @since v6.1
 */
@WebTest ({ FUNC_TEST, UPGRADE_TASKS })
public class TestUpgradeTask6152 extends FuncTestCase
{
    private JiraRestClient restClient;

    @Override
    protected void setUpTest()
    {
        restClient = createRestClient();
    }

    @SuppressWarnings ("unchecked")
    public void testLegacyDirectionPropertyTrueIfClonersSwapped()
    {
        administration.restoreDataWithBuildNumber("ClonersMixedUp.xml", 6151);
        final Iterable<IssueLink> links = restClient.getIssueClient().getIssue("CLON-1").claim().getIssueLinks();
        assertThat(links, Matchers.<IssueLink>containsInAnyOrder(
                issueLink("Cloners", INBOUND, "CLON-4"), issueLink("Cloners", INBOUND, "CLON-7")));
    }

    private BaseMatcher<IssueLink> issueLink(final String linkType, final IssueLinkType.Direction linkDirection, final String linkedIssueKey)
    {
        return new BaseMatcher<IssueLink>()
        {
            @Override
            public boolean matches(final Object item)
            {
                return item instanceof IssueLink
                        && ((IssueLink) item).getIssueLinkType().getName().equals(linkType)
                        && ((IssueLink) item).getIssueLinkType().getDirection() == linkDirection
                        && ((IssueLink) item).getTargetIssueKey().equals(linkedIssueKey);
            }

            @Override
            public void describeTo(final Description description)
            {
                description.appendText("Issue Link of type " + linkType + " " + linkDirection + " to issue " + linkedIssueKey);
            }
        };
    }

}
