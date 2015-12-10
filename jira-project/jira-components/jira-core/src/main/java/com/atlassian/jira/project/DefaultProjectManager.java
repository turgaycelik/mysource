/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.project;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;
import java.util.concurrent.ThreadFactory;
import java.util.regex.Pattern;

import javax.annotation.Nonnull;

import com.atlassian.crowd.embedded.api.User;
import com.atlassian.event.api.EventListener;
import com.atlassian.event.api.EventPublisher;
import com.atlassian.jira.EventComponent;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.avatar.Avatar;
import com.atlassian.jira.avatar.AvatarManager;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.config.properties.PropertiesManager;
import com.atlassian.jira.entity.Delete;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.entity.property.EntityPropertyType;
import com.atlassian.jira.entity.property.JsonEntityPropertyManager;
import com.atlassian.jira.event.ComponentManagerShutdownEvent;
import com.atlassian.jira.event.project.ProjectAvatarUpdateEvent;
import com.atlassian.jira.event.project.ProjectCategoryChangeEvent;
import com.atlassian.jira.event.project.ProjectCategoryUpdateEvent;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueKey;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.comparator.OfBizComparators;
import com.atlassian.jira.issue.security.IssueSecurityLevelManager;
import com.atlassian.jira.ofbiz.FieldMap;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.PrimitiveMap;
import com.atlassian.jira.project.util.ProjectKeyStore;
import com.atlassian.jira.security.roles.ProjectRoleManager;
import com.atlassian.jira.transaction.Transaction;
import com.atlassian.jira.transaction.TransactionSupport;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.util.UserManager;
import com.atlassian.jira.util.collect.CollectionBuilder;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Objects;
import com.google.common.collect.ImmutableList;
import com.google.common.util.concurrent.ThreadFactoryBuilder;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;
import org.ofbiz.core.entity.Transformation;

import static com.atlassian.jira.project.DefaultProjectManager.Model.ASSIGNEE_TYPE_FIELD;
import static com.atlassian.jira.project.DefaultProjectManager.Model.AVATAR_FIELD;
import static com.atlassian.jira.project.DefaultProjectManager.Model.COUNTER_FIELD;
import static com.atlassian.jira.project.DefaultProjectManager.Model.DESCRIPTION_FIELD;
import static com.atlassian.jira.project.DefaultProjectManager.Model.ENTITY_NAME;
import static com.atlassian.jira.project.DefaultProjectManager.Model.ID_FIELD;
import static com.atlassian.jira.project.DefaultProjectManager.Model.KEY_FIELD;
import static com.atlassian.jira.project.DefaultProjectManager.Model.LEAD_FIELD;
import static com.atlassian.jira.project.DefaultProjectManager.Model.NAME_FIELD;
import static com.atlassian.jira.project.DefaultProjectManager.Model.ORIGINAL_KEY_FIELD;
import static com.atlassian.jira.project.DefaultProjectManager.Model.URL_FIELD;
import static com.atlassian.jira.project.ProjectRelationConstants.PROJECT_CATEGORY;
import static com.atlassian.jira.project.ProjectRelationConstants.PROJECT_CATEGORY_ASSOC;
import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.ImmutableSortedMap.copyOf;
import static java.lang.String.CASE_INSENSITIVE_ORDER;
import static java.util.Collections.singletonList;
import static org.apache.commons.lang3.Validate.validState;
import static org.ofbiz.core.entity.EntityOperator.EQUALS;
import static org.ofbiz.core.entity.EntityUtil.getOnly;

/**
 * A class to manage interactions with projects
 */
@EventComponent
public class DefaultProjectManager extends AbstractProjectManager
{
    private static final Logger log = Logger.getLogger(DefaultProjectManager.class);

    public static final String APPLINKS_LOCAL_PROPERTY_PREFIX = "applinks.local";

