package com.atlassian.jira.config;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Set;

import com.atlassian.core.util.collection.EasyList;
import com.atlassian.crowd.embedded.api.User;
import com.atlassian.jira.bean.SubTaskBean;
import com.atlassian.jira.bean.SubTaskBeanImpl;
import com.atlassian.jira.config.properties.APKeys;
import com.atlassian.jira.config.properties.ApplicationProperties;
import com.atlassian.jira.event.type.EventType;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.Issue;
import com.atlassian.jira.issue.IssueFieldConstants;
import com.atlassian.jira.issue.IssueManager;
import com.atlassian.jira.issue.fields.config.manager.IssueTypeSchemeManager;
import com.atlassian.jira.issue.history.ChangeItemBean;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.link.IssueLink;
import com.atlassian.jira.issue.link.IssueLinkManager;
import com.atlassian.jira.issue.link.IssueLinkType;
import com.atlassian.jira.issue.link.IssueLinkTypeManager;
import com.atlassian.jira.issue.link.SequenceIssueLinkComparator;
import com.atlassian.jira.issue.util.IssueUpdateBean;
import com.atlassian.jira.security.PermissionManager;
import com.atlassian.jira.security.Permissions;
import com.atlassian.jira.util.CollectionReorderer;

import org.apache.log4j.Logger;
import org.ofbiz.core.entity.GenericValue;

public class DefaultSubTaskManager implements SubTaskManager
{
    private static final Logger log = Logger.getLogger(DefaultSubTaskManager.class);

    private static final String ISSUE_TYPE_NAME = "IssueType";

    private static final String DEFAULT_SUB_TASK_ISSUE_TYPE_NAME = "Sub-task";
    private static final Long DEFAULT_SUB_TASK_ISSUE_TYPE_SEQUENCE = new Long(0);
    private static final String DEFAULT_SUB_TASK_ISSUE_TYPE_DESCRIPTION = "The sub-task of the issue";
    private static final String DEFAULT_SUB_TASK_ISSUE_TYPE_ICON_URL = "/images/icons/issuetypes/subtask_alternate.png";

    private final IssueLinkTypeManager issueLinkTypeManager;
    private final IssueLinkManager issueLinkManager;
    private final PermissionManager permissionManager;
    private final ApplicationProperties applicationProperties;
    private final CollectionReorderer collectionReorderer;
    private final ConstantsManager constantsManager;
    private final IssueTypeSchemeManager issueTypeSchemeManager;
    private final IssueManager issueManager;

    public DefaultSubTaskManager(ConstantsManager constantsManager, IssueLinkTypeManager issueLinkTypeManager,
            IssueLinkManager issueLinkManager, PermissionManager permissionManager,
            ApplicationProperties applicationProperties, CollectionReorderer collectionReorderer,
            IssueTypeSchemeManager issueTypeSchemeManager, IssueManager issueManager)
    {
        this.constantsManager = constantsManager;
        this.issueLinkTypeManager = issueLinkTypeManager;
        this.issueLinkManager = issueLinkManager;
        this.permissionManager = permissionManager;
        this.applicationProperties = applicationProperties;
        this.collectionReorderer = collectionReorderer;
        this.issueTypeSchemeManager = issueTypeSchemeManager;
        this.issueManager = issueManager;
    }

