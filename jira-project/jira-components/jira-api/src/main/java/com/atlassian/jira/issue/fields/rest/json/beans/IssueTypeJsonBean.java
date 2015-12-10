package com.atlassian.jira.issue.fields.rest.json.beans;


import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.Collection;

/**
* @since v5.0
*/
public class IssueTypeJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private String description;

    @JsonProperty
    private String iconUrl;

    @JsonProperty
    private String name;

    @JsonProperty
    private boolean subtask;

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

    public String getIconUrl()
    {
        return iconUrl;
    }

    public void setIconUrl(String iconUrl)
    {
        this.iconUrl = iconUrl;
    }

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public boolean isSubtask()
    {
        return subtask;
    }

    public void setSubtask(boolean subtask)
    {
        this.subtask = subtask;
    }
    
    public static Collection<IssueTypeJsonBean> shortBeans(final Collection<IssueType> issuetypes, final JiraBaseUrls urls) {
        Collection<IssueTypeJsonBean> result = Lists.newArrayListWithCapacity(issuetypes.size());
        for (IssueType from : issuetypes)
        {
            result.add(shortBean(from, urls));
        }

        return result;
    }

    /**
     *
     * @return null if the input is null
     */
    public static IssueTypeJsonBean shortBean(final IssueType issuetype, final JiraBaseUrls urls)
    {
        if (issuetype == null)
        {
            return null;
        }

        String iconAbsoluteUrl;
        try
        {
            iconAbsoluteUrl = new URL(issuetype.getIconUrl()).toString();
        }
        catch (MalformedURLException e)
        {
            iconAbsoluteUrl = urls.baseUrl() + issuetype.getIconUrl();
        }

        final IssueTypeJsonBean bean = new IssueTypeJsonBean();
        bean.self = urls.restApi2BaseUrl() + "issuetype/" + JiraUrlCodec.encode(issuetype.getId().toString());
        bean.id = issuetype.getId().toString();
        bean.description = issuetype.getDescTranslation();
        bean.name = issuetype.getNameTranslation();
        bean.subtask = issuetype.isSubTask();
        bean.iconUrl = iconAbsoluteUrl;

        return bean;
    }

    public static IssueTypeJsonBean shortBean(String self, String id, String name, String description, boolean subtask, String iconUrl)
    {
        final IssueTypeJsonBean bean = new IssueTypeJsonBean();
        bean.self = self;
        bean.id = id;
        bean.name = name;
        bean.description = description;
        bean.subtask = subtask;
        bean.iconUrl = iconUrl;
        return bean;
    }
}
