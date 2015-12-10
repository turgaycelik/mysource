/*
 * Copyright (c) 2002-2004
 * All rights reserved.
 */

package com.atlassian.jira.config;

import com.atlassian.annotations.PublicApi;
import com.atlassian.jira.exception.CreateException;
import com.atlassian.jira.exception.DataAccessException;
import com.atlassian.jira.exception.RemoveException;
import com.atlassian.jira.issue.IssueConstant;
import com.atlassian.jira.issue.issuetype.IssueType;
import com.atlassian.jira.issue.priority.Priority;
import com.atlassian.jira.issue.resolution.Resolution;
import com.atlassian.jira.issue.status.Status;
import com.atlassian.jira.util.ErrorCollection;
import org.ofbiz.core.entity.GenericValue;

import java.util.Collection;
import java.util.List;

/**
 * Manager for issue types, statuses, priorities and resolutions.  This manager is responsible for
 * caching these constants as well as all the usual update, delete, add operations in the database.
 */
@PublicApi
public interface ConstantsManager
{
    /**
     * Used to retrieve a standard IssueTypes.
     */
    public static final String ALL_STANDARD_ISSUE_TYPES = "-2";
    /**
     * Used to retrieve a subtask IssueTypes.
     */
    public static final String ALL_SUB_TASK_ISSUE_TYPES = "-3";
    /**
     * Used to retrieve all IssueTypes.
     */
    public static final String ALL_ISSUE_TYPES = "-4";

    /**
     * Used in the generic {@link #getConstantObject(String, String)} method
     */
    public static final String PRIORITY_CONSTANT_TYPE = "Priority";
    /**
     * Used in the generic {@link #getConstantObject(String, String)} method
     */
    public static final String STATUS_CONSTANT_TYPE = "Status";
    /**
     * Used in the generic {@link #getConstantObject(String, String)} method
     */
    public static final String RESOLUTION_CONSTANT_TYPE = "Resolution";
    /**
     * Used in the generic {@link #getConstantObject(String, String)} method
     */
    public static final String ISSUE_TYPE_CONSTANT_TYPE = "IssueType";

    /**
     * Retrieve all Priorities in JIRA.
     *
     * @return A list of Priority {@link GenericValue}s.
     * @deprecated since 1.99. Use {@link #getPriorityObjects} instead
     */
    @Deprecated
    public Collection<GenericValue> getPriorities();

    /**
     * Retrieve all Priorities in JIRA.
     *
     * @return A list of {@link Priority}s.
     */
    Collection<Priority> getPriorityObjects();

    /**
     * Given a priority ID, this method retrieves that priority.
     *
     * @param id The id of the priority
     * @return A {@link Priority} object.
     */
    public Priority getPriorityObject(String id);

    /**
     * Returns the priority Name for a given priority ID.
     *
     * @param id The id of a priority
     * @return The name of the priority with the given ID, or an i18n'd String indicating that
     *         no priority is set (e.g. "None") if the ID is null.
     */
    public String getPriorityName(String id);

    /**
     * Returns the default  priority configured in JIRA.
     *
     * @return The default priority {@link GenericValue}.
     * @deprecated Use {@link #getDefaultPriorityObject()} instead. Since v4.0
     */
    public GenericValue getDefaultPriority();

    /**
     * Returns the default  priority configured in JIRA.
     *
     * @return The default priority.
     */
    public Priority getDefaultPriorityObject();

    /**
     * Reloads all priorities from the DB.
     */
    public void refreshPriorities();

    /**
     * Retrieve all Resolutions in JIRA.
     *
     * @return A List of Resolution {@link GenericValue}s.
     * @deprecated Use {@link #getResolutionObjects()} instead.
     */
    @Deprecated
    public Collection<GenericValue> getResolutions();

    /**
     * Retrieve all Resolutions in JIRA.
     *
     * @return A List of {@link Resolution} objects.
     */
    public Collection<Resolution> getResolutionObjects();

