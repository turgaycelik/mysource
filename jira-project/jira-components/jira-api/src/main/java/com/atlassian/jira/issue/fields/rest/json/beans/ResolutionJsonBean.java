package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;

/**
* @since v5.0
*/
public class ResolutionJsonBean
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

    public static Collection<ResolutionJsonBean> shortBeans(final Collection<Resolution> resolutions, final JiraBaseUrls urls) {
        Collection<ResolutionJsonBean> result = Lists.newArrayListWithCapacity(resolutions.size());
        for (Resolution from : resolutions)
        {
            result.add(shortBean(from, urls));
        }

        return result;
    }

    /**
     *
     * @return null if the input is null
     */
    public static ResolutionJsonBean shortBean(final Resolution resolution, final JiraBaseUrls urls)
    {
        if (resolution == null)
        {
            return null;
        }
        final ResolutionJsonBean bean = new ResolutionJsonBean();
        bean.self = urls.restApi2BaseUrl() + "resolution/" + JiraUrlCodec.encode(resolution.getId().toString());
        bean.id = resolution.getId();
        bean.name = resolution.getNameTranslation();
        bean.description = resolution.getDescTranslation();
        // Icon URL is not currently used for Resolutions
        // bean.iconUrl = resolution.getIconUrl();
        return bean;
    }
}
