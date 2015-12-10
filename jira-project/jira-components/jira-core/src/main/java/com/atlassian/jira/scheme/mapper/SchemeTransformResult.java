package com.atlassian.jira.scheme.mapper;

import com.atlassian.crowd.embedded.api.Group;
import com.atlassian.jira.bc.projectroles.ProjectRoleService;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeEntity;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.security.roles.ProjectRole;
import com.atlassian.jira.security.roles.ProjectRoleComparator;
import com.atlassian.jira.security.type.GroupDropdown;
import com.atlassian.jira.security.util.GroupToNotificationSchemeMapper;
import org.apache.commons.collections.set.ListOrderedSet;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.TreeMap;

/**
 * This class represents the result of a single GroupToRole transform as performed by the {@link
 * SchemeGroupsToRolesTransformer}. NOTE: this object is statefull and is NOT threadsafe.
 */
public class SchemeTransformResult
{
    private Collection projects;
    private Map roleToGroupsMappingByProjectRole;
    private Scheme originalScheme;
    private Scheme transformedScheme;
    private Scheme resultingScheme;

    public SchemeTransformResult(Scheme originalScheme)
    {
        this.originalScheme = originalScheme;
        this.roleToGroupsMappingByProjectRole = new TreeMap(ProjectRoleComparator.COMPARATOR);
    }

    public boolean originalSchemeTransformed()
    {
        return getTransformedScheme() != null;
    }

    /**
     * Gets the resulting {@link Scheme} from the transform operation. NOTE: this scheme does not exist within the
     * persistent store.
     *
     * @return Scheme object representing the newly created scheme.
     */
    public Scheme getTransformedScheme()
    {
        // Build the transformed scheme only once
        if (transformedScheme == null)
        {
            // Start by cloning the original, Copy over the id and name to the transformed scheme since we are really
            // setting this up as an edit that is just not committed yet.
            transformedScheme = new Scheme(originalScheme.getId(), originalScheme.getType(),
                    originalScheme.getName(), originalScheme.getDescription(), new ArrayList(originalScheme.getEntities()));

            Set entitiesToRemove = new HashSet();
            Set entitiesToAdd = new HashSet();

            // Run through all the entities
            determineSchemeEntitiesToAddAndRemove(entitiesToAdd, entitiesToRemove);

            // If we have not transformed then lets keep the transformedScheme as null
            if (entitiesToRemove.isEmpty() && entitiesToAdd.isEmpty())
            {
                transformedScheme = null;
            }
            else
            {
                //JRA-11692: If we don't use a Set here to combined the oldEntities with the new,
                //          we may end up with duplicate entries in the resulting scheme entities.
                Set entities = new ListOrderedSet();
                entities.addAll(transformedScheme.getEntities());
                entities.removeAll(entitiesToRemove);
                entities.addAll(entitiesToAdd);
                transformedScheme.setEntities(entities);
            }
        }
        return transformedScheme;
    }

    /**
     * The original scheme that was the template for the transform operation.
     *
     * @return Scheme object representing the original scheme. This should never be null.
     */
    public Scheme getOriginalScheme()
    {
        return originalScheme;
    }

    /**
     * Gets all the associated {@link com.atlassian.jira.project.Project}'s for the originalScheme.
     *
     * @return a Collection of {@link com.atlassian.jira.project.Project}'s, empty list if there are none.
     */
    public Collection getAssociatedProjects()
    {
        if (this.projects == null)
        {
            return Collections.EMPTY_LIST;
        }
        return this.projects;
    }

    public void setAssociatedProjects(Collection projects)
    {
        this.projects = projects;
    }

