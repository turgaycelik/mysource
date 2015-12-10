package com.atlassian.jira.applinks;

import com.atlassian.applinks.api.ApplicationId;
import com.atlassian.applinks.api.ApplicationType;
import com.atlassian.applinks.api.EntityType;
import com.atlassian.applinks.api.application.jira.JiraApplicationType;
import com.atlassian.applinks.api.application.jira.JiraProjectEntityType;
import com.atlassian.applinks.host.OsgiServiceProxyFactory;
import com.atlassian.applinks.host.spi.AbstractInternalHostApplication;
import com.atlassian.applinks.host.spi.DefaultEntityReference;
import com.atlassian.applinks.host.spi.EntityReference;
import com.atlassian.applinks.spi.util.TypeAccessor;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bc.license.JiraLicenseService;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.extension.Startable;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.ProjectManager;
import com.atlassian.jira.security.JiraAuthenticationContext;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.velocity.VelocityRequestContextFactory;
import com.atlassian.plugin.PluginAccessor;
import com.google.common.base.Function;
import com.google.common.base.Predicate;
import com.google.common.collect.Iterables;
import org.apache.commons.lang.StringUtils;

import javax.annotation.Nullable;
import java.io.UnsupportedEncodingException;
import java.net.URI;
import java.net.URISyntaxException;
import java.util.Collections;
import java.util.UUID;

import static com.google.common.collect.Lists.newArrayList;

/**
 * @since v4.3
 */
public class JiraAppLinksHostApplication extends AbstractInternalHostApplication implements Startable
{
    private final ApplicationProperties applicationProperties;
    private final JiraLicenseService jiraLicenseService;
    private final VelocityRequestContextFactory velocityRequestContextFactory;
    private final ProjectManager projectManager;
    private final JiraAuthenticationContext jiraAuthenticationContext;
    private final OsgiServiceProxyFactory osgiServiceProxyFactory;
    private final PermissionManager permissionManager;
    public static final long TIMEOUT = 10000L;
    private TypeAccessor typeAccessor;

    private final ThreadLocal<Boolean>  skipPermissionCheck = new ThreadLocal<Boolean>();

    /**
     * Creates a new JiraAppLinksHostApplication instance.
     */
    public JiraAppLinksHostApplication(PluginAccessor pluginAccessor, ApplicationProperties applicationProperties, JiraLicenseService jiraLicenseService, VelocityRequestContextFactory velocityRequestContextFactory, ProjectManager projectManager, JiraAuthenticationContext jiraAuthenticationContext, OsgiServiceProxyFactory osgiServiceProxyFactory, PermissionManager permissionManager)
    {
        super(pluginAccessor);
        this.applicationProperties = applicationProperties;
        this.jiraLicenseService = jiraLicenseService;
        this.velocityRequestContextFactory = velocityRequestContextFactory;
        this.projectManager = projectManager;
        this.jiraAuthenticationContext = jiraAuthenticationContext;
        this.osgiServiceProxyFactory = osgiServiceProxyFactory;
        this.permissionManager = permissionManager;
    }

    /**
     * @return an absolute {@link java.net.URI} used as the base for constructing links to help pages. e.g. {@code
     *         http://docs.atlassian.com/fisheye/docs-023} or {@code http://confluence.atlassian.com/display/APPLINKS}.
     *         The returned {@link java.net.URI} should not have a trailing slash.
     */
    @Override
    public URI getDocumentationBaseUrl()
    {
        return URI.create("http://confluence.atlassian.com/display/APPLINKS");
    }

    /**
     * @return the name of this application instance, e.g. "My JIRA Server". If this application type doesn't support
     *         customising the name of the instance, implementations should delegate to {@link
     *         com.atlassian.applinks.host.util.InstanceNameGenerator} to generate a name from the application's base
     *         URL.
     */
    @Override
    public String getName()
    {
        return applicationProperties.getString(APKeys.JIRA_TITLE);
    }

    /**
     * @return the {@link com.atlassian.applinks.api.ApplicationType} for this application instance. Implementations
     *         should delegate to the {@link com.atlassian.applinks.spi.util.TypeAccessor} to resolve an instance of the
     *         desired type.
     */
    @Override
    public ApplicationType getType()
    {
        return typeAccessor.getApplicationType(JiraApplicationType.class);
    }

