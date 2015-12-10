package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import java.util.HashMap;

import com.atlassian.jira.functest.framework.admin.GeneralConfiguration;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.testkit.client.restclient.Comment;
import com.atlassian.jira.testkit.client.restclient.CommentClient;
import com.atlassian.jira.testkit.client.restclient.CommentsWithPaginationBean;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.Visibility;
import com.atlassian.jira.util.json.JSONObject;
import com.atlassian.jira.webtests.Groups;

import com.google.inject.internal.Lists;
import com.meterware.httpunit.GetMethodWebRequest;
import com.meterware.httpunit.HttpNotFoundException;

import org.hamcrest.Matchers;

import junit.framework.AssertionFailedError;

import static org.junit.Assert.assertThat;

/**
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestCommentResource extends RestFuncTest
{
    private CommentClient commentClient;

    public void testViewCommentNotFound() throws Exception
    {
        administration.restoreData("TestEditComment.xml");

        // {"errorMessages":["Can not find a comment for the id: 1."],"errors":[]}
        Response content1 = commentClient.getResponse("HSP-1", "1");
        assertEquals(404, content1.statusCode);
        assertEquals(1, content1.entity.errorMessages.size());
        assertTrue(content1.entity.errorMessages.contains("Can not find a comment for the id: 1."));

        // {"errorMessages":["Can not find a comment for the id: piolho."],"errors":[]}
        Response contentPiolho = commentClient.getResponse("HSP-1", "piolho");
        assertEquals(404, contentPiolho.statusCode);
        assertEquals(1, contentPiolho.entity.errorMessages.size());
        assertTrue(contentPiolho.entity.errorMessages.contains("Can not find a comment for the id: piolho."));
    }

    public void testAnonymousComment() throws Exception
    {
        administration.restoreData("TestRESTAnonymous.xml");
        final Response<Comment> comment = commentClient.get("HSP-1", "10000");
        assertNull(comment.body.author);
    }

    public void testAnonymous() throws Exception
    {
        // first add a comment that only Administrators can see
        administration.restoreBlankInstance();
        administration.generalConfiguration().setCommentVisibility(GeneralConfiguration.CommentVisibility.PROJECT_ROLES);
        administration.roles().addProjectRoleForUser("monkey", "Administrators", ADMIN_USERNAME);
        final String key = navigation.issue().createIssue("monkey", "Bug", "First Test Issue");
        navigation.issue().addComment(key, "comment", "Administrators");

        navigation.logout();

        try
        {
            String url = getEnvironmentData().getBaseUrl() + "/rest/api/2/issue/HSP-1/comment/10000";
            tester.getDialog().getWebClient().sendRequest(new GetMethodWebRequest(url));
            fail();
        }
        catch (HttpNotFoundException expected)
        {
            // ignored
        }
    }

    public void testViewCommentJson() throws Exception
    {
        // first add a comment that only Administrators can see
        administration.restoreBlankInstance();
        administration.generalConfiguration().setCommentVisibility(GeneralConfiguration.CommentVisibility.PROJECT_ROLES);
        administration.roles().addProjectRoleForUser("monkey", "Administrators", ADMIN_USERNAME);
        final String key = navigation.issue().createIssue("monkey", "Bug", "First Test Issue");
        navigation.issue().addComment(key, "comment", "Administrators");

        Response<Comment> response = commentClient.get("MKY-1", "10000");
        Comment json = response.body;

        // we don't want to try verifying the actual timestamp because testing time is a path of madness, ask Andreas.
        assertNotNull(json.created);
        assertNotNull(json.updated);

        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/issue/10000/comment/10000", json.self);
        assertEquals("10000", json.id);
        assertEquals("comment", json.body);
        assertEquals("role", json.visibility.type);
        assertEquals("Administrators", json.visibility.value);

        assertEquals(ADMIN_USERNAME, json.author.name);
        assertEquals(ADMIN_FULLNAME, json.author.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/user?username=admin", json.author.self);

        assertEquals(ADMIN_USERNAME, json.updateAuthor.name);
        assertEquals(ADMIN_FULLNAME, json.updateAuthor.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/user?username=admin", json.updateAuthor.self);
    }

    public void testViewComments()
    {
        administration.restoreData("TestEditComment.xml");

        // {"errorMessages":["Can not find a comment for the id: 1."],"errors":[]}
        Response<CommentsWithPaginationBean> response = commentClient.getComments("HSP-1");

        assertEquals(3, response.body.getMaxResults().intValue());
        assertEquals(3, response.body.getTotal().intValue());
        assertEquals(3, response.body.getComments().size());

        Comment comment = response.body.getComments().get(0);
        assertNotNull(comment.created);
        assertNotNull(comment.updated);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/issue/10000/comment/10031", comment.self);
        assertEquals("10031", comment.id);
        assertEquals("I'm a hero!", comment.body);
        assertNull(comment.visibility);
        assertEquals(ADMIN_USERNAME, comment.author.name);
        assertEquals(ADMIN_USERNAME, comment.updateAuthor.name);
    }

    public void testGetCommentExpandRendered()
    {
        final String body = "*Bolded comment body*";
        final String renderedBody = "<p><b>Bolded comment body</b></p>";

        administration.restoreData("TestEditComment.xml");
        administration.fieldConfigurations().defaultFieldConfiguration().setRenderer("Comment", "Wiki Style Renderer");

        Comment newComment = new Comment();
        newComment.body = body;

        Response<Comment> post = commentClient.post("HSP-1", newComment);
        Response<Comment> get = commentClient.get("HSP-1", post.body.id, "renderedBody");
        assertEquals(renderedBody, get.body.renderedBody);
    }

    public void testGetCommentsExpandRendered()
    {
        final String body = "*Bolded comment body*";
        final String renderedBody = "<p><b>Bolded comment body</b></p>";

        administration.restoreData("TestEditComment.xml");
        administration.fieldConfigurations().defaultFieldConfiguration().setRenderer("Comment", "Wiki Style Renderer");

        Comment newComment = new Comment();
        newComment.body = body;

        Response<Comment> post = commentClient.post("HSP-1", newComment);
        Response<CommentsWithPaginationBean> response = commentClient.getComments("HSP-1", "renderedBody");
        Comment comment = response.body.getComments().get(response.body.getComments().size()-1);
        assertEquals(renderedBody, comment.renderedBody);
    }

    public void testEditComment()
    {
        administration.restoreData("TestEditComment.xml");

        Response<Comment> comparison = commentClient.get("HSP-1", "10031");

        Comment newComment = new Comment();
        newComment.body = "new comment body";
        newComment.visibility = new Visibility("group", "jira-administrators");
        newComment.id = comparison.body.id;

        comparison.body.body = newComment.body;
        comparison.body.visibility = newComment.visibility;

        Response<Comment> put = commentClient.put("HSP-1", newComment);
        assertNotNull(put);
        assertEquals(200, put.statusCode);
        assertCommentsEqual(comparison.body, put.body);

        Response<Comment> reget = commentClient.get("HSP-1", "10031");
        assertCommentsEqual(comparison.body, reget.body);
    }

    public void testEditCommentExpandRendered()
    {
        final String body = "*Bolded comment body*";
        final String renderedBody = "<p><b>Bolded comment body</b></p>";

        administration.restoreData("TestEditComment.xml");
        administration.fieldConfigurations().defaultFieldConfiguration().setRenderer("Comment", "Wiki Style Renderer");

        Response<Comment> comparison = commentClient.get("HSP-1", "10031");

        Comment newComment = new Comment();
        newComment.body = body;
        newComment.id = comparison.body.id;

        Response<Comment> post = commentClient.put("HSP-1", newComment, "renderedBody");
        assertEquals(renderedBody, post.body.renderedBody);
    }

    public void testEditCommentWhenRoleDoesntExist()
    {
        administration.restoreData("TestEditComment.xml");

        Response<Comment> comment = commentClient.get("HSP-1", "10031");
        Comment newComment = new Comment();
        newComment.id = comment.body.id;
        newComment.body = "new comment body";
        newComment.visibility = new Visibility("role", "not-existing-role");

        Response post = commentClient.put("HSP-1", newComment);
        assertEquals(400, post.statusCode);
        assertEquals(1, post.entity.errors.size());
    }

    public void testEditCommentWhenGroupDoesntExist()
    {
        administration.restoreData("TestEditComment.xml");

        Response<Comment> comment = commentClient.get("HSP-1", "10031");
        Comment newComment = new Comment();
        newComment.id = comment.body.id;
        newComment.body = "new comment body";
        newComment.visibility = new Visibility("group", "not-existing-group");

        Response post = commentClient.put("HSP-1", newComment);
        assertEquals(400, post.statusCode);
        assertEquals(1, post.entity.errors.size());
    }

    public void testAddComment()
    {
        administration.restoreData("TestEditComment.xml");

        Comment newComment = new Comment();
        newComment.body = "new comment body";
        newComment.visibility = new Visibility("group", "jira-administrators");

        Response<Comment> post = commentClient.post("HSP-1", newComment);
        assertNotNull(post);
        assertEquals(201, post.statusCode);
        assertNotNull(post.body);
        assertEquals("new comment body", post.body.body);
        assertEquals("group", post.body.visibility.type);
        assertEquals("jira-administrators", post.body.visibility.value);

        Response<Comment> reget = commentClient.get("HSP-1", post.body.id);
        assertCommentsEqual(post.body, reget.body);

        newComment = new Comment();
        post = commentClient.post("HSP-1", newComment);
        assertEquals(400, post.statusCode);
        assertEquals(1, post.entity.errors.size());
    }

    public void testAddCommentExpandRendered()
    {
        final String body = "*Bolded comment body*";
        final String renderedBody = "<p><b>Bolded comment body</b></p>";

        administration.restoreData("TestEditComment.xml");
        administration.fieldConfigurations().defaultFieldConfiguration().setRenderer("Comment", "Wiki Style Renderer");

        Comment newComment = new Comment();
        newComment.body = body;

        Response<Comment> post = commentClient.post("HSP-1", newComment, "renderedBody");
        assertEquals(renderedBody, post.body.renderedBody);
    }

    public void testAddCommentWhenRoleDoesntExist()
    {
        administration.restoreData("TestEditComment.xml");

        Comment newComment = new Comment();
        newComment.body = "new comment body";
        newComment.visibility = new Visibility("role", "not-existing-role");

        Response post = commentClient.post("HSP-1", newComment);
        assertEquals(400, post.statusCode);
        assertEquals(1, post.entity.errors.size());
    }

    public void testAddCommentWhenGroupDoesntExist()
    {
        administration.restoreData("TestEditComment.xml");

        Comment newComment = new Comment();
        newComment.body = "new comment body";
        newComment.visibility = new Visibility("group", "not-existing-group");

        Response post = commentClient.post("HSP-1", newComment);
        assertEquals(400, post.statusCode);
        assertEquals(1, post.entity.errors.size());
    }

    public void testDeleteComment()
    {
        administration.restoreData("TestEditComment.xml");

        Comment newComment = new Comment();
        newComment.body = "new comment body";
        newComment.visibility = new Visibility("group", "jira-administrators");
        Response<Comment> put = commentClient.post("HSP-1", newComment);

        Response deleteResponse = commentClient.delete("HSP-1", put.body);
        assertEquals(204, deleteResponse.statusCode);

        Response response = commentClient.getResponse("HSP-1", put.body.id);
        assertEquals(404, response.statusCode);
        assertEquals(1, response.entity.errorMessages.size());
        assertTrue(response.entity.errorMessages.get(0).startsWith("Can not find a comment for the id: 10050."));

        deleteResponse = commentClient.delete("HSP-1", put.body);
        assertEquals(404, deleteResponse.statusCode);
        assertEquals(1, deleteResponse.entity.errorMessages.size());
        assertTrue(deleteResponse.entity.errorMessages.get(0).startsWith("Can not find a comment for the id: 10050."));
    }

    public void testIncorrectIssueKey()
    {
        Response<CommentsWithPaginationBean> response = commentClient.getComments("NONEXISTANT-1");
        assertEquals(404, response.statusCode);
        assertEquals(1, response.entity.errorMessages.size());
        assertTrue(response.entity.errorMessages.get(0).startsWith("Issue Does Not Exist"));

        Response<Comment> response2 = commentClient.get("NONEXISTANT-1", "00000");
        assertEquals(404, response2.statusCode);
        assertEquals(1, response2.entity.errorMessages.size());
        assertTrue(response2.entity.errorMessages.get(0).startsWith("Issue Does Not Exist"));
    }

    public void testGetCommentFromWrongIssue()
    {
        administration.restoreData("TestEditComment.xml");

        // {"errorMessages":["Can not find a comment for the id: 1."],"errors":[]}
        Response<CommentsWithPaginationBean> goodResponse = commentClient.getComments("HSP-1");
        Comment comment = goodResponse.body.getComments().get(0);

        Response<Comment> response = commentClient.get("HSP-2", comment.id);
        assertEquals(404, response.statusCode);
        assertEquals(1, response.entity.errorMessages.size());
        assertTrue(response.entity.errorMessages.get(0).startsWith("No comment with given ID found for issue HSP-2"));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        commentClient = new CommentClient(getEnvironmentData());
    }




    public void testAdminCanEditAndDeleteAllComments() throws Exception
    {
        administration.restoreData("TestCommentPermissions.xml");

        // Should see 3 comments
        Response<CommentsWithPaginationBean> comments = commentClient.getComments("RAT-2");
        assertEquals(3, comments.body.getComments().size());

        // test that admin can edit own comments
        Response<Comment> original = commentClient.get("RAT-2", "10000");
        original.body.body = "admin edited comment";
        Response<Comment> updated = commentClient.put("RAT-2", original.body);
        assertCommentsEqual(updated.body, original.body);
        assertEquals(200, updated.statusCode);

        // test that admin can edit other user's comments
        original = commentClient.get("RAT-2", "10001");
        original.body.body = "admin edited comment";
        updated = commentClient.put("RAT-2", original.body);
        assertCommentsEqual(updated.body, original.body);
        assertEquals(200, updated.statusCode);

        // test that admin can even edit the anonymous comment.
        original = commentClient.get("RAT-2", "10002");
        original.body.body = "admin edited comment";
        updated = commentClient.put("RAT-2", original.body);
        assertCommentsEqual(updated.body, original.body);
        assertEquals(200, updated.statusCode);

        // Assert he can delete Fred's
        Response deleteResponse = commentClient.delete("RAT-2", "10001");
        assertEquals(204, deleteResponse.statusCode);

        // Assert he can delete anon
        deleteResponse = commentClient.delete("RAT-2", "10002");
        assertEquals(204, deleteResponse.statusCode);

        //Assert he can delete his own
        deleteResponse = commentClient.delete("RAT-2", "10000");
        assertEquals(204, deleteResponse.statusCode);
    }

    public void testFredCanEditAndDeleteOwnComments() throws Exception
    {
        administration.restoreData("TestCommentPermissions.xml");

        // log in as fred
        commentClient.loginAs(FRED_USERNAME);

        // Should see 3 comments
        Response<CommentsWithPaginationBean> comments =commentClient.getComments("RAT-2");
        assertEquals(3, comments.body.getComments().size());

        // Being normal user, he has Edit own Comments and Delete own Comments.
        // test that he can edit own comments
        Response<Comment> original = commentClient.get("RAT-2", "10001");
        original.body.body = "fred edited comment";
        Response<Comment> updated = commentClient.put("RAT-2", original.body);
        assertCommentsEqual(updated.body, original.body);
        assertEquals(200, updated.statusCode);

        // test that he can't edit other user's comments
        original = commentClient.get("RAT-2", "10000");
        String originalBody = original.body.body;
        original.body.body = "fred edited comment";
        updated = commentClient.put("RAT-2", original.body);
        assertEquals(400, updated.statusCode);
        assertEquals(1, updated.entity.errorMessages.size());
        assertTrue(updated.entity.errorMessages.get(0).contains("do not have the permission to edit this comment."));

        original.body.body = originalBody;
        Response<Comment> reget = commentClient.get("RAT-2", "10000");
        assertCommentsEqual(reget.body, original.body);

        // Assert he can delete Fred's
        Response deleteResponse = commentClient.delete("RAT-2", "10001");
        assertEquals(204, deleteResponse.statusCode);

        // Assert he cant delete anon
        original = commentClient.get("RAT-2", "10002");
        deleteResponse = commentClient.delete("RAT-2", "10002");
        assertEquals(400, deleteResponse.statusCode);
        assertEquals(1, deleteResponse.entity.errorMessages.size());
        assertTrue(deleteResponse.entity.errorMessages.get(0).contains("You do not have permission to delete comment with id"));

        reget = commentClient.get("RAT-2", "10002");
        assertEquals(200, reget.statusCode);
        assertNotNull(reget.body);
        assertCommentsEqual(original.body, reget.body);
    }

    public void testAnonCanEditAndDeleteNothing() throws Exception
    {
        administration.restoreData("TestCommentPermissions.xml");

        // logout (now I'm anonymous).
        commentClient.anonymous();

        // Browse to RAT-2
        Response<CommentsWithPaginationBean> comments = commentClient.getComments("RAT-2");
        assertEquals(3, comments.body.getComments().size());

        // test that he can't edit other user's comments
        Response<Comment> original = commentClient.get("RAT-2", "10000");
        String originalBody = original.body.body;
        original.body.body = "fred edited comment";
        Response<Comment> updated = commentClient.put("RAT-2", original.body);
        assertEquals(400, updated.statusCode);
        assertEquals(1, updated.entity.errorMessages.size());
        assertTrue(updated.entity.errorMessages.get(0).startsWith("You do not have the permission to edit this comment."));

        original.body.body = originalBody;
        Response<Comment> reget = commentClient.get("RAT-2", "10000");
        assertCommentsEqual(reget.body, original.body);

        // test that he can't edit other anonymour's comments
        original = commentClient.get("RAT-2", "10000");
        originalBody = original.body.body;
        original.body.body = "fred edited comment";
        updated = commentClient.put("RAT-2", original.body);
        assertEquals(400, updated.statusCode);
        assertEquals(1, updated.entity.errorMessages.size());
        assertTrue(updated.entity.errorMessages.get(0).startsWith("You do not have the permission to edit this comment."));

        original.body.body = originalBody;
        reget = commentClient.get("RAT-2", "10000");
        assertCommentsEqual(reget.body, original.body);

        // Assert he cant delete normal comment
        original = commentClient.get("RAT-2", "10002");
        Response deleteResponse = commentClient.delete("RAT-2", "10002");
        assertEquals(400, deleteResponse.statusCode);
        assertEquals(1, deleteResponse.entity.errorMessages.size());
        assertTrue(deleteResponse.entity.errorMessages.get(0).startsWith("You do not have permission to delete comment with id"));

        reget = commentClient.get("RAT-2", "10002");
        assertEquals(200, reget.statusCode);
        assertNotNull(reget.body);
        assertCommentsEqual(original.body, reget.body);

        // Assert he cant delete anon comment
        original = commentClient.get("RAT-2", "10002");
        deleteResponse = commentClient.delete("RAT-2", "10002");
        assertEquals(400, deleteResponse.statusCode);
        assertEquals(1, deleteResponse.entity.errorMessages.size());
        assertTrue(deleteResponse.entity.errorMessages.get(0).startsWith("You do not have permission to delete comment with id"));

        reget = commentClient.get("RAT-2", "10002");
        assertEquals(200, reget.statusCode);
        assertNotNull(reget.body);
        assertCommentsEqual(original.body, reget.body);
    }

    public void testCanAddCommentWhenNoIssueEditPerm()
    {
        administration.restoreData("TestIssueResourceEditMeta.xml");

        // check the config is correct and he can't actually edit the issue
        IssueClient issueClient = new IssueClient(getEnvironmentData());
        Issue issue = issueClient.loginAs("farnsworth").get("PH-1", Issue.Expand.editmeta);
        assertNotNull(issue.editmeta);
        assertTrue(issue.editmeta.fields.isEmpty());

        Comment newComment = new Comment();
        newComment.body = "new comment body";

        Response<Comment> post = commentClient.loginAs("farnsworth").post("PH-1", newComment);
        assertNotNull(post);
        assertEquals(201, post.statusCode);
        assertNotNull(post.body);
        assertEquals("new comment body", post.body.body);

        Response<Comment> reget = commentClient.loginAs("farnsworth").get("PH-1", post.body.id);
        assertCommentsEqual(post.body, reget.body);
    }

    public void testSettingPropertyDuringCreate()
    {
        administration.restoreBlankInstance();
        setupPermissionsToEditComments();

        IssueCreateResponse issue = backdoor.issues().createIssue("HSP", "Issue with comments properties");

        Comment comment = new Comment();
        comment.body = "comment with properties";

        final HashMap<String, Object> commentPropValue = new HashMap<String, Object>();
        commentPropValue.put("value", "proper-val");
        Comment.CommentProperty commentProperty = new Comment.CommentProperty("key", new JSONObject(commentPropValue).toString());

        comment.properties = Lists.newArrayList(commentProperty);

        Comment commentResponse = commentClient.post(issue.key, comment, "properties").body;

        assertThat(commentResponse.body, Matchers.is("comment with properties"));
        assertThat(commentResponse.properties, Matchers.<Comment.CommentProperty>hasItem(Matchers.allOf(
                Matchers.hasProperty("key", Matchers.is("key")),
                Matchers.hasProperty("value", Matchers.containsString("proper-val"))
        )));
    }

    public void testUpdateCommentWithProperty()
    {
        administration.restoreBlankInstance();
        setupPermissionsToEditComments();

        IssueCreateResponse issue = backdoor.issues().createIssue("HSP", "Issue with comments properties");

        Comment comment = new Comment();
        comment.body = "comment with properties";

        Comment commentCreateResponse = commentClient.post(issue.key, comment, "properties").body;

        assertThat(commentCreateResponse.body, Matchers.is("comment with properties"));
        assertThat(commentCreateResponse.properties, Matchers.hasSize(0));

        Comment updatedComment = new Comment();
        updatedComment.body = "comment with updated properties";
        updatedComment.id = commentCreateResponse.id;

        final HashMap<String, Object> commentPropValue = new HashMap<String, Object>();
        commentPropValue.put("value", "proper-val");
        Comment.CommentProperty commentProperty = new Comment.CommentProperty("key", new JSONObject(commentPropValue).toString());
        updatedComment.properties = Lists.newArrayList(commentProperty);

        Comment updatedCommentResponse = commentClient.put(issue.key, updatedComment, "properties").body;

        assertThat(updatedCommentResponse.body, Matchers.is("comment with updated properties"));
        assertThat(updatedCommentResponse.properties, Matchers.<Comment.CommentProperty>hasItem(Matchers.allOf(
                Matchers.hasProperty("key", Matchers.is("key")),
                Matchers.hasProperty("value", Matchers.containsString("proper-val"))
        )));
    }

    private void setupPermissionsToEditComments()
    {
        Long permSchemeId = backdoor.permissionSchemes().copyDefaultScheme("comment perm scheme");
        Long projectId = backdoor.project().getProjectId("HSP");
        backdoor.permissionSchemes().addGroupPermission(permSchemeId, Permissions.COMMENT_EDIT_ALL, Groups.USERS);
        backdoor.project().setPermissionScheme(projectId, permSchemeId);
    }

    public void assertCommentsEqual(Comment comment1, Comment comment2)
    {
        //ignores updated date and author for easyness
        assertEquals(comment1.self, comment2.self);
        assertEquals(comment1.id, comment2.id);
        assertEquals(comment1.body, comment2.body);

        if ((comment1.author == null && comment2.author != null) || (comment1.author != null && comment2.author == null))
        {
            throw new AssertionFailedError("Authors not the same, one null, the other not");
        }
        else if (comment1.author != null && comment2.author != null)
        {
            assertEquals(comment1.author.displayName, comment1.author.displayName);
        }

        if ((comment1.visibility == null && comment2.visibility != null) || (comment1.visibility != null && comment2.visibility == null))
        {
            throw new AssertionFailedError("Visibility not the same, one null, the other not");
        }
        else if (comment1.visibility != null && comment2.visibility != null)
        {
            assertEquals(comment1.visibility.type, comment2.visibility.type);
            assertEquals(comment1.visibility.value, comment2.visibility.value);
        }
    }
}
