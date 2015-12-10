package com.atlassian.jira.gadgets.system;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.rest.v1.util.CacheControl;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.plugins.rest.common.security.AnonymousAllowed;
import org.apache.commons.lang.builder.EqualsBuilder;
import org.apache.commons.lang.builder.HashCodeBuilder;
import org.apache.commons.lang.builder.ToStringBuilder;
import org.apache.commons.lang.builder.ToStringStyle;
import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

import javax.ws.rs.GET;
import javax.ws.rs.Path;
import javax.ws.rs.Produces;
import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.Response;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.XmlType;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

/**
 * This REST resource is used to generate an option list that is closely mapped to the option elements of a HTML select
 * element, so that the returned JSON object can replace the option elements of a HTML select element, without further
 * manipulation. However, this also tightly couples this resource to HTML structure. Future improvement of decoupling is
 * recommended.
 *
 * @since v4.0
 */
@Path("/projectsAndProjectCategories")
@AnonymousAllowed
@Produces({MediaType.APPLICATION_JSON})
public class ProjectsAndProjectCategoriesResource
{
    private static final Logger log = Logger.getLogger(ProjectsAndProjectCategoriesResource.class);
    private static final ToStringStyle TO_STRING_STYLE = ToStringStyle.SIMPLE_STYLE;

    public static final String ALL_PROJECTS = "allprojects";
    public static final String ALL_CATEGORIES = "catallCategories";
    static final String CATEGORY = "cat";

    private JiraAuthenticationContext authenticationContext;
    private final PermissionManager permissionManager;

    public ProjectsAndProjectCategoriesResource(final JiraAuthenticationContext authenticationContext,
                                                final PermissionManager permissionManager)
    {
        this.authenticationContext = authenticationContext;
        this.permissionManager = permissionManager;

    }

    /**
     * Generates an option list of all projects and categories.
     * <p/>
     * This is the only REST endpoint, accessed via a GET method to the path annotating the class.
     * <p/>
     * Currently, there is no parameter required.
     * <p/>
     * The returned option list will always contain an option group for &quot;Projects&quot;, which will always contain
     * an option &quot;All Projects&quot; as the first element, even if there is no project at all.
     * <p/>
     * The result may also contain a &quot;Project Categories&quot; option group, if any project is assigned a category.
     * It should be noted that in this implementation, any existing project category is not included if there is no
     * project is listed under that category. This is consistent with the implementation of the road map portlet.
     * <p/>
     * There are some noticeable differences between this implementation of that of road map gadget: <ul> <li>The option
     * group name is now always shown even if there is only one group.</li> <li>There is no enterprise edition check
     * when handling project categories, as this will be available in all versions soon.</li> </ul>
     *
     * @return an OK response with an {@link com.atlassian.jira.gadgets.system.ProjectsAndProjectCategoriesResource.ProjectAndCategories}
     *         object.
     */
    @GET
    public Response generate()
    {
        final User user = authenticationContext.getLoggedInUser();
        final Collection<Project> projects = getProjects(user);

        final List<Option> projectOptions = getProjectOptions(user, projects);

        final Collection<Option> categories = getCategoriesForProjects(projects);
        return Response.ok(new ProjectAndCategories(categories, projectOptions)).cacheControl(CacheControl.NO_CACHE).build();
    }

    // The following are copied from com.atlassian.jira.portal.ProjectAndProjectCategoryValuesGenerator

    /**
     * Filter project ids from the given collection. Returns a set of project ids as Long objects. Value is a valid
     * project id if it is a long number.
     *
     * @param projectOrCategoryIds collection of project or category ids
     * @return set of project ids, never null
     */
    public static Set<Long> filterProjectIds(final Collection<String> projectOrCategoryIds)
    {
        final Set<Long> projectIds = new HashSet<Long>();
        if ((projectOrCategoryIds != null) && !projectOrCategoryIds.isEmpty())
        {
            for (final String id : projectOrCategoryIds)
            {
                if (!id.startsWith(CATEGORY))
                {
                    try
                    {
                        final Long projectId = new Long(id);
                        projectIds.add(projectId);
                    }
                    catch (final NumberFormatException e)
                    {
                        // ignore if ID is not a number
                        log.info("Project ID '" + id + "' could not be parsed!");
                    }
                }
            }
        }
        return projectIds;
    }

