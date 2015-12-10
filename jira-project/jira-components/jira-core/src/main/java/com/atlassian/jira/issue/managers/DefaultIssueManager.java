/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.issue.managers;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import javax.annotation.Nonnull;
import javax.annotation.Nullable;

import com.atlassian.core.util.map.EasyMap;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.association.NodeAssociationStore;
import com.atlassian.jira.association.UserAssociationStore;
import com.atlassian.jira.component.ComponentAccessor;
import com.atlassian.jira.entity.Entity;
import com.atlassian.jira.event.issue.IssueEventSource;
import com.atlassian.jira.event.type.EventDispatchOption;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.ActionConstants;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFactory;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueKey;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.IssueRelationConstants;
import com.atlassian.jira.issue.IssueUtils;
import com.atlassian.jira.issue.ModifiedValue;
import com.atlassian.jira.issue.MutableIssue;
import com.atlassian.jira.issue.UpdateIssueRequest;
import com.atlassian.jira.issue.comparator.IssueKeyComparator;
import com.atlassian.jira.issue.fields.FieldManager;
import com.atlassian.jira.issue.fields.OrderableField;
import com.atlassian.jira.issue.fields.TextFieldCharacterLengthValidator;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutItem;
import com.atlassian.jira.issue.fields.layout.field.FieldLayoutManager;
import com.atlassian.jira.issue.search.parameters.filter.NoBrowsePermissionPredicate;
import com.atlassian.jira.issue.util.DefaultIssueChangeHolder;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.issue.util.IssueUpdater;
import com.atlassian.jira.issue.util.MovedIssueKeyStore;
import com.atlassian.jira.ofbiz.DefaultOfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizDelegator;
import com.atlassian.jira.ofbiz.OfBizListIterator;
import com.atlassian.jira.project.Project;
import com.atlassian.jira.project.util.ProjectKeyStore;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.user.ApplicationUser;
import com.atlassian.jira.user.ApplicationUsers;
import com.atlassian.jira.util.collect.MapBuilder;
import com.atlassian.jira.util.lang.Pair;
import com.atlassian.jira.workflow.WorkflowException;
import com.atlassian.jira.workflow.WorkflowFunctionUtils;
import com.atlassian.jira.workflow.WorkflowManager;
import com.atlassian.util.profiling.UtilTimerStack;

import com.google.common.annotations.VisibleForTesting;
import com.google.common.base.Function;
import com.google.common.collect.ImmutableList;
import com.google.common.collect.ImmutableMap;
import com.google.common.collect.ImmutableSet;
import com.google.common.collect.Lists;
import com.opensymphony.workflow.InvalidInputException;

import org.ofbiz.core.entity.EntityCondition;
import org.ofbiz.core.entity.EntityConditionList;
import org.ofbiz.core.entity.EntityExpr;
import org.ofbiz.core.entity.EntityFieldMap;
import org.ofbiz.core.entity.EntityOperator;
import org.ofbiz.core.entity.EntityUtil;
import org.ofbiz.core.entity.GenericEntityException;
import org.ofbiz.core.entity.GenericValue;

import static com.atlassian.jira.util.dbc.Assertions.notNull;
import static com.google.common.collect.Collections2.transform;
import static org.ofbiz.core.entity.EntityFindOptions.findOptions;

public class DefaultIssueManager implements IssueManager
{
    private static final String ISSUE_MAP_KEY = "issue";
    private final OfBizDelegator ofBizDelegator;
    private final WorkflowManager workflowManager;
    private final NodeAssociationStore nodeAssociationStore;
    private final UserAssociationStore userAssociationStore;
    private final IssueUpdater issueUpdater;
    private IssueDeleteHelper issueDeleteHelper;
    private FieldManager fieldManager;
    private FieldLayoutManager fieldLayoutManager;
    private final PermissionManager permissionManager;
    private final MovedIssueKeyStore movedIssueKeyStore;
    private final ProjectKeyStore projectKeyStore;
    private final Map<String, Object> unassignedCondition = Collections.singletonMap("assignee", null);
    private final TextFieldCharacterLengthValidator textFieldCharacterLengthValidator;

    private final IssueFinder issueFinder = new IssueFinder()
    {
        @Override
        protected Long getProjectByKey(final String projectKey)
        {
            return projectKeyStore.getProjectId(projectKey);
        }
    };

    private final IssueFinder caseInsensitiveIssueFinder = new IssueFinder()
    {
        @Override
        protected Long getProjectByKey(final String projectKey)
        {
            return projectKeyStore.getProjectIdByKeyIgnoreCase(projectKey);
        }
    };

    public DefaultIssueManager(OfBizDelegator ofBizDelegator, final WorkflowManager workflowManager,
            final NodeAssociationStore nodeAssociationStore,
            UserAssociationStore userAssociationStore, final IssueUpdater issueUpdater,
            final PermissionManager permissionManager,
            final MovedIssueKeyStore movedIssueKeyStore, final ProjectKeyStore projectKeyStore, final TextFieldCharacterLengthValidator textFieldCharacterLengthValidator)
    {
        this.ofBizDelegator = ofBizDelegator;
        this.workflowManager = workflowManager;
        this.nodeAssociationStore = nodeAssociationStore;
        this.userAssociationStore = userAssociationStore;
        this.issueUpdater = issueUpdater;
        this.permissionManager = permissionManager;
        this.movedIssueKeyStore = movedIssueKeyStore;
        this.projectKeyStore = projectKeyStore;
        this.textFieldCharacterLengthValidator = textFieldCharacterLengthValidator;
    }

