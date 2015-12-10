package com.atlassian.jira.webtests.ztests.upgrade.tasks;

import com.atlassian.jira.functest.framework.FuncTestCase;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.client.api.JiraRestClient;
import com.atlassian.jira.rest.client.api.JiraRestClientFactory;
import com.atlassian.jira.rest.client.api.domain.IssuelinksType;
import com.atlassian.jira.rest.client.internal.async.AsynchronousJiraRestClientFactory;
import org.hamcrest.Matchers;

import java.net.URISyntaxException;

import static com.atlassian.jira.functest.framework.suite.Category.FUNC_TEST;
import static com.atlassian.jira.functest.framework.suite.Category.RENAME_USER;
import static com.atlassian.jira.functest.framework.suite.Category.UPGRADE_TASKS;
import static com.atlassian.jira.functest.matcher.IssueLinksTypeMatcher.issueLinkType;
import static org.junit.Assert.assertThat;

/**
 * @since v6.0
 */
@WebTest ({ FUNC_TEST, RENAME_USER, UPGRADE_TASKS })
public class TestUpgradeTask6085and6153 extends FuncTestCase
{

    private JiraRestClient restClient;

    @Override
    protected void setUpTest()
    {
        restClient = createRestClient();
    }

    public void testLegacyDirectionPropertyTrueIfClonersSwapped()
    {
        administration.restoreDataWithBuildNumber("ClonersSwapped_Post5.2.6.xml", 855);
        final Iterable<IssuelinksType> issuelinksTypes = restClient.getMetadataClient().getIssueLinkTypes().claim();
        assertThat(issuelinksTypes, Matchers.contains(issueLinkType("Cloners", "is cloned by", "clones")));
    }

    public void testLegacyDirectionPropertyFalseIfClonersChanged()
    {
        administration.restoreDataWithBuildNumber("ClonersModified_Post5.2.6.xml", 855);
        final Iterable<IssuelinksType> issuelinksTypes = restClient.getMetadataClient().getIssueLinkTypes().claim();
        //in this case this upgrade task guesses wrong because there is no way to tell how Cloners was configured
        assertThat(issuelinksTypes, Matchers.contains(issueLinkType("Cloners", "copies", "is copied by")));
    }

    public void testLegacyDirectionPropertyFalseIfClonersUnmodified()
    {
        administration.restoreDataWithBuildNumber("ClonersUnmodified_Post5.2.6.xml", 855);
        final Iterable<IssuelinksType> issuelinksTypes = restClient.getMetadataClient().getIssueLinkTypes().claim();
        assertThat(issuelinksTypes, Matchers.contains(issueLinkType("Cloners", "is cloned by", "clones")));
    }
}