    /**
     * Filter project category ids from the given collection. Returns a set of project category ids as Long objects.
     * Value is a valid category if it takes form "catXYZ" where "cat" is the prefix ({@link #CATEGORY}) and XYZ is a
     * long number.
     *
     * @param projectOrCategoryIds collection of project or category ids
     * @return set of project category ids, never null
     */
    public static Set<Long> filterProjectCategoryIds(final Collection<String> projectOrCategoryIds)
    {
        final Set<Long> categoryIds = new HashSet<Long>();
        if ((projectOrCategoryIds != null) && !projectOrCategoryIds.isEmpty())
        {
            for (final String id : projectOrCategoryIds)
            {
                if (id.startsWith(CATEGORY))
                {
                    try
                    {
                        final Long categoryId = new Long(id.substring(CATEGORY.length()));
                        categoryIds.add(categoryId);
                    }
                    catch (final NumberFormatException e)
                    {
                        // ignore if ID is not a number
                        log.warn("Project Category ID '" + id + "' could not be parsed!");
                    }
                }
            }
        }
        return categoryIds;
    }


    private List<Option> getProjectOptions(User user, Collection<Project> projects)
    {
        final List<Option> projectOptions = new ArrayList<Option>(projects.size());

        for (Project project : projects)
        {
            projectOptions.add(new Option(project.getName(), project.getId().toString()));
        }
        return projectOptions;
    }

    private Collection<Project> getProjects(User user)
    {
        return permissionManager.getProjectObjects(Permissions.BROWSE, user);
    }

    private List<Option> getCategoriesForProjects(Collection<Project> projects)
    {

        final Set<Option> categoryOptionsSet = new HashSet<Option>();
        if ((projects != null) && !projects.isEmpty())
        {
            for (final Project project : projects)
            {
                final GenericValue categoryGV = project.getProjectCategory();
                if (categoryGV != null)
                {
                    categoryOptionsSet.add(new Option(categoryGV.getString("name"), "cat" + categoryGV.getString("id")));
                }
            }
        }

        final List<Option> categoryOptions = new ArrayList<Option>(categoryOptionsSet);
        Collections.sort(categoryOptions, new Comparator<Option>()
        {

            public int compare(Option option, Option option1)
            {
                return option.getLabel().compareTo(option1.getLabel());
            }
        });

        return categoryOptions;
    }


    ///CLOVER:OFF
    @XmlType(namespace = "com.atlassian.jira.gadgets.system.ProjectsAndProjectCategoriesResource")
    @XmlRootElement
    public static class ProjectAndCategories
    {
        @XmlElement
        private Collection<Option> categories;
        @XmlElement
        private Collection<Option> projects;

        public ProjectAndCategories()
        {
        }

        public ProjectAndCategories(Collection<Option> categories, Collection<Option> projects)
        {
            this.categories = categories;
            this.projects = projects;
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(final Object o)
        {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, TO_STRING_STYLE);
        }
    }


    @XmlType(namespace = "com.atlassian.jira.gadgets.system.ProjectsAndProjectCategoriesResource")
    @XmlRootElement
    public static class Option
    {
        @XmlElement
        private String label;
        @XmlElement
        private String value;

        public Option()
        {
        }

        Option(String label, String value)
        {
            this.label = label;
            this.value = value;
        }

        public String getLabel()
        {
            return label;
        }

        public String getValue()
        {
            return value;
        }

        @Override
        public int hashCode()
        {
            return HashCodeBuilder.reflectionHashCode(this);
        }

        @Override
        public boolean equals(final Object o)
        {
            return EqualsBuilder.reflectionEquals(this, o);
        }

        @Override
        public String toString()
        {
            return ToStringBuilder.reflectionToString(this, TO_STRING_STYLE);
        }
    }
///CLOVER:ON
}
