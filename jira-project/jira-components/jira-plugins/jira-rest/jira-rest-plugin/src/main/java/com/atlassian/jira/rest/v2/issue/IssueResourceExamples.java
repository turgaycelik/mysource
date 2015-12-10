package com.atlassian.jira.rest.v2.issue;

import java.util.Arrays;
import java.util.Collections;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.fields.rest.StandardOperation;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinkJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinkTypeJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueRefJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.StatusJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.WorklogJsonBean;
import com.atlassian.jira.rest.api.issue.BulkOperationErrorResult;
import com.atlassian.jira.rest.api.issue.FieldOperation;
import com.atlassian.jira.rest.api.issue.IssueCreateResponse;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.rest.api.issue.IssuesCreateResponse;
import com.atlassian.jira.rest.api.issue.IssuesUpdateRequest;
import com.atlassian.jira.rest.api.issue.TimeTracking;
import com.atlassian.jira.rest.api.util.ErrorCollection;
import com.atlassian.jira.rest.v2.issue.project.ProjectBean;
import com.atlassian.jira.util.collect.CollectionBuilder;
import com.atlassian.jira.util.collect.MapBuilder;

import com.google.common.collect.ImmutableList;
import com.google.common.collect.Lists;

import static com.atlassian.jira.issue.IssueFieldConstants.DESCRIPTION;
import static com.atlassian.jira.issue.IssueFieldConstants.PROJECT;
import static com.atlassian.jira.issue.IssueFieldConstants.TIMETRACKING;
import static com.atlassian.jira.issue.IssueFieldConstants.UPDATED;
import static com.atlassian.jira.rest.api.issue.ResourceRef.withId;
import static com.atlassian.jira.rest.api.issue.ResourceRef.withName;

/**
 * Example JSON payloads for issue-related use cases.
 *
 * @since v5.0
 */
public class IssueResourceExamples
{
    public static final IssueUpdateRequest CREATE_REQUEST, CREATE_REQUEST_SECOND_ELEMENT;

    public static final IssuesUpdateRequest BULK_CREATE_REQUEST;

    public static final IssueCreateResponse CREATE_RESPONSE_201 = new IssueCreateResponse()
            .id("10000")
            .key("TST-24")
            .self(Examples.restURI("issue/10000").toString());

    private static final IssueCreateResponse CREATE_RESPONSE_201_SECOND_ELEMENT = new IssueCreateResponse()
            .id("10001")
            .key("TST-25")
            .self(Examples.restURI("issue/10001").toString());

    public static final ErrorCollection CREATE_RESPONSE_400 = ErrorCollection.of("Field 'priority' is required");

    public static final IssuesCreateResponse BULK_CREATE_RESPONSE_201 = new IssuesCreateResponse(ImmutableList
            .of(CREATE_RESPONSE_201, CREATE_RESPONSE_201_SECOND_ELEMENT), Collections.<BulkOperationErrorResult>emptyList());

    public static final BulkOperationErrorResult BULK_CREATE_RESPONSE_400 = new BulkOperationErrorResult(400, CREATE_RESPONSE_400 ,3 );


    /**
     * Short example for use in automatically generated documentation.
     */
    public static final IssueBean ISSUE_SHORT = new IssueBean(10001L, "HSP-1", Examples.restURI("issue/10001"));

    public static final IssueLinkTypeJsonBean SUBTASK_ISSUE_LINK_TYPE = new IssueLinkTypeJsonBean(10000L, "", "Parent", "Sub-task", null);
    public static final IssueLinkTypeJsonBean DEPENDENT_ISSUE_LINK_TYPE = new IssueLinkTypeJsonBean(10000L, "Dependent", "depends on", "is depended by", null);

    /**
     * Example IssueBean instance for use in automatically generated documentation.
     */
    public static final IssueBean GET_RESPONSE_200;

