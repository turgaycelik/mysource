package com.atlassian.jira.rest.v2.issue;

import com.atlassian.jira.issue.fields.rest.json.beans.StatusCategoryJsonBean;

import java.util.ArrayList;
import java.util.List;

/**
 * @since v6.1
 */
public class StatusCategoryBeanExample
{
    /**
     * Example status category bean. JSON:
     * <p>
     * <pre>
     * {
     *     self: "http://localhost:8090/jira/rest/api/2.0/statuscategory/1",
     *     id: 1,
     *     key: "new",
     *     name: "New",
     *     colorName: "blue-gray"
     * }
     * </pre>
     * </p>
     */
    static final StatusCategoryJsonBean DOC_EXAMPLE;
    static final StatusCategoryJsonBean DOC_EXAMPLE_2;
    static final List<StatusCategoryJsonBean> STATUS_CATEGORIES_EXAMPLE;

    static
    {
        DOC_EXAMPLE = StatusCategoryJsonBean.bean(
                "http://localhost:8090/jira/rest/api/2.0/statuscategory/1",
                1L,
                "in-flight",
                "yellow",
                "In Progress"
        );

        DOC_EXAMPLE_2 = StatusCategoryJsonBean.bean(
                "http://localhost:8090/jira/rest/api/2.0/statuscategory/9",
                9L,
                "completed",
                "green"
        );

        STATUS_CATEGORIES_EXAMPLE = new ArrayList<StatusCategoryJsonBean>();
        STATUS_CATEGORIES_EXAMPLE.add(DOC_EXAMPLE);
        STATUS_CATEGORIES_EXAMPLE.add(DOC_EXAMPLE_2);
    }
}
