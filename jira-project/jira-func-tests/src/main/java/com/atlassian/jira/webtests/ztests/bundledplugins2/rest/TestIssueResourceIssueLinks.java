package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import java.io.IOException;
import java.util.Arrays;
import java.util.List;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.FieldOperation;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.rest.api.issue.ResourceRef;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.IssueLink;
import com.atlassian.jira.testkit.client.restclient.IssueType;
import com.atlassian.jira.testkit.client.restclient.LinkRequest;
import com.atlassian.jira.testkit.client.restclient.Priority;
import com.atlassian.jira.testkit.client.restclient.Status;
import com.atlassian.jira.testkit.client.restclient.StatusCategory;

import static com.atlassian.jira.functest.matcher.LinkedIssuesMatcher.hasLinkWithOutwardIssue;
import static com.atlassian.jira.testkit.client.restclient.LinkRequest.FIELD_NAME;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.is;
import static org.hamcrest.Matchers.not;
import static org.junit.Assert.assertThat;

/**
 * Functional tests for REST issue linking (JRADEV-1657).
 *
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST, Category.ISSUE_LINKS })
public class TestIssueResourceIssueLinks extends RestFuncTest
{
    private IssueClient issueClient;

    public void testIssueLinksDisabled() throws Exception
    {
        restoreData(false);
        Issue issue = issueClient.get("LNK-4");
        assertNull(issue.fields.issuelinks);
    }

    // LNK-5 has no links
    public void testNoIssueLinks() throws Exception
    {
        restoreData(true);
        Issue issue = issueClient.get("LNK-5");
        assertEquals("Object should have no issue links", 0, issue.fields.issuelinks.size());
    }

    // LNK-1 has inward links, but no outward links
    public void testNoOutwardIssueLinks() throws Exception
    {
        restoreData(true);
        Issue issue = issueClient.get("LNK-1");
        List<IssueLink> links = issue.fields.issuelinks;
        for (IssueLink link : links)
        {
            assertNull(link.outwardIssue());
        }
    }

    // LNK-2 has a link to another issue that's not visible to user
    public void testNoVisibleLinks() throws Exception
    {
        restoreData(true);
        Issue issue = issueClient.loginAs("reporter").get("LNK-2");
        assertEquals("Object should have no visible issue links", 0, issue.fields.issuelinks.size());
    }

    // LNK-4 has 1 visible and 1 invisible link
    public void testInvisibleIssueNotShown() throws Exception
    {
        restoreData(true);
        Issue issue = issueClient.loginAs("reporter").get("LNK-4");
        List<IssueLink> links = issue.fields.issuelinks;
        assertEquals(1, links.size());

        // only LNK-1 should be visible to reporter
        assertEquals("LNK-1", links.get(0).outwardIssue().key());
    }

    // LNK-4 has several issue links
    public void testSeveralIssueLinks() throws Exception
    {
        restoreData(true);
        Issue issue = issueClient.get("LNK-4");
        List<IssueLink> links = issue.fields.issuelinks;

        // LNK-1 and LNK-3 should both be visible to admin. order matters!
        assertEquals(2, links.size());
        assertEquals(links.get(0).outwardIssue().key(), "LNK-1");
        assertEquals(links.get(1).outwardIssue().key(), "LNK-3");
    }

    // tests that link metadata returned
    public void testEachIssueLinkShouldContainAllTheInformationNecessaryForClientsToRenderIt() throws Exception
    {
        restoreData(true);
        Issue issue = issueClient.loginAs("reporter").get("LNK-6");
        List<IssueLink> links = issue.fields.issuelinks;

        assertEquals(1, links.size());
        IssueLink linktoLnk1 = links.get(0);

        // full type information must be returned
        assertThat(linktoLnk1.type(), is(equalTo(
                new IssueLink.Type()
                        .id("10000")
                        .name("Duplicate")
                        .inward("is duplicated by")
                        .outward("duplicates")
                        .self(getBaseUrlPlus("/rest/api/2/issueLinkType/10000"))
        )));

        assertThat(linktoLnk1.inwardIssue(), is(equalTo(null)));
        assertThat(linktoLnk1.outwardIssue(), is(equalTo(
                new IssueLink.IssueLinkRef()
                        .id("10000")
                        .key("LNK-1")
                        .self(getRestApiUrl("/issue/10000"))
                        .fields(new IssueLink.IssueLinkRef.Fields()
                                .summary("1 visible link")
                                .issueType(new IssueType().id("1")
                                        .self(getRestApiUrl("/issuetype/1"))
                                        .name("Bug")
                                        .subtask(false)
                                        .description("A problem which impairs or prevents the functions of the product.")
                                        .iconUrl(getBaseUrlPlus("/images/icons/issuetypes/bug.png")))
                                .status(new Status().id("1")
                                        .name("Open")
                                        .self(getRestApiUrl("/status/1"))
                                        .description("The issue is open and ready for the assignee to start work on it.")
                                        .iconUrl(getBaseUrlPlus("/images/icons/statuses/open.png"))
                                        .statusCategory(
                                                new StatusCategory(getRestApiUrl("statuscategory/2"), 2l, "new", "blue-gray", "New")
                                        ))
                                .priority(new Priority().id("3")
                                        .name("Major")
                                        .self(getRestApiUrl("/priority/3"))
                                        .iconUrl(getBaseUrlPlus("/images/icons/priorities/major.png")))
                        )
        )));
    }

    public void testIssueEditShouldHandleAddLinkOperation() throws Exception
    {
        restoreData(true);

        final String fromIssueKey = "LNK-1";
        final String lnk5_key = "LNK-5";
        final String lnk6_key = "LNK-6";
        final String lnk6_id = "10010";
        final String duplicate_name = "Duplicate";
        final String duplicate_id = "10000";

        // check that the link doesn't exist before running the test
        assertThat("Link already exists, test needs to be updated", issueClient.get(fromIssueKey), not(hasLinkWithOutwardIssue(lnk5_key, duplicate_name)));
        assertThat("Link already exists, test needs to be updated", issueClient.get(fromIssueKey), not(hasLinkWithOutwardIssue(lnk6_key, duplicate_name)));

        // try to add a link using the issue keys and link type name
        issueClient.edit("LNK-1", new IssueUpdateRequest().update(FIELD_NAME, Arrays.asList(
                new FieldOperation().operation("add").value(
                        new LinkRequest().type(ResourceRef.withName(duplicate_name)).outwardIssue(ResourceRef.withKey(lnk5_key))
                )))
        );
        assertThat(issueClient.get(fromIssueKey), hasLinkWithOutwardIssue(lnk5_key, duplicate_name));

        // now add using the issue and link type id
        issueClient.edit("LNK-1", new IssueUpdateRequest().update(FIELD_NAME, Arrays.asList(
                new FieldOperation().operation("add").value(
                        new LinkRequest().type(ResourceRef.withId(duplicate_id)).outwardIssue(ResourceRef.withId(lnk6_id))
                )))
        );
        assertThat(issueClient.get(fromIssueKey), hasLinkWithOutwardIssue(lnk6_key, duplicate_name));
    }

    /**
     * Setup for an actual test
     */
    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
    }

    private void restoreData(boolean issueLinkingEnabled) throws IOException
    {
        administration.restoreData("TestIssueResourceIssueLinks.xml");
        if (!issueLinkingEnabled)
        {
            administration.issueLinking().disable();
        }
    }
}
