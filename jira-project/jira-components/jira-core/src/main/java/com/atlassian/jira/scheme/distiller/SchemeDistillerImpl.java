package com.atlassian.jira.scheme.distiller;

import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.event.type.EventTypeManager;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.permission.ProjectPermission;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.scheme.Scheme;
import com.atlassian.jira.scheme.SchemeManager;
import com.atlassian.jira.scheme.SchemeManagerFactory;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.plugin.ProjectPermissionKey;
import com.google.common.collect.Lists;
import org.apache.commons.collections.MultiHashMap;
import org.apache.commons.collections.MultiMap;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;

import static com.atlassian.jira.permission.LegacyProjectPermissionKeyMapping.getIdAsLong;

/**
 * Implements {@link SchemeDistiller}.
 */
public class SchemeDistillerImpl implements SchemeDistiller
{
    private SchemeManagerFactory schemeManagerFactory;
    private PermissionManager permissionManager;
    private EventTypeManager eventTypeManager;

    public SchemeDistillerImpl(SchemeManagerFactory schemeManagerFactory, PermissionManager permissionManager, EventTypeManager eventTypeManager)
    {
        this.schemeManagerFactory = schemeManagerFactory;
        this.permissionManager = permissionManager;
        this.eventTypeManager = eventTypeManager;
    }

    public Scheme persistNewSchemeMappings(DistilledSchemeResult distilledSchemeResult) throws DataAccessException
    {
        // Create the new scheme and associated entities
        SchemeManager schemeManager = schemeManagerFactory.getSchemeManager(distilledSchemeResult.getType());

        // Set the name of the scheme from the tempname in the distilled result
        distilledSchemeResult.getResultingScheme().setName(distilledSchemeResult.getResultingSchemeTempName());

        Scheme scheme = schemeManager.createSchemeAndEntities(distilledSchemeResult.getResultingScheme());
        distilledSchemeResult.setResultingScheme(scheme);
        modifyAllProjectAssociations(schemeManager, distilledSchemeResult);

        return scheme;
    }

    public SchemeRelationships getSchemeRelationships(DistilledSchemeResults distilledSchemeResults)
    {
        SchemeRelationships schemeRelationships = null;

        // Initialize the results with the correct schemeTypes, either Notification types (Issue Created, etc..) or
        // Permission Types (Browse Project, etc...).
        final String schemeType = distilledSchemeResults.getSchemeType();
        if (SchemeManagerFactory.NOTIFICATION_SCHEME_MANAGER.equals(schemeType))
        {
            Map schemeTypes = eventTypeManager.getEventTypesMap();

            schemeRelationships = new SchemeRelationships(distilledSchemeResults.getDistilledSchemeResults(),
                    distilledSchemeResults.getUnDistilledSchemes(), getNotificationTypes(schemeTypes));
        }
        else if (SchemeManagerFactory.PERMISSION_SCHEME_MANAGER.equals(schemeType))
        {
            Collection<ProjectPermission> permissions = permissionManager.getAllProjectPermissions();

            // Create the holder for our results
            schemeRelationships = new SchemeRelationships(distilledSchemeResults.getDistilledSchemeResults(),
                    distilledSchemeResults.getUnDistilledSchemes(), getPermissionTypes(permissions));
        }

        return schemeRelationships;
    }

    private Collection getNotificationTypes(Map events)
    {
        Collection notificationTypes = new ArrayList();
        for (final Object o : events.entrySet())
        {
            Map.Entry entry = (Map.Entry) o;
            EventType eventType = (EventType) entry.getValue();
            notificationTypes.add(new SchemeEntityType(entry.getKey(), eventType.getNameKey()));
        }
        return notificationTypes;
    }