    /**
     * Given a resolution ID, this method retrieves that resolution.
     *
     * @param id The id of the resolution
     * @return A resolution {@link GenericValue}
     * @deprecated Use {@link #getResolutionObject(String)} instead.
     */
    @Deprecated
    public GenericValue getResolution(String id);

    /**
     * Given a resolution ID, this method retrieves that resolution.
     *
     * @param id The id of the resolution
     * @return A {@link Resolution} object.
     */
    public Resolution getResolutionObject(String id);

    /**
     * Reloads all resolutions from the DB.
     */
    public void refreshResolutions();

    /**
     * Given an IssueType ID this method retrieves that IssueType.
     *
     * @param id The ID of the IssueType.
     * @return An IssueType {@link GenericValue}
     * @deprecated Use {@link #getIssueTypeObject(String)} instead. Since 5.0
     */
    @Deprecated
    public GenericValue getIssueType(String id);

    /**
     * Given an IssueType ID this method retrieves that IssueType.
     *
     * @param id The ID of the IssueType.
     * @return An {@link IssueType} object
     */
    public IssueType getIssueTypeObject(String id);

    /**
     * Retrieve regular (non-subtask) issue types.
     *
     * @return A collection of IssueType {@link GenericValue}s
     * @deprecated Use {@link #getRegularIssueTypeObjects()} instead.
     */
    @Deprecated
    public Collection<GenericValue> getIssueTypes();

    /**
     * Retrieve regular (non-subtask) issue types.
     *
     * @return A collection of {@link IssueType}s
     */
    Collection<IssueType> getRegularIssueTypeObjects();

    /**
     * Returns a list of IssueTypes.
     *
     * @return A Collection of {@link IssueType} objects.
     */
    Collection<IssueType> getAllIssueTypeObjects();

    /**
     * Returns a list of IssueTypes.
     *
     * @return A list of {@link GenericValue} issueTypes.
     * @deprecated Use {@link #getAllIssueTypeObjects} instead. Deprecated since v4.0
     */
    @Deprecated
    public List<GenericValue> getAllIssueTypes();

    /**
     * Returns all issueType Ids.
     *
     * @return A list of all the IssueType Ids.
     */
    List<String> getAllIssueTypeIds();

    /**
     * Retrieves all the sub-task issue types.  These will be non-modifiable.
     * Use {@link #getEditableSubTaskIssueTypes()} instead if you require an editable list.
     *
     * @return A Collection of sub-task {@link GenericValue}s.
     * @deprecated Use {@link #getSubTaskIssueTypeObjects} instead. Deprecated since v4.0
     */
    @Deprecated
    public Collection<GenericValue> getSubTaskIssueTypes();

    /**
     * Retrieves all the sub-task issue types
     *
     * @return A Collection of all sub-task {@link IssueType}s.
     */
    Collection<IssueType> getSubTaskIssueTypeObjects();

    /**
     * Retrieves an editable list of sub-task issues.
     *
     * @return A List of editable sub-task {@link GenericValue}s
     */
    public List<GenericValue> getEditableSubTaskIssueTypes();

    /**
     * Converts the 'special' ids of issue types to a list of issue type ids
     * For example, converts a special id to a list of all sub-task issue types
     * Also see {@link #ALL_STANDARD_ISSUE_TYPES}, {@link #ALL_SUB_TASK_ISSUE_TYPES} and
     * {@link #ALL_ISSUE_TYPES}.
     *
     * @param issueTypeIds A collection of the issuetype Ids to retrieve.
     * @return A list of "actual" IssueType ID's expanded from the macro constants (or a new copy of the original list if it doesn't contain macros).
     */
    public List<String> expandIssueTypeIds(Collection<String> issueTypeIds);

    /**
     * Reloads all IssueTypes from the DB.
     */
    public void refreshIssueTypes();

