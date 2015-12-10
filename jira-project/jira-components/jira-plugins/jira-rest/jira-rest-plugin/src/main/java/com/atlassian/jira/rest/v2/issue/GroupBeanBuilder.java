package com.atlassian.jira.rest.v2.issue;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBeanBuilder;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.issue.fields.rest.json.beans.UserJsonBean;
import com.atlassian.jira.rest.api.expand.PagedListWrapper;

/**
 * Builder class for GroupBean.
 *
 * @since 6.0
 */
public class GroupBeanBuilder
{
    private final JiraBaseUrls jiraBaseUrls;
    private String name;
    private PagedListWrapper<UserJsonBean, User> users = null;

    public GroupBeanBuilder(final JiraBaseUrls jiraBaseUrls, final String name)
    {
        this.name = name;
        this.jiraBaseUrls = jiraBaseUrls;
    }

    public GroupBeanBuilder users(final PagedListWrapper<UserJsonBean, User> users)
    {
        this.users = users;
        return this;
    }

    public GroupBean build()
    {
        return new GroupBean(name, GroupJsonBeanBuilder.makeSelfUri(name, jiraBaseUrls), users);
    }
}