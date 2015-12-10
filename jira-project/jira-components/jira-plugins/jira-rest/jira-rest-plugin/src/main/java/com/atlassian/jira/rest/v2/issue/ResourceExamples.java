package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadata;
import com.atlassian.jira.issue.changehistory.metadata.HistoryMetadataParticipant;
import com.atlassian.jira.issue.fields.rest.json.beans.CommentJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinkJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueLinkTypeJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.IssueRefJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.LinkIssueRequestJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.StatusJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.VisibilityJsonBean;
import com.atlassian.jira.rest.api.issue.FieldOperation;
import com.atlassian.jira.rest.api.issue.IssueFields;
import com.atlassian.jira.rest.api.issue.IssueUpdateRequest;
import com.atlassian.jira.rest.api.issue.ResourceRef;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v5.0
 */
public class ResourceExamples
{
        static final CommentJsonBean DOC_COMMENT_LINK_ISSUE_EXAMPLE = new CommentJsonBean();
        static {
            DOC_COMMENT_LINK_ISSUE_EXAMPLE.setBody("Linked related issue!");
            DOC_COMMENT_LINK_ISSUE_EXAMPLE.setVisibility(new VisibilityJsonBean(VisibilityJsonBean.VisibilityType.group, "jira-users"));
        }

    /**
     * Example representation for use in auto-generated docs.
     */
    public static final LinkIssueRequestJsonBean LINK_ISSUE_REQUEST_EXAMPLE = new LinkIssueRequestJsonBean(new IssueRefJsonBean().key("HSP-1"), new IssueRefJsonBean().key("MKY-1"), new IssueLinkTypeJsonBean().name("Duplicate"), ResourceExamples.DOC_COMMENT_LINK_ISSUE_EXAMPLE);

    static final IssueLinkTypeJsonBean ISSUE_LINK_TYPE_EXAMPLE;
    static final IssueLinkTypeJsonBean ISSUE_LINK_TYPE_EXAMPLE_2;
    static final IssueLinkTypeJsonBean ISSUE_LINK_TYPE_EXAMPLE_CREATE;
    static
    {
        ISSUE_LINK_TYPE_EXAMPLE = new IssueLinkTypeJsonBean(1000l, "Duplicate", "Duplicated by", "Duplicates", Examples.restURI("/issueLinkType/1000"));
        ISSUE_LINK_TYPE_EXAMPLE_2 = new IssueLinkTypeJsonBean(1010l, "Blocks", "Blocked by", "Blocks", Examples.restURI("/issueLinkType/1010"));
        ISSUE_LINK_TYPE_EXAMPLE_CREATE = new IssueLinkTypeJsonBean((String)null, "Duplicate", "Duplicated by", "Duplicates", null);
    }

    static final HistoryMetadata HISTORY_METADATA_EXAMPLE = HistoryMetadata.builder("myplugin:type")
            .description("text description")
            .descriptionKey("plugin.changereason.i18.key")
            .activityDescription("text description")
            .activityDescriptionKey("plugin.activity.i18.key")
            .actor(HistoryMetadataParticipant
                            .builder("tony", "mysystem-user")
                            .url("http://mysystem/users/tony")
                            .displayName("Tony")
                            .avatarUrl("http://mysystem/avatar/tony.jpg")
                            .build()
            )
            .cause(HistoryMetadataParticipant
                            .builder("myevent", "mysystem-event").build()
            )
            .generator(HistoryMetadataParticipant.builder("mysystem-1", "mysystem-application").build())
            .extraData("keyvalue", "extra data")
            .extraData("goes", "here")
            .build();