    // Get / Finder Methods --------------------------------------------------------------------------------------------
    public GenericValue getIssue(final Long id) throws DataAccessException
    {
        if (id == null)
        {
            return null; // JRA-17080
        }
        return ofBizDelegator.findById(Entity.Name.ISSUE, id);
    }

    public GenericValue getIssue(final String key) throws GenericEntityException
    {
        return getIssueFinder().getIssue(key);
    }

    @Override
    public boolean isExistingIssueKey(final String issueKey) throws GenericEntityException
    {
        return getIssue(issueKey) != null;
    }

    @Override
    public List<GenericValue> getIssues(final Collection<Long> ids)
    {
        if (ids.isEmpty())
        {
            return Collections.emptyList();
        }

        // Long SQL queries cause databases to blow up as they run out of resources
        // So retrieve issues in batches
        List<EntityExpr> entityExpressions = new ArrayList<EntityExpr>();
        List<GenericValue> unsortedIssues = null;
        final int batchSize = DefaultOfBizDelegator.getQueryBatchSize();
        int i = 0;
        for (final Long issueId : ids)
        {
            i++;
            entityExpressions.add(new EntityExpr("id", EntityOperator.EQUALS, issueId));

            if (i >= batchSize)
            {
                // Get the batch from the database
                if (unsortedIssues == null)
                {
                    // Save a call to addAll() if we can so that we do not iterate over the returned list unless we have to
                    unsortedIssues = ofBizDelegator.findByOr(Entity.Name.ISSUE, entityExpressions, null);
                }
                else
                {
                    unsortedIssues.addAll(ofBizDelegator.findByOr(Entity.Name.ISSUE, entityExpressions, null));
                }

                // Reset the query and the counter
                entityExpressions = new ArrayList<EntityExpr>();
                i = 0;
            }
        }

        // If we have some more issues to retrieve then do it
        if (!entityExpressions.isEmpty())
        {
            if (unsortedIssues == null)
            {
                // Save a call to addAll() if we can so that we do not iterate over the returned list unless we have to
                unsortedIssues = ofBizDelegator.findByOr(Entity.Name.ISSUE, entityExpressions, null);
            }
            else
            {
                unsortedIssues.addAll(ofBizDelegator.findByOr(Entity.Name.ISSUE, entityExpressions, null));
            }
        }

        return getIssuesSortedByIds(unsortedIssues, ids);
    }

    @Override
    public List<Issue> getIssueObjects(Collection<Long> ids)
    {
        // Get Issues returns a list of GVs that may contain embedded nulls, We don't
        // turn these into IssueObjects or return them as null either.
        List<Issue> issues = new ArrayList<Issue>(ids.size());
        for (GenericValue gv : getIssues(ids))
        {
            if (gv != null)
            {
                issues.add(getIssueFactory().getIssue(gv));
            }
        }
        return issues;
    }

    /**
     * Retrieve a collection of all issue ids that belong to a given project.
     *
     * @param projectId the id of the project for which to retrieve all issue ids
     */
    public Collection<Long> getIssueIdsForProject(final Long projectId) throws GenericEntityException
    {
        if (projectId == null)
        {
            throw new NullPointerException("Project Id cannot be null.");
        }

        // JRA-6987 - do not retrieve all issues at once - use iterator to iterate over each issue id
        OfBizListIterator issueIterator = null;
        final Collection<Long> issueIds = new ArrayList<Long>();

        try
        {
            issueIterator = ofBizDelegator.findListIteratorByCondition(Entity.Name.ISSUE,
                    new EntityFieldMap(ImmutableMap.of("project", projectId), EntityOperator.AND), null,
                    ImmutableList.of("id"), null, null);
            GenericValue issueIdGV = (GenericValue) issueIterator.next();
            // As documented in org.ofbiz.core.entity.EntityListIterator.hasNext() the best way to find out
            // if there are any results left in the iterator is to iterate over it until null is returned
            // (i.e. not use hasNext() method)
            // The documentation mentions efficiency only - but the functionality is totally broken when using
            // hsqldb JDBC drivers (hasNext() always returns true).
            // So listen to the OfBiz folk and iterate until null is returned.
            while (issueIdGV != null)
            {
                // record the issue id
                issueIds.add(issueIdGV.getLong("id"));
                // See if we have another issue
                issueIdGV = (GenericValue) issueIterator.next();
            }
        }
        finally
        {
            if (issueIterator != null)
            {
                issueIterator.close();
            }
        }

        return issueIds;
    }

    public long getIssueCountForProject(final Long projectId)
    {
        notNull("projectId", projectId);

        long count;
        final EntityCondition condition = new EntityFieldMap(ImmutableMap.of("project", projectId), EntityOperator.AND);
        final GenericValue countGV = EntityUtil.getOnly(ofBizDelegator.findByCondition("IssueCount", condition,
                Collections.singletonList("count"), Collections.<String>emptyList()));
        count = countGV.getLong("count");

        return count;
    }

    @Override
    public boolean hasUnassignedIssues()
    {
        OfBizListIterator unassignedIssuesIt = ofBizDelegator.findListIteratorByCondition(Entity.Name.ISSUE,
                new EntityFieldMap(unassignedCondition, EntityOperator.EQUALS), null, ImmutableList.of("id"),
                ImmutableList.of("id"), findOptions().maxResults(1));
        try
        {
            return unassignedIssuesIt.next() != null;
        }
        finally
        {
            unassignedIssuesIt.close();
        }
    }

