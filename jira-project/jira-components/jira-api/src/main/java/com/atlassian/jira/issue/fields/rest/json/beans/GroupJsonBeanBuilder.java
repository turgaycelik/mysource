package com.atlassian.jira.issue.fields.rest.json.beans;

import java.net.URI;
import java.util.Collection;

import javax.annotation.Nullable;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.util.JiraUrlCodec;

import com.google.common.collect.Lists;

/**
 * Builder class for GroupJsonBean.
 *
 * @since v6.0
 */
public class GroupJsonBeanBuilder
{
    private final JiraBaseUrls jiraBaseUrls;
    private String name;

    public GroupJsonBeanBuilder(JiraBaseUrls jiraBaseUrls)
    {
        this.jiraBaseUrls = jiraBaseUrls;
    }

    /**
     * Sets group details on base of provided group object. If provided object is null, then no data will be copied.
     * @param group Grop to copy data from.
     * @return this
     */
    public GroupJsonBeanBuilder group(@Nullable final Group group) {
        if (group != null) {
            this.name = group.getName();
        }
        return this;
    }

    public GroupJsonBeanBuilder name(final String name) {
        this.name = name;
        return this;
    }

    /**
     * Creates new GroupJsonBean.
     * @return an instance of created GroupJsonBean or null if no group data was set.
     */
    @Nullable
    public GroupJsonBean build() {
        if (name == null) {
            return null;
        }
        else {
            return new GroupJsonBean(name, makeSelfUri(name, jiraBaseUrls));
        }
    }

    public static URI makeSelfUri(String groupName, JiraBaseUrls urls)
    {
        return URI.create(urls.restApi2BaseUrl() + "group?groupname=" + JiraUrlCodec.encode(groupName));
    }

    public static Collection<GroupJsonBean> buildBeans(final Collection<Group> Groups, final JiraBaseUrls jiraBaseUrls)
    {
        Collection<GroupJsonBean> result = Lists.newArrayListWithCapacity(Groups.size());
        for (Group from : Groups)
        {
            result.add(new GroupJsonBeanBuilder(jiraBaseUrls).group(from).build());
        }

        return result;
    }
}
