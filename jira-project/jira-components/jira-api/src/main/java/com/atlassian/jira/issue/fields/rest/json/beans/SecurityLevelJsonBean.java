package com.atlassian.jira.issue.fields.rest.json.beans;


import com.atlassian.jira.issue.security.IssueSecurityLevel;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;

/**
* @since v5.0
*/
public class SecurityLevelJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private String description;

    @JsonProperty
    private String name;

    public String getSelf()
    {
        return self;
    }

    public void setSelf(String self)
    {
        this.self = self;
    }

    public String getId()
    {
        return id;
    }

    public void setId(String id)
    {
        this.id = id;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public static Collection<SecurityLevelJsonBean> shortBeans(final Collection<IssueSecurityLevel> securitylevels, final JiraBaseUrls urls)
    {
        Collection<SecurityLevelJsonBean> result = Lists.newArrayListWithCapacity(securitylevels.size());
        for (IssueSecurityLevel from : securitylevels)
        {
            result.add(shortBean(from, urls));
        }

        return result;
    }

    /**
     *
     * @return null if the input is null
     */
    public static SecurityLevelJsonBean shortBean(final IssueSecurityLevel securityLevel, final JiraBaseUrls urls)
    {
        if (securityLevel == null)
        {
            return null;
        }
        final SecurityLevelJsonBean bean = new SecurityLevelJsonBean();
        bean.self = urls.restApi2BaseUrl() + "securitylevel/" + JiraUrlCodec.encode(securityLevel.getId().toString());
        bean.id = securityLevel.getId().toString();
        bean.description = securityLevel.getDescription();
        bean.name = securityLevel.getName();

        return bean;
    }

}