    /**
     * @return an {@link Iterable} containing an {@link com.atlassian.applinks.host.spi.EntityReference} for every
     *         entity in the local instance visible to the currently logged in user. Note, the implementation
     *         <strong>must perform a permission check</strong> and return only entities visible the context user (who
     *         may be anonymous).
     *         User requires to have either the BROWSE project, JIRA Administrator or PROJECT ADMIN permission.
     */
    @Override
    public Iterable<EntityReference> getLocalEntities()
    {
        final Iterable<Project> projects = Iterables.filter(projectManager.getProjectObjects(), new Predicate<Project>()
        {
            @Override
            public boolean apply(@Nullable Project input)
            {
                return (input != null) && checkProjectPermissions(input, true);
            }
        });
        if (!projects.iterator().hasNext())
        {
            return Collections.emptyList();
        }
        return newArrayList(Iterables.transform(projects, new ProjectToEntityRef()));
    }

    /**
     * @param key the key of an entity local to this application (e.g. JRA, CONF)
     * @param type the class of the {@link com.atlassian.applinks.api.EntityType} of the entity (e.g. {@link
     * com.atlassian.applinks.api.application.jira.JiraProjectEntityType})
     *
     * @return true, if the specified entity exists, false otherwise. Note, the implementation <strong>must perform a
     *         permission check</strong> and return true if, and only if, the specified entity exists and is visible to
     *         the context user (who may be anonymous). User requires to have either the BROWSE project, JIRA Administrator or PROJECT ADMIN permission.
     */
    @Override
    public boolean doesEntityExist(String key, Class<? extends EntityType> type)
    {
        if (JiraProjectEntityType.class.isAssignableFrom(type))
        {
            Project project = projectManager.getProjectObjByKey(key);
            if (project != null)
            {
                return checkProjectPermissions(project, true);
            }
        }
        return false;
    }

    /**
     * @param key  the key of an entity local to this application (e.g. JRA, CONF)
     * @param type the class of the {@link EntityType} of the entity (e.g. {@link JiraProjectEntityType})
     * @return true, if the specified entity exists, false otherwise. For a variant on this method with permission
     * checking, use {@link #doesEntityExist(String, Class)}.
     * @since 3.6
     */
    public boolean doesEntityExistNoPermissionCheck(String key, Class<? extends EntityType> type)
    {
        if (JiraProjectEntityType.class.isAssignableFrom(type))
        {
            Project project = projectManager.getProjectObjByKey(key);
            if (project != null)
            {
                return true;
            }
        }
        return false;
    }

    /**
     * @param domainObject an entity domain object from the application's API (e.g. com.atlassian.jira.project.Project,
     * com.atlassian.confluence.spaces.Space). Implementations are free to choose which objects supported by this class,
     * but the complete list should be maintained on the {@link com.atlassian.applinks.api.EntityLinkService} javadoc.
     * @return an {@link com.atlassian.applinks.host.spi.EntityReference} initialised with the key and type of the
     *         supplied domain object. This method need not perform any permission checking. Implementations should
     *         delegate to the {@link com.atlassian.applinks.spi.util.TypeAccessor} to resolve an instance of the
     *         desired {@link com.atlassian.applinks.api.EntityType}.
     */
    @Override
    public EntityReference toEntityReference(Object domainObject)
    {
        if (domainObject instanceof Project)
        {
            Project jiraProject = (Project) domainObject;
            return new DefaultEntityReference(jiraProject.getKey(), jiraProject.getName(), entityType(JiraProjectEntityType.class));
        }

        throw new IllegalArgumentException("Entity is not supported: " + domainObject);
    }

    /**
     * @param key the key of a local entity (e.g. "JRA", "FECRUDEV", "CR-BAM")
     * @param type the class of the {@link com.atlassian.applinks.api.EntityType} of the entity (e.g. {@link
     * com.atlassian.applinks.api.application.jira.JiraProjectEntityType})
     * @return an {@link com.atlassian.applinks.host.spi.EntityReference} initialised with the key and type of the
     *         supplied domain object. This method need not perform any permission checking. Implementations should
     *         delegate to the {@link com.atlassian.applinks.spi.util.TypeAccessor} to resolve an instance of the
     *         specified {@link com.atlassian.applinks.api.EntityType}.
     * @throws IllegalArgumentException if the specified type is not assignable to {@link JiraProjectEntityType} or
     *         if no project with the specified key can be found
     */
    @Override
    public EntityReference toEntityReference(String key, Class<? extends EntityType> type)
    {
        if (type != null && JiraProjectEntityType.class.isAssignableFrom(type))
        {
            Project project = projectManager.getProjectObjByKey(key);
            if (project == null)
            {
                throw new IllegalArgumentException("The specified key could not be resolved to a project: " + key);
            }
            return new DefaultEntityReference(project.getKey(), project.getName(), entityType(JiraProjectEntityType.class));
        }

        throw new IllegalArgumentException("Entity type is not supported: " + type);
    }

