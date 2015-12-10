package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.ResourceRef;
import com.atlassian.jira.testkit.client.restclient.Comment;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.IssueLink;
import com.atlassian.jira.testkit.client.restclient.LinkIssueClient;
import com.atlassian.jira.testkit.client.restclient.LinkRequest;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.Visibility;

import java.util.List;

import static com.atlassian.jira.functest.matcher.LinkedIssuesMatcher.hasLinkWithInwardIssue;
import static com.atlassian.jira.functest.matcher.LinkedIssuesMatcher.hasLinkWithOutwardIssue;
import static org.hamcrest.Matchers.equalTo;
import static org.hamcrest.Matchers.hasItem;
import static org.junit.Assert.assertThat;

/**
 *
 *
 * @since v4.3
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestLinkIssueResource extends RestFuncTest
{
    /**
     * A link request to be used as-is or to quickly build up other link requests. Contains:
     * <pre>
     *     {
     *         "type": { "name": "Duplicate" },
     *         "fromIssueKey": "HSP-1",
     *         "toIssueKey": "MKY-1"
     *     }
     * </pre>
     */
    static final LinkRequest DUP_HSP1_MKY1 = new LinkRequest()
                    .type(ResourceRef.withName("Duplicate"))
                    .inwardIssue(ResourceRef.withKey("HSP-1"))
                    .outwardIssue(ResourceRef.withKey("MKY-1"));

    private LinkIssueClient linkIssueClient;
    private IssueClient issueClient;

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        linkIssueClient = new LinkIssueClient(getEnvironmentData());
        issueClient = new IssueClient(getEnvironmentData());
        administration.restoreData("TestLinkIssueResource.xml");
    }

    public void testLinkIssuesWithComment() throws Exception
    {
        Comment comment = new Comment();
        comment.body = "Issue linked via REST!";

        LinkRequest linkRequest = DUP_HSP1_MKY1.comment(comment);

        final Response response = linkIssueClient.linkIssues( linkRequest);
        assertEquals(201, response.statusCode);

        Issue issue = issueClient.get("HSP-1");
        List<Comment> comments = issue.fields.comment.getComments();
        assertEquals(comment.body, comments.get(0).body);
        List<IssueLink> issueLinks = issue.fields.issuelinks;
        IssueLink issueLink = issueLinks.get(0);
        assertEquals("MKY-1", issueLink.outwardIssue().key());
        IssueLink.Type type = issueLink.type();
        assertEquals("Duplicate", type.name());

        issue = issueClient.get("MKY-1");
        comments = issue.fields.comment.getComments();
        assertEquals(0, comments.size());
        issueLinks = issue.fields.issuelinks;
        issueLink = issueLinks.get(0);
        assertEquals("HSP-1", issueLink.inwardIssue().key());
        type = issueLink.type();
        assertEquals("Duplicate", type.name());
    }

    public void testLinkIssues() throws Exception
    {
        Response linkToMky1Resp = linkIssueClient.linkIssues(DUP_HSP1_MKY1);
        assertEquals(201, linkToMky1Resp.statusCode);
        assertThat(issueClient.get("HSP-1"), hasLinkWithOutwardIssue("MKY-1", "Duplicate"));

        final ResourceRef duplicates_id = ResourceRef.withId("10000");
        final ResourceRef hsp1_key = ResourceRef.withKey("HSP-1");
        final ResourceRef hsp2_id = ResourceRef.withId("10101");

        // now try to link to HSP-2 using link and issue id's instead of name/key
        Response linkToHsp2Resp = linkIssueClient.linkIssues(DUP_HSP1_MKY1.type(duplicates_id).inwardIssue(hsp1_key).outwardIssue(hsp2_id));
        assertEquals(201, linkToHsp2Resp.statusCode);
        assertThat(issueClient.get("HSP-2"), hasLinkWithInwardIssue("HSP-1", "Duplicate"));
    }

    public void testLinkIssuesToSelf() throws Exception
    {
        LinkRequest linkRequest = new LinkRequest()
                    .type(ResourceRef.withName("Duplicate"))
                    .inwardIssue(ResourceRef.withKey("HSP-1"))
                    .outwardIssue(ResourceRef.withKey("HSP-1"));

        Response response = linkIssueClient.linkIssues(linkRequest);
        assertEquals(400, response.statusCode);
        assertEquals("You cannot link an issue to itself.", response.entity.errorMessages.get(0));
    }

    public void testLinkIssuesWithInvalidRoleLevelSpecified() throws Exception
    {
        Comment comment = new Comment();
        comment.body = "Issue linked via REST!";
        comment.visibility = new Visibility("role", "Developers");

        LinkRequest linkRequest = DUP_HSP1_MKY1.comment(comment);

        final Response response = linkIssueClient.linkIssues( linkRequest);
        assertEquals(400, response.statusCode);
        assertEquals("You are currently not a member of the project role: Developers.", response.entity.errors.values().iterator().next());
    }

    public void testLinkIssuesWithGroupLevelSpecified() throws Exception
    {
        Comment comment = new Comment();
        comment.body = "Issue linked via REST!";
        comment.visibility = new Visibility("group", "jira-administrators");

        LinkRequest linkRequest = DUP_HSP1_MKY1.comment(comment);

        final Response response = linkIssueClient.linkIssues( linkRequest);
        assertEquals(201, response.statusCode);
        final Issue issue = issueClient.get("HSP-1");
        final List<Comment> comments = issue.fields.comment.getComments();
        assertEquals(comment.body, comments.get(0).body);
    }

    public void testLinkIssuesWithRoleLevelSpecified() throws Exception
    {
        Comment comment = new Comment();
        comment.body = "Issue linked via REST!";
        comment.visibility = new Visibility("role", "Administrators");

        LinkRequest linkRequest = DUP_HSP1_MKY1.comment(comment);

        final Response response = linkIssueClient.linkIssues( linkRequest);
        assertEquals(201, response.statusCode);
        final Issue issue = issueClient.get("HSP-1");
        final List<Comment> comments = issue.fields.comment.getComments();
        assertEquals(comment.body, comments.get(0).body);
    }

    public void testLinkIssuesFailedBecauseIssueLinkingDisabled() throws Exception
    {
        oldway_consider_porting.deactivateIssueLinking();
        Comment comment = new Comment();
        comment.body = "Issue linked via REST!";
        LinkRequest linkRequest = DUP_HSP1_MKY1.comment(comment);

        final Response response = linkIssueClient.linkIssues( linkRequest);
        assertEquals(404, response.statusCode);
        assertEquals("Issue linking is currently disabled.", response.entity.errorMessages.get(0));
    }

    public void testLinkIssuesFailedIssueANotVisible() throws Exception
    {
        final Response response = linkIssueClient.loginAs("fred").linkIssues(DUP_HSP1_MKY1);

        assertEquals(404, response.statusCode);
        assertEquals("You do not have the permission to see the specified issue.", response.entity.errorMessages.get(0));
    }

    public void testLinkIssuesFailedIssueBNotVisible() throws Exception
    {
        final Response response = linkIssueClient.loginAs("bob").linkIssues(DUP_HSP1_MKY1);

        assertEquals(404, response.statusCode);
        assertEquals("You do not have the permission to see the specified issue.", response.entity.errorMessages.get(0));
    }

    public void testLinkIssuesFailedNoLInkIssuePermissionIssueA() throws Exception
    {
        LinkRequest linkRequest = DUP_HSP1_MKY1.outwardIssue(ResourceRef.withKey("HSP-2"));

        final Response response = linkIssueClient.loginAs("bob").linkIssues( linkRequest);
        assertEquals(401, response.statusCode);
        assertEquals("No Link Issue Permission for issue 'HSP-1'", response.entity.errorMessages.get(0));
    }

    public void testLinkIssuesFailedNoLInkIssuePermissionIssueB() throws Exception
    {
        Comment comment = new Comment();
        comment.body = "Issue linked via REST!";

        LinkRequest linkRequest = DUP_HSP1_MKY1
                .inwardIssue(ResourceRef.withKey("MKY-1"))
                .outwardIssue(ResourceRef.withKey("MKY-2"))
                .comment(comment);

        final Response response = linkIssueClient.loginAs("fred").linkIssues( linkRequest);
        assertEquals(401, response.statusCode);
        assertEquals("No Link Issue Permission for issue 'MKY-1'", response.entity.errorMessages.get(0));
    }

    public void testLinkIssuesFailedBecauseLinkTypeDoesNotExist() throws Exception
    {
        LinkRequest linkRequest = DUP_HSP1_MKY1.type(ResourceRef.withName("calculated"));

        final Response response = linkIssueClient.linkIssues( linkRequest);
        assertEquals(404, response.statusCode);
        assertEquals("No issue link type with name 'calculated' found.", response.entity.errorMessages.get(0));
    }

    public void testLinkIssuesLinkIssuePermissionForIssueAButNotB() throws Exception
    {
        final Response response = linkIssueClient.loginAs("linker").linkIssues(DUP_HSP1_MKY1);

        assertEquals(201, response.statusCode);
    }

    public void testLinkIssuesNoLinkIssuePermissionForIssueBButB() throws Exception
    {
        LinkRequest linkRequest = DUP_HSP1_MKY1
                .inwardIssue(ResourceRef.withKey("MKY-1"))
                .outwardIssue(ResourceRef.withKey("HSP-1"));

        final Response response = linkIssueClient.loginAs("linker").linkIssues( linkRequest);
        assertEquals(401, response.statusCode);
        assertEquals("No Link Issue Permission for issue 'MKY-1'", response.entity.errorMessages.get(0));
    }

    public void testLinkIssuesUsingIssueTypeIdInsteadOfName() throws Exception
    {
        Response notFoundResponse = linkIssueClient.linkIssues(DUP_HSP1_MKY1.type(ResourceRef.withId("1")));
        assertEquals(404, notFoundResponse.statusCode);
        assertThat(notFoundResponse.entity.errorMessages, hasItem("No issue link type with id '1' found."));

        Response resp = linkIssueClient.linkIssues(DUP_HSP1_MKY1.type(ResourceRef.withId("10100")));
        assertEquals(201, resp.statusCode);
        assertThat(issueClient.get("HSP-1").fields.issuelinks.get(0).type().name(), equalTo("Blocks"));
    }

    public void testGetIssueLink() throws Exception
    {
        IssueLink issueLink = linkIssueClient.getIssueLink("10100");
        assertEquals("10100", issueLink.id());
        assertEquals(getRestApiUri("issueLink/10100"), issueLink.self());
        assertEquals(issueLink.inwardIssue().id(), "10200");
        assertEquals(issueLink.outwardIssue().id(), "10201");
    }

    public void testGetIssueLinkInvalidId() throws Exception
    {
        Response linkIssueClientIssueLinkResponse = linkIssueClient.getIssueLinkResponse("32423");
        assertEquals(404, linkIssueClientIssueLinkResponse.statusCode);
        assertEquals("No issue link with id '32423' exists.", linkIssueClientIssueLinkResponse.entity.errorMessages.get(0));
    }

    public void testGetIssueLinkWithoutPermission() throws Exception
    {
        Response response = linkIssueClient.loginAs("linker").getIssueLinkResponse("10100");
        assertEquals(404, response.statusCode);
    }

    public void testDeleteIssueLink() throws Exception
    {
        Response response = linkIssueClient.deleteIssueLink("10100");
        assertEquals(204, response.statusCode);
        Response linkIssueClientIssueLinkResponse = linkIssueClient.getIssueLinkResponse("10100");
        assertEquals(404, linkIssueClientIssueLinkResponse.statusCode);
        assertEquals("No issue link with id '10100' exists.", linkIssueClientIssueLinkResponse.entity.errorMessages.get(0));
    }

    public void testDeleteIssueLinkInvalidId() throws Exception
    {
        Response linkIssueClientIssueLinkResponse = linkIssueClient.deleteIssueLink("32423");
        assertEquals(404, linkIssueClientIssueLinkResponse.statusCode);
        assertEquals("No issue link with id '32423' exists.", linkIssueClientIssueLinkResponse.entity.errorMessages.get(0));
    }

    public void testDeleteIssueLinkWithoutPermission() throws Exception
    {
        Response response = linkIssueClient.loginAs("linker").deleteIssueLink("10100");
        assertEquals(404, response.statusCode);
    }



}