    /**
     * Note this metod does not validate the input - i.e. It does not check for duplicate names etc. Use
     * this method in conjunction with {@link #validateCreateIssueType(String, String, String, String, com.atlassian.jira.util.ErrorCollection, String)}
     *
     * @param name        Name of the new IssueType
     * @param sequence    Sequence number used for ordering the issuetypes in the UI.
     * @param style       Used to record the type of issue, such as SUBTASK.  Null for regular issues.
     * @param description A short description of the new issue type.
     * @param iconurl     A URL to an icon to be used for the new issueType.
     * @return The newly created IssueType
     * @throws CreateException If there is an error creating this Issue Type.
     *
     * @deprecated Use {@link #insertIssueType(String, Long, String, String, String)} instead. Since v5.0.
     */
    public GenericValue createIssueType(String name, Long sequence, String style, String description, String iconurl)
            throws CreateException;

    /**
     * Creates a new IssueType.
     * <p>
     * Note this method does not validate the input - i.e. It does not check for duplicate names etc. Use
     * this method in conjunction with {@link #validateCreateIssueType(String, String, String, String, com.atlassian.jira.util.ErrorCollection, String)}
     *
     * @param name        Name of the new IssueType
     * @param sequence    Sequence number used for ordering the issuetypes in the UI.
     * @param style       Used to record the type of issue, such as SUBTASK.  Null for regular issues.
     * @param description A short description of the new issue type.
     * @param iconurl     A URL to an icon to be used for the new issueType.
     * @return The newly created IssueType
     * @throws CreateException If there is an error creating this Issue Type.
     * @deprecated Use {@link #insertIssueType(String, Long, String, String, Long)} instead. Since v6.3.
     */
    @Deprecated
    public IssueType insertIssueType(String name, Long sequence, String style, String description, String iconurl)
            throws CreateException;

    /**
     * Creates a new IssueType.
     * <p>
     * Note this method does not validate the input - i.e. It does not check for duplicate names etc. Use
     * this method in conjunction with {@link #validateCreateIssueType(String, String, String, String, com.atlassian.jira.util.ErrorCollection, String)}
     *
     * @param name        Name of the new IssueType
     * @param sequence    Sequence number used for ordering the issuetypes in the UI.
     * @param style       Used to record the type of issue, such as SUBTASK.  Null for regular issues.
     * @param description A short description of the new issue type.
     * @param avatarId    Avatar id,
     * @return The newly created IssueType
     * @throws CreateException If there is an error creating this Issue Type.
     *
     * @since v6.3
     */
    IssueType insertIssueType(String name, Long sequence, String style, String description, Long avatarId) throws CreateException;

    /**
     * Validates creation of a new issuetype.  In particular, this function checks that a name has been submitted, no
     * other issueTypes with the same name exist, and that the icon URL exists.
     *
     * @param name          Name of the new IssueType
     * @param style         Used to record the type of issue, such as SUBTASK.  Null for regular issues.
     * @param description   A short description of the new issue type.
     * @param iconurl       A URL to an icon to be used for the new issueType.
     * @param errors        A collection of errors used to pass back any problems.
     * @param nameFieldName The field to which the errors should be added.
     */
    void validateCreateIssueType(String name, String style, String description, String iconurl, ErrorCollection errors, String nameFieldName);

    /**
     * Validates creation of a new issuetype.  In particular, this function checks that a name has been submitted, no
     * other issueTypes with the same name exist and correct avatarId is passed.
     *
     * @param name          Name of the new IssueType
     * @param style         Used to record the type of issue, such as SUBTASK.  Null for regular issues.
     * @param description   A short description of the new issue type.
     * @param avatarId      An avatar id.
     * @param errors        A collection of errors used to pass back any problems.
     * @param nameFieldName The field to which the errors should be added.
     *
     * @since v6.3
     */
    void validateCreateIssueTypeWithAvatar(String name, String style, String description, String avatarId, ErrorCollection errors, String nameFieldName);