    /**
     * This creates an association between a groupName and a {@link ProjectRole}. The ProjectRole is defined in the
     * passed in {@link GroupToRoleMapping}.
     *
     * @param groupToRoleMapping defineds the {@link com.atlassian.jira.security.roles.ProjectRole} to associate with
     * @param group          the group to associate with
     */
    public void addRoleMappingForGroup(GroupToRoleMapping groupToRoleMapping, Group group)
    {
        RoleToGroupsMapping roleToGroupsMapping = (RoleToGroupsMapping) roleToGroupsMappingByProjectRole.get(groupToRoleMapping.getProjectRole());
        if (roleToGroupsMapping == null)
        {
            roleToGroupsMapping = new RoleToGroupsMapping(groupToRoleMapping);
            roleToGroupsMappingByProjectRole.put(groupToRoleMapping.getProjectRole(), roleToGroupsMapping);
        }

        if(isGroupinSchemeEntities(group.getName()))
        {
            roleToGroupsMapping.addMappedGroup(group);
        }
    }

    private boolean isGroupinSchemeEntities(String groupName)
    {
        for (SchemeEntity schemeEntity : originalScheme.getEntities())
        {
            // If we encounter a group entity then we may want to transform it.
            if (GroupDropdown.DESC.equals(schemeEntity.getType()) ||
                    GroupToNotificationSchemeMapper.GROUP_DROPDOWN.equals(schemeEntity.getType()))
            {
                if (groupName.equals(schemeEntity.getParameter()))
                {
                    return true;
                }
            }
        }

        return false;
    }

    /**
     * This will return all the {@link RoleToGroupsMapping}'s that are defined for this scheme transformation.
     *
     * @return Collection of {@link Group} objects, and Empty collection if there are no mappings.
     */
    public Collection getRoleToGroupsMappings()
    {
        return roleToGroupsMappingByProjectRole.values();
    }

    private void determineSchemeEntitiesToAddAndRemove(Set entitiesToAdd, Set entitiesToRemove)
    {
        for (SchemeEntity schemeEntity : transformedScheme.getEntities())
        {
            String groupName = schemeEntity.getParameter();

            // If we encounter a group entity then we may want to transform it.
            if (GroupDropdown.DESC.equals(schemeEntity.getType()) || GroupToNotificationSchemeMapper.GROUP_DROPDOWN.equals(schemeEntity.getType()))
            {
                boolean matchedGroup = false;

                // Iterator over all our groupToRoleMappings
                for (final Object o : roleToGroupsMappingByProjectRole.values())
                {
                    RoleToGroupsMapping roleToGroupsMapping = (RoleToGroupsMapping) o;
                    ProjectRole projectRole = roleToGroupsMapping.getProjectRole();

                    // If our mapping contains the group name we are looking at then we need to add a SchemeEntity
                    // for the project role the mapping is mapped to.
                    if (projectRole != null && groupName != null && roleToGroupsMapping.getMappedGroupNames().contains(groupName))
                    {
                        boolean isNotification = SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER.equals(transformedScheme.getType());
                        String entityType = (isNotification) ? ProjectRoleService.PROJECTROLE_NOTIFICATION_TYPE : ProjectRoleService.PROJECTROLE_PERMISSION_TYPE;
                        // Create a clone of the schemeEntity with the correct entityType for a projectRole and
                        // the projectRoleId as the param
                        SchemeEntity projectRoleSchemeEntity =
                                new SchemeEntity(null, entityType,
                                        projectRole.getId().toString(), schemeEntity.getEntityTypeId(),
                                        schemeEntity.getTemplateId(), null);

                        // Keep all the entities to add in a list and do it after we have finished iterating
                        entitiesToAdd.add(projectRoleSchemeEntity);
                        matchedGroup = true;
                    }
                }

                // We only really delete the group entity if we have replaced it with something, we need to just
                // keep track of what we want to remove and do it after we are done iteration through the list
                if (matchedGroup)
                {
                    entitiesToRemove.add(schemeEntity);
                }
            }
        }
    }

    public Scheme getResultingScheme()
    {
        return resultingScheme;
    }

    public void setResultingScheme(Scheme resultingScheme)
    {
        this.resultingScheme = resultingScheme;
    }
}
