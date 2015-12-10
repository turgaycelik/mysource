package com.atlassian.jira.rest.v2.issue.project;

import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAttribute;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;

import com.atlassian.jira.issue.fields.rest.json.beans.IssueTypeJsonBean;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.rest.api.util.StringList;
import com.atlassian.jira.rest.v2.issue.Examples;
import com.atlassian.jira.rest.v2.issue.IssueTypeBeanExample;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.rest.v2.issue.component.ComponentBean;
import com.atlassian.jira.rest.v2.issue.version.VersionBean;
import com.atlassian.jira.util.collect.MapBuilder;

import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;

import static java.util.Collections.singletonList;

/**
 * @since 4.2
 */
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name="project")
public class ProjectBean
{
    /**
     * Project bean example used in auto-generated documentation.
     */
    public static final ProjectBean DOC_EXAMPLE;
    static
    {
        ProjectBean project = new ProjectBean();
        project.id = "10000";
        project.key = "EX";
        project.name = "Example";
        project.self = Examples.restURI("project/" + project.key);
        project.description = "This project was created as an example for REST.";
        project.lead = UserBean.SHORT_DOC_EXAMPLE;
        project.components = singletonList(ComponentBean.DOC_EXAMPLE);
        project.url = Examples.jiraURI("browse", project.key).toString();
        project.email = "from-jira@example.com";
        project.assigneeType = AssigneeType.PROJECT_LEAD;
        project.versions = Collections.emptyList();
        project.issueTypes = IssueTypeBeanExample.ISSUE_TYPES_EXAMPLE;
        project.roles = MapBuilder.<String, URI>newBuilder()
                .add("Developers", Examples.restURI("project", project.key, "role", "10000"))
                .toMap();
        project.avatarUrls = MapBuilder.<String, String>newBuilder()
                .add("16x16", Examples.jiraURI("secure/projectavatar?size=xsmall&pid=10000").toString())
                .add("24x24", Examples.jiraURI("secure/projectavatar?size=small&pid=10000").toString())
                .add("32x32", Examples.jiraURI("secure/projectavatar?size=medium&pid=10000").toString())
                .add("48x48", Examples.jiraURI("secure/projectavatar?size=large&pid=10000").toString())
                .toMap();
        project.projectCategory = ProjectCategoryBean.DOC_EXAMPLE1;

        DOC_EXAMPLE = project;
    }

    public static final ProjectBean SHORT_DOC_EXAMPLE_1;
    static
    {
        ProjectBean project = new ProjectBean();
        project.id = "10000";
        project.key = "EX";
        project.self = Examples.restURI("project/" + project.key);
        project.name = "Example";
        project.avatarUrls = MapBuilder.<String, String>newBuilder()
                .add("16x16", Examples.jiraURI("secure/projectavatar?size=xsmall&pid=10000").toString())
                .add("24x24", Examples.jiraURI("secure/projectavatar?size=small&pid=10000").toString())
                .add("32x32", Examples.jiraURI("secure/projectavatar?size=medium&pid=10000").toString())
                .add("48x48", Examples.jiraURI("secure/projectavatar?size=large&pid=10000").toString())
                .toMap();
        project.projectCategory = ProjectCategoryBean.DOC_EXAMPLE1;


        SHORT_DOC_EXAMPLE_1 = project;
    }

    public static final ProjectBean SHORT_DOC_EXAMPLE_2;
    static
    {
        ProjectBean project = new ProjectBean();
        project.id = "10001";
        project.key = "ABC";
        project.self = Examples.restURI("project/" + project.key);
        project.name = "Alphabetical";
        project.avatarUrls = MapBuilder.<String, String>newBuilder()
                .add("16x16", Examples.jiraURI("secure/projectavatar?size=xsmall&pid=10001").toString())
                .add("24x24", Examples.jiraURI("secure/projectavatar?size=small&pid=10001").toString())
                .add("32x32", Examples.jiraURI("secure/projectavatar?size=medium&pid=10001").toString())
                .add("48x48", Examples.jiraURI("secure/projectavatar?size=large&pid=10001").toString())
                .toMap();
        project.projectCategory = ProjectCategoryBean.DOC_EXAMPLE1;


        SHORT_DOC_EXAMPLE_2 = project;
    }