    static class Model {
        static final String ENTITY_NAME = "Project";
        // Fields from entitymodel.xml
        static final String ID_FIELD = "id";
        static final String NAME_FIELD = "name";
        static final String URL_FIELD = "url";
        static final String LEAD_FIELD = "lead";
        static final String DESCRIPTION_FIELD = "description";
        static final String KEY_FIELD = "key";
        static final String COUNTER_FIELD = "counter";
        static final String ASSIGNEE_TYPE_FIELD = "assigneetype";
        static final String AVATAR_FIELD = "avatar";
        static final String ORIGINAL_KEY_FIELD = "originalkey";
    }

    private final OfBizDelegator delegator;
    private final NodeAssociationStore nodeAssociationStore;
    private final ProjectFactory projectFactory;
    private final ProjectRoleManager projectRoleManager;
    private final IssueManager issueManager;
    private final AvatarManager avatarManager;
    private final ProjectCategoryStore projectCategoryStore;
    private final ProjectKeyStore projectKeyStore;
    private final TransactionSupport transactionSupport;
    private final PropertiesManager propertiesManager;
    private final NextIdGenerator nextIdGenerator;
    private final JsonEntityPropertyManager jsonEntityPropertyManager;
    private final EventPublisher eventPublisher;


    public DefaultProjectManager(final OfBizDelegator delegator, final NodeAssociationStore nodeAssociationStore,
            final ProjectFactory projectFactory, final ProjectRoleManager projectRoleManager, final IssueManager issueManager,
            AvatarManager avatarManager, UserManager userManager,
            ProjectCategoryStore projectCategoryStore, ApplicationProperties applicationProperties,
            ProjectKeyStore projectKeyStore, TransactionSupport transactionSupport,
            final PropertiesManager propertiesManager, JsonEntityPropertyManager jsonEntityPropertyManager,
            final EventPublisher eventPublisher)
    {
        super(userManager, applicationProperties);
        this.delegator = delegator;
        this.nodeAssociationStore = nodeAssociationStore;
        this.projectFactory = projectFactory;
        this.projectRoleManager = projectRoleManager;
        this.issueManager = issueManager;
        this.avatarManager = avatarManager;
        this.projectCategoryStore = projectCategoryStore;
        this.projectKeyStore = projectKeyStore;
        this.transactionSupport = transactionSupport;
        this.propertiesManager = propertiesManager;
        this.jsonEntityPropertyManager = jsonEntityPropertyManager;
        this.nextIdGenerator = new NextIdGenerator(delegator, issueManager);
        this.eventPublisher = eventPublisher;
    }

    @Override
    public Project createProject(final String name, final String key, final String description, final String leadKey,
            final String url, final Long assigneeType, Long avatarId)
    {
        notNull(KEY_FIELD, key);
        notNull(NAME_FIELD, name);
        notNull(LEAD_FIELD, leadKey);
        if (avatarId == null)
        {
            avatarId = avatarManager.getDefaultAvatarId(Avatar.Type.PROJECT);
        }

        Transaction transaction = transactionSupport.begin();
        try
        {
            final Map<String, Object> params = new PrimitiveMap.Builder()
                    .add(KEY_FIELD, key)
                    .add(ORIGINAL_KEY_FIELD, key)
                    .add(NAME_FIELD, name)
                    .add(URL_FIELD, url)
                    .add(LEAD_FIELD, leadKey)
                    .add(DESCRIPTION_FIELD, description)
                    .add(COUNTER_FIELD, 0L)
                    .add(ASSIGNEE_TYPE_FIELD, assigneeType)
                    .add(AVATAR_FIELD, avatarId)
                    .toMap();
            final GenericValue projectGV = delegator.createValue(ENTITY_NAME, params);

            final Project newProject = new ProjectImpl(projectGV);

            projectKeyStore.addProjectKey(newProject.getId(), newProject.getKey());

            // Give the project role manager a chance to assign the project role default actors to this new project
            projectRoleManager.applyDefaultsRolesToProject(newProject);
            transaction.commit();

            return newProject;
        }
        finally
        {
            transaction.finallyRollbackIfNotCommitted();
            // JRA-39710
            //
            // Once the project is created on the DB, but before the transaction is committed, the CachingProjectKeyStore receives a call to addProjectKey,
            // which would internally clean all the values of its cache.
            //
            // If in that moment (when the project key store cache is empty and before the transaction is committed) another thread is scheduled and
            // accesses the CachingProjectKeyStore, the cache would get repopulated. But the new project won't be present in the cache (since the
            // transaction that created it has not been committed yet, and we are executing in a completely different thread that has no visibility over that transaction).
            //
            // Once the transaction is committed under this scenario, the new project would not exist for the CachingProjectKeyStore, until the cache is refreshed again.
            // In order to avoid this, we need to refresh the project key store cache once the transaction is committed, so whomever accesses the cache again it gets the most up to date data.
            projectKeyStore.refresh();
        }
    }