    @Override
    public long getUnassignedIssueCount()
    {
        List<GenericValue> unassignedCount = ofBizDelegator.findByCondition("IssueCountByAssignee",
                new EntityFieldMap(unassignedCondition, EntityOperator.EQUALS), ImmutableList.of("count"), null);
        return EntityUtil.getOnly(unassignedCount).getLong("count");
    }

    @Override
    public long getIssueCount()
    {
        try
        {
            return ofBizDelegator.getDelegatorInterface().countAll(Entity.Name.ISSUE);
        }
        catch (GenericEntityException e)
        {
            throw new RuntimeException(e);
        }
    }

    /**
     * Public method used internally, not safe to override.
     */
    @Override
    public final Issue findMovedIssue(final String originalKey)
    {
        notNull("originalKey", originalKey);

        String key = originalKey.toUpperCase();
        final Long issueId = movedIssueKeyStore.getMovedIssueId(key);
        if (issueId != null)
        {
            return getIssueObject(issueId);
        }
        else
        {
            return null;
        }
    }

    @Override
    public void recordMovedIssueKey(Issue oldIssue)
    {
        // Record the historical issue key under all historical keys for the project, for easy lookup later
        final Set<String> projectKeys = projectKeyStore.getProjectKeys(oldIssue.getProjectId());
        for (final String projectKey : projectKeys)
        {
            String oldIssueKey = IssueKey.format(projectKey, oldIssue.getNumber());
            movedIssueKeyStore.recordMovedIssueKey(oldIssueKey, oldIssue.getId());
        }
    }

    /**
     * Return the issues sorted in the order that the ids are in.
     *
     * @param unsortedIssues unsorted list of Issue GVs
     * @param ids            Ordered list of Issue ID's
     * @return The Issues in the same order
     */
    private List<GenericValue> getIssuesSortedByIds(final Collection<GenericValue> unsortedIssues,
                                                    final Collection<Long> ids)
    {
        final Map<Long, GenericValue> idToIssue = new HashMap<Long, GenericValue>();
        for (final GenericValue issue : unsortedIssues)
        {
            idToIssue.put(issue.getLong("id"), issue);
        }
        final List<GenericValue> sortedIssues = new ArrayList<GenericValue>();
        for (final Long id : ids)
        {
            sortedIssues.add(idToIssue.get(id));
        }
        return sortedIssues;
    }

    public GenericValue getIssueByWorkflow(final Long wfid) throws GenericEntityException
    {
        return EntityUtil.getOnly(ofBizDelegator.findByAnd(Entity.Name.ISSUE, EasyMap.build("workflowId", wfid)));
    }

    public MutableIssue getIssueObjectByWorkflow(Long workflowId) throws GenericEntityException
    {
        return getIssueObject(getIssueByWorkflow(workflowId));
    }

    public MutableIssue getIssueObject(final Long id) throws DataAccessException
    {
        final GenericValue issueGV = getIssue(id);
        // return null if the issue does not exist JRA-11464
        if (issueGV == null)
        {
            return null;
        }
        return getIssueObject(issueGV);
    }

