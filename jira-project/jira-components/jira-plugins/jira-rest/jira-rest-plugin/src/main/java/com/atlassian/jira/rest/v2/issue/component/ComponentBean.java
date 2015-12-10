package com.atlassian.jira.rest.v2.issue.component;

import com.atlassian.jira.action.component.ComponentUtils;
import com.atlassian.jira.avatar.AvatarService;
import com.atlassian.jira.bc.project.component.ProjectComponent;
import com.atlassian.jira.issue.fields.rest.json.beans.JiraBaseUrls;
import com.atlassian.jira.project.AssigneeTypes;
import com.atlassian.jira.project.ComponentAssigneeTypes;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.rest.v2.issue.ComponentResource;
import com.atlassian.jira.rest.v2.issue.Examples;
import com.atlassian.jira.rest.v2.issue.UserBean;
import com.atlassian.jira.rest.v2.issue.UserBeanBuilder;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;

import javax.ws.rs.core.UriBuilder;
import javax.ws.rs.core.UriInfo;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import java.net.URI;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

/**
 * @since 4.2
 */
@SuppressWarnings ( { "UnusedDeclaration" })
@XmlRootElement (name = "component")
public class ComponentBean
{
    /**
     * Example ComponentBean instance used for auto-generated REST documentation.
     */
    public static final ComponentBean DOC_EXAMPLE;
    public static final ComponentBean DOC_EXAMPLE_2;
    public static final ComponentBean DOC_CREATE_EXAMPLE;
    public static final ComponentBean DOC_EDIT_EXAMPLE;
    public static final List<ComponentBean> DOC_EXAMPLE_LIST = new ArrayList<ComponentBean>();

    static
    {
        ComponentBean component = new ComponentBean();
        component.self = Examples.restURI("component", "10000");
        component.id = "10000";
        component.name = "Component 1";
        component.description = "This is a JIRA component";
        component.lead = UserBean.SHORT_DOC_EXAMPLE;
        component.assigneeType = AssigneeType.PROJECT_LEAD;
        component.assignee = UserBean.SHORT_DOC_EXAMPLE;
        component.realAssigneeType = AssigneeType.PROJECT_LEAD;
        component.realAssignee = UserBean.SHORT_DOC_EXAMPLE;

        DOC_EXAMPLE = component;

        component = new ComponentBean();
        component.self = Examples.restURI("component", "10000");
        component.id = "10050";
        component.name = "PXA";
        component.description = "This is a another JIRA component";
        component.lead = UserBean.SHORT_DOC_EXAMPLE;
        component.assigneeType = AssigneeType.PROJECT_LEAD;
        component.assignee = UserBean.SHORT_DOC_EXAMPLE;
        component.realAssigneeType = AssigneeType.PROJECT_LEAD;
        component.realAssignee = UserBean.SHORT_DOC_EXAMPLE;

        DOC_EXAMPLE_2 = component;

        DOC_EXAMPLE_LIST.add(DOC_EXAMPLE);
        DOC_EXAMPLE_LIST.add(DOC_EXAMPLE_2);

        component = new ComponentBean();
        component.project = "PXA";
        component.name = "Component 1";
        component.description = "This is a JIRA component";
        component.leadUserName = "fred";
        component.assigneeType = AssigneeType.PROJECT_LEAD;

        DOC_CREATE_EXAMPLE = component;

        component = new ComponentBean();
        component.name = "Component 1";
        component.description = "This is a JIRA component";
        component.leadUserName = "fred";
        component.assigneeType = AssigneeType.PROJECT_LEAD;

        DOC_EDIT_EXAMPLE = component;
    }

    public static Collection<ComponentBean> asBeans(final Collection<? extends ProjectComponent> components, final JiraBaseUrls jiraBaseUrls)
    {
        final ArrayList<ComponentBean> list = new ArrayList<ComponentBean>();
        for (ProjectComponent component : components)
        {
            list.add(ComponentBean.shortComponent(component, jiraBaseUrls));
        }
        return list;
    }