    // Create Methods --------------------------------------------------------------------------------------------------

    @Override
    public long getNextId(final Project project)
    {
        return nextIdGenerator.getNextId(project);
    }

    @Override
    public Project updateProject(final Project updatedProject, final String name, final String description,
            final String leadKey, final String url, final Long assigneeType, Long avatarId, final String projectKey)
    {
        notNull(ENTITY_NAME, updatedProject);
        notNull(NAME_FIELD, name);
        notNull(LEAD_FIELD, leadKey);

        ProjectAvatarUpdateEvent projectAvatarUpdateEvent = null;

        Transaction transaction = transactionSupport.begin();
        try
        {
            // Make a fresh Project GV and only add a subset of fields because we don't want to overwrite "counter"
            final GenericValue projectUpdate = delegator.makeValue(ENTITY_NAME);

            projectUpdate.set(ID_FIELD, updatedProject.getId());
            projectUpdate.setString(NAME_FIELD, name);
            projectUpdate.setString(URL_FIELD, url);
            projectUpdate.setString(LEAD_FIELD, leadKey);
            projectUpdate.setString(DESCRIPTION_FIELD, description);
            projectUpdate.set(ASSIGNEE_TYPE_FIELD, assigneeType);
            if (avatarId != null)
            {
                projectUpdate.set(AVATAR_FIELD, avatarId);
                if (updatedProject.getAvatar() == null ||
                        !avatarId.equals(updatedProject.getAvatar().getId())) {
                    projectAvatarUpdateEvent = new ProjectAvatarUpdateEvent(updatedProject, avatarId);
                }
            }
            if (projectKey != null && !updatedProject.getKey().equals(projectKey))
            {
                projectUpdate.setString(KEY_FIELD, projectKey);
                final Long projectId = projectKeyStore.getProjectId(projectKey);
                if (projectId == null)
                {
                    // Record that the new project key is taken
                    projectKeyStore.addProjectKey(updatedProject.getId(), projectKey);
                }
                else if (!projectId.equals(updatedProject.getId()))
                {
                    //this is defensive check against bypassing validation and inconsistent
                    //entries in ProjectKey pointing to non-existent project
                    throw new RuntimeException("Key " + projectKey + " already used by project: " + projectId);
                }
                updateEntityLinks(updatedProject.getKey(), projectKey);
            }

            // Store the partial update
            delegator.store(projectUpdate);

            transaction.commit();
        }
        finally
        {
            transaction.finallyRollbackIfNotCommitted();
        }

        // JRA-18152: must clear the issue security level cache so that if project lead has changed, user permissions are recalculated
        getIssueSecurityLevelManager().clearUsersLevels();

        if (projectAvatarUpdateEvent != null )
        {
            eventPublisher.publish(projectAvatarUpdateEvent);
        }

        return getProjectObj(updatedProject.getId());
    }