    /**
     * Updates an existing issueType.  This will cause a invalidate of all issue types (i.e. reload from the DB).
     *
     * @param id          ID of the existing issuetype.
     * @param name        Name of the new IssueType
     * @param sequence    Sequence number used for ordering the issuetypes in the UI.
     * @param style       Used to record the type of issue, such as SUBTASK.  Null for regular issues.
     * @param description A short description of the new issue type.
     * @param iconurl     A URL to an icon to be used for the new issueType.
     * @throws DataAccessException indicates an error in the Data Access Layer
     * @deprecated use {@link #updateIssueType(String, String, Long, String, String, Long)} since v6.3
     */
    @Deprecated
    public void updateIssueType(String id, String name, Long sequence, String style, String description, String iconurl)
            throws DataAccessException;

    /**
     * Updates an existing issueType.  This will cause a invalidate of all issue types (i.e. reload from the DB).
     *
     * @param id          ID of the existing issuetype.
     * @param name        Name of the new IssueType
     * @param sequence    Sequence number used for ordering the issuetypes in the UI.
     * @param style       Used to record the type of issue, such as SUBTASK.  Null for regular issues.
     * @param description A short description of the new issue type.
     * @param avatarId    avatarid of new issueType.
     * @throws DataAccessException indicates an error in the Data Access Layer
     *
     * @since v6.3
     */
    public void updateIssueType(String id, String name, Long sequence, String style, String description, Long avatarId);

    /**
     * Removes an existing issueType. This will cause a invalidate of all issue types (i.e. reload from the DB).
     *
     * @param id ID of an existing issueType
     * @throws RemoveException if the issueType with id doesn't exist, or an error occured removing the issue.
     */
    public void removeIssueType(String id) throws RemoveException;

    /**
     * Bulk operation to store a list of issueTypes.
     *
     * @param issueTypes A list of IssueType {@link GenericValue}s
     * @throws DataAccessException indicates an error in the Data Access Layer
     */
    public void storeIssueTypes(List<GenericValue> issueTypes) throws DataAccessException;

    /**
     * Returns a Status given an id.
     *
     * @param id The id of a status
     * @return Returns a status {@link GenericValue}
     * @deprecated Use {@link #getStatusObject} instead. Deprecated since v4.0
     */
    @Deprecated
    public GenericValue getStatus(String id);

    /**
     * Returns a Status given an id.
     *
     * @param id The id of a status
     * @return Returns a {@link Status} object.
     */
    public Status getStatusObject(String id);

    /**
     * Returns all statuses
     *
     * @return Returns a Collection of status {@link GenericValue}s.
     * @deprecated Use {@link #getStatusObjects} instead. Deprecated since v4.0
     */
    @Deprecated
    public Collection<GenericValue> getStatuses();

    /**
     * Returns all statuses
     *
     * @return Returns a Collection of {@link Status} objects
     */
    public Collection<Status> getStatusObjects();

    /**
     * Reloads all statuses from DB.
     */
    public void refreshStatuses();

    /**
     * Searches for a given status by name. This is not the most efficient implementation.
     * If the name is not found, or the given name is null, then it returns null.
     *
     * @param name The name of the status.
     * @return A {@link Status} object with the given name, or <code>null</code> if none found.
     */
    Status getStatusByName(String name);

    /**
     * Searches for a given status by name ignoring case. This is not the most efficient implementation.
     * If the name is not found, or the given name is null, then it returns null.
     *
     * @param name The name of the status.
     * @return A {@link Status} object with the given name, or <code>null</code> if none found.
     */
    Status getStatusByNameIgnoreCase(String name);

    /**
     * Searches for a given status by its translated name.
     * If no matching translated name is found the true (untranslated) name
     * will be tried.
     * If the name is not found, or the given name is null, then it returns null.
     *
     * @param name The name of the status.
     * @return A {@link Status} object with the given name, or <code>null</code> if none found.
     */
    Status getStatusByTranslatedName(String name);

