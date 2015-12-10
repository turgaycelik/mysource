package com.atlassian.jira.security.roles;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.SortedSet;
import java.util.TreeSet;

import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.EntityUtils;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.security.groups.GroupManager;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.util.Supplier;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.collect.MultiMap;
import com.atlassian.jira.util.collect.MultiMaps;
import com.atlassian.util.profiling.UtilTimerStack;

import com.google.common.collect.ImmutableList;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityFindOptions;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

/**
 * OfBiz persistent implementation.
 */
public class OfBizProjectRoleAndActorStore implements ProjectRoleAndActorStore
{
    private static final Logger log = Logger.getLogger(OfBizProjectRoleAndActorStore.class);
    private static final String ENTITY_NAME = "ProjectRole";

    private final OfBizDelegator ofBizDelegator;
    private final RoleActorFactory roleActorFactory;
    private final GroupManager groupManager;
    private static final String PROJECT_ROLE_FIELD_NAME = "name";
    private static final String PROJECT_ROLE_FIELD_DESCRIPTION = "description";
    private static final String ROLE_ACTOR_PID = "pid";
    private static final String FIELD_ID = "id";
    private static final String ROLE_ACTOR_ROLETYPE = "roletype";
    private static final String ROLE_ACTOR_TYPE = ROLE_ACTOR_ROLETYPE;
    private static final String ROLE_ACTOR_PARAMETER = "roletypeparameter";
    private static final String ROLE_ACTOR_PROJECTROLEID = "projectroleid";
    private static final String ROLE_ACTOR_ENTITY_NAME = "ProjectRoleActor";

    public OfBizProjectRoleAndActorStore(final OfBizDelegator genericDelegator, final RoleActorFactory roleActorFactory, GroupManager groupManager)
    {
        ofBizDelegator = genericDelegator;
        this.roleActorFactory = roleActorFactory;
        this.groupManager = groupManager;
    }

    public ProjectRole addProjectRole(final ProjectRole projectRole)
    {
        try
        {
            final GenericValue projectRoleGV = EntityUtils.createValue(ENTITY_NAME, MapBuilder.<String, Object>build(PROJECT_ROLE_FIELD_NAME, projectRole.getName(),
                PROJECT_ROLE_FIELD_DESCRIPTION, projectRole.getDescription()));
            return new ProjectRoleImpl(projectRoleGV.getLong(FIELD_ID), projectRoleGV.getString(PROJECT_ROLE_FIELD_NAME),
                projectRoleGV.getString(PROJECT_ROLE_FIELD_DESCRIPTION));
        }
        catch (final Exception e)
        {
            log.error("Unable to store project role, removing any partially stored entity: " + projectRole.getName(), e);
            throw new DataAccessException(e);
        }
    }