    static
    {
        StatusJsonBean status = new StatusJsonBean().name("Open").iconUrl(Examples.jiraURI("/images/icons/statuses/open.png").toString());

        IssueBean issue = new IssueBean(10002L, "EX-1", Examples.restURI("issue/10002"));
        issue.addRawField(UPDATED, UPDATED, null, new Date(1));
        issue.addRawField(DESCRIPTION, DESCRIPTION, null, "example bug report");
        issue.addRawField(PROJECT, PROJECT, null, ProjectBean.SHORT_DOC_EXAMPLE_1);
        issue.addRawField(TIMETRACKING, TIMETRACKING, null, new TimeTrackingBean(600L, 200L, 400L));
        issue.addRawField(IssueFieldConstants.ATTACHMENT, IssueFieldConstants.ATTACHMENT, null, CollectionBuilder.list(AttachmentBean.DOC_EXAMPLE));
        issue.addRawField(IssueFieldConstants.COMMENT, IssueFieldConstants.COMMENT, null, CollectionBuilder.list(CommentJsonBean.DOC_EXAMPLE));
        issue.addRawField(IssueFieldConstants.WORKLOG, IssueFieldConstants.WORKLOG, null, CollectionBuilder.list(WorklogJsonBean.DOC_EXAMPLE));
        issue.addRawField("sub-tasks", "sub-tasks", null,
                CollectionBuilder.list(new IssueLinkJsonBean().id("10000").outwardIssue(new IssueRefJsonBean("10003", "EX-2", Examples.restURI("issue/EX-2"), new IssueRefJsonBean.Fields().status(status))).type(SUBTASK_ISSUE_LINK_TYPE)));
        issue.addRawField(IssueFieldConstants.ISSUE_LINKS, IssueFieldConstants.ISSUE_LINKS, null,
                CollectionBuilder.list(
                        new IssueLinkJsonBean().id("10001").type(DEPENDENT_ISSUE_LINK_TYPE).outwardIssue(new IssueRefJsonBean("10004L", "PRJ-2", Examples.restURI("issue/PRJ-2"), new IssueRefJsonBean.Fields().status(status))),
                        new IssueLinkJsonBean().id("10002").type(DEPENDENT_ISSUE_LINK_TYPE).inwardIssue(new IssueRefJsonBean("10004", "PRJ-3", Examples.restURI("issue/PRJ-3"), new IssueRefJsonBean.Fields().status(status)))
                ));
        issue.addRawField(IssueFieldConstants.WATCHERS, IssueFieldConstants.WATCHERS, null, WatchersBean.DOC_EXAMPLE);

        GET_RESPONSE_200 = issue;

        CREATE_REQUEST = new com.atlassian.jira.rest.api.issue.IssueUpdateRequest().fields(new IssueFields()
                .project(withId("10000")) // TST
                .issueType(withId("10000"))  // Bug
                .priority(withId("20000"))   // Blocker
                .reporter(withName("smithers"))
                .assignee(withName("homer"))
                .summary("something's wrong")
                .labels(Arrays.asList("bugfix", "blitz_test"))
                .timeTracking(new TimeTracking("10", "5"))
                .securityLevel(withId("10000"))
                .versions(withId("10000"))
                .environment("environment")
                .description("description")
                .dueDate("2011-03-11")
                .fixVersions(withId("10001"))
                .components(withId("10000"))
//            .logWork(new LogWork().started("05/Jul/11 11:05 AM").timeSpent("1h"))
                .customField(10000L, "09/Jun/81")
                .customField(20000L, "06/Jul/11 3:25 PM")
                .customField(30000L, new String[] {"10000", "10002"})
                .customField(40000L, "this is a text field")
                .customField(50000L, "this is a text area. big text.")
                .customField(60000L, "jira-developers")
                .customField(70000L, new String[] {"jira-administrators", "jira-users"})
                .customField(80000L, Collections.singletonMap("value", "red"))
        );

        CREATE_REQUEST_SECOND_ELEMENT = new com.atlassian.jira.rest.api.issue.IssueUpdateRequest().fields(new IssueFields()
                .project(withId("1000")) // TST
                .issueType(withId("10000"))  // Bug
                .priority(withId("20000"))   // Blocker
                .reporter(withName("kosecki"))
                .assignee(withName("jerry"))
                .summary("something's very wrong")
                .labels(Arrays.asList("new_release"))
                .timeTracking(new TimeTracking("15", "5"))
                .securityLevel(withId("10000"))
                .versions(withId("10000"))
                .environment("environment")
                .description("description")
                .dueDate("2011-04-16")
                .fixVersions(withId("10001"))
                .components(withId("10000"))
//            .logWork(new LogWork().started("05/Jul/11 11:05 AM").timeSpent("1h"))
                .customField(10000L, "09/Jun/81")
                .customField(20000L, "06/Jul/11 3:25 PM")
                .customField(30000L, new String[] {"10000", "10002"})
                .customField(40000L, "this is a text field")
                .customField(50000L, "this is a text area. big text.")
                .customField(60000L, "jira-developers")
                .customField(70000L, new String[] {"jira-administrators", "jira-users"})
                .customField(80000L, Collections.singletonMap("value", "red"))
        );
        final Map<Object, Object> worklog = MapBuilder.newBuilder().add("started", "2011-07-05T11:05:00.000+0000").add("timeSpent", "60m").toMap();
        CREATE_REQUEST.update(MapBuilder.build("worklog", Arrays.asList(new FieldOperation().operation("add").value(worklog))));
        BULK_CREATE_REQUEST = new IssuesUpdateRequest(ImmutableList.of(CREATE_REQUEST,CREATE_REQUEST_SECOND_ELEMENT));
    }

    /**
     * Example CreateMetaBean instance for use in automatically generated documentation.
     */
    public static final CreateMetaBean GET_CREATEMETA_RESPONSE_200;
    static
    {
        Map<String, FieldMetaBean> fields = new HashMap<String, FieldMetaBean>();
        fields.put("issuetype", new FieldMetaBean(true, false, null, "Issue Type", null, Lists.newArrayList(StandardOperation.SET.getName()), null));

        CreateMetaIssueTypeBean issueType = new CreateMetaIssueTypeBean(
                Examples.restURI("issueType/1").toString(),
                "1",
                "Bug",
                "An error in the code",
                false,
                Examples.jiraURI("images/icons/issuetypes/bug.png").toString(),
                null);

        issueType.setFields(fields);

        Map<String, String> projectAvatarUrls = MapBuilder.<String, String>newBuilder()
                .add("16x16", Examples.jiraURI("secure/projectavatar?size=xsmall&pid=10000&avatarId=10011").toString())
                .add("24x24", Examples.jiraURI("secure/projectavatar?size=small&pid=10000&avatarId=10011").toString())
                .add("32x32", Examples.jiraURI("secure/projectavatar?size=medium&pid=10000&avatarId=10011").toString())
                .add("48x48", Examples.jiraURI("secure/projectavatar?pid=10000&avatarId=10011").toString())
                .toMap();

        CreateMetaProjectBean project = new CreateMetaProjectBean(
                Examples.restURI("project/EX").toString(),
                "10000",
                "EX",
                "Example Project",
                projectAvatarUrls,
                Arrays.asList(issueType));
                
        GET_CREATEMETA_RESPONSE_200 = new CreateMetaBean(Arrays.asList(project));
    }
}
