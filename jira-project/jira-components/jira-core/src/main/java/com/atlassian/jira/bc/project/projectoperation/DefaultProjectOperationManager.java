package com.atlassian.jira.bc.project.projectoperation;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.plugin.projectoperation.PluggableProjectOperation;
import com.atlassian.jira.plugin.projectoperation.ProjectOperationModuleDescriptor;
import com.atlassian.jira.plugin.util.ModuleDescriptorComparator;
import com.atlassian.jira.project.Project;
import com.atlassian.plugin.PluginAccessor;
import org.apache.log4j.Logger;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 * @since v3.12
 */
public class DefaultProjectOperationManager implements ProjectOperationManager
{
    private static final Logger log = Logger.getLogger(DefaultProjectOperationManager.class);
    private final PluginAccessor pluginAccessor;


    public DefaultProjectOperationManager(PluginAccessor pluginAccessor)
    {
        this.pluginAccessor = pluginAccessor;
    }

    public List<PluggableProjectOperation> getVisibleProjectOperations(Project project, User user)
    {
        final List<PluggableProjectOperation> returnList = new ArrayList<PluggableProjectOperation>();
        final List<ProjectOperationModuleDescriptor> projectOperationDescriptors = new ArrayList<ProjectOperationModuleDescriptor>(pluginAccessor.getEnabledModuleDescriptorsByClass(ProjectOperationModuleDescriptor.class));
        //sort the plugins by their order attribute.
        Collections.sort(projectOperationDescriptors, ModuleDescriptorComparator.COMPARATOR);

        for (ProjectOperationModuleDescriptor descriptor : projectOperationDescriptors)
        {
            loadProjectOperation(project, user, returnList, descriptor);
        }

        return returnList;
    }

    private void loadProjectOperation(final Project project, final User user, final List<? super PluggableProjectOperation> returnList, final ProjectOperationModuleDescriptor descriptor)
    {
        // if a particular operation dies, we don't want it to take out _all_ operations
        try
        {
            PluggableProjectOperation pluggableProjectOperation = descriptor.getModule();
            if (pluggableProjectOperation != null && pluggableProjectOperation.showOperation(project, user))
            {
                returnList.add(pluggableProjectOperation);
            }
        }
        catch (Exception e)
        {
            log.error("Error loading project operation " + descriptor.getCompleteKey(), e);
        }
    }
}
