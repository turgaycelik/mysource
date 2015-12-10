package com.atlassian.jira.rest.v2.search;

import com.atlassian.jira.issue.fields.rest.json.beans.GroupJsonBean;
import com.atlassian.jira.rest.v2.issue.project.ProjectBean;
import com.atlassian.jira.rest.v2.issue.project.ProjectRoleBean;
import com.atlassian.jira.sharing.type.ShareType;
import com.google.common.collect.Lists;

import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.util.List;

/**
 * Represents a saved filter.
 *
 * @since v5.2
 */
@XmlRootElement (name = "filter")
public class FilterPermissionBean
{
    public static final FilterPermissionBean DOC_EXAMPLE_1 = new FilterPermissionBean(
            10000L,
            ShareType.Name.GLOBAL.get(),
            null, null, null);

     public static final FilterPermissionBean DOC_EXAMPLE_2 = new FilterPermissionBean(
            10010L,
            ShareType.Name.PROJECT.get(),
            ProjectBean.SHORT_DOC_EXAMPLE_1,
            null,
            null);

    public static final FilterPermissionBean DOC_EXAMPLE_3 = new FilterPermissionBean(
            10010L,
            ShareType.Name.PROJECT.get(),
            ProjectBean.SHORT_DOC_EXAMPLE_3,
            ProjectRoleBean.DOC_EXAMPLE,
            null);

    public static final FilterPermissionBean DOC_EXAMPLE_4 = new FilterPermissionBean(
            10010L,
            ShareType.Name.GROUP.get(),
            null,
            null,
            GroupJsonBean.DOC_EXAMPLE);

    public static final List<FilterPermissionBean> DOC_FILTER_LIST_EXAMPLE = Lists.newArrayList(DOC_EXAMPLE_1, DOC_EXAMPLE_2, DOC_EXAMPLE_3, DOC_EXAMPLE_4);

    @XmlElement
    private Long id;

    @XmlElement
    private String type;

    @XmlElement
    private ProjectBean project;

    @XmlElement
    private ProjectRoleBean role;

    @XmlElement
    private GroupJsonBean group;

    public FilterPermissionBean() { }

    public FilterPermissionBean(Long id, String type, ProjectBean project, ProjectRoleBean role, GroupJsonBean group)
    {
        this.id = id;
        this.type = type;
        this.project = project;
        this.role = role;
        this.group = group;
    }

    public Long getId()
    {
        return id;
    }

    public String getType()
    {
        return type;
    }

    public ProjectBean getProject()
    {
        return project;
    }

    public ProjectRoleBean getRole()
    {
        return role;
    }

    public GroupJsonBean getGroup()
    {
        return group;
    }
}
