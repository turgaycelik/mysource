package com.atlassian.jira.issue.fields.rest.json.beans;

import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.project.Project;
import com.google.common.collect.Lists;
import org.codehaus.jackson.annotate.JsonProperty;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

/**
 * @since 5.0
 */
public class ProjectJsonBean
{
    @JsonProperty
    private String self;

    @JsonProperty
    private String id;

    @JsonProperty
    private String key;

    @JsonProperty
    private String name;

    @JsonProperty
    private Map<String, String> avatarUrls;

    @JsonProperty
    private ProjectCategoryJsonBean projectCategory;

    public String getSelf()
    {
        return self;
    }

    public String getId()
    {
        return id;
    }

    public String getKey()
    {
        return key;
    }

    public String getName()
    {
        return name;
    }

    public Map<String, String> getAvatarUrls()
    {
        return avatarUrls;
    }

    public ProjectCategoryJsonBean getProjectCategory()
    {
        return projectCategory;
    }

    public static ProjectJsonBean shortBean(Project project, final JiraBaseUrls urls)
    {
        if (project == null)
        {
            return null;
        }

        final ProjectJsonBean bean = new ProjectJsonBean();
        bean.self = urls.restApi2BaseUrl() + "project/" + project.getId().toString();
        bean.id = project.getId().toString();
        bean.key = project.getKey();
        bean.name = project.getName();
        bean.avatarUrls = getAvatarUrls(project);
        bean.projectCategory = ProjectCategoryJsonBean.bean(project.getProjectCategoryObject(), urls);

        return bean;
    }

    public static Map<String, String> getAvatarUrls(final Project project)
    {
        AvatarService avatarService = ComponentAccessor.getAvatarService();
        final Avatar avatar = project.getAvatar();
        if (avatar == null) return null;

        final Map<String, String> avatarUrls = new HashMap<String, String>();
        for (Avatar.Size size : Avatar.Size.values())
        {
            final int px = size.getPixels();
            final String sizeName = String.format("%dx%d",px,px);
            if (px > 48) continue; // TODO JRADEV-20790 - Don't output higher res URLs in our REST endpoints until we start using them ourselves.
            avatarUrls.put(sizeName, avatarService.getProjectAvatarAbsoluteURL(project, size).toString());
        }
        return avatarUrls;
    }

    public static Collection<ProjectJsonBean> shortBeans(final Collection<Project> allowedValues, final JiraBaseUrls baseUrls)
    {
        Collection<ProjectJsonBean> result = Lists.newArrayListWithCapacity(allowedValues.size());
        for (Project from : allowedValues)
        {
            result.add(ProjectJsonBean.shortBean(from, baseUrls));
        }

        return result;

    }
}