    static final IssueUpdateRequest UPDATE_DOC_EXAMPLE;
    static
    {
        final IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        final List<FieldOperation> summaryOperations = new ArrayList<FieldOperation>();
        final FieldOperation summaryOperation = new FieldOperation();
        summaryOperation.init("set", "Bug in business logic");
        summaryOperations.add(summaryOperation);
        issueUpdateRequest.update().put("summary", summaryOperations);

        final List<FieldOperation> componentsOperations = new ArrayList<FieldOperation>();
        final FieldOperation componentsOperation = new FieldOperation();
        componentsOperation.init("set", "");
        componentsOperations.add(componentsOperation);
        issueUpdateRequest.update().put("components", componentsOperations);

        final List<FieldOperation> timetrackingOperations = new ArrayList<FieldOperation>();
        final FieldOperation timetrackingOperation = new FieldOperation();
        final TimeTrackingBean timeTrackingBean = new TimeTrackingBean(null, null, null);
        timeTrackingBean.setOriginalEstimate("1w 1d");
        timeTrackingBean.setRemainingEstimate("4d");
        timetrackingOperation.init("edit", timeTrackingBean);
        timetrackingOperations.add(timetrackingOperation);
        issueUpdateRequest.update().put("timetracking", timetrackingOperations);

        final List<FieldOperation> labelOperations = new ArrayList<FieldOperation>();
        final FieldOperation labelOperation1 = new FieldOperation();
        labelOperation1.init("add", "triaged");
        labelOperations.add(labelOperation1);

        final FieldOperation labelOperation2 = new FieldOperation();
        labelOperation2.init("remove", "blocker");
        labelOperations.add(labelOperation2);

        issueUpdateRequest.update().put("labels", labelOperations);

        issueUpdateRequest.fields(new IssueFields()).fields()
                .summary("This is a shorthand for a set operation on the summary field")
                .customField(10010L, 1)
                .customField(10000L, "This is a shorthand for a set operation on a text custom field");
        issueUpdateRequest.historyMetadata(HISTORY_METADATA_EXAMPLE);

        UPDATE_DOC_EXAMPLE = issueUpdateRequest;
    }


    static final IssueUpdateRequest TRANSITION_DOC_EXAMPLE;

    static
    {
        final IssueUpdateRequest issueUpdateRequest = new IssueUpdateRequest();
        issueUpdateRequest.transition(ResourceRef.withId("5"));
        final IssueFields issueFields = new IssueFields();
        issueFields.resolution(ResourceRef.withName("Fixed"));
        issueFields.assignee(ResourceRef.withName("bob"));
        issueUpdateRequest.fields(issueFields);

        final List<FieldOperation> commentOperations = new ArrayList<FieldOperation>();
        final FieldOperation commentOperation = new FieldOperation();
        final CommentJsonBean commentJsonBean = new CommentJsonBean();
        commentJsonBean.setBody("Bug has been fixed.");
        commentOperation.init("add", commentJsonBean);
        commentOperations.add(commentOperation);
        issueUpdateRequest.update().put("comment", commentOperations);
        issueUpdateRequest.historyMetadata(HISTORY_METADATA_EXAMPLE);

        TRANSITION_DOC_EXAMPLE = issueUpdateRequest;
    }

    static final IssueLinkJsonBean ISSUE_LINK_EXAMPLE;
    static
    {
        final StatusJsonBean status = new StatusJsonBean().name("Open").iconUrl(Examples.jiraURI("/images/icons/statuses/open.png").toString());
        final IssueLinkJsonBean issueLinkJsonBean = new IssueLinkJsonBean().id("10001").type(ISSUE_LINK_TYPE_EXAMPLE);
        issueLinkJsonBean.outwardIssue(new IssueRefJsonBean("10004L", "PRJ-2", Examples.restURI("issue/PRJ-2"), new IssueRefJsonBean.Fields().status(status)));
        issueLinkJsonBean.inwardIssue(new IssueRefJsonBean("10004", "PRJ-3", Examples.restURI("issue/PRJ-3"), new IssueRefJsonBean.Fields().status(status)));
        ISSUE_LINK_EXAMPLE = issueLinkJsonBean;
    }

}
