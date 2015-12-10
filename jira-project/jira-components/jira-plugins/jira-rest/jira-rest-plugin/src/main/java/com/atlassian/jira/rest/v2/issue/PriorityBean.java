package com.atlassian.jira.rest.v2.issue;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.jira.issue.fields.rest.json.beans.PriorityJsonBean;

import java.util.Collection;

/**
 * @since v4.2
 */
public class PriorityBean
{
    static final PriorityJsonBean DOC_EXAMPLE;
    static final PriorityJsonBean DOC_EXAMPLE_2;
    static final Collection<PriorityJsonBean> DOC_EXAMPLE_LIST;
    static
    {
        PriorityJsonBean priority = new PriorityJsonBean();
        priority.setSelf(Examples.restURI("priority/3").toString());
        priority.setName("Major");
        priority.setStatusColor("#009900");
        priority.setDescription("Major loss of function.");
        priority.setIconUrl(Examples.jiraURI("images/icons/priorities/major.png").toString());
        DOC_EXAMPLE = priority;

        priority = new PriorityJsonBean();
        priority.setSelf(Examples.restURI("priority/5").toString());
        priority.setName("Trivial");
        priority.setStatusColor("#cfcfcf");
        priority.setDescription("Very little impact.");
        priority.setIconUrl(Examples.jiraURI("images/icons/priorities/trivial.png").toString());
        DOC_EXAMPLE_2 = priority;

        DOC_EXAMPLE_LIST = EasyList.build(DOC_EXAMPLE, DOC_EXAMPLE_2);
    }

}
