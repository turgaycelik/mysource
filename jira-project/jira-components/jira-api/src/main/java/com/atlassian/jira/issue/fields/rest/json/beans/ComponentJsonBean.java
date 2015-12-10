package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.util.JiraUrlCodec;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonIgnoreProperties;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;


/**
 * A JSON-convertable representation of a ProjectComponent
 *
 * @since v5.0
 */
@JsonIgnoreProperties (ignoreUnknown = true)
public class ComponentJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private String name;

    @JsonProperty
    private String description;

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

    public String getName()
    {
        return name;
    }

    public void setName(String name)
    {
        this.name = name;
    }

    public String getDescription()
    {
        return description;
    }

    public void setDescription(String description)
    {
        this.description = description;
    }

    public static Collection<ComponentJsonBean> shortBeans(final Collection<ProjectComponent> components, final JiraBaseUrls urls) {
        Collection<ComponentJsonBean> result = Lists.newArrayListWithCapacity(components.size());
        for (ProjectComponent from : components)
        {
            result.add(shortBean(from, urls));
        }

        return result;
    }

    /**
     *
     * @return null if the input is null
     */
    public static ComponentJsonBean shortBean(final ProjectComponent component, final JiraBaseUrls urls)
    {
        if (component == null)
        {
            return null;
        }
        final ComponentJsonBean bean = new ComponentJsonBean();
        bean.self = urls.restApi2BaseUrl() + "component/" + JiraUrlCodec.encode(component.getId().toString());
        bean.id = component.getId().toString();
        bean.description = component.getDescription();
        bean.name = component.getName();
        return bean;
    }
}