    @VisibleForTesting
    void updateEntityLinks(final String oldKey, final String newKey)
    {
        final Collection<String> oldPropertyKeys = getEntityLinkKeys(oldKey);
        for (final String oldPropertyKey : oldPropertyKeys)
        {
            final String value = propertiesManager.getPropertySet().getText(oldPropertyKey);
            final String newPropertyKey = oldPropertyKey
                    .replaceFirst("^" + Pattern.quote(prefix(oldKey)), prefix(newKey));
            propertiesManager.getPropertySet().setText(newPropertyKey, value);
            propertiesManager.getPropertySet().remove(oldPropertyKey);
        }
    }

    @SuppressWarnings ("unchecked")
    private Collection<String> getEntityLinkKeys(final String oldKey)
    {
        return propertiesManager.getPropertySet().getKeys(prefix(oldKey));
    }

    private String prefix(final String key)
    {
        return APPLINKS_LOCAL_PROPERTY_PREFIX + "." + key + ".";
    }


    @Override
    public void removeProjectIssues(final Project project) throws RemoveException
    {
        notNull(ENTITY_NAME, project);

        final Collection<Long> issueIds;
        try
        {
            issueIds = issueManager.getIssueIdsForProject(project.getId());
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        for (final Long issueId : issueIds)
        {
            final Issue issue = issueManager.getIssueObject(issueId);
            // We have retrieved all issue ids for the project.
            if (issue != null)
            {
                try
                {
                    issueManager.deleteIssueNoEvent(issue);
                }
                catch (final Exception e)
                {
                    log.error("Exception removing issues", e);
                    throw new RemoveException("Error removing issues: " + e, e);
                }
            }
            else
            {
                log.debug("Issue with id '" + issueId + "' was not found."
                        + " Most likely it is a sub-task and has been deleted previously with its parent.");
            }
        }
    }

    @Override
    public void removeProject(final Project project)
    {
        notNull(ENTITY_NAME, project);

        // Remove all project role associations for this project from the projectRoleManager
        projectRoleManager.removeAllRoleActorsByProject(project);

        projectKeyStore.deleteProjectKeys(project.getId());

        jsonEntityPropertyManager.deleteByEntity(EntityPropertyType.PROJECT_PROPERTY.getDbEntityName(), project.getId());

        // remove the project itself
        Delete.from(ENTITY_NAME)
                .whereIdEquals(project.getId())
                .execute(delegator);
    }

    @Override
    public GenericValue getProject(final Long id)
    {
        return getDelegator().findById(ENTITY_NAME, id);
    }

    @Override
    public Project getProjectObj(final Long id)
    {
        Project project = null;
        final GenericValue gv = getProject(id);
        if (gv != null)
        {
            project = projectFactory.getProject(gv);
        }
        return project;
    }

    @Override
    public GenericValue getProjectByName(final String name)
    {
        return getOnly(getDelegator().findByAnd(ENTITY_NAME, FieldMap.build(NAME_FIELD, name)));
    }

    @Override
    public GenericValue getProjectByKey(final String key)
    {
        final GenericValue gv = getOnly(getDelegator().findByAnd(ENTITY_NAME, FieldMap.build(KEY_FIELD, key)));
        if (gv != null)
        {
            return gv;
        }
        final Long projectId = projectKeyStore.getProjectId(key);
        return projectId != null ? getProject(projectId) : null;
    }

    @Override
    public Project getProjectByCurrentKey(final String projectKey)
    {
        final GenericValue gv = getOnly(getDelegator().findByAnd(ENTITY_NAME, FieldMap.build(KEY_FIELD, projectKey)));
        if (gv != null)
        {
            return projectFactory.getProject(gv);
        }
        return null;
    }

    @Override
    public Project getProjectObjByKey(final String projectKey)
    {
        Project project = null;
        final GenericValue projectGv = getProjectByKey(projectKey);
        if (projectGv != null)
        {
            project = projectFactory.getProject(projectGv);
        }
        return project;
    }

    @Override
    public Project getProjectByCurrentKeyIgnoreCase(final String projectKey)
    {
        Project project = null;
        final GenericValue projectGv = getProjectByKey(projectKey);
        if (projectGv == null)
        {
            // Try to run through all the projects and compare on the key
            for (Project prj : getProjectObjects())
            {
                if (prj.getKey().equalsIgnoreCase(projectKey))
                {
                    project = prj;
                    break;
                }
            }
        }
        else
        {
            project = projectFactory.getProject(projectGv);
        }
        return project;
    }

    @Override
    public Project getProjectObjByKeyIgnoreCase(final String projectKey)
    {
        final Map<String, Long> projectKeys = copyOf(projectKeyStore.getAllProjectKeys(), CASE_INSENSITIVE_ORDER);
        final Long projectId = projectKeys.get(projectKey);
        if (projectId != null)
        {
            final GenericValue projectGv = getProject(projectId);
            return projectGv != null ? projectFactory.getProject(projectGv) : null;
        }
        return null;
    }

    @Override
    public Set<String> getAllProjectKeys(Long projectId)
    {
        return projectKeyStore.getProjectKeys(projectId);
    }

    @Override
    public Project getProjectObjByName(final String projectName)
    {
        Project project = null;
        final GenericValue projectGv = getProjectByName(projectName);
        if (projectGv != null)
        {
            project = projectFactory.getProject(projectGv);
        }
        return project;
    }

    /**
     * @deprecated use ProjectComponentManager instead
     */
    @Deprecated
    @Override
    public GenericValue getComponent(final Long id)
    {
        return getDelegator().findById("Component", id);
    }

    /**
     * @deprecated use ProjectComponentManager instead
     */
    @Deprecated
    @Override
    public GenericValue getComponent(final GenericValue project, final String name)
    {
        // don't bother caching - only used when creating components anyway
        return getOnly(getDelegator().findByAnd("Component", FieldMap.build("project", project.getLong(ID_FIELD), NAME_FIELD, name)));
    }

    /**
     * @deprecated use ProjectComponentManager instead
     */
    @Deprecated
    @Override
    public Collection<GenericValue> getComponents(final GenericValue project)
    {
        try
        {
            return project.getRelated("ChildComponent",
                    Collections.<String, Object>emptyMap(), singletonList(NAME_FIELD));
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    /**
     * Return all project {@link GenericValue}s.
     */
    @Override
    public Collection<GenericValue> getProjects()
    {
        final List<GenericValue> allProjects = getDelegator().findAll(ENTITY_NAME, singletonList(NAME_FIELD));
        Collections.sort(allProjects, OfBizComparators.NAME_COMPARATOR); // Fixes JRA-1246
        return allProjects;
    }

    @Override
    public List<Project> getProjectObjects() throws DataAccessException
    {
        final Collection<GenericValue> projectGVs = getProjects();
        return projectFactory.getProjects(projectGVs);
    }

    @Override
    public long getProjectCount() throws DataAccessException
    {
        return getDelegator().getCount(ENTITY_NAME);
    }

    // Business Logic Methods ------------------------------------------------------------------------------------------

    protected OfBizDelegator getDelegator()
    {
        return delegator;
    }

    @Override
    public Collection<GenericValue> getProjectCategories()
    {
        return getDelegator().findAll("ProjectCategory", singletonList(NAME_FIELD));
    }

    @Override
    public List<ProjectCategory> getAllProjectCategories()
    {
        return projectCategoryStore.getAllProjectCategories();
    }

    @Override
    public GenericValue getProjectCategory(final Long id)
    {
        return getDelegator().findByPrimaryKey("ProjectCategory", id);
    }

    @Override
    public ProjectCategory getProjectCategoryObject(final Long id)
    {
        return projectCategoryStore.getProjectCategory(id);
    }

    @Override
    public void updateProjectCategory(final GenericValue projectCat)
    {
        getDelegator().storeAll(CollectionBuilder.newBuilder(projectCat).asList());
    }

    @Override
    public void updateProjectCategory(@Nonnull ProjectCategory projectCategory) throws DataAccessException
    {
        final ProjectCategory oldProjectCategory = projectCategoryStore.getProjectCategory(projectCategory.getId());

        if (!Objects.equal(oldProjectCategory.getName(), projectCategory.getName()) ||
                !Objects.equal(oldProjectCategory.getDescription(), projectCategory.getDescription()))
        {
            projectCategoryStore.updateProjectCategory(projectCategory);
            eventPublisher.publish(new ProjectCategoryUpdateEvent(oldProjectCategory, projectCategory));
        }
    }

    /**
     * Gather a list of projects that are in a project category.
     *
     * @param projectCategory Project to look up against
     * @return Collection of Projects
     */
    @Override
    public Collection<GenericValue> getProjectsFromProjectCategory(final GenericValue projectCategory)
    {
        if (null == projectCategory)
        {
            return Collections.emptyList();
        }

        final List<GenericValue> projects =
                nodeAssociationStore.getSourcesFromSink(projectCategory, ENTITY_NAME, PROJECT_CATEGORY);

        //alphabetic order on the project name
        Collections.sort(projects, OfBizComparators.NAME_COMPARATOR);
        return projects;
    }

    @Override
    public Collection<Project> getProjectsFromProjectCategory(ProjectCategory projectCategory)
            throws DataAccessException
    {
        return getProjectObjectsFromProjectCategory(projectCategory.getId());
    }

    @Override
    public Collection<Project> getProjectObjectsFromProjectCategory(final Long projectCategoryId)
    {
        return projectFactory.getProjects(getProjectsFromProjectCategory(getProjectCategory(projectCategoryId)));
    }

    /**
     * Gets a list of projects that are not associated with any project category
     */
    @Override
    public Collection<GenericValue> getProjectsWithNoCategory()
    {
        final List<GenericValue> result = new ArrayList<GenericValue>();
        for (final GenericValue project : getProjects())
        {
            if (getProjectCategoryFromProject(project) == null)
            {
                result.add(project);
            }
        }

        //alphabetic order on the project name
        Collections.sort(result, OfBizComparators.NAME_COMPARATOR);
        return result;
    }

    @Override
    public Collection<Project> getProjectObjectsWithNoCategory() throws DataAccessException
    {
        return projectFactory.getProjects(getProjectsWithNoCategory());
    }

    /**
     * Get the Project Category  given a Project.
     *
     * @param project Project
     * @return Project Category
     */
    @Override
    public GenericValue getProjectCategoryFromProject(final GenericValue project)
    {
        if (project == null)
        {
            return null;
        }

        final List<GenericValue> projectCats =
                nodeAssociationStore.getSinksFromSource(project, "ProjectCategory", PROJECT_CATEGORY);

        if (projectCats == null || projectCats.isEmpty())
        {
            return null;
        }

        return projectCats.iterator().next();
    }

    @Override
    public ProjectCategory getProjectCategoryForProject(final Project project) throws DataAccessException
    {
        if (project == null)
        {
            return null;
        }

        final List<GenericValue> projectCats = nodeAssociationStore.getSinksFromSource(
                ENTITY_NAME, project.getId(), "ProjectCategory", PROJECT_CATEGORY);

        if ((null == projectCats) || projectCats.isEmpty())
        {
            return null;
        }

        return Entity.PROJECT_CATEGORY.build(projectCats.get(0));
    }

    @Override
    public ProjectCategory createProjectCategory(String name, String description)
    {
        return projectCategoryStore.createProjectCategory(name, description);
    }

    @Override
    public void removeProjectCategory(Long id)
    {
        projectCategoryStore.removeProjectCategory(id);
    }

    /**
     * If <code>category</code> is non-null, set <code>project</code>'s Project Category to <code>category</code>. If
     * <code>category</code> is null, remove <code>project</code>'s Project Category association, if one exists.
     */
    @Override
    public void setProjectCategory(final GenericValue projectGV, final GenericValue projectCategoryGV)
    {
        if (projectGV == null)
        {
            throw new IllegalArgumentException("Cannot associate a category with a null project");
        }

        ProjectCategory projectCategory = null;

        if (projectCategoryGV != null)
        {
            projectCategory = new ProjectCategoryImpl(
                    projectCategoryGV.getLong("id"),
                    projectCategoryGV.getString("name"),
                    projectCategoryGV.getString("description"));
        }

        setProjectCategory(new ProjectImpl(projectGV), projectCategory);
    }

    @Override
    public void setProjectCategory(final Project project, final ProjectCategory projectCategory)
    {
        if (project == null)
        {
            throw new IllegalArgumentException("Cannot associate a category with a null project");
        }

        final ProjectCategory oldProjectCategory = getProjectCategoryForProject(project);

        //when category was not changed, do not perform unecessery actions
        if (projectCategory != null && oldProjectCategory != null &&
                Objects.equal(projectCategory.getId(), oldProjectCategory.getId()))
        {
            return;
        }

        final ProjectCategoryChangeEvent.Builder eventBuilder = new ProjectCategoryChangeEvent.Builder(project);

        if (oldProjectCategory != null)
        {
            nodeAssociationStore.removeAssociation(PROJECT_CATEGORY_ASSOC, project.getId(), oldProjectCategory.getId());
            eventBuilder.addOldCategory(oldProjectCategory);
        }

        if (projectCategory != null)
        {
            nodeAssociationStore.createAssociation(PROJECT_CATEGORY_ASSOC, project.getId(), projectCategory.getId());
            eventBuilder.addNewCategory(projectCategory);
        }

        if(eventBuilder.canBePublished())
        {
            eventPublisher.publish(eventBuilder.build());
        }
    }

    @Override
    public List<Project> getProjectsLeadBy(User leadUser)
    {
        List<GenericValue> projects = findProjectsByLead(leadUser);
        return projectFactory.getProjects(projects);
    }

    @Override
    public List<Project> getProjectsLeadBy(ApplicationUser leadUser)
    {
        return getProjectsLeadBy(leadUser.getDirectoryUser());
    }

    @Override
    public final Collection<GenericValue> getProjectsByLead(final User leadUser)
    {
        return findProjectsByLead(leadUser);
    }

    private List<GenericValue> findProjectsByLead(final User leadUser)
    {
        if (leadUser == null)
        {
            return Collections.emptyList();
        }
        ApplicationUser leadAppUser = userManager.getUserByName(leadUser.getName());
        if (leadAppUser == null)
        {
            return Collections.emptyList();
        }
        // ordering by name of project
        return getDelegator().findByAnd(
                ENTITY_NAME, FieldMap.build(LEAD_FIELD, leadAppUser.getKey()), singletonList(NAME_FIELD));
    }

    @Override
    public void refresh()
    {
    }

    IssueSecurityLevelManager getIssueSecurityLevelManager()
    {
        return ComponentAccessor.getComponentOfType(IssueSecurityLevelManager.class);
    }

    @Override
    public long getCurrentCounterForProject(Long id)
    {
        return nextIdGenerator.getCurrentCounterForProject(id);
    }

    @Override
    public void setCurrentCounterForProject(Project project, long counter)
    {
        nextIdGenerator.resetCounter(project, counter);
    }

    /**
     * Responsible for generating the numerical part of the next Issue key for a given project.
     */
    static class NextIdGenerator
    {
        private static EntityCondition getProjectIdEqualsCondition(final long projectId)
        {
            return new EntityExpr(ID_FIELD, EQUALS, projectId);
        }

        private static ExecutorService createSelfCleaningExecutorService()
        {
            // We create daemon threads so that JIRA can shut down without waiting for these threads to age out
            final ThreadFactory threadFactory = new ThreadFactoryBuilder()
                    .setDaemon(true)
                    .setNameFormat("ProjectCounterUpdateThread-%d")
                    .build();
            return Executors.newCachedThreadPool(threadFactory);
        }

        private static long getNonNullCounter(final GenericValue project)
        {
            final Long counterObject = project.getLong(COUNTER_FIELD);
            return counterObject == null ? 0 : counterObject;
        }

        private final OfBizDelegator ofBizDelegator;
        private final IssueManager issueManager;
        private final ExecutorService executor;

        NextIdGenerator(final OfBizDelegator delegator, final IssueManager issueManager)
        {
            this.executor = createSelfCleaningExecutorService();
            this.ofBizDelegator = delegator;
            this.issueManager = issueManager;
        }

        long getNextId(final Project project)
        {
            if (project == null)
            {
                throw new IllegalArgumentException();
            }

            final EntityCondition projectIdCondition = getProjectIdEqualsCondition(project.getId());

            try
            {
                long nextId;
                do
                {
                    /**
                     * We want the obtaining of an issue ID to happen in a new short-running transaction,
                     * so that any existing transaction doesn't indefinitely lock the row in the Project
                     * table, which can lead to deadlocks between different threads creating issues.
                     *
                     * Unfortunately the simplest way to enforce a new transaction given the current OfBiz
                     * infrastructure is to start a new thread and wait for it to complete.
                     */
                    final Future<Long> issueIdFuture = executor.submit(new Callable<Long>()
                    {
                        @Override
                        public Long call() throws Exception
                        {
                            final GenericValue updatedProject = ofBizDelegator.transformOne(
                                    ENTITY_NAME, projectIdCondition, COUNTER_FIELD, new Transformation()
                            {
                                @Override
                                public void transform(final GenericValue entity)
                                {
                                    final long currentCounter = getNonNullCounter(entity);
                                    entity.set(COUNTER_FIELD, currentCounter + 1);
                                }
                            });
                            return getNonNullCounter(updatedProject);
                        }
                    });
                    nextId = issueIdFuture.get();
                }
                while (counterAlreadyExists(nextId, project));
                return nextId;
            }
            catch (final Exception e)
            {
                throw new RuntimeException(e);
            }
        }

        long getCurrentCounterForProject(final long projectId)
        {
            final List<GenericValue> gvs = ofBizDelegator.findByCondition(
                    ENTITY_NAME, getProjectIdEqualsCondition(projectId), ImmutableList.of(COUNTER_FIELD));
            validState(gvs.size() <= 1, "Expected at most one Project with ID %d but found these: %s", projectId, gvs);
            return gvs.isEmpty() ? 0L : getNonNullCounter(gvs.get(0));
        }

        /**
         * This is a sanity check to ensure that we are only giving out project keys that haven't already been given out.
         * <p> In an ideal world, this should never return true. </p> Note that this method isn't guaranteed to avoid
         * duplicates, as it will only work if the Issue has already been inserted in the DB.
         *
         * @param incCount the suggested Issue number
         * @param project The project
         * @return true if this Issue Key already exists in the DB.
         */
        private boolean counterAlreadyExists(final long incCount, final Project project) throws GenericEntityException
        {
            final String issueKey = IssueKey.format(project, incCount);
            final boolean alreadyExists = issueManager.isExistingIssueKey(issueKey);
            if (alreadyExists)
            {
                log.warn("Existing issue found for key " + issueKey + ". Incrementing key.");
            }
            return (alreadyExists);
        }

        void resetCounter(final Project project, final long counter)
        {
            final EntityCondition projectIdCondition = getProjectIdEqualsCondition(project.getId());
            ofBizDelegator.transformOne(ENTITY_NAME, projectIdCondition, COUNTER_FIELD,
                    new Transformation()
                    {
                        @Override
                        public void transform(final GenericValue entity)
                        {
                            entity.set(COUNTER_FIELD, counter);
                        }
                    });
        }

        public void shutdown()
        {
            executor.shutdownNow();
        }
    }

    @EventListener
    public void shutdown(final ComponentManagerShutdownEvent shutdownEvent)
    {
        nextIdGenerator.shutdown();
    }
}