    /**
     * Turn on sub-tasks by creating a sub-task issue link type
     * and a default sub-task issue type
     */
    @Override
    public void enableSubTasks() throws CreateException
    {
        // Check that the sub-task issue link does not exist
        final Collection subTaskIssueLinks = getSubTaskIssueLinkTypes();
        if (subTaskIssueLinks == null || subTaskIssueLinks.isEmpty())
        {
            // Create default sub-task issue link
            issueLinkTypeManager.createIssueLinkType(SUB_TASK_LINK_TYPE_NAME, SUB_TASK_LINK_TYPE_OUTWARD_NAME, SUB_TASK_LINK_TYPE_INWARD_NAME, SUB_TASK_LINK_TYPE_STYLE);
        }

        // Check if sub-task already exists
        final Collection subTaskIssueTypes = constantsManager.getSubTaskIssueTypeObjects();
        if (subTaskIssueTypes == null || subTaskIssueTypes.isEmpty())
        {
            // If not, create default sub-task issue type
            createSubTaskIssueType(DEFAULT_SUB_TASK_ISSUE_TYPE_NAME, DEFAULT_SUB_TASK_ISSUE_TYPE_SEQUENCE, DEFAULT_SUB_TASK_ISSUE_TYPE_DESCRIPTION, DEFAULT_SUB_TASK_ISSUE_TYPE_ICON_URL);
        }

        // Update the application property
        applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS, true);
    }

    @Override
    public void disableSubTasks()
    {
        // Just set the proeprty to false - do not remove any entities so that nothing breaks
        applicationProperties.setOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS, false);
    }


    @Override
    public Collection<IssueType> getSubTaskIssueTypeObjects()
    {
        return constantsManager.getSubTaskIssueTypeObjects();
    }

    @Override
    public boolean isSubTasksEnabled()
    {
        return applicationProperties.getOption(APKeys.JIRA_OPTION_ALLOWSUBTASKS);
    }

    @Override
    public GenericValue createSubTaskIssueType(String name, Long sequence, String description, String iconurl)
            throws CreateException
    {
        // Check that an issue type of that name does not already exist.
        if (issueTypeExistsByName(name))
        {
            throw new CreateException("Issue Type with name '" + name + "' already exists.");
        }

        // Insert the record into the database
        final GenericValue subTask = constantsManager.createIssueType(name, sequence, SUB_TASK_ISSUE_TYPE_STYLE, description, iconurl);

        // Add to default scheme
        issueTypeSchemeManager.addOptionToDefault(subTask.getString("id"));

        return subTask;
    }

    @Override
    public IssueType insertSubTaskIssueType(String name, Long sequence, String description, String iconurl)
            throws CreateException
    {
        // Check that an issue type of that name does not already exist.
        if (issueTypeExistsByName(name))
        {
            throw new CreateException("Issue Type with name '" + name + "' already exists.");
        }

        // Insert the record into the database
        final IssueType subTaskIssueType = constantsManager.insertIssueType(name, sequence, SUB_TASK_ISSUE_TYPE_STYLE, description, iconurl);

        // Add to default scheme
        issueTypeSchemeManager.addOptionToDefault(subTaskIssueType.getId());

        return subTaskIssueType;
    }

    public IssueType insertSubTaskIssueType(String name, Long sequence, String description, Long avatarId) throws CreateException
    {
        // Check that an issue type of that name does not already exist.
        if (issueTypeExistsByName(name))
        {
            throw new CreateException("Issue Type with name '" + name + "' already exists.");
        }

        // Insert the record into the database
        final IssueType subTaskIssueType = constantsManager.insertIssueType(name, sequence, SUB_TASK_ISSUE_TYPE_STYLE, description, avatarId);

        // Add to default scheme
        issueTypeSchemeManager.addOptionToDefault(subTaskIssueType.getId());

        return subTaskIssueType;

    }

    @Override
    public void updateSubTaskIssueType(String id, String name, Long sequence, String description, String iconurl)
            throws DataAccessException
    {
        constantsManager.updateIssueType(id, name, sequence, SUB_TASK_ISSUE_TYPE_STYLE, description, iconurl);
    }

    @Override
    public void updateSubTaskIssueType(String id, String name, Long sequence, String description, Long avatarId)
            throws DataAccessException
    {
        constantsManager.updateIssueType(id, name, sequence, SUB_TASK_ISSUE_TYPE_STYLE, description, avatarId);
    }

    private Collection getSubTaskIssueLinkTypes()
    {
        return issueLinkTypeManager.getIssueLinkTypesByStyle(SUB_TASK_LINK_TYPE_STYLE);
    }

    @Override
    public boolean issueTypeExistsByName(String name)
    {
        return constantsManager.constantExists(ISSUE_TYPE_NAME, name);
    }

    @Override
    public void moveSubTaskIssueTypeUp(String id) throws DataAccessException
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Id cannot be null.");
        }

        List<GenericValue> subTasksIssueTypes = new ArrayList<GenericValue>(constantsManager.getEditableSubTaskIssueTypes());
        final GenericValue issueType = getSubTaskIssueTypeById(id);
        collectionReorderer.increasePosition(subTasksIssueTypes, issueType);

        recalculateSequencesAndStore(subTasksIssueTypes);
    }

    @Override
    public void moveSubTaskIssueTypeDown(String id) throws DataAccessException
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Id cannot be null.");
        }

        List<GenericValue> subTasksIssueTypes = new ArrayList<GenericValue>(constantsManager.getEditableSubTaskIssueTypes());
        final GenericValue issueType = constantsManager.getIssueType(id);
        collectionReorderer.decreasePosition(subTasksIssueTypes, issueType);

        recalculateSequencesAndStore(subTasksIssueTypes);
    }

    @Override
    public GenericValue getSubTaskIssueTypeById(String id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Id cannot be null.");
        }

        final GenericValue issueTypeGV = constantsManager.getIssueType(id);
        if (issueTypeGV != null)
        {
            // Ensure that the issue type is a sub-task issue type
            if (!isSubTaskIssueType(issueTypeGV))
            {
                throw new IllegalArgumentException("The issue type with id '" + id + "' is not a sub-task issue type.");
            }
        }

        return issueTypeGV;
    }

    @Override
    public IssueType getSubTaskIssueType(String id)
    {
        if (id == null)
        {
            throw new IllegalArgumentException("Id cannot be null.");
        }

        final IssueType issueType = constantsManager.getIssueTypeObject(id);
        if (issueType == null)
        {
            return null;
        }
        else if (issueType.isSubTask())
        {
            return issueType;
        }
        else
        {
            throw new IllegalArgumentException("The issue type with id '" + id + "' is not a sub-task issue type.");
        }
    }

    private void recalculateSequencesAndStore(List<GenericValue> c) throws DataAccessException
    {
        int i = 0;
        for (final GenericValue genericValue : c)
        {
            genericValue.set("sequence", new Long(i));
            i++;
        }

        // Store the list of sub-task issue types
        try
        {
            constantsManager.storeIssueTypes(c);
        }
        catch (DataAccessException e)
        {
            throw new DataAccessException("Error occurred while storing sub-task issue types.", e);
        }

    }

    @Override
    public void removeSubTaskIssueType(String name) throws RemoveException
    {
        // Ensure that an issue type with that name exists
        final IssueType issueType = (IssueType) constantsManager.getIssueConstantByName(ISSUE_TYPE_NAME, name);
        if (issueType != null)
        {
            // Ensure that the issue type is a sub-task
            if (issueType.isSubTask())
            {
                // Remove the record from the database
                constantsManager.removeIssueType(issueType.getId());
            }
            else
            {
                throw new RemoveException("Issue Type with name '" + name + "' is not a sub-task issue type.");
            }
        }
        else
        {
            throw new RemoveException("Issue Type with name '" + name + "' does not exist.");
        }
    }

    @Override
    public boolean issueTypeExistsById(String id)
    {
        return (getSubTaskIssueTypeById(id) != null);
    }

    @Override
    public boolean isSubTask(GenericValue issue)
    {
        return (getParentIssueId(issue) != null);
    }

    @Override
    public boolean isSubTaskIssueType(GenericValue issueType)
    {
        return SUB_TASK_ISSUE_TYPE_STYLE.equals(issueType.getString("style"));
    }

    @Override
    public Long getParentIssueId(GenericValue issue)
    {
        ensureIssueNotNull(issue);

        // Check if we have any incoming sub-task issue links
        final List<IssueLink> inwardLinks = issueLinkManager.getInwardLinks(issue.getLong("id"));
        for (final IssueLink inwardLink : inwardLinks)
        {
            if (inwardLink.getIssueLinkType().isSubTaskLinkType())
            {
                return inwardLink.getLong("source");
            }
        }
        return null;
    }

    @Override
    public GenericValue getParentIssue(GenericValue subtask)
    {
        final Long parentId = getParentIssueId(subtask);
        try
        {
            return issueManager.getIssue(parentId);
        }
        catch (DataAccessException parentNotFound)
        {
            return null;
        }
    }

    @Override
    public SubTaskBean getSubTaskBean(GenericValue issue, User remoteUser)
    {
        SubTaskBeanImpl subTaskBean = new SubTaskBeanImpl();

        final Collection<IssueLink> subTaskIssueLinks = getSubTaskIssueLinks(issue.getLong("id"));
        for (final IssueLink subTaskIssueLink : subTaskIssueLinks)
        {
            Issue subTaskIssue = subTaskIssueLink.getDestinationObject();
            // Check that the remote user has the permissions to actually see the sub-task
            // This only becomes useful in Enterprise version, due to issue level security.
            // Even though we keep the issue security level the same on sub-tasks and parent issues
            // due to things like 'assignee' and 'reporter' permissions, it is possible to have
            // a situation where a user can see the parent issue but not its sub task, or vice versa
            if (permissionManager.hasPermission(Permissions.BROWSE, subTaskIssue, remoteUser))
            {
                subTaskBean.addSubTask(subTaskIssueLink.getSequence(), subTaskIssue.getGenericValue(), issue);
            }
        }

        return subTaskBean;
    }

    @Override
    public SubTaskBean getSubTaskBean(Issue issue, User remoteUser)
    {
        SubTaskBeanImpl subTaskBean = new SubTaskBeanImpl();

        final Collection<IssueLink> subTaskIssueLinks = getSubTaskIssueLinks(issue.getLong("id"));
        for (final IssueLink subTaskIssueLink : subTaskIssueLinks)
        {
            Issue subTaskIssue = subTaskIssueLink.getDestinationObject();
            // Check that the remote user has the permissions to actually see the sub-task due to issue level security.
            // Even though we keep the issue security level the same on sub-tasks and parent issues
            // due to things like 'assignee' and 'reporter' permissions, it is possible to have
            // a situation where a user can see the parent issue but not its sub task, or vice versa
            if (permissionManager.hasPermission(Permissions.BROWSE, subTaskIssue, remoteUser))
            {
                subTaskBean.addSubTask(subTaskIssueLink.getSequence(), subTaskIssue, issue);
            }
        }

        return subTaskBean;
    }

    @Override
    public void moveSubTask(GenericValue parentIssue, Long currentSequence, Long sequence)
    {
        final List<IssueLink> subTaskIssueLinks = getSubTaskIssueLinks(parentIssue.getLong("id"));
        issueLinkManager.moveIssueLink(subTaskIssueLinks, currentSequence, sequence);
    }

    @Override
    public void moveSubTask(Issue parentIssue, Long currentSequence, Long sequence)
    {
        moveSubTask(parentIssue.getGenericValue(), currentSequence, sequence);
    }

    @Override
    public void resetSequences(Issue issue)
    {
        resetSequences(issue.getId());
    }

    private void resetSequences(Long issueId)
    {
        final List<IssueLink> subTaskIssueLinks = getSubTaskIssueLinks(issueId);
        issueLinkManager.resetSequences(subTaskIssueLinks);
    }

    /**
     * Retrieves ids of all sub-task issues in the system.
     */
    @Override
    public Collection<Long> getAllSubTaskIssueIds()
    {
        // Get the sub-task issue link type
        final IssueLinkType subTaskIssueLinkType = getSubTaskIssueLinkType();

        // Find all sub-task links
        final Collection<IssueLink> issueLinks = issueLinkManager.getIssueLinks(subTaskIssueLinkType.getId());

        // Theorietically we should be able to return the count of issue links (as a sub-task should cannot be a sub-task of
        // more than one issue). However if this ever changes, this will likely cause a bug. Hence, this code is in place.
        // As it is not expected to be executed often, the safe approach is taken.
        Set<Long> subTaskIssueIds = new HashSet<Long>();
        for (final IssueLink issueLink : issueLinks)
        {
            subTaskIssueIds.add(issueLink.getDestinationId());
        }

        return subTaskIssueIds;
    }

    private void ensureIssueNotNull(GenericValue issue)
    {
        if (issue == null)
        {
            throw new IllegalArgumentException("Issue cannot be null.");
        }
        else if (!"Issue".equals(issue.getEntityName()))
        {
            throw new IllegalArgumentException("The argument must be an issue.");
        }
    }

    @Override
    public List<IssueLink> getSubTaskIssueLinks(final Long issueId)
    {
        List<IssueLink> subTasks = new ArrayList<IssueLink>();
        for (IssueLink link : issueLinkManager.getOutwardLinks(issueId))
        {
            if (link.getIssueLinkType().isSubTaskLinkType())
            {
                subTasks.add(link);
            }
        }
        // Sort the sub-task issue links on sequence
        Collections.sort(subTasks, new SequenceIssueLinkComparator());
        return subTasks;
    }

    /**
     * @deprecated Use {@link #getSubTaskObjects(com.atlassian.jira.issue.Issue)} instead.
     */
    @Override
    public Collection<GenericValue> getSubTasks(GenericValue issue)
    {
        Collection<GenericValue> subTaskIssues = new LinkedList<GenericValue>();
        for (final IssueLink issueLink : getSubTaskIssueLinks(issue.getLong("id")))
        {
            subTaskIssues.add(issueLink.getDestinationObject().getGenericValue());
        }

        return subTaskIssues;
    }

    /**
     * Get an issue's subtasks.
     *
     * @return A collection of {@link Issue}s.
     */
    @Override
    public Collection<Issue> getSubTaskObjects(Issue parentIssue)
    {
        Collection<Issue> subTaskIssues = new LinkedList<Issue>();
        for (final IssueLink issueLink : getSubTaskIssueLinks(parentIssue.getId()))
        {
            subTaskIssues.add(issueLink.getDestinationObject());
        }

        return subTaskIssues;

    }

    @Override
    public void createSubTaskIssueLink(GenericValue parentIssue, GenericValue subTaskIssue, User remoteUser)
            throws CreateException
    {
        if (parentIssue == null)
        {
            throw new IllegalArgumentException("Parent Issue cannot be null.");
        }
        else if (subTaskIssue == null)
        {
            throw new IllegalArgumentException("Sub-Task Issue cannot be null.");
        }

        // Determine the next sequence of the issue
        final Collection subTaskIssueLinks = getSubTaskIssueLinks(parentIssue.getLong("id"));

        // Determine the sequence of the new sub-task link
        final Long sequence;
        if (subTaskIssueLinks == null)
        {
            sequence = new Long(0);
        }
        else
        {
            sequence = new Long(subTaskIssueLinks.size());
        }

        issueLinkManager.createIssueLink(parentIssue.getLong("id"), subTaskIssue.getLong("id"), getSubTaskIssueLinkType().getId(), sequence, remoteUser);
    }

    @Override
    public void createSubTaskIssueLink(Issue parentIssue, Issue subTaskIssue, User remoteUser) throws CreateException
    {
        createSubTaskIssueLink(parentIssue.getGenericValue(), subTaskIssue.getGenericValue(), remoteUser);
    }

    private IssueLinkType getSubTaskIssueLinkType()
    {
        final Collection subTaskIssueLinkTypes = getSubTaskIssueLinkTypes();
        if (subTaskIssueLinkTypes != null && !subTaskIssueLinkTypes.isEmpty())
        {
            if (subTaskIssueLinkTypes.size() > 1)
            {
                log.warn("Found '" + subTaskIssueLinkTypes.size() + "' sub-task issue link types. Returning first one.");
            }

            return (IssueLinkType) subTaskIssueLinkTypes.iterator().next();
        }
        else
        {
            return null;
        }
    }

    @Override
    public IssueUpdateBean changeParent(GenericValue subTask, GenericValue newParentIssue, User currentUser)
            throws RemoveException, CreateException
    {
        GenericValue oldParentIssue = getParentIssue(subTask);
        Collection<IssueLink> inwardLinks = issueLinkManager.getInwardLinks(subTask.getLong("id"));
        for (final IssueLink issueLink : inwardLinks)
        {
            if (issueLink.getIssueLinkType().isSubTaskLinkType())
            {
                issueLinkManager.removeIssueLink(issueLink, currentUser);
            }
        }

        // Create new Link
        createSubTaskIssueLink(newParentIssue, subTask, currentUser);
        // Reorder new parent subTask
        resetSequences(newParentIssue.getLong("id"));
        // Reorder old parent subTask
        resetSequences(oldParentIssue.getLong("id"));

        // Create change item for subtask
        ChangeItemBean cibParent = new ChangeItemBean(ChangeItemBean.CUSTOM_FIELD, "Parent Issue",
                oldParentIssue.getString("key"), oldParentIssue.getString("key"),
                newParentIssue.getString("key"), newParentIssue.getString("key"));

        GenericValue newSubTask = (GenericValue) subTask.clone();
        //JRA-10546 - the sub task must have the same security level as its parent
        //Note, the IssueUpdater takes care of generating the change history (by checking which fields
        //have changed from the old to the new issue)
        newSubTask.set(IssueFieldConstants.SECURITY, newParentIssue.getLong(IssueFieldConstants.SECURITY));

        IssueUpdateBean issueUpdateBean = new IssueUpdateBean(newSubTask, subTask, EventType.ISSUE_UPDATED_ID, currentUser);
        issueUpdateBean.setChangeItems(EasyList.build(cibParent));
        return issueUpdateBean;
    }

    @Override
    public IssueUpdateBean changeParent(Issue subTask, Issue parentIssue, User currentUser)
            throws RemoveException, CreateException
    {
        return changeParent(subTask.getGenericValue(), parentIssue.getGenericValue(), currentUser);
    }
}
