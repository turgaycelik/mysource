package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.functest.matcher.IssueLinksTypeMatcher;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.hamcrest.Matchers;

import java.net.URISyntaxException;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.RENAME_USER;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;
import static org.junit.Assert.assertThat;

/**
 * @since v6.0
 */
@WebTest ({ FUNC_TEST, RENAME_USER, UPGRADE_TASKS })
public class TestUpgradeTask849and6153 extends FuncTestCase
{

    private JiraRestClient restClient;

    @Override
    protected void setUpTest()
    {
        restClient = createRestClient();
    }

    public void testLegacyDirectionPropertyTrueIfClonersSwapped()
    {
        administration.restoreDataWithBuildNumber("ClonersSwapped_Pre5.2.6.xml", 848);
        final Iterable<IssuelinksType> issuelinksTypes = restClient.getMetadataClient().getIssueLinkTypes().claim();
        assertThat(issuelinksTypes, Matchers.contains(IssueLinksTypeMatcher.issueLinkType("Cloners", "is cloned by", "clones")));
    }

    public void testLegacyDirectionPropertyTrueIfClonersChanged()
    {
        administration.restoreDataWithBuildNumber("ClonersModified_Pre5.2.6.xml", 848);
        final Iterable<IssuelinksType> issuelinksTypes = restClient.getMetadataClient().getIssueLinkTypes().claim();
        assertThat(issuelinksTypes, Matchers.contains(IssueLinksTypeMatcher.issueLinkType("Cloners", "is copied by", "copies")));
    }

    public void testLegacyDirectionPropertyFalseIfClonersUnmodified()
    {
        administration.restoreDataWithBuildNumber("ClonersUnmodified_Pre5.2.6.xml", 848);
        final Iterable<IssuelinksType> issuelinksTypes = restClient.getMetadataClient().getIssueLinkTypes().claim();
        assertThat(issuelinksTypes, Matchers.contains(IssueLinksTypeMatcher.issueLinkType("Cloners", "is cloned by", "clones")));
    }
}