    /**
     * Returns an {@link IssueConstant} object for the given type & id.
     *
     * @param constantType See {@link #PRIORITY_CONSTANT_TYPE}, {@link #STATUS_CONSTANT_TYPE}, {@link #RESOLUTION_CONSTANT_TYPE}, {@link #ISSUE_TYPE_CONSTANT_TYPE}
     * @param id           The id of the constant.
     * @return A {@link IssueConstant} object. Null if it doesn't exist.
     */
    public IssueConstant getConstantObject(String constantType, String id);

    /**
     * Returns all {@link IssueConstant} objects for the given type.
     *
     * @param constantType See {@link #PRIORITY_CONSTANT_TYPE}, {@link #STATUS_CONSTANT_TYPE}, {@link #RESOLUTION_CONSTANT_TYPE}, {@link #ISSUE_TYPE_CONSTANT_TYPE}
     * @return A collection of {@link IssueConstant} object.
     */
    Collection getConstantObjects(String constantType);

    /**
     * Converts the list of ids to the objects of appropriate types
     *
     * @param constantType the constant type. Case insenstive
     * @param ids          list of constant ids or GenericValues
     * @return List of IssueConstant subclasses. Null if constantType is null or the ids are empty
     */
    public List<IssueConstant> convertToConstantObjects(String constantType, Collection ids);

    /**
     * Checks if a constant exists.
     *
     * @param constantType See {@link #PRIORITY_CONSTANT_TYPE}, {@link #STATUS_CONSTANT_TYPE}, {@link #RESOLUTION_CONSTANT_TYPE}, {@link #ISSUE_TYPE_CONSTANT_TYPE}
     * @param name         The name of the constant.
     * @return True if the constant exists. False otherwise
     */
    public boolean constantExists(String constantType, String name);

    /**
     * Returns a constant by name.
     *
     * @param constantType See {@link #PRIORITY_CONSTANT_TYPE}, {@link #STATUS_CONSTANT_TYPE}, {@link #RESOLUTION_CONSTANT_TYPE}, {@link #ISSUE_TYPE_CONSTANT_TYPE}
     * @param name         The Name of the constant.
     * @return A constant {@link GenericValue}
     *
     * @deprecated Use {@link #getIssueConstantByName(String, String)}. Since v4.3
     */
    public GenericValue getConstantByName(String constantType, String name);

    /**
     * Returns a constant by name.
     *
     * @param constantType See {@link #PRIORITY_CONSTANT_TYPE}, {@link #STATUS_CONSTANT_TYPE}, {@link #RESOLUTION_CONSTANT_TYPE}, {@link #ISSUE_TYPE_CONSTANT_TYPE}
     * @param name         The Name of the constant.
     * @return The IssueConstant
     */
    public IssueConstant getIssueConstantByName(String constantType, String name);

    /**
     * Returns a constant by name ignoring the case of the name passed in.
     *
     * @param constantType See {@link #PRIORITY_CONSTANT_TYPE}, {@link #STATUS_CONSTANT_TYPE}, {@link #RESOLUTION_CONSTANT_TYPE}, {@link #ISSUE_TYPE_CONSTANT_TYPE}
     * @param name         The Name of the constant, case-insensitive.
     * @return An IssueConstant (or null if not found)
     */
    public IssueConstant getConstantByNameIgnoreCase(String constantType, String name);

    /**
     * Converts a constant {@link GenericValue} to an {@link IssueConstant} object.
     *
     * @param issueConstantGV the constant {@link GenericValue}.
     * @return An {@link IssueConstant} object.
     */
    IssueConstant getIssueConstant(GenericValue issueConstantGV);

    /**
     * Sets all cached copies of constant to null.  This will cause them to be re-loaded from the DB
     * the next time they are accessed.
     *
     * @deprecated since v6.2.  Use {@link #invalidateAll()}
     */
    @Deprecated
    public void refresh();

    /**
     * Sets all cached copies of constant to null.  This will cause them to be re-loaded from the DB
     * the next time they are accessed.
     */
    public void invalidateAll();

    /**
     * Clear the cache for this Issue Constant.
     * Implementations may clear additional IssueConstants at their discretion.
     */
    public void invalidate(IssueConstant issueConstant);
}
