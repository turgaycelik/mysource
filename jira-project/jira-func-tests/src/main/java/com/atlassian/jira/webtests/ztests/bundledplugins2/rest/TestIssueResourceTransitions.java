package com.atlassian.jira.webtests.ztests.bundledplugins2.rest;

import com.atlassian.jira.functest.framework.suite.Category;
import com.atlassian.jira.functest.framework.suite.WebTest;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.rest.api.issue.FieldOperation;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.rest.api.issue.ResourceRef;
import com.atlassian.jira.testkit.client.restclient.Comment;
import com.atlassian.jira.testkit.client.restclient.Issue;
import com.atlassian.jira.testkit.client.restclient.IssueClient;
import com.atlassian.jira.testkit.client.restclient.IssueTransitionsMeta;
import com.atlassian.jira.testkit.client.restclient.Response;
import com.atlassian.jira.testkit.client.restclient.TransitionsClient;
import com.atlassian.jira.testkit.client.restclient.Visibility;
import com.google.common.collect.Lists;
import org.codehaus.jackson.JsonNode;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.hamcrest.CoreMatchers.equalTo;
import static org.junit.Assert.assertThat;

/**
 * @since v4.2
 */
@WebTest ({ Category.FUNC_TEST, Category.REST })
public class TestIssueResourceTransitions extends RestFuncTest
{
    private IssueClient issueClient;
    private TransitionsClient transitionsClient;

    public void testTransitionLink() throws Exception
    {
        administration.restoreData("TestWorkflowActions.xml");

        Issue issue = issueClient.get("HSP-1", Issue.Expand.transitions);

        assertEquals(3, issue.transitions.size());
    }

    public void testCustomFieldInTransition() throws Exception
    {
        administration.restoreData("TestIssueResourceTransitions.xml");

        final IssueTransitionsMeta transitions = transitionsClient.get("HSP-1");
        final Map<String,IssueTransitionsMeta.TransitionField> fields = transitions.transitions.get(2).fields;
        for (Map.Entry<String, IssueTransitionsMeta.TransitionField> field : fields.entrySet())
        {
            if (field.getKey().equals("customfield_10000"))
            {
                assertEquals("com.atlassian.jira.plugin.system.customfieldtypes:float", field.getValue().schema.custom);
            }
        }
    }

    // JRADEV-3474 JRADEV-3471
    public void testNumberCustomFieldLocalized() throws Exception
    {
        administration.restoreData("TestIssueResourceTransitions.xml");
        navigation.userProfile().changeUserLanguage("fran\u00e7ais (France)");

        IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        issueUpdateRequest.fields(new IssueFields());
        issueUpdateRequest.transition(ResourceRef.withId("2"));

        issueUpdateRequest.fields().resolution(new ResourceRef().name("Duplicate"));
        issueUpdateRequest.fields().customField(10000l, 2.5);
        final Response response = transitionsClient.postResponse("HSP-1", issueUpdateRequest);
        assertEquals(204, response.statusCode);

        navigation.userProfile().changeUserLanguageToJiraDefault();
    }

    public void testIssueTransitionDestination() throws Exception
    {
        administration.restoreData("TestIssueResourceTransitions.xml");

        final IssueTransitionsMeta transitions = transitionsClient.get("HSP-1");
        assertEquals(3, transitions.transitions.size());

        // Mask to check we get all the cases
        byte mask = 0;
        for (IssueTransitionsMeta.Transition transition : transitions.transitions)
        {
            switch (transition.id)
            {
                case 2 :
                    assertEquals(transition.to.id(), "6");
                    mask |= 1;
                    break;
                case 4 :
                    assertEquals(transition.to.id(), "3");
                    mask |= 2;
                    break;
                case 5 :
                    assertEquals(transition.to.id(), "5");
                    mask |= 4;
                    break;
            }
        }
        // Check all cases
        assertEquals(mask, 7);
    }

