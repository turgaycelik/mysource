package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.admin.GeneralConfiguration;
import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.rest.api.issue.FieldOperation;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.testkit.client.restclient.Comment;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.Visibility;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceComments extends RestFuncTest
{
    private IssueClient issueClient;

    public void testCommentRendering() throws Exception
    {
        administration.restoreData("TestEditComment.xml");
        Issue hsp1 = issueClient.get("HSP-1");
        assertEquals("I'm a hero!", hsp1.fields.comment.getComments().get(0).body);

        Issue expandedHsp1 = issueClient.get("HSP-1", Issue.Expand.renderedFields);
        assertEquals("I'm a hero!", expandedHsp1.fields.comment.getComments().get(0).body);
        assertEquals("2007-02-13T17:09:12.012+1100", expandedHsp1.fields.comment.getComments().get(0).created);
        assertEquals("2007-02-13T17:09:12.012+1100", expandedHsp1.fields.comment.getComments().get(0).updated);
        assertEquals("I&#39;m a hero!", expandedHsp1.renderedFields.comment.getComments().get(0).body);
        assertEquals("13/Feb/07 5:09 PM", expandedHsp1.renderedFields.comment.getComments().get(0).created);
        assertEquals("13/Feb/07 5:09 PM", expandedHsp1.renderedFields.comment.getComments().get(0).updated);
    }

    public void testSystemTextFieldRendering() throws Exception
    {
        administration.restoreData("TestEditComment.xml");
        navigation.issue().setDescription("HSP-1", "I'll have 5<10<15 things?");
        navigation.issue().setEnvironment("HSP-1", "I'll have 5<10<15 things?");

        Issue hsp1 = issueClient.get("HSP-1");
        // wtf is up with the \r\n being prepended here?
        assertEquals("\r\nI'll have 5<10<15 things?", hsp1.fields.description);
        assertEquals("I'll have 5<10<15 things?", hsp1.fields.environment);

        Issue expandedHsp1 = issueClient.get("HSP-1", Issue.Expand.renderedFields);
        assertEquals("\r<br/>\nI&#39;ll have 5&lt;10&lt;15 things?", expandedHsp1.renderedFields.description);
        assertEquals("I&#39;ll have 5&lt;10&lt;15 things?", expandedHsp1.renderedFields.environment);
    }

    public void testComment() throws Exception
    {
        administration.restoreBlankInstance();
        administration.generalConfiguration().setCommentVisibility(GeneralConfiguration.CommentVisibility.PROJECT_ROLES);
        administration.roles().addProjectRoleForUser("monkey", "Administrators", ADMIN_USERNAME);
        final String key = navigation.issue().createIssue("monkey", "Bug", "First Test Issue");
        navigation.issue().addComment(key, "comment", "Administrators");

        Issue issue = issueClient.get(key);
        assertEquals(1, issue.fields.comment.getComments().size());

        tester.gotoPage("/rest/api/2/issue/" + key);
        Comment comment = issue.fields.comment.getComments().get(0);

        // assert the comment itself.
        assertEquals("comment", comment.body);
        assertEquals("role", comment.visibility.type);
        assertEquals("Administrators", comment.visibility.value);

        assertNotNull(comment.created);
        assertNotNull(comment.updated);

        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/issue/10000/comment/10000", comment.self);

        assertEquals(ADMIN_USERNAME, comment.author.name);
        assertEquals(ADMIN_FULLNAME, comment.author.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/user?username=admin", comment.author.self);

        assertEquals(ADMIN_USERNAME, comment.updateAuthor.name);
        assertEquals(ADMIN_FULLNAME, comment.updateAuthor.displayName);
        assertEquals(getEnvironmentData().getBaseUrl() + "/rest/api/2/user?username=admin", comment.updateAuthor.self);
    }

    public void testEditCommentBody() throws Exception
    {
        administration.restoreData("TestEditComment.xml");
        String issueKey = "HSP-1";

        IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        Map<String, List<FieldOperation>> operations = new HashMap<String, List<FieldOperation>>();
        String commentId = "10041";
        String commentBody = "Test";

        Comment jsonComment = new Comment();
        jsonComment.id = commentId;
        jsonComment.body = commentBody;
        addCommentOperation("edit", operations, jsonComment);
        issueUpdateRequest.update(operations);

        issueClient.edit(issueKey, issueUpdateRequest);
        Issue issue = issueClient.get(issueKey);
        Comment comment = issue.fields.comment.getComments().get(2);
        assertEquals(commentId, comment.id);
        assertEquals(commentBody, comment.body);
    }

    public void testEditCommentSecurityRoleLevel() throws Exception
    {
        administration.restoreData("TestEditComment.xml");
        String issueKey = "HSP-1";

        IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        Map<String, List<FieldOperation>> operations = new HashMap<String, List<FieldOperation>>();

        Comment jsonComment = new Comment();
        String commentId = "10041";
        jsonComment.id = commentId;
        jsonComment.visibility = new Visibility("role", "Administrators");
        addCommentOperation("edit", operations, jsonComment);
        issueUpdateRequest.update(operations);

        issueClient.edit(issueKey, issueUpdateRequest);
        Issue issue = issueClient.get(issueKey);
        Comment comment = issue.fields.comment.getComments().get(2);
        assertEquals(commentId, comment.id);
        assertEquals("role", comment.visibility.type);
        assertEquals("Administrators", comment.visibility.value);
    }

    public void testEditCommentSecurityGroupLevel() throws Exception
    {
        administration.restoreData("TestEditComment.xml");
        String issueKey = "HSP-1";

        IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        Map<String, List<FieldOperation>> operations = new HashMap<String, List<FieldOperation>>();

        Comment jsonComment = new Comment();
        String commentId = "10041";
        jsonComment.id = commentId;
        jsonComment.visibility = new Visibility("group", "jira-administrators");
        addCommentOperation("edit", operations, jsonComment);
        issueUpdateRequest.update(operations);

        issueClient.edit(issueKey, issueUpdateRequest);
        Issue issue = issueClient.get(issueKey);
        Comment comment = issue.fields.comment.getComments().get(2);
        assertEquals(commentId, comment.id);
        assertEquals("group", comment.visibility.type);
        assertEquals("jira-administrators", comment.visibility.value);
    }

    public void testEditCommentSecurityFromRoleToGroupLevel() throws Exception
    {
        administration.restoreData("TestEditComment.xml");
        String issueKey = "HSP-1";

        IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        Map<String, List<FieldOperation>> operations = new HashMap<String, List<FieldOperation>>();

        Comment jsonComment = new Comment();
        String commentId = "10041";
        jsonComment.id = commentId;
        jsonComment.visibility = new Visibility("role", "Administrators");
        addCommentOperation("edit", operations, jsonComment);
        issueUpdateRequest.update(operations);

        issueClient.edit(issueKey, issueUpdateRequest);

        Issue issue = issueClient.get(issueKey);
        Comment comment = issue.fields.comment.getComments().get(2);
        Visibility visibility = comment.visibility;
        assertEquals("role", visibility.type);
        assertEquals("Administrators", visibility.value);

        jsonComment = new Comment();
        jsonComment.id = commentId;
        jsonComment.visibility = new Visibility("group", "jira-administrators");
        addCommentOperation("edit", operations, jsonComment);
        issueUpdateRequest.update(operations);

        issueClient.edit(issueKey, issueUpdateRequest);

        issue = issueClient.get(issueKey);
        comment = issue.fields.comment.getComments().get(2);
        visibility = comment.visibility;
        assertEquals("group", visibility.type);
        assertEquals("jira-administrators", visibility.value);
    }

    public void testEditCommentRemoveSecurityLevel() throws Exception
    {
        administration.restoreData("TestEditComment.xml");
        String issueKey = "HSP-1";

        IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        Map<String, List<FieldOperation>> operations = new HashMap<String, List<FieldOperation>>();

        Comment jsonComment = new Comment();
        String commentId = "10041";
        jsonComment.id = commentId;
        jsonComment.visibility = new Visibility("role", "Administrators");
        addCommentOperation("edit", operations, jsonComment);
        issueUpdateRequest.update(operations);

        issueClient.edit(issueKey, issueUpdateRequest);

        Issue issue = issueClient.get(issueKey);
        Comment comment = issue.fields.comment.getComments().get(2);
        Visibility visibility = comment.visibility;
        assertEquals("role", visibility.type);
        assertEquals("Administrators", visibility.value);

        jsonComment = new Comment();
        jsonComment.id = commentId;
        jsonComment.visibility = null;
        addCommentOperation("edit", operations, jsonComment);
        issueUpdateRequest.update(operations);

        issueClient.edit(issueKey, issueUpdateRequest);

        issue = issueClient.get(issueKey);
        comment = issue.fields.comment.getComments().get(2);
        visibility = comment.visibility;
        assertNull("No security level expected!", visibility);
    }

    public void testRemoveComment() throws Exception
    {
        administration.restoreData("TestEditComment.xml");
        String issueKey = "HSP-1";
        String commentId = "10041";
        Issue issue = issueClient.get(issueKey);
        assertEquals(3, issue.fields.comment.getComments().size());
        Comment comment = issue.fields.comment.getComments().get(2);
        assertEquals(commentId, comment.id);

        IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        Map<String, List<FieldOperation>> operations = new HashMap<String, List<FieldOperation>>();

        Comment jsonComment = new Comment();
        jsonComment.id = commentId;
        addCommentOperation("remove", operations, jsonComment);
        issueUpdateRequest.update(operations);
        issueClient.edit(issueKey, issueUpdateRequest);

        issue = issueClient.get(issueKey);
        assertEquals(2, issue.fields.comment.getComments().size());
    }

    public void testAddCommentWithSecurityLevel() throws Exception
    {
        administration.restoreData("TestEditComment.xml");
        String issueKey = "HSP-1";
        IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        Map<String, List<FieldOperation>> operations = new HashMap<String, List<FieldOperation>>();

        Comment jsonComment = new Comment();
        jsonComment.body = "New Comment!";
        jsonComment.visibility = new Visibility("role", "Administrators");
        addCommentOperation("add", operations, jsonComment);
        issueUpdateRequest.update(operations);
        issueClient.edit(issueKey, issueUpdateRequest);

        Issue issue = issueClient.get(issueKey);
        Comment comment = issue.fields.comment.getComments().get(3);
        Visibility visibility = comment.visibility;
        assertEquals("role", visibility.type);
        assertEquals("Administrators", visibility.value);
    }



    private void addCommentOperation(String operation, Map<String, List<FieldOperation>> operations, Comment comment)
    {
        List<FieldOperation> fieldOperations = new ArrayList<FieldOperation>();
        FieldOperation fieldOperation = new FieldOperation();
        fieldOperation.init(operation, comment);
        fieldOperations.add(fieldOperation);
        operations.put("comment", fieldOperations);
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
}
