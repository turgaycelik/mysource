package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.rest.json.beans.StatusJsonBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v4.2
 */
class StatusBeanExample
{
    /**
     * Example status bean. JSON:
     * <p>
     * <pre>
     * {
     *   self: "http://localhost:8090/jira/rest/api/2.0/issueType/2",
     *   description: "A new feature of the product, which has yet to be developed.",
     *   iconUrl: "http://localhost:8090/jira/images/icons/issuetypes/newfeature.png",
     *   name: "New Feature",
     *   subtask: false
     * }
     * <pre>
     */
    static final StatusJsonBean DOC_EXAMPLE;
    static final StatusJsonBean DOC_EXAMPLE_2;
    static final List<StatusJsonBean> STATUSES_EXAMPLE;


    static
    {
        DOC_EXAMPLE = StatusJsonBean.bean(
                "10000",
                "In Progress",
                "http://localhost:8090/jira/rest/api/2.0/status/10000",
                "http://localhost:8090/jira/images/icons/progress.gif",
                "The issue is currently being worked on.",
                StatusCategoryBeanExample.DOC_EXAMPLE
        );
        DOC_EXAMPLE_2 = StatusJsonBean.bean(
                "5",
                "Closed",
                "http://localhost:8090/jira/rest/api/2.0/status/5",
                "http://localhost:8090/jira/images/icons/closed.gif",
                "The issue is closed.",
                StatusCategoryBeanExample.DOC_EXAMPLE_2
        );

        STATUSES_EXAMPLE = new ArrayList<StatusJsonBean>();
        STATUSES_EXAMPLE.add(DOC_EXAMPLE);
        STATUSES_EXAMPLE.add(DOC_EXAMPLE_2);
    }
}