    public void testTransitionGET() throws Exception
    {
        administration.restoreData("TestWorkflowActions.xml");

        final IssueTransitionsMeta transitions = transitionsClient.get("HSP-1");

        assertEquals(3, transitions.transitions.size());

        // Mask to check we get all the cases
        byte mask = 0;
        for (IssueTransitionsMeta.Transition transition : transitions.transitions)
        {
            switch (transition.id)
            {
                case 2 :
                    assertEquals("Close Issue", transition.name);
                    final Map<String, IssueTransitionsMeta.TransitionField> closeIssue = transition.fields;
                    assertEquals(3, closeIssue.size());
                    assertNotNull(closeIssue.get("resolution"));
                    assertEquals("resolution", closeIssue.get("resolution").schema.type);
                    assertTrue(closeIssue.get("resolution").required);
                    assertNotNull(closeIssue.get("fixVersions"));
                    assertEquals("array", closeIssue.get("fixVersions").schema.type);
                    assertFalse(closeIssue.get("fixVersions").required);
                    assertNotNull(closeIssue.get("assignee"));
                    assertEquals("user", closeIssue.get("assignee").schema.type);
                    assertFalse(closeIssue.get("assignee").required);
                    mask |= 1;
                    break;
                case 4 :
                    assertEquals("Start Progress", transition.name);
                    final Map<String, IssueTransitionsMeta.TransitionField> startProgress = transition.fields;
                    assertEquals(0, startProgress.size());
                    mask |= 2;
                    break;
                case 5 :
                    assertEquals("Resolve Issue", transition.name);
                    final Map<String, IssueTransitionsMeta.TransitionField> resolveIssue = transition.fields;
                    assertEquals(3, resolveIssue.size());
                    assertNotNull(resolveIssue.get("resolution"));
                    assertEquals("resolution", resolveIssue.get("resolution").schema.type);
                    assertTrue(resolveIssue.get("resolution").required);
                    assertNotNull(resolveIssue.get("fixVersions"));
                    assertEquals("array", resolveIssue.get("fixVersions").schema.type);
                    assertEquals("version", resolveIssue.get("fixVersions").schema.items);
                    assertFalse(resolveIssue.get("fixVersions").required);
                    assertNotNull(resolveIssue.get("assignee"));
                    assertEquals("user", resolveIssue.get("assignee").schema.type);
                    assertFalse(resolveIssue.get("assignee").required);
                    mask |= 4;
                    break;
            }
        }
        // Check all cases
        assertEquals(mask, 7);

    }

    public void testTransitionPUT_noComment() throws Exception
    {
        administration.restoreData("TestWorkflowActions.xml");
        IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        issueUpdateRequest.fields(new IssueFields());
        issueUpdateRequest.transition(ResourceRef.withId("2")); // id for "Close Issue"
        issueUpdateRequest.fields().resolution(new ResourceRef().name("Won't Fix"));
        issueUpdateRequest.fields().fixVersions(Lists.newArrayList(new ResourceRef().name("New Version 4"), new ResourceRef().name("New Version 5")));

        final Response response = transitionsClient.postResponse("HSP-1", issueUpdateRequest);

        assertEquals(204, response.statusCode);

        Issue issue = issueClient.get("HSP-1");
        assertEquals("Closed", issue.fields.status.name());
        assertEquals("Won't Fix", issue.fields.resolution.name);
        assertEquals("New Version 4", issue.fields.fixVersions.get(0).name);
        assertEquals("New Version 5", issue.fields.fixVersions.get(1).name);
    }


    public void testTransitionPOST_invalidRole() throws Exception
    {
        administration.restoreData("TestWorkflowActions.xml");

        final IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        issueUpdateRequest.fields(new IssueFields());
        issueUpdateRequest.transition(ResourceRef.withId("2")); // id for "Close Issue"
        issueUpdateRequest.fields().resolution(new ResourceRef().name("Won't Fix"));

        Map<String, List<FieldOperation>> operations = new HashMap<String, List<FieldOperation>>();
        Comment jsonComment = new Comment();
        jsonComment.visibility = new Visibility("ROLE", "NON-EXISTING-ROLE");
        jsonComment.body = "My comment";
        addCommentOperation("add", operations, jsonComment);
        issueUpdateRequest.update(operations);

        Response response = transitionsClient.postResponse("HSP-1", issueUpdateRequest);
        assertEquals(400, response.statusCode);
        String error = response.entity.errors.get("comment");
        assertTrue(error.startsWith("Can not construct instance of com.atlassian.jira.issue.fields.rest.json.beans.VisibilityJsonBean$VisibilityType from String value 'ROLE': value not one of declared Enum instance names"));
    }

    private void addCommentOperation(String operation, Map<String, List<FieldOperation>> operations, Comment comment)
    {
        List<FieldOperation> fieldOperations = new ArrayList<FieldOperation>();
        FieldOperation fieldOperation = new FieldOperation();
        fieldOperation.init(operation, comment);
        fieldOperations.add(fieldOperation);
        operations.put("comment", fieldOperations);
    }

    // time tracking doesn't correspond to anything in JiraDataTypes. make sure we don't throw an exception on fields like that.
    public void testBasicTimeTracking() throws Exception
    {
        administration.restoreData("TestRESTTransitionsSimple.xml");
        transitionsClient.get("MKY-1");
    }

    public void testTransitionWithMetadata() throws Exception
    {
        // having
        administration.restoreBlankInstance();
        backdoor.project().addProject("TRANSITION", "TRANSITION", ADMIN_USERNAME);
        final IssueCreateResponse issue = backdoor.issues().createIssue("TRANSITION", "summary");

        // when
        transitionsClient.postResponse(issue.key(), new IssueUpdateRequest()
                .transition(ResourceRef.withId("4")) // startProgress from empty xml
                .historyMetadata(
                        HistoryMetadata.builder("transitionMetadataTest").build()
                ));
        final JsonNode metadata = backdoor.issueNavControl().getHistoryMetadata(issue.key).get(0);

        // then
        assertThat(metadata.get("type").asText(), equalTo("transitionMetadataTest"));
    }

    @Override
    protected void setUpTest()
    {
        super.setUpTest();
        issueClient = new IssueClient(getEnvironmentData());
        transitionsClient = new TransitionsClient(getEnvironmentData());
    }
}