    private Collection<SchemeEntityType> getPermissionTypes(Collection<ProjectPermission> permissions)
    {
        final List<SchemeEntityType> permissionTypes = Lists.newArrayListWithCapacity(permissions.size());
        for (ProjectPermission permission : permissions)
        {
            ProjectPermissionKey permissionKey = new ProjectPermissionKey(permission.getKey());
            // This will be null for non-system permissions, but at the moment only system permissions are used.
            Long permissionId = getIdAsLong(permissionKey);
            if (permissionId != null)
            {
                permissionTypes.add(new SchemeEntityType(permissionId, permission.getNameI18nKey()));
            }
        }
        return permissionTypes;
    }

    private void modifyAllProjectAssociations(SchemeManager schemeManager, DistilledSchemeResult distilledSchemeResult)
    {
        // update the associations for all projects
        for (final Object o : distilledSchemeResult.getAllAssociatedProjects())
        {
            Project project = (Project) o;
            // Remove the current scheme
            schemeManager.removeSchemesFromProject(project);
            // Add the new scheme association
            schemeManager.addSchemeToProject(project, distilledSchemeResult.getResultingScheme());
        }
    }

    public DistilledSchemeResults distillSchemes(Collection schemes)
    {
        if (schemes == null)
        {
            return new DistilledSchemeResults(null);
        }
        else
        {
            // If we are creating a result set for a collection of schemes then we want to record the
            // type of the schemes we are a result container for.
            String type = null;
            if (!schemes.isEmpty())
            {
                type = ((Scheme) schemes.iterator().next()).getType();
            }

            MultiMap commonSchemeBucket = new MultiHashMap();

            // Iterate through all the schemes and divide them into categories based on the hashcode
            // of their child entities. This should put all the 'equal' schemes together. NOTE: this
            // does not separate based on type but it is VERY unlikely that a set of notification
            // schemeEntities and a set of permission schemeEntities will be exactly the same, so
            // we can safely ignore this.
            for (final Object scheme1 : schemes)
            {
                Scheme scheme = (Scheme) scheme1;
                commonSchemeBucket.put(new HashSet(scheme.getEntities()), scheme);
            }

            DistilledSchemeResults distilledSchemeResults = new DistilledSchemeResults(type);
            // Ideally we would iterate over entrySet, but commonSchemeBucket is an instance of MultiMap and therefore
            // we must get the keySet first and lookup the Collection of values for it.
            for (final Object o : commonSchemeBucket.keySet())
            {
                List commonSchemes = new ArrayList((Collection) commonSchemeBucket.get(o));

                // Check to see if the scheme matches any other schemes, this means we will smoosh, otherwise it is
                // just the original scheme :(
                if (commonSchemes.size() > 1)
                {
                    distilledSchemeResults.addDistilledSchemeResult(getDistilledSchemeResult(commonSchemes));
                }
                else
                {
                    distilledSchemeResults.addUndistilledScheme(((Scheme) commonSchemes.get(0)));
                }
            }
            return distilledSchemeResults;
        }
    }

    private DistilledSchemeResult getDistilledSchemeResult(List commonSchemes)
    {
        MultiMap projectsByScheme = new MultiHashMap();
        String type = null;
        for (final Object commonScheme : commonSchemes)
        {
            Scheme scheme = (Scheme) commonScheme;
            if (type == null)
            {
                type = scheme.getType();
            }
            for (final Object o : getProjectsForScheme(scheme, type))
            {
                Project project = (Project) o;
                projectsByScheme.put(scheme, project);
            }
        }
        return new DistilledSchemeResult(type, commonSchemes, projectsByScheme, ((Scheme) commonSchemes.get(0)).cloneScheme());
    }

    private Collection getProjectsForScheme(Scheme scheme, String type)
    {
        Collection projects = null;

        SchemeManager schemeManager = schemeManagerFactory.getSchemeManager(type);
        if (scheme.getId() != null)
        {
            projects = schemeManager.getProjects(scheme);
        }
        if (projects == null)
        {
            projects = Collections.EMPTY_LIST;
        }

        return projects;
    }

}