    public static final ProjectBean SHORT_DOC_EXAMPLE_3;
    static
    {
        ProjectBean project = new ProjectBean();
        project.id = "10002";
        project.key = "MKY";
        project.self = Examples.restURI("project/" + project.key);
        project.name = "Example";
        project.avatarUrls = MapBuilder.<String, String>newBuilder()
                .add("16x16", Examples.jiraURI("secure/projectavatar?size=xsmall&pid=10002").toString())
                .add("24x24", Examples.jiraURI("secure/projectavatar?size=small&pid=10002").toString())
                .add("32x32", Examples.jiraURI("secure/projectavatar?size=medium&pid=10002").toString())
                .add("48x48", Examples.jiraURI("secure/projectavatar?size=large&pid=10002").toString())
                .toMap();
        project.projectCategory = ProjectCategoryBean.DOC_EXAMPLE1;


        SHORT_DOC_EXAMPLE_3 = project;
    }

    public static final List<ProjectBean> PROJECTS_EXAMPLE;
    static
    {
        PROJECTS_EXAMPLE = new ArrayList<ProjectBean>();
        PROJECTS_EXAMPLE.add(SHORT_DOC_EXAMPLE_1);
        PROJECTS_EXAMPLE.add(SHORT_DOC_EXAMPLE_2);
    }

    @XmlAttribute
    private String expand;

    @XmlElement
    private URI self;

    @XmlElement
    private String id;

    @XmlElement
    private String key;

    @XmlElement
    private String description;

    @XmlElement
    private UserBean lead;

    @XmlElement
    private Collection<ComponentBean> components;

    @XmlElement
    private Collection<IssueTypeJsonBean> issueTypes;

    @XmlElement
    private String url;

    @XmlElement
    private String email;

    @XmlElement
    private AssigneeType assigneeType;

    @XmlElement
    private Collection<VersionBean> versions;

    @XmlElement
    private String name;

    @XmlElement
    private Map<String, URI> roles;

    @XmlElement
    private Map<String, String> avatarUrls;

    @XmlElement
    private Collection<String> projectKeys;

    @XmlElement
    private ProjectCategoryBean projectCategory;

    ProjectBean(String expand, URI self, String id, String key, String name, String description, UserBean lead, Long assigneeType,  String url,
            String email, Collection<ComponentBean> components,
            Collection<VersionBean> versions, Collection<IssueTypeJsonBean> issueTypes, final Map<String, URI> roles,
            final Map<String, String> avatarUrls, Collection<String> projectKeys, ProjectCategoryBean projectCategory)
    {
        this.expand = expand;
        this.self = self;
        this.id = id;
        this.key = key;
        this.description = description;
        this.lead = lead;
        this.components = components;
        this.url = url;
        this.email = email;
        this.projectKeys = projectKeys;
        this.assigneeType = AssigneeType.getAssigneeType(assigneeType);
        this.versions = versions;
        this.name = name;
        this.issueTypes = issueTypes;
        this.roles = roles;
        this.avatarUrls = avatarUrls;
        this.projectCategory = projectCategory;
    }

    public ProjectBean() {}

    public List<String> expand()
    {
        return StringList.fromQueryParam(expand).asList();
    }

    public URI getSelf()
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

    @Override
    public String toString()
    {
        return ToStringBuilder.reflectionToString(this, ToStringStyle.SHORT_PREFIX_STYLE);
    }

    public enum AssigneeType
    {
        PROJECT_LEAD (AssigneeTypes.PROJECT_LEAD),
        UNASSIGNED (AssigneeTypes.UNASSIGNED);

        private final long id;

        AssigneeType(long id)
        {
            this.id = id;
        }

        public long getId()
        {
            return id;
        }

        static AssigneeType getAssigneeType(Long assigneeType)
        {
            if (assigneeType != null)
            {
                switch ((short) assigneeType.longValue())
                {
                    case (short) AssigneeTypes.PROJECT_LEAD:
                        return PROJECT_LEAD;
                    case (short) AssigneeTypes.UNASSIGNED:
                        return UNASSIGNED;
                }
            }
            return PROJECT_LEAD;
        }
    }
}


