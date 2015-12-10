package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.rest.json.beans.IssueTypeJsonBean;
import com.atlassian.jira.issue.fields.rest.json.beans.StatusJsonBean;

import java.util.ArrayList;
import java.util.List;

/**
* @since v4.2
*/
public class IssueTypeBeanExample
{
    /**
     * Example representation of an issue type.
     * <pre>
     * {
     *   self: "http://localhost:8090/jira/rest/api/2.0/issueType/3",
     *   description: "A task that needs to be done.",
     *   iconUrl: "http://localhost:8090/jira/images/icons/issuetypes/task.png",
     *   name: "Task",
     *   subtask: false
     * }
     * </pre>
     */
    public static final IssueTypeJsonBean DOC_EXAMPLE;
    public static final IssueTypeJsonBean DOC_EXAMPLE_2;
    public static final List<IssueTypeJsonBean> ISSUE_TYPES_EXAMPLE;
    static
    {
        DOC_EXAMPLE = IssueTypeJsonBean.shortBean(
                "http://localhost:8090/jira/rest/api/2.0/issueType/3",
                "3",
                "Task",
                "A task that needs to be done.",
                false,
                "http://localhost:8090/jira/images/icons/issuetypes/task.png"
        );
        DOC_EXAMPLE_2 = IssueTypeJsonBean.shortBean(
                "http://localhost:8090/jira/rest/api/2.0/issueType/1",
                "1",
                "Bug",
                "A problem with the software.",
                false,
                "http://localhost:8090/jira/images/icons/issuetypes/bug.png"
        );
        ISSUE_TYPES_EXAMPLE = new ArrayList<IssueTypeJsonBean>();
        ISSUE_TYPES_EXAMPLE.add(DOC_EXAMPLE);
        ISSUE_TYPES_EXAMPLE.add(DOC_EXAMPLE_2);
  }

}