    public static Collection<ComponentBean> asFullBeans(final Collection<? extends ProjectComponent> components, final JiraBaseUrls jiraBaseUrls, String projectLeadUserName, long projectAssigneeType, final UserManager userManager, final AvatarService avatarService, final PermissionManager permissionManager, final ProjectManager projectManager)
    {
        final ArrayList<ComponentBean> list = new ArrayList<ComponentBean>();
        for (ProjectComponent component : components)
        {
            list.add(ComponentBean.fullComponent(component, jiraBaseUrls, projectLeadUserName, projectAssigneeType, userManager, avatarService, permissionManager, projectManager));
        }
        return list;
    }

    @XmlElement
    private URI self;

    @XmlElement
    private String id;

    @XmlElement
    private String name;

    @XmlElement
    private String description;

    @XmlElement
    private UserBean lead;

    /**
     * The name of the lead.  Used on Create & Edit
     */
    @XmlElement
    private String leadUserName;

    @XmlElement
    private AssigneeType assigneeType;

    /**
     * The configured assignee
     */
    @XmlElement
    private UserBean assignee;

    @XmlElement
    private AssigneeType realAssigneeType;

    /**
     * The real assignee in cases where the configured assignee is not assignable
     */
    @XmlElement
    private UserBean realAssignee;

    /**
     * If the assignee type is valid. So if the real assignee type matches the specified one, and if the user is assignable.
     */
    @XmlElement
    private boolean isAssigneeTypeValid;

    @XmlElement
    private String project;

    public ComponentBean() {}

    private static ComponentBean shortComponent(final ProjectComponent component, final JiraBaseUrls jiraBaseUrls)
    {
        final ComponentBean bean = new ComponentBean();
        bean.self = UriBuilder.fromPath(jiraBaseUrls.restApi2BaseUrl()).path(ComponentResource.class).path(component.getId().toString()).build();
        bean.id = component.getId().toString();
        bean.description = component.getDescription();
        bean.name = component.getName();
        return bean;
    }

    public static ComponentBean fullComponent(final ProjectComponent component, JiraBaseUrls jiraBaseUrls, String projectLeadUserName, long projectAssigneeType, final UserManager userManager, final AvatarService avatarService, final PermissionManager permissionManager, final ProjectManager projectManager)
    {
        final ComponentBean bean = shortComponent(component, jiraBaseUrls);
        bean.description = component.getDescription();

        final String lead = component.getLead();
        final ApplicationUser user = userManager.getUserByKeyEvenWhenUnknown(lead);
        final ApplicationUser projectLeadUser = userManager.getUserByName(projectLeadUserName);
        final String projectLeadUserKey = (projectLeadUser == null) ? null :projectLeadUser.getKey();

        if (user != null)
        {
            bean.lead = new UserBeanBuilder(jiraBaseUrls).user(user).buildShort();
        }

        populateAssignee(bean, component, projectLeadUserKey, projectAssigneeType, jiraBaseUrls, userManager, permissionManager, projectManager);

        return bean;
    }