    /**
     * @param entityReference an {@link com.atlassian.applinks.host.spi.EntityReference} representing an entity
     * contained in the local application instance.
     * @return {@code true} if the current user has permission to link or unlink the specified {@link
     *         com.atlassian.applinks.host.spi.EntityReference} to other entities, {@code false} otherwise.
     *         User requires to have either the JIRA Administrator or PROJECT ADMIN permission.
     */
    @Override
    public boolean canManageEntityLinksFor(EntityReference entityReference)
    {
        if (entityReference != null && entityReference.getType() instanceof JiraProjectEntityType)
        {
            Project project = projectManager.getProjectObjByKey(entityReference.getKey());
            if (project != null)
            {
                return checkProjectPermissions(project, false);
            }
        }
        return false;
    }

    /**
     * Method used to perform permission checking for UAL.
     *
     * @param project the project to check the permissions for.
     * @param allowBrowse whether to pass the permission check if the user has BROWSE project permission.
     * @return true if the user has the required permission.
     */
    private boolean checkProjectPermissions(Project project, boolean allowBrowse)
    {
      if (skipPermissions())
      {
          return true;
      }
      if (project == null)
      {
          return false;
      }

      return (allowBrowse && permissionManager.hasPermission(Permissions.BROWSE, project, jiraAuthenticationContext.getUser())) ||
              permissionManager.hasPermission(Permissions.ADMINISTER, jiraAuthenticationContext.getUser()) ||
              permissionManager.hasPermission(Permissions.PROJECT_ADMIN, project, jiraAuthenticationContext.getUser());
    }

    private boolean skipPermissions()
    {
        return skipPermissionCheck.get() == null? false: skipPermissionCheck.get().booleanValue();
    }

    public void setSkipPermissionCheck(boolean skip)
    {
        skipPermissionCheck.set(skip);
    }

    public void clearSkipPermissionCheck()
    {
        skipPermissionCheck.remove();
    }

    /**
     * @return {@code true} if the host application allows public signup, {@code false} otherwise.
     */
    @Override
    public boolean hasPublicSignup()
    {
        return "public".equals(applicationProperties.getString(APKeys.JIRA_MODE));
    }

    @Override
    public ApplicationId getId()
    {
        String serverId = jiraLicenseService.getServerId();
        try
        {
            return new ApplicationId(UUID.nameUUIDFromBytes(serverId.getBytes("UTF-8")).toString());
        }
        catch (UnsupportedEncodingException e)
        {
            throw new IllegalStateException("UTF-8 is not supported. WTF?", e);
        }
    }

    /**
     * Returns the base URL that is configured for this JIRA instance.
     *
     * @return a String containing the base URL that is configured for this JIRA instance
     */
    @Override
    public URI getBaseUrl()
    {
        String baseUrl = applicationProperties.getString(APKeys.JIRA_BASEURL);
        if (StringUtils.isEmpty(baseUrl))
        {
            baseUrl = velocityRequestContextFactory.getJiraVelocityRequestContext().getCanonicalBaseUrl();
        }
        try
        {
            return new URI(baseUrl);
        }
        catch (URISyntaxException e)
        {
            throw new IllegalStateException("Invalid base url: " + baseUrl, e);
        }
    }

    /**
     * Uses the TypeAccessor that is made available by the UAL plugin to create an EntityType instance for the given
     * entity type class.
     *
     * @param entityTypeClass an EntityType class
     * @return an EntityType
     */
    private <T extends com.atlassian.applinks.api.EntityType> T entityType(Class<T> entityTypeClass)
    {
        return typeAccessor.getEntityType(entityTypeClass);
    }

    /**
     * Returns the currently logged-in user.
     *
     * @return a User object for the currently logged-in user
     */
    private User getUser()
    {
        return jiraAuthenticationContext.getLoggedInUser();
    }

     /**
     * The OsgiServiceProxyFactory creates a proxy to the TypeAccessor which is a component of the applinks-plugin. The
     * TypeAccessor is not available when the host application starts, but when the plugins framework has finished
     * starting.
     */
    @Override
    public void start() throws Exception
    {
        typeAccessor = osgiServiceProxyFactory.createProxy(TypeAccessor.class, TIMEOUT);
    }

    /**
     * Function class to convert from a JIRA Project into an EntityReference.
     */
    private class ProjectToEntityRef implements Function<Project, EntityReference>
    {

        @Override
        public EntityReference apply(@Nullable Project from)
        {
            return new DefaultEntityReference(from.getKey(), from.getName(), entityType(JiraProjectEntityType.class));
        }
    }

}