    public MutableIssue getIssueObject(final String key) throws DataAccessException
    {
        try
        {
            final GenericValue issueGV = getIssue(key);
            // return null if the issue does not exist JRA-11464
            if (issueGV == null)
            {
                return null;
            }
            return getIssueObject(issueGV);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public MutableIssue getIssueByKeyIgnoreCase(final String key) throws DataAccessException
    {
        try
        {
            final GenericValue issueGV = getCaseInsensitiveIssueFinder().getIssue(key);

            // return null if the issue does not exist JRA-11464
            if (issueGV == null)
            {
                return null;
            }
            return getIssueObject(issueGV);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
    }

    @Override
    public MutableIssue getIssueByCurrentKey(final String key) throws DataAccessException
    {
        final GenericValue issue;
        try
        {
            issue = getIssueFinder().getIssue(key);
        }
        catch (final GenericEntityException e)
        {
            throw new DataAccessException(e);
        }
        if (issue != null && issue.getString("key").equals(key))
        {
            return getIssueObject(issue);
        }
        else
        {
            return null;
        }
    }

    public List<GenericValue> getEntitiesByIssue(final String relationName, final GenericValue issue)
            throws GenericEntityException
    {
        if (relationName.equals(IssueRelationConstants.COMPONENT))
        {
            return nodeAssociationStore.getSinksFromSource(issue, "Component", relationName);
        }
        else if (relationName.equals(IssueRelationConstants.FIX_VERSION))
        {
            return nodeAssociationStore.getSinksFromSource(issue, "Version", relationName);
        }
        else if (relationName.equals(IssueRelationConstants.VERSION))
        {
            return nodeAssociationStore.getSinksFromSource(issue, "Version", relationName);
        }
        else if (relationName.equals(IssueRelationConstants.CHANGE_GROUPS))
        {
            return issue.getRelatedCache("ChildChangeGroup");
        }
        else if (relationName.equals(IssueRelationConstants.WORKFLOW_HISTORY))
        {
            //noinspection unchecked
            return workflowManager.makeWorkflowWithUserKey(null).getHistorySteps(issue.getLong("workflowId"));
        }
        else if (relationName.equals(IssueRelationConstants.COMMENTS))
        {
            return issue.getRelatedByAnd("ChildAction", MapBuilder.build("type", ActionConstants.TYPE_COMMENT));
        }
        else if (relationName.equals(IssueRelationConstants.TYPE_WORKLOG))
        {
            return issue.getRelated("ChildWorklog");
        }
        else if (relationName.equals(IssueRelationConstants.LINKS_INWARD))
        {
            return ofBizDelegator.findByAnd("IssueLink", MapBuilder.build("destination", issue.getLong("id")));
        }
        else if (relationName.equals(IssueRelationConstants.LINKS_OUTWARD))
        {
            return ofBizDelegator.findByAnd("IssueLink", MapBuilder.build("source", issue.getLong("id")));
        }
        else if (relationName.equals(IssueRelationConstants.CUSTOM_FIELDS_VALUES))
        {
            return ofBizDelegator.findByAnd("CustomFieldValue", MapBuilder.build("issue", issue.getLong("id")));
        }
        return Collections.emptyList();
    }

    public List<GenericValue> getEntitiesByIssueObject(final String relationName, final Issue issue)
            throws GenericEntityException
    {
        return getEntitiesByIssue(relationName, issue.getGenericValue());

    }

    public List<GenericValue> getIssuesByEntity(final String relationName, final GenericValue entity)
            throws GenericEntityException
    {
        return nodeAssociationStore.getSourcesFromSink(entity, Entity.Name.ISSUE, relationName);
    }

    @Override
    public List<Issue> getIssueObjectsByEntity(String relationName, GenericValue entity)
            throws GenericEntityException
    {
        return Lists.transform(getIssuesByEntity(relationName, entity), new Function<GenericValue, Issue>()
        {
            @Override
            public Issue apply(@Nullable GenericValue from)
            {
                return getIssueFactory().getIssue(from);
            }
        });
    }

    @Override
    public Set<String> getAllIssueKeys(final Long issueId)
    {
        notNull("issueId", issueId);
        final Issue issue = getIssueObject(issueId);
        final Project project = issue.getProjectObject();
        final Set<String> projectKeys = projectKeyStore.getProjectKeys(project.getId());
        return ImmutableSet.<String>builder()
                .addAll(getPreviousIssueKeysForMovedIssues(issueId))
                .addAll(transform(projectKeys, new Function<String, String>()
                {
                    @Override
                    public String apply(final String projectKey)
                    {
                        return IssueKey.format(projectKey, issue.getNumber());
                    }
                }))
                .build();
    }

    private Collection<String> getPreviousIssueKeysForMovedIssues(final Long issueId)
    {
        try
        {
            EntityCondition condition = new EntityFieldMap(
                    ImmutableMap.of("issueId", issueId), EntityOperator.AND);
            final List<GenericValue> gvs = ofBizDelegator.findByCondition("MovedIssueKey", condition,
                    ImmutableList.of("oldIssueKey"), ImmutableList.of("id"));

            return collectPreviousIssueKeys(gvs);
        }
        catch (DataAccessException e) //Fixes JRA-5067
        {
            return Collections.emptySet();
        }
    }

    private Collection<String> collectPreviousIssueKeys(List<GenericValue> issueChangeItemIteratorGVs)
    {
        final Collection<String> result = new LinkedHashSet<String>();
        for (final GenericValue issueChangeItemIteratorGV : issueChangeItemIteratorGVs)
        {
            result.add(issueChangeItemIteratorGV.getString("oldIssueKey"));
        }
        return result;
    }

    /**
     * This function creates an issue in Jira.  Read the javadoc under the fields parameter to see what object need
     * to be passed to create an issue.
     *
     * @param remoteUser User that is creating this issue
     * @param fields     see below
     *                   <h4>Required Fields</h4>
     *                   projectId:    A Long value representing the id of the project<br>
     *                   issueType:    The String id of an issueType<br>
     *                   summary:      A String describing the issue (max 255 chars)<br>
     *                   <h4>Recomended Fields</h4>
     *                   assignee:     A String representing the username of the assignee<br>
     *                   reporter:     A String representing the username of the reporter<br>
     *                   priority:     The String id of a priority<br>
     *                   <h4>Optional Fields</h4>
     *                   description:  A String description of the issue<br>
     *                   environment:  A String description of the environment the issue is in. e.g W2K<br>
     *                   fixVersions:  A List of Long values representing fixVersion ids<br>
     *                   components:   A List of Long values representing component ids<br>
     *                   timeOriginalEstimate: A Long value representing the number of seconds this tast should take<br>
     *                   timeEstimate: A Long value representing the number of seconds allocated for this issue<br>
     *                   versions: =   A List of Long value representing version ids<br>
     *                   customFields: A Map with the CustomField as the key and Transport Object of the CF as the value <br>
     *                   created:      The date which the issue was created.  If not specified, defaults to {@link System#currentTimeMillis() }<br>
     *                   updated:      The date which the issue was updated.  If not specified, defaults to {@link System#currentTimeMillis() }<br>
     * @return A generic value representing the issue created
     * @throws CreateException
     * @see com.atlassian.jira.workflow.function.issue.IssueCreateFunction
     */
    @Override
    public GenericValue createIssue(final User remoteUser, final Map<String, Object> fields) throws CreateException
    {
        return createIssue(remoteUser != null ? remoteUser.getName() : null, fields);
    }

    @Override
    public GenericValue createIssue(final User remoteUser, final Issue issue) throws CreateException
    {
        ensureCharacterLimitNotExceeded(issue.getDescription(), issue.getEnvironment());
        try
        {
            final Map<String, Object> fields = new HashMap<String, Object>();
            fields.put(ISSUE_MAP_KEY, issue);
            final MutableIssue originalIssueGV = getIssueObject(issue.getId());
            fields.put(WorkflowFunctionUtils.ORIGINAL_ISSUE_KEY, originalIssueGV);
            final GenericValue issueGV =
                    workflowManager.createIssue(remoteUser != null ? remoteUser.getName() : null, fields);
            return issueGV;
        }
        catch (final WorkflowException workflowException)
        {

            final Throwable cause = workflowException.getCause();
            if (cause instanceof InvalidInputException)
            {
                throw new CreateException("Error occurred while creating issue through workflow: " + cause.getMessage(),
                        (InvalidInputException) cause);
            }
            throw new CreateException(workflowException);
        }
    }

    /**
     * Throws a CreateException if description or environment exceeds character limit set in JIRA.
     * @param description description
     * @param environment environment
     * @throws CreateException throw an exception when description or environment exceeds the character limit
     */
    private void ensureCharacterLimitNotExceeded(final String description, final String environment) throws CreateException
    {
        if (textFieldCharacterLengthValidator.isTextTooLong(description))
        {
            throw new CreateException("Description field exceeds character limit of " + textFieldCharacterLengthValidator.getMaximumNumberOfCharacters() + " characters.");
        }
        if (textFieldCharacterLengthValidator.isTextTooLong(environment))
        {
            throw new CreateException("Environment field exceeds character limit of " + textFieldCharacterLengthValidator.getMaximumNumberOfCharacters() + " characters.");
        }
    }

    @Override
    public List<GenericValue> getProjectIssues(final GenericValue project) throws GenericEntityException
    {
        return project.getRelated("ChildIssue");
    }

    public boolean isEditable(final Issue issue)
    {
        return workflowManager.isEditable(issue);
    }

    @Override
    public boolean isEditable(final Issue issue, final User user)
    {
        return isEditable(issue) && permissionManager.hasPermission(Permissions.EDIT_ISSUE, issue, user);
    }

    /**
     * This method is here because this is a logical place for the "createIssue" method to be. As the issues are
     * actually created using workflow, the current implementation of this method uses the {@link WorkflowManager}
     * to create the issue
     *
     * @param remoteUserName the user who is creating the issue
     * @param fields         issue attributes
     * @return the created issue
     * @see #createIssue(User, java.util.Map)
     */
    @Override
    public GenericValue createIssue(final String remoteUserName, final Map<String, Object> fields)
            throws CreateException
    {
        final Object issueObject = fields.get(ISSUE_MAP_KEY);
        if (issueObject instanceof Issue)
        {
            Issue issue = (Issue) issueObject;
            ensureCharacterLimitNotExceeded(issue.getDescription(), issue.getEnvironment());
        }
        try
        {
            // We don't actually create the Issue directly - we call initialise the workflow for the Issue, and the
            // JIRA issue is created as a side-effect in the IssueCreateFunction workflow post-function.
            final GenericValue issue = workflowManager.createIssue(remoteUserName, fields);
            return issue;
        }
        catch (final WorkflowException e)
        {
            final Throwable cause = e.getCause();
            if ((cause != null) && (cause instanceof InvalidInputException))
            {
                throw new CreateException("Error occurred while creating issue through workflow: " + cause.getMessage(),
                        (InvalidInputException) cause);
            }
            throw new CreateException(e.getMessage(), e);
        }
    }

    @Override
    public Issue createIssueObject(String remoteUserName, Map<String, Object> fields) throws CreateException
    {
        return getIssueFactory().getIssue(createIssue(remoteUserName, fields));
    }

    @Override
    public Issue createIssueObject(User remoteUser, Map<String, Object> fields) throws CreateException
    {
        return getIssueFactory().getIssue(createIssue(remoteUser, fields));
    }

    @Override
    public Issue createIssueObject(User remoteUser, Issue issue) throws CreateException
    {
        return getIssueFactory().getIssue(createIssue(remoteUser, issue));
    }

    @Override
    public List<Issue> getVotedIssues(final User user) throws GenericEntityException
    {
        return getVotedIssues(ApplicationUsers.from(user));
    }

    @Override
    public List<Issue> getVotedIssuesOverrideSecurity(final User user) throws GenericEntityException
    {
        return getVotedIssuesOverrideSecurity(ApplicationUsers.from(user));
    }

    @Override
    public List<Issue> getVotedIssues(ApplicationUser user)
    {
        final List<Issue> votedIssues = getVotedIssuesOverrideSecurity(user);
        IssueUtils.filterIssues(votedIssues, new NoBrowsePermissionPredicate(user));
        return votedIssues;
    }

    @Override
    public List<Issue> getVotedIssuesOverrideSecurity(ApplicationUser user)
    {
        final List<GenericValue> issueGVs = userAssociationStore.getSinksFromUser("VoteIssue", user, Entity.Name.ISSUE);
        return getIssueObjectsFromGVs(issueGVs);
    }

    private List<Issue> getIssueObjectsFromGVs(final List<GenericValue> issueGVs)
    {
        Collections.sort(issueGVs, IssueKeyComparator.COMPARATOR);
        final List<Issue> issues = new ArrayList<Issue>();
        for (final GenericValue issue : issueGVs)
        {
            issues.add(getIssueObject(issue));
        }
        return issues;
    }

    @Override
    public List<User> getWatchers(Issue issue)
    {
        return ApplicationUsers.toDirectoryUsers(getWatchersFor(issue));
    }

    @Override
    public List<ApplicationUser> getWatchersFor(Issue issue)
    {
        return userAssociationStore.getUsersFromSink("WatchIssue", issue.getGenericValue());
    }

    @Override
    public List<Issue> getWatchedIssues(final User user)
    {
        return getWatchedIssues(ApplicationUsers.from(user));
    }

    @Override
    public List<Issue> getWatchedIssuesOverrideSecurity(final User user)
    {
        return getWatchedIssuesOverrideSecurity(ApplicationUsers.from(user));
    }

    @Override
    public List<Issue> getWatchedIssues(ApplicationUser user)
    {
        final List<Issue> watchedIssues = getWatchedIssuesOverrideSecurity(user);
        IssueUtils.filterIssues(watchedIssues, new NoBrowsePermissionPredicate(user));
        return watchedIssues;
    }

    @Override
    public List<Issue> getWatchedIssuesOverrideSecurity(ApplicationUser user)
    {
        final List<GenericValue> issueGVs =
                userAssociationStore.getSinksFromUser("WatchIssue", user, Entity.Name.ISSUE);
        return getIssueObjectsFromGVs(issueGVs);
    }

    @Override
    public Issue updateIssue(User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
    {
        return updateIssue(
                ApplicationUsers.from(user),
                issue,
                UpdateIssueRequest.builder().eventDispatchOption(eventDispatchOption).sendMail(sendMail).build());
    }

    @Override
    public Issue updateIssue(ApplicationUser user, MutableIssue issue, UpdateIssueRequest updateIssueRequest)
    {
        // Get the original issue before we store the new data
        GenericValue originalIssueGV = issue.getGenericValue();

        StringBuffer modifiedText = new StringBuffer();

        // Generate all of our change items and give the fields a chance to store their changes if needed + build
        // up the modified text to analyze
        DefaultIssueChangeHolder issueChangeHolder = updateFieldValues(issue, modifiedText);

        // Reset the fields as they all have been persisted to the db.
        issue.resetModifiedFields();

        // Perform the update which will also fire the event and create the change group/items
        doUpdate(user, issue, originalIssueGV, issueChangeHolder, updateIssueRequest);

        return issue;
    }

    protected void doUpdate(ApplicationUser user, MutableIssue issue, GenericValue originalIssueGV,
                            DefaultIssueChangeHolder issueChangeHolder, UpdateIssueRequest updateRequest)
    {
        final EventDispatchOption dispatchOption = updateRequest.getEventDispatchOption();
        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(issue.getGenericValue(), originalIssueGV,
                dispatchOption.getEventTypeId(), user, updateRequest.isSendMail(), true);
        issueUpdateBean.setComment(issueChangeHolder.getComment());
        issueUpdateBean.setChangeItems(issueChangeHolder.getChangeItems());
        issueUpdateBean.setDispatchEvent(dispatchOption.isEventBeingSent());
        issueUpdateBean.setHistoryMetadata(updateRequest.getHistoryMetadata());
        issueUpdateBean.setParams(MapBuilder.newBuilder("eventsource", IssueEventSource.ACTION).toMutableMap());
        issueUpdater.doUpdate(issueUpdateBean, false);
    }

    protected DefaultIssueChangeHolder updateFieldValues(MutableIssue issue, StringBuffer modifiedText)
    {
        DefaultIssueChangeHolder issueChangeHolder = new DefaultIssueChangeHolder();
        Map<String, ModifiedValue> modifiedFields = issue.getModifiedFields();

        for (final String fieldId : modifiedFields.keySet())
        {
            if (getFieldManager().isOrderableField(fieldId))
            {
                OrderableField field = getFieldManager().getOrderableField(fieldId);
                FieldLayoutItem fieldLayoutItem =
                        getFieldLayoutManager().getFieldLayout(issue).getFieldLayoutItem(field);
                final ModifiedValue modifiedValue = modifiedFields.get(fieldId);
                field.updateValue(fieldLayoutItem, issue, modifiedValue, issueChangeHolder);
                if (IssueFieldConstants.DESCRIPTION.equals(fieldId) || IssueFieldConstants.ENVIRONMENT.equals(fieldId))
                {
                    modifiedText.append(modifiedValue != null ? modifiedValue.getNewValue() : "").append(" ");
                }
            }
        }
        return issueChangeHolder;
    }

    @Override
    public void deleteIssue(User user, Issue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException
    {
        getIssueDeleteHelper().deleteIssue(user, issue, eventDispatchOption, sendMail);
    }

    @Override
    public void deleteIssue(User user, MutableIssue issue, EventDispatchOption eventDispatchOption, boolean sendMail)
            throws RemoveException
    {
        getIssueDeleteHelper().deleteIssue(user, issue, eventDispatchOption, sendMail);
    }

    @Override
    public void deleteIssueNoEvent(Issue issue) throws RemoveException
    {
        getIssueDeleteHelper().deleteIssueNoEvent(issue);
    }

    @Override
    public void deleteIssueNoEvent(MutableIssue issue) throws RemoveException
    {
        getIssueDeleteHelper().deleteIssueNoEvent(issue);
    }

    /**
     * Creates a MutableIssue object from an Issue GenericValue.
     * <p/>
     * <p> If a null GenericValue is passed, then null is returned.
     *
     * @param issueGV the Issue GenericValue.
     * @return the MutableIssue Object (will be null if issueGV is null).
     */
    @VisibleForTesting
    protected MutableIssue getIssueObject(final GenericValue issueGV)
    {
        if (issueGV == null)
        {
            return null;
        }
        return getIssueFactory().getIssue(issueGV);
    }

    private IssueFactory getIssueFactory()
    {
        // We can't have IssueFactory injected as we would get a circular dependency.
        return ComponentAccessor.getIssueFactory();
    }

    FieldLayoutManager getFieldLayoutManager()
    {
        if (fieldLayoutManager == null)
        {
            fieldLayoutManager = ComponentAccessor.getFieldLayoutManager();
        }
        return fieldLayoutManager;
    }

    FieldManager getFieldManager()
    {
        if (fieldManager == null)
        {
            fieldManager = ComponentAccessor.getFieldManager();
        }
        return fieldManager;
    }

    IssueDeleteHelper getIssueDeleteHelper()
    {
        if (issueDeleteHelper == null)
        {
            issueDeleteHelper = ComponentAccessor.getComponentOfType(IssueDeleteHelper.class);
        }
        return issueDeleteHelper;
    }

    @VisibleForTesting
    IssueFinder getIssueFinder()
    {
        return issueFinder;
    }

    @VisibleForTesting
    IssueFinder getCaseInsensitiveIssueFinder()
    {
        return caseInsensitiveIssueFinder;
    }

    private Pair<Set<String>, Map<String, Set<Long>>> breakDownIssueKeys(@Nonnull final Set<String> issueKeys)
    {
        Set<String> invalidKeys = new HashSet<String>();
        Map<String, Set<Long>> projectKeyIssueNumbers = new HashMap<String, Set<Long>>();
        for (String issueKey : issueKeys)
        {
            IssueKey issueKeyObject;
            try
            {
                issueKeyObject = IssueKey.from(issueKey);
            }
            catch (IllegalArgumentException ex)
            {
                invalidKeys.add(issueKey);
                continue;
            }
            Set<Long> issueNumbers = projectKeyIssueNumbers.get(issueKeyObject.getProjectKey());
            if (issueNumbers == null)
            {
                issueNumbers = new HashSet<Long>();
                projectKeyIssueNumbers.put(issueKeyObject.getProjectKey(), issueNumbers);
            }
            issueNumbers.add(issueKeyObject.getIssueNumber());
        }
        return Pair.of(invalidKeys, projectKeyIssueNumbers);
    }

    private Set<Pair<Long, String>> getProjectIssueTypePairs(@Nonnull final Collection<String> fields,
                                                             @Nonnull final EntityCondition condition)
    {
        OfBizListIterator iterator = null;
        try
        {
            iterator = ofBizDelegator.findListIteratorByCondition(Entity.Name.ISSUE, condition, null,
                    fields, null, null);

            Set<Pair<Long, String>> projectIssueTypes = new HashSet<Pair<Long, String>>();
            GenericValue genericValue = iterator.next();
            while (genericValue != null)
            {
                Long projectId = genericValue.getLong(IssueFieldConstants.PROJECT);
                String issueTypeId = genericValue.getString("type");
                projectIssueTypes.add(Pair.of(projectId, issueTypeId));
                genericValue = iterator.next();
            }
            return projectIssueTypes;
        }
        finally
        {
            if (iterator != null)
            {
                iterator.close();
            }
        }
    }

    @Nonnull
    @Override
    public Set<Pair<Long, String>> getProjectIssueTypePairsByKeys(@Nonnull final Set<String> issueKeys)
    {
        if (issueKeys.isEmpty())
            return new HashSet<Pair<Long, String>>();
        UtilTimerStack.push("DefaultIssueManager.getProjectIssueTypePairsByKeys()");
        Pair<Set<String>, Map<String, Set<Long>>> result = breakDownIssueKeys(issueKeys);
        // Ignore invalid issue keys for context creation.
        Map<String, Set<Long>> projectIssueNumbers = result.second();
        Set<Pair<Long, String>> projectIssueTypes = new HashSet<Pair<Long, String>>();
        for (Map.Entry<String, Set<Long>> entry : projectIssueNumbers.entrySet())
        {
            Long projectId = projectKeyStore.getProjectId(entry.getKey());
            if (projectId == null)
            {
                // Ignore wrong project keys for context creation.
                continue;
            }
            EntityCondition projectIdCondition = new EntityExpr("project", EntityOperator.EQUALS, projectId);
            EntityCondition issueNumberCondition = new EntityExpr("number", EntityOperator.IN, entry.getValue());
            EntityConditionList conditions =
                    new EntityConditionList(Arrays.asList(projectIdCondition, issueNumberCondition),
                            EntityOperator.AND);
            projectIssueTypes
                    .addAll(getProjectIssueTypePairs(Arrays.asList(IssueFieldConstants.PROJECT, "type"), conditions));
        }
        UtilTimerStack.pop("DefaultIssueManager.getProjectIssueTypePairsByKeys()");
        return projectIssueTypes;
    }

    @Nonnull
    @Override
    public Set<Pair<Long, String>> getProjectIssueTypePairsByIds(@Nonnull final Set<Long> issueIds)
    {
        if (issueIds.isEmpty())
            return new HashSet<Pair<Long, String>>();
        UtilTimerStack.push("DefaultIssueManager.getProjectIssueTypePairsByIds()");
        EntityCondition condition = new EntityExpr("id", EntityOperator.IN, issueIds);
        Set<Pair<Long, String>> projectIssueTypePairsByIds =
                getProjectIssueTypePairs(Arrays.asList(IssueFieldConstants.PROJECT, "type"), condition);
        UtilTimerStack.pop("DefaultIssueManager.getProjectIssueTypePairsByIds()");
        return projectIssueTypePairsByIds;
    }

    private <E> Set<E> checkIssuesExist(@Nonnull final Set<E> allItems, @Nonnull final Collection<String> fields,
                                        EntityCondition condition,
                                        @Nonnull final Function<GenericValue, E> function)
    {
        Set<E> missingItems = new HashSet<E>();
        OfBizListIterator iterator = null;
        try
        {
            iterator = ofBizDelegator.findListIteratorByCondition(Entity.Name.ISSUE, condition, null,
                    fields, null, null);
            missingItems.addAll(allItems);
            UtilTimerStack.push("DefaultIssueManager.checkIssuesExist()");
            GenericValue genericValue = iterator.next();
            while (genericValue != null)
            {
                E issueId = function.apply(genericValue);
                missingItems.remove(issueId);
                genericValue = iterator.next();
            }
            UtilTimerStack.pop("DefaultIssueManager.checkIssuesExist()");
        }
        finally
        {
            if (iterator != null)
            {
                iterator.close();
            }
        }
        return missingItems;
    }

    @Nonnull
    @Override
    public Set<String> getKeysOfMissingIssues(@Nonnull final Set<String> issueKeys)
    {
        if (issueKeys.isEmpty())
            return new HashSet<String>();
        UtilTimerStack.push("DefaultIssueManager.getKeysOfMissingIssues()");
        Pair<Set<String>, Map<String, Set<Long>>> result = breakDownIssueKeys(issueKeys);
        Set<String> invalidIssueKeys = result.first();
        Map<String, Set<Long>> projectIssueNumbers = result.second();
        Set<String> missingIssueKeys = new HashSet<String>(invalidIssueKeys);
        for (Map.Entry<String, Set<Long>> entry : projectIssueNumbers.entrySet())
        {
            Set<String> missingIssueKeysForThisProject = new HashSet<String>();
            final String projectKey = entry.getKey();
            Set<String> issueKeysForThisProject = new HashSet<String>();
            for (Long issueKey : entry.getValue())
                issueKeysForThisProject.add(new IssueKey(projectKey, issueKey).toString());
            Long projectId = projectKeyStore.getProjectId(entry.getKey());
            if (projectId == null)
            {
                missingIssueKeysForThisProject.addAll(issueKeysForThisProject);
                continue;
            }
            EntityCondition projectIdCondition = new EntityExpr("project", EntityOperator.EQUALS, projectId);
            EntityCondition issueNumberCondition = new EntityExpr("number", EntityOperator.IN, entry.getValue());
            EntityConditionList conditions =
                    new EntityConditionList(Arrays.asList(projectIdCondition, issueNumberCondition),
                            EntityOperator.AND);
            missingIssueKeysForThisProject.addAll(
                    checkIssuesExist(issueKeysForThisProject, Arrays.asList("id", "number"), conditions,
                            new Function<GenericValue, String>()
                            {
                                @Override
                                public String apply(@Nonnull final GenericValue genericValue)
                                {
                                    return new IssueKey(projectKey, genericValue.getLong("number")).toString();
                                }
                            }));
            Set<String> movedIssueKeys = movedIssueKeyStore.getMovedIssueKeys(missingIssueKeysForThisProject);
            missingIssueKeysForThisProject.removeAll(movedIssueKeys);
            missingIssueKeys.addAll(missingIssueKeysForThisProject);
        }
        UtilTimerStack.pop("DefaultIssueManager.getKeysOfMissingIssues()");
        return missingIssueKeys;
    }

    @Nonnull
    @Override
    public Set<Long> getIdsOfMissingIssues(@Nonnull final Set<Long> issueIds)
    {
        if (issueIds.isEmpty())
            return new HashSet<Long>();
        UtilTimerStack.push("DefaultIssueManager.getIdsOfMissingIssues()");
        EntityCondition condition = new EntityExpr("id", EntityOperator.IN, issueIds);
        Set<Long> missingIssueIds =
                checkIssuesExist(issueIds, Arrays.asList("id"), condition, new Function<GenericValue, Long>()
                {
                    @Override
                    public Long apply(@Nonnull final GenericValue genericValue)
                    {
                        return genericValue.getLong("id");
                    }
                });
        UtilTimerStack.pop("DefaultIssueManager.getIdsOfMissingIssues()");
        return missingIssueIds;
    }

    abstract class IssueFinder
    {
        public GenericValue getIssue(final String key) throws GenericEntityException
        {
            if (key == null)
            {
                return null; // JRA-17080
            }
            final GenericValue issue = getIssueFromIssueEntity(key);
            if (issue == null)
            {
                final Issue movedIssue = findMovedIssue(key);
                if (movedIssue == null)
                {
                    return null;
                }
                return movedIssue.getGenericValue();
            }
            return issue;
        }

        private GenericValue getIssueFromIssueEntity(final String key)
        {
            final IssueKey issueKey;
            try
            {
                issueKey = IssueKey.from(key);
            }
            catch (IllegalArgumentException ex)
            {
                // For backward compatibility in the getIssue methods which returned null for invalid input
                return null;
            }
            Long projectId = getProjectByKey(issueKey.getProjectKey());
            if (projectId == null)
            {
                return null;
            }
            return getIssueFromIssueEntityByProjectAndNumber(projectId, issueKey.getIssueNumber());
        }

        private GenericValue getIssueFromIssueEntityByProjectAndNumber(final Long projectId, final Long issueNumber)
        {
            return EntityUtil.getOnly(ofBizDelegator.findByAnd(Entity.Name.ISSUE,
                    ImmutableMap.of("project", projectId, "number", issueNumber)));
        }

        protected abstract Long getProjectByKey(final String projectKey);
    }
}