    public void updateProjectRole(final ProjectRole projectRole)
    {
        try
        {
            final GenericValue projectRoleGv = ofBizDelegator.findById(ENTITY_NAME, projectRole.getId());
            projectRoleGv.set(PROJECT_ROLE_FIELD_NAME, projectRole.getName());
            projectRoleGv.set(PROJECT_ROLE_FIELD_DESCRIPTION, projectRole.getDescription());
            projectRoleGv.store();
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public Collection<ProjectRole> getAllProjectRoles()
    {
        try
        {
            final List<GenericValue> projectRoleGVs = ofBizDelegator.findAll(ENTITY_NAME, ImmutableList.of("name ASC"));
            final SortedSet<ProjectRole> projectRoles = new TreeSet<ProjectRole>(ProjectRoleComparator.COMPARATOR);
            for (final GenericValue projectRoleGV : projectRoleGVs)
            {
                projectRoles.add(convertProjectRoleGVToProjectRoleObject(projectRoleGV));
            }
            return projectRoles;
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public ProjectRole getProjectRole(final Long id)
    {
        try
        {
            UtilTimerStack.push("OfBizProjectRoleAndActorStore.getProjectRole");
            final GenericValue projectRoleGV = ofBizDelegator.findById(ENTITY_NAME, id);
            return convertProjectRoleGVToProjectRoleObject(projectRoleGV);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        finally
        {
            UtilTimerStack.pop("OfBizProjectRoleAndActorStore.getProjectRole");
        }
    }

    public ProjectRole getProjectRoleByName(final String name)
    {
        try
        {
            UtilTimerStack.push("OfBizProjectRoleAndActorStore.getProjectRoleByName");
            final List<GenericValue> projectRolesGV = ofBizDelegator.findByAnd(ENTITY_NAME, FieldMap.build(PROJECT_ROLE_FIELD_NAME, name));
            if (projectRolesGV.size() == 1)
            {
                final GenericValue projectRoleGV = projectRolesGV.get(0);
                return convertProjectRoleGVToProjectRoleObject(projectRoleGV);
            }
            else if (projectRolesGV.size() > 1)
            {
                log.error("You have more than one ProjectRole with name " + name);
                throw new DataAccessException("You have more than one ProjectRole with name " + name);
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        finally
        {
            UtilTimerStack.pop("OfBizProjectRoleAndActorStore.getProjectRoleByName");
        }
        return null;
    }

    public void deleteProjectRole(final ProjectRole projectRole)
    {
        try
        {
            final GenericValue projectRoleGV = ofBizDelegator.findById(ENTITY_NAME, projectRole.getId());

            projectRoleGV.removeRelated("ChildProjectRoleActor");

            // delete the role itself
            ofBizDelegator.removeValue(projectRoleGV);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    public ProjectRoleActors getProjectRoleActors(final Long projectRoleId, final Long projectId)
    {
        final Set<ProjectRoleActor> actors = getRoleActors(projectId, projectRoleId);
        return new ProjectRoleActorsImpl(projectId, projectRoleId, actors);
    }

    public void updateProjectRoleActors(final ProjectRoleActors projectRoleActors)
    {
        updateRoleActors(projectRoleActors, projectRoleActors.getProjectId());
    }

    public void updateDefaultRoleActors(final DefaultRoleActors defaultRoleActors)
    {
        updateRoleActors(defaultRoleActors, null);
    }

    public DefaultRoleActors getDefaultRoleActors(final Long projectRoleId)
    {
        final Set<ProjectRoleActor> actors = getRoleActors(null, projectRoleId);
        return new DefaultRoleActorsImpl(projectRoleId, actors);
    }

    public void applyDefaultsRolesToProject(final Project project)
    {
        final Collection<ProjectRole> projectRoles = getAllProjectRoles();
        for (final ProjectRole projectRole : projectRoles)
        {
            // get out all default actors for the role (with a null project)
            final Set<ProjectRoleActor> roleActors = getRoleActors(null, projectRole.getId());
            for (final RoleActor roleActor : roleActors)
            {
                // create a role actor from this for the project
                EntityUtils.createValue(ROLE_ACTOR_ENTITY_NAME, FieldMap.build(
                        ROLE_ACTOR_PID, project.getId(),
                        ROLE_ACTOR_PROJECTROLEID, projectRole.getId(),
                        ROLE_ACTOR_ROLETYPE, roleActor.getType(),
                        "roletypeparameter", roleActor.getParameter()));
            }
        }
    }

    public void removeAllRoleActorsByKeyAndType(final String key, final String type)
    {
        ofBizDelegator.removeByAnd(ROLE_ACTOR_ENTITY_NAME, FieldMap.build(ROLE_ACTOR_PARAMETER, key, ROLE_ACTOR_ROLETYPE, type));
    }

    public void removeAllRoleActorsByProject(final Project project)
    {
        ofBizDelegator.removeByAnd(ROLE_ACTOR_ENTITY_NAME, FieldMap.build(ROLE_ACTOR_PID, project.getId()));
    }

    public Collection<Long> getProjectIdsContainingRoleActorByKeyAndType(final String key, final String type)
    {
        final ArrayList<Long> projectIds = new ArrayList<Long>();

        OfBizListIterator listIterator = null;

        try
        {
            final EntityCondition condition = new EntityFieldMap(FieldMap.build(
                    ROLE_ACTOR_PARAMETER, key,
                    ROLE_ACTOR_ROLETYPE, type),
                    EntityOperator.AND);
            final EntityFindOptions findOptions = new EntityFindOptions();
            findOptions.setDistinct(true);
            listIterator = ofBizDelegator.findListIteratorByCondition(ROLE_ACTOR_ENTITY_NAME, condition, null, ImmutableList.of(ROLE_ACTOR_PID), null,
                findOptions);
            GenericValue projectIdGV = listIterator.next();
            while (projectIdGV != null)
            {
                projectIds.add(projectIdGV.getLong(ROLE_ACTOR_PID));

                // See if we have another status
                projectIdGV = listIterator.next();
            }
        }
        finally
        {
            if (listIterator != null)
            {
                listIterator.close();
            }
        }

        return projectIds;
    }

    public List<Long> roleActorOfTypeExistsForProjects(final List<Long> projectsToLimitBy, final ProjectRole projectRole, final String projectRoleType, final String projectRoleParameter)
    {
        OfBizListIterator listIterator = null;

        try
        {
            final EntityCondition projectIdsClause = new EntityExpr(ROLE_ACTOR_PID, EntityOperator.IN, projectsToLimitBy);
            final EntityCondition otherClause = new EntityFieldMap(FieldMap.build(
                    ROLE_ACTOR_PARAMETER, projectRoleParameter,
                    ROLE_ACTOR_ROLETYPE, projectRoleType,
                    ROLE_ACTOR_PROJECTROLEID, projectRole.getId()),
                    EntityOperator.AND);
            final EntityCondition condition = new EntityExpr(otherClause, EntityOperator.AND, projectIdsClause);
            final EntityFindOptions findOptions = new EntityFindOptions();
            findOptions.setDistinct(true);
            listIterator = ofBizDelegator.findListIteratorByCondition(ROLE_ACTOR_ENTITY_NAME, condition, null, ImmutableList.of(ROLE_ACTOR_PID), null,
                findOptions);
            GenericValue projectIdGV = listIterator.next();
            final List<Long> projectsIn = new ArrayList<Long>();
            while (projectIdGV != null)
            {
                projectsIn.add(projectIdGV.getLong(ROLE_ACTOR_PID));
                projectIdGV = listIterator.next();
            }
            return projectsIn;
        }
        finally
        {
            if (listIterator != null)
            {
                listIterator.close();
            }
        }
    }

    public Map<Long, List<String>> getProjectIdsForUserInGroupsBecauseOfRole(final List<Long> projectsToLimitBy, final ProjectRole projectRole, final String projectRoleType, final String userKey)
    {
        final MultiMap<Long, String, List<String>> groupNamesUserInForProjects = MultiMaps.create(new Supplier<List<String>>()
        {
            public List<String> get()
            {
                return new ArrayList<String>();
            }
        });

        final EntityCondition projectIdsClause = new EntityExpr(ROLE_ACTOR_PID, EntityOperator.IN, projectsToLimitBy);

        final ApplicationUser user = getUser(userKey);
        if (user != null)
        {

            @SuppressWarnings("unchecked")
            final List<String> allGroups = new ArrayList<String>(groupManager.getGroupNamesForUser(user.getDirectoryUser()));

            // We need to batch this query if the groups that the user is in is larger than the max batch size
            final int queryBatchSize = DefaultOfBizDelegator.getQueryBatchSize();

            int index = 0;

            while (index < allGroups.size())
            {
                // Get a sublist (if needed) with the start being our running index and the end being either the
                // batch size + the current index count or the size of the all list.
                final List<String> groups = getSafeSublist(allGroups, index, queryBatchSize + index);

                final EntityCondition groupNamesClause = new EntityExpr(ROLE_ACTOR_PARAMETER, EntityOperator.IN, groups);
                final EntityCondition otherClause = new EntityFieldMap(MapBuilder.build(ROLE_ACTOR_ROLETYPE, projectRoleType, ROLE_ACTOR_PROJECTROLEID,
                        projectRole.getId()), EntityOperator.AND);
                final EntityCondition mainClaus = new EntityExpr(otherClause, EntityOperator.AND, groupNamesClause);

                // if the project list is empty, don't limit by project
                final EntityCondition condition = projectsToLimitBy.isEmpty() ? mainClaus : new EntityExpr(mainClaus, EntityOperator.AND,
                    projectIdsClause);

                final EntityFindOptions findOptions = new EntityFindOptions();
                findOptions.setDistinct(true);

                final OfBizListIterator listIterator = ofBizDelegator.findListIteratorByCondition(ROLE_ACTOR_ENTITY_NAME, condition, null,
                        Arrays.asList(ROLE_ACTOR_PID, ROLE_ACTOR_PARAMETER), null, findOptions);
                try
                {
                    GenericValue projectIdGV = listIterator.next();
                    while (projectIdGV != null)
                    {
                        groupNamesUserInForProjects.putSingle(projectIdGV.getLong(ROLE_ACTOR_PID), projectIdGV.getString(ROLE_ACTOR_PARAMETER));

                        // See if we have another status
                        projectIdGV = listIterator.next();
                    }
                }
                finally
                {
                    if (listIterator != null)
                    {
                        listIterator.close();
                    }
                }

                // Increment our counter
                index += groups.size();
            }
            return groupNamesUserInForProjects;
        }
        else
        {
            log.error("Unable to find user with name: " + userKey);
            throw new IllegalArgumentException("Unable to find user with name: " + userKey);
        }
    }

    private <T> List<T> getSafeSublist(final List<T> originalList, final int start, int end)
    {
        if (start > originalList.size())
        {
            return Collections.emptyList();
        }

        // Make the end the list size if it is greater than the list size
        if (end > originalList.size())
        {
            end = originalList.size();
        }
        return originalList.subList(start, end);
    }

    private Set<ProjectRoleActor> getRoleActors(final Long projectId, final Long projectRoleId)
    {
        try
        {
            UtilTimerStack.push("OfBizProjectRoleAndActorStore.getRoleActors");

            final Set<ProjectRoleActor> actors = new HashSet<ProjectRoleActor>();

            // We are cool with null projects, this will just mean that we are creating a roleActor association that is
            // treated as a default.

            // Get all the related project role actors from the db for the given project
            final Collection<GenericValue> actorGVs = ofBizDelegator.findByAnd(ROLE_ACTOR_ENTITY_NAME, MapBuilder.build(ROLE_ACTOR_PROJECTROLEID,
                projectRoleId, ROLE_ACTOR_PID, projectId));

            // translate all the project role actor gv's into proper objects and populate the role object with these
            for (final GenericValue actorGV : actorGVs)
            {
                // If a we have an actor entry in the db but the actor type plugin has been disabled then we should
                // log the fact and move on
                try
                {
                    // Let the factory determine what impl of RoleActor we need for this db entity
                    final ProjectRoleActor actor = roleActorFactory.createRoleActor(actorGV.getLong(FIELD_ID), projectRoleId, projectId,
                        actorGV.getString(ROLE_ACTOR_TYPE), actorGV.getString(ROLE_ACTOR_PARAMETER));
                    // group all the actors by project
                    actors.add(actor);
                }
                catch (final IllegalArgumentException iae)
                {
                    log.warn("Unable to create a project role actor for type '" + actorGV.getString(ROLE_ACTOR_TYPE) + "'. " + iae.getMessage());
                }
                catch (RoleActorDoesNotExistException ex)
                {
                    log.warn("Unable to create a project role actor. " + ex.getMessage());
                }

            }
            return actors;
        }
        finally
        {
            UtilTimerStack.pop("OfBizProjectRoleAndActorStore.getRoleActors");
        }
    }

    private void updateRoleActors(final DefaultRoleActors projectRoleActors, final Long projectId)
    {
        try
        {
            // We are cool with null projects, this will just mean that we are creating a roleActor association that is
            // treated as a default.

            // Get a collection of all the role actors that are relevant here
            final Set<ProjectRoleActor> existingActors = getRoleActors(projectId, projectRoleActors.getProjectRoleId());

            // Make a copy of all the actors that have been passed in, we will use this set to determine which
            // actors need adding and which need to be removed.
            final Set<RoleActor> actorsToAdd = new HashSet<RoleActor>(projectRoleActors.getRoleActors());

            // Make a copy of existing actors since we will need it to determine the actorsToAdd, remove all the
            // actors passed in from the existing actors, the ones that are left should be the ones removed.
            final Set<ProjectRoleActor> actorsToRemove = new HashSet<ProjectRoleActor>(existingActors);
            actorsToRemove.removeAll(actorsToAdd);

            // To determine the actors to add we just remove all the existing actors from passed in actors, the remainder
            // are the ones to add.
            actorsToAdd.removeAll(existingActors);

            // Delete all the ones to delete
            if (!actorsToRemove.isEmpty())
            {
                final List<Long> ids = new ArrayList<Long>();
                for (final RoleActor roleActor : actorsToRemove)
                {
                    ids.add(roleActor.getId());
                }
                ofBizDelegator.removeByOr(ROLE_ACTOR_ENTITY_NAME, FIELD_ID, ids);
            }

            // Delete all the role actors
            for (final RoleActor roleActor : actorsToAdd)
            {
                EntityUtils.createValue(ROLE_ACTOR_ENTITY_NAME, MapBuilder.<String, Object>build(ROLE_ACTOR_PID, projectId, ROLE_ACTOR_PROJECTROLEID,
                    projectRoleActors.getProjectRoleId(), ROLE_ACTOR_ROLETYPE, roleActor.getType(), "roletypeparameter", roleActor.getParameter()));
            }
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    private ProjectRole convertProjectRoleGVToProjectRoleObject(final GenericValue projectRoleGV) throws GenericEntityException
    {
        ProjectRole role = null;
        if (projectRoleGV != null)
        {
            role = new ProjectRoleImpl(projectRoleGV.getLong(FIELD_ID), projectRoleGV.getString(PROJECT_ROLE_FIELD_NAME),
                projectRoleGV.getString(PROJECT_ROLE_FIELD_DESCRIPTION));
        }
        return role;

    }

    ApplicationUser getUser(final String userKey)
    {
        return ComponentAccessor.getUserManager().getUserByKey(userKey);
    }

}
