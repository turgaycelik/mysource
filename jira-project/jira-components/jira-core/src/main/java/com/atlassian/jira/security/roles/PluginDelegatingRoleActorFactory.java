package com.atlassian.jira.security.roles;

import com.atlassian.jira.plugin.roles.ProjectRoleActorModuleDescriptor;
import com.atlassian.plugin.PluginAccessor;
import com.atlassian.util.profiling.UtilTimerStack;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Creator of RoleActor objects that have been registered dynamically.
 */
public class PluginDelegatingRoleActorFactory implements RoleActorFactory
{

    private final PluginAccessor pluginAccessor;

    public PluginDelegatingRoleActorFactory(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    public ProjectRoleActor createRoleActor(Long id, Long projectRoleId, Long projectId, String type, String parameter)
            throws RoleActorDoesNotExistException
    {
        Map implementations = getImplementations();

        ProjectRoleActorModuleDescriptor roleActorModuleDescriptor = getRoleActorModuleDescriptor(type, implementations);
        if (roleActorModuleDescriptor == null)
        {
            throw new IllegalArgumentException("Type " + type + " is not a registered RoleActor implementation");
        }
        // Let the module descriptor instantiate the concrete RoleActorFactory for us in case it has any deps it needs
        // injected.
        RoleActorFactory roleActorFactory = roleActorModuleDescriptor.getModule();
        return roleActorFactory.createRoleActor(id, projectRoleId, projectId, type, parameter);
    }

    /*
     * Delegates to plugin RoleActorFactories
     *
     * @see com.atlassian.jira.security.roles.RoleActorFactory#optimizeRoleActorSet(java.util.Set)
     */
    public Set optimizeRoleActorSet(Set roleActors)
    {
        for (final Object o : getImplementations().values())
        {
            final ProjectRoleActorModuleDescriptor moduleDescriptor = (ProjectRoleActorModuleDescriptor) o;
            final RoleActorFactory roleActorFactory = moduleDescriptor.getModule();
            roleActors = roleActorFactory.optimizeRoleActorSet(roleActors);
        }
        return roleActors;
    }

    private ProjectRoleActorModuleDescriptor getRoleActorModuleDescriptor(String type, Map implementations)
    {
        if (implementations == null)
        {
            return null;
        }
        else
        {
            return (ProjectRoleActorModuleDescriptor) implementations.get(type);
        }
    }

    Map getImplementations()
    {
        UtilTimerStack.push("DefaultRoleActorFactory.getImplementations");
        UtilTimerStack.push("DefaultRoleActorFactory.getImplementations-getEnabledModuleDescriptorByClass");
        List<ProjectRoleActorModuleDescriptor> descriptors = pluginAccessor.getEnabledModuleDescriptorsByClass(ProjectRoleActorModuleDescriptor.class);
        UtilTimerStack.pop("DefaultRoleActorFactory.getImplementations-getEnabledModuleDescriptorByClass");

        Map actorsByType = new HashMap(descriptors.size());

        for (final ProjectRoleActorModuleDescriptor projectRoleModuleDescriptor : descriptors)
        {
            actorsByType.put(projectRoleModuleDescriptor.getKey(), projectRoleModuleDescriptor);
        }
        UtilTimerStack.pop("DefaultRoleActorFactory.getImplementations");

        return actorsByType;
    }
}