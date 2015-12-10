package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.rest.json.beans.StatusCategoryJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.StatusJsonBean;
import com.atlassian.jira.rest.api.issue.IssueTypeWithStatusJsonBean;
import com.google.common.collect.ImmutableList;

import java.util.List;

/**
 * @since v6.0
 */
public class IssueTypeWithStatusJsonBeanExample
{
    /**
     * Example representation of an issue type with statuses
     * <pre>
     * {
     * "self": "http://localhost:2990/jira/rest/api/latest/issuetype/1",
     * "id": "1",
     * "name": "Bug",
     * "subtask": false,
     * "statuses": [
     * {
     * "self": "http://localhost:2990/jira/rest/api/latest/status/1",
     * "description": "The issue is open and ready for the assignee to start work on it.",
     * "iconUrl": "http://localhost:2990/jira/images/icons/status_open.gif",
     * "name": "Open",
     * "id": "1"
     * },
     * {
     * "self": "http://localhost:2990/jira/rest/api/latest/status/3",
     * "description": "This issue is being actively worked on at the moment by the assignee.",
     * "iconUrl": "http://localhost:2990/jira/images/icons/status_inprogress.gif",
     * "name": "In Progress",
     * "id": "3"
     * }
     * ]
     * }
     * </pre>
     */
    public static final List<IssueTypeWithStatusJsonBean> DOC_EXAMPLE;
    public static final IssueTypeWithStatusJsonBean ISSUE_TYPE_EXAMPLE;

    static final StatusJsonBean STATUS_EXAMPLE;
    static final StatusJsonBean STATUS_EXAMPLE_2;
    static final List<StatusJsonBean> STATUSES_EXAMPLE;

    static
    {
        STATUS_EXAMPLE = StatusJsonBean.bean(
                "10000",
                "In Progress",
                "http://localhost:8090/jira/rest/api/2.0/status/10000",
                "http://localhost:8090/jira/images/icons/progress.gif",
                "The issue is currently being worked on."
        );
        STATUS_EXAMPLE_2 = StatusJsonBean.bean(
                "5",
                "Closed",
                "http://localhost:8090/jira/rest/api/2.0/status/5",
                "http://localhost:8090/jira/images/icons/closed.gif",
                "The issue is closed."
        );

        STATUSES_EXAMPLE = ImmutableList.of(STATUS_EXAMPLE, STATUS_EXAMPLE_2);

        ISSUE_TYPE_EXAMPLE = new IssueTypeWithStatusJsonBean(
                "http://localhost:8090/jira/rest/api/2.0/issueType/3",
                "3",
                "Task",
                false,
                STATUSES_EXAMPLE
        );

        DOC_EXAMPLE = ImmutableList.of(ISSUE_TYPE_EXAMPLE);
    }

}