    private static void populateAssignee(ComponentBean bean, ProjectComponent component, String projectLeadUserKey, long projectAssigneeType, JiraBaseUrls jiraBaseUrls, UserManager userManager, PermissionManager permissionManager, ProjectManager projectManager)
    {
        String configuredAssigneeName = null;
        switch ((int) component.getAssigneeType())
        {
            case (int) AssigneeTypes.COMPONENT_LEAD:
            {
                configuredAssigneeName = component.getLead();
                break;
            }
            case (int) AssigneeTypes.PROJECT_LEAD:
            {
                configuredAssigneeName = projectLeadUserKey;
                break;
            }
            case (int) AssigneeTypes.PROJECT_DEFAULT:
            {
                if (projectAssigneeType == AssigneeTypes.PROJECT_LEAD)
                {
                    configuredAssigneeName = projectLeadUserKey;
                }
                else
                {
                    configuredAssigneeName = null;
                }
                break;
            }
        }
        // Get the real picture
        long realAssigneeType = ComponentUtils.getAssigneeType(component.getGenericValue(), component.getAssigneeType());
        String realAssigneeName = null;
        switch ((int) realAssigneeType)
        {
            case (int) AssigneeTypes.COMPONENT_LEAD:
            {
                realAssigneeName = component.getLead();
                break;
            }
            case (int) AssigneeTypes.PROJECT_LEAD:
            {
                realAssigneeName = projectLeadUserKey;
                break;
            }
            case (int) AssigneeTypes.PROJECT_DEFAULT:
            {
                if (projectAssigneeType == AssigneeTypes.PROJECT_LEAD)
                {
                    realAssigneeName = projectLeadUserKey;
                }
                else
                {
                    realAssigneeName = null;
                }
                break;
            }
        }

        bean.assigneeType = AssigneeType.getAssigneeType(component.getAssigneeType());
        if (configuredAssigneeName != null)
        {
            final ApplicationUser user = userManager.getUserByKeyEvenWhenUnknown(configuredAssigneeName);
            bean.assignee = user != null ? new UserBeanBuilder(jiraBaseUrls).user(user).buildShort() : null;
        }

        bean.realAssigneeType = AssigneeType.getAssigneeType(realAssigneeType);
        if (realAssigneeName != null)
        {
            final ApplicationUser user = userManager.getUserByKeyEvenWhenUnknown(realAssigneeName);
            bean.realAssignee = user != null ? new UserBeanBuilder(jiraBaseUrls).user(user).buildShort() : null;

            final Project project = projectManager.getProjectObj(component.getProjectId());
            final boolean assignable = permissionManager.hasPermission(Permissions.ASSIGNABLE_USER, project, user);
            bean.isAssigneeTypeValid = assignable;
        }

        if (bean.realAssigneeType == AssigneeType.UNASSIGNED) {
            bean.isAssigneeTypeValid = ComponentAssigneeTypes.isAssigneeTypeValid(component.getGenericValue(),
                    AssigneeType.UNASSIGNED.getId());

        }

        if (bean.assigneeType != bean.realAssigneeType)
        {
            bean.isAssigneeTypeValid = false;
        }
    }

    public String getId()
    {
        return id;
    }

    public String getName()
    {
        return name;
    }

    public String getDescription()
    {
        return description;
    }

    public UserBean getLead()
    {
        return lead;
    }

    public URI getSelf()
    {
        return this.self;
    }

    public String getLeadUserName()
    {
        return leadUserName;
    }

    public AssigneeType getAssigneeType()
    {
        return assigneeType;
    }

    public String getProject()
    {
        return project;
    }

    public enum AssigneeType
    {
        PROJECT_DEFAULT(AssigneeTypes.PROJECT_DEFAULT),
        COMPONENT_LEAD(AssigneeTypes.COMPONENT_LEAD),
        PROJECT_LEAD(AssigneeTypes.PROJECT_LEAD),
        UNASSIGNED(AssigneeTypes.UNASSIGNED);

        private final long id;

        AssigneeType(long id)
        {
            this.id = id;
        }

        public long getId()
        {
            return id;
        }

        static AssigneeType getAssigneeType(long assigneeType)
        {
            switch ((short) assigneeType)
            {
                case (short) AssigneeTypes.PROJECT_DEFAULT:
                    return PROJECT_DEFAULT;
                case (short) AssigneeTypes.COMPONENT_LEAD:
                    return COMPONENT_LEAD;
                case (short) AssigneeTypes.PROJECT_LEAD:
                    return PROJECT_LEAD;
                case (short) AssigneeTypes.UNASSIGNED:
                    return UNASSIGNED;
            }
            return null;
        }
    }

}